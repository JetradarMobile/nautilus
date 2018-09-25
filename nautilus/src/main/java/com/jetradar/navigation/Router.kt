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

import androidx.annotation.MainThread
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlin.properties.Delegates

class Router(
    private val logger: ((message: String) -> Unit)? = null
) {
  private val navigationEvents = PublishRelay.create<NavigationEvent>()
  private var navigationEventsDisposable: Disposable? = null
  var navigator by Delegates.observable<Navigator?>(null) { _, _, newNavigator ->
    navigationEventsDisposable?.dispose()
    newNavigator?.let { navigator ->
      navigationEventsDisposable = navigator.navigationEvents().subscribe { event ->
        logger?.invoke(event.message)
        navigationEvents.accept(event)
      }
    }
  }

  @MainThread
  fun navigate(vararg commands: NavigationCommand) {
    navigator?.let { navigator ->
      for (command in commands) {
        navigator.navigate(command)
      }
    }
  }

  fun navigationEvents(): Observable<NavigationEvent> = navigationEvents
}
