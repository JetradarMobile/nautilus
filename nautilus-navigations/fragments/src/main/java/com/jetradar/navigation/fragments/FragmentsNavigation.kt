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
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.transaction
import com.jakewharton.rxrelay2.PublishRelay
import com.jetradar.navigation.Navigation
import com.jetradar.navigation.NavigationCommand
import com.jetradar.navigation.NavigationEvent
import com.jetradar.navigation.Navigator
import io.reactivex.Observable

class FragmentsNavigation(
    private val fragmentManager: FragmentManager,
    @IdRes private val containerViewId: Int,
    private val defaultAnimations: AnimationHolder? = null
) : Navigation {

  private val navigationEvents = PublishRelay.create<NavigationEvent>()

  override fun navigationEvents(): Observable<NavigationEvent> = navigationEvents

  override fun navigate(command: NavigationCommand): Boolean = with(command) {
    when (this) {
      is ForwardCommand -> forward(fragment, tag, animations)
      is ReplaceCommand -> replace(fragment, tag, animations)
      is BackToCommand  -> backTo(tag)
      else              -> return false
    }
    return true
  }

  private fun forward(fragment: Fragment, tag: String, animations: AnimationHolder?) {
    fragmentManager.transaction(allowStateLoss = true) {
      replace(containerViewId, fragment, tag)
      addToBackStack(tag)
      applyAnimation(animations)
    }
    navigationEvents.accept(OpenScreenEvent(tag))
  }

  private fun replace(fragment: Fragment, tag: String, animations: AnimationHolder?) {
    fragmentManager.transaction(allowStateLoss = true) {
      replace(containerViewId, fragment, tag)
      applyAnimation(animations)
    }
    navigationEvents.accept(OpenScreenEvent(tag))
  }

  private fun FragmentTransaction.applyAnimation(animations: AnimationHolder?) {
    val customAnimation = animations ?: defaultAnimations
    customAnimation?.let { setCustomAnimations(it.enter, it.exit, it.popEnter, it.popExit) }
  }

  private fun backTo(tag: String) {
    do {
      if (currentScreen()?.tag == tag) {
        fragmentManager.executePendingTransactions()
        return
      }
    } while (back())
    throw IllegalStateException("Screen with tag=$tag not found in stack")
  }

  override fun back(): Boolean {
    val currentScreen = currentScreen() ?: return false
    if (fragmentManager.backStackEntryCount == 0) return false
    fragmentManager.popBackStack(currentScreen.tag ?: currentScreen.javaClass.name, 0)
    navigationEvents.accept(CloseScreenEvent(currentScreen.tag ?: currentScreen.javaClass.name))
    return true
  }

  override fun clear() {
    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
  }

  private fun currentScreen(): Fragment? = fragmentManager.findFragmentById(containerViewId)

  companion object {

    fun Navigator.currentScreen(): Fragment? =
        checkNotNull(findNavigation<FragmentsNavigation>()).currentScreen()
  }
}
