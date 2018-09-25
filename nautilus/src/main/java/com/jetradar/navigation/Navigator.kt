/*
 * Copyright (C) 2018 JetRadar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jetradar.navigation

import android.app.Activity
import android.os.Bundle
import androidx.annotation.MainThread
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

open class Navigator(
    protected val activity: Activity,
    val navigations: List<Navigation>,
    savedState: Bundle?
) {
  protected val activityNavigationEvents = PublishRelay.create<NavigationEvent>()

  init {
    if (savedState != null) {
      restoreState(savedState)
    } else {
      activityNavigationEvents.accept(LaunchActivityEvent(activity.javaClass.name))
    }
  }

  fun navigationEvents(): Observable<NavigationEvent> =
      Observable.merge(navigations.map { it.navigationEvents() }.plus(activityNavigationEvents))

  @MainThread
  fun navigate(command: NavigationCommand) {
    if (command is BackCommand) {
      back()
    } else {
      for (navigation in navigations) {
        if (navigation.navigate(command)) return
      }
      throw RuntimeException("Could not process the command ${command.javaClass.name}")
    }
  }

  @MainThread
  fun back() {
    for (navigation in navigations) {
      if (navigation.back()) return
    }
    activity.finish()
    activityNavigationEvents.accept(FinishActivityEvent(activity.javaClass.name))
  }

  fun saveState(outState: Bundle) {
    navigations.forEach { it.saveState(outState) }
  }

  fun restoreState(savedState: Bundle) {
    navigations.forEach { it.restoreState(savedState) }
  }

  inline fun <reified T : Navigation> findNavigation(): T? =
      navigations.firstOrNull { T::class.java.isInstance(it) } as T?
}
