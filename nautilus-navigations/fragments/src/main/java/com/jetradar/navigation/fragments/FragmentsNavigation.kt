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

package com.jetradar.navigation.fragments

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.transaction
import com.jakewharton.rxrelay2.PublishRelay
import com.jetradar.navigation.Navigation
import com.jetradar.navigation.NavigationCommand
import com.jetradar.navigation.NavigationEvent
import com.jetradar.navigation.Navigator
import io.reactivex.Observable

class FragmentsNavigation(
    private val fragmentManager: FragmentManager,
    @IdRes private val containerViewId: Int
) : Navigation {

  private val navigationEvents = PublishRelay.create<NavigationEvent>()

  override fun navigationEvents(): Observable<NavigationEvent> = navigationEvents

  override fun navigate(command: NavigationCommand): Boolean = with(command) {
    when (this) {
      is OpenScreenCommand       -> openScreen(fragment, addToBackStack)
      is OpenAsRootScreenCommand -> openAsRootScreenCommand(fragment)
      else                       -> return false
    }
    return true
  }

  private fun openScreen(fragment: Fragment, addToBackStack: Boolean) {
    fragmentManager.transaction(allowStateLoss = true) {
      add(containerViewId, fragment)
      if (addToBackStack) addToBackStack(null)
    }
    navigationEvents.accept(OpenScreenEvent(fragment.javaClass.name))
  }

  private fun openAsRootScreenCommand(fragment: Fragment) {
    clear()
    fragmentManager.transaction(allowStateLoss = true) { replace(containerViewId, fragment) }
    fragmentManager.executePendingTransactions()
  }

  override fun back(): Boolean {
    val currentScreen = currentScreen() ?: return false
    val popped = fragmentManager.popBackStackImmediate() // TODO: remove fragments by container id
    if (popped) navigationEvents.accept(CloseScreenEvent(currentScreen.javaClass.name))
    return popped
  }

  override fun clear() {
    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE) // TODO: remove fragments by container id
  }

  private fun currentScreen(): Fragment? =
      fragmentManager.findFragmentById(containerViewId)

  companion object {

    fun Navigator.currentScreen(): Fragment? =
        checkNotNull(findNavigation<FragmentsNavigation>()).currentScreen()
  }
}
