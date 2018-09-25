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

package com.jetradar.navigation.tabs

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.transaction
import com.jakewharton.rxrelay2.PublishRelay
import com.jetradar.nautilus.tabs.R
import com.jetradar.navigation.Navigation
import com.jetradar.navigation.NavigationCommand
import com.jetradar.navigation.NavigationEvent
import com.jetradar.navigation.Navigator
import com.jetradar.navigation.OnBackPressHandler
import io.reactivex.Observable
import kotlin.properties.Delegates

class TabsNavigation(
    private val activity: FragmentActivity,
    @IdRes private val containerViewId: Int,
    private val mainTab: Tab? = null
) : Navigation {

  private val fragmentManager: FragmentManager = activity.supportFragmentManager
  private val navigationEvents = PublishRelay.create<NavigationEvent>()
  private var tabsBackStack = TabsBackStack()
  private var currentTab by Delegates.observable<Tab?>(null) { _, _, tab ->
    tab?.let { navigationEvents.accept(SwitchTabEvent(it)) }
  }

  override fun navigationEvents(): Observable<NavigationEvent> = navigationEvents

  override fun navigate(command: NavigationCommand): Boolean = with(command) {
    when (this) {
      is OpenTabScreenCommand       -> openTabScreen(tab, fragment, addToBackStack)
      is SwitchTabCommand           -> switchTab(tab)
      is ReselectTabCommand         -> reselectTab()
      is ClearTabBackStackCommand   -> clearTabBackStack(tab)
      is BackToRootTabScreenCommand -> backToRoot()
      else                          -> return false
    }
    return true
  }

  private fun openTabScreen(tab: Tab?, fragment: Fragment, addToBackStack: Boolean) {
    val tab = tab ?: checkNotNull(currentTab)
    val currentTab = currentTab
    val currentScreen = currentTabScreen()
    fragmentManager.transaction(allowStateLoss = true) {
      setCustomAnimations(R.animator.fragment_open_enter, R.animator.fragment_open_exit)
      replace(containerViewId, fragment)
    }
    if (addToBackStack && currentScreen != null && currentTab != null) {
      tabsBackStack.push(TabsBackStackEntry.create(currentTab, currentScreen, fragmentManager))
    }
    if (tab != currentTab) {
      this.currentTab = tab
    }
    navigationEvents.accept(OpenTabScreenEvent(tab, fragment.javaClass.name))
  }

  private fun switchTab(tab: Tab) {
    if (tab == currentTab) return
    openTabScreen(
        tab = tab,
        fragment = tabsBackStack.pop(tab)?.recreateFragment(activity)
            ?: Fragment.instantiate(activity, tab.rootFragment),
        addToBackStack = true
    )
  }

  private fun reselectTab() {
    val currentTab = checkNotNull(currentTab)
    val currentScreen = checkNotNull(currentTabScreen())
    if (currentScreen is OnTabReselectHandler && currentScreen.onTabReselected()) return
    if (currentScreen.isRoot(currentTab).not()) {
      backToRoot()
    }
  }

  private fun clearTabBackStack(tab: Tab? = null) {
    tabsBackStack.run {
      if (tab != null) clear(tab)
      else clear()
    }
  }

  private fun backToRoot() {
    val currentTab = checkNotNull(currentTab)
    val currentScreen = checkNotNull(currentTabScreen())
    if (currentScreen.isRoot(currentTab)) return
    if (tabsBackStack.size(currentTab) > 1) tabsBackStack.resetToRoot(currentTab)
    val rootScreen = tabsBackStack.pop(currentTab)?.recreateFragment(activity)?.takeIf { it.isRoot(currentTab) }
        ?: Fragment.instantiate(activity, currentTab.rootFragment)
    backTo(rootScreen)
  }

  private fun backTo(fragment: Fragment, tab: Tab = checkNotNull(currentTab)) {
    fragmentManager.transaction(now = true, allowStateLoss = true) {
      setCustomAnimations(R.animator.fragment_close_enter, R.animator.fragment_close_exit)
      replace(containerViewId, fragment)
    }
    if (tab != currentTab) {
      this.currentTab = tab
    }
  }

  override fun back(): Boolean {
    val currentTab = currentTab ?: return false
    val currentScreen = currentTabScreen() ?: return false
    if (currentScreen is OnBackPressHandler && currentScreen.onBackPressed()) return true
    if (tabsBackStack.empty(currentTab).not()) {
      backTo(checkNotNull(tabsBackStack.pop(currentTab)).recreateFragment(activity))
    } else if (currentScreen.isRoot(currentTab).not()) {
      backTo(Fragment.instantiate(activity, currentTab.rootFragment))
    } else if (tabsBackStack.empty().not()) {
      checkNotNull(tabsBackStack.pop()).run {
        backTo(recreateFragment(activity), tab)
      }
    } else if (mainTab != null && currentTab != mainTab) {
      backTo(Fragment.instantiate(activity, mainTab.rootFragment), mainTab)
    } else {
      return false
    }
    navigationEvents.accept(CloseTabScreenEvent(currentTab, currentScreen.javaClass.name))
    return true
  }

  override fun clear() {
    clearTabBackStack()
  }

  override fun saveState(outState: Bundle) {
    outState.putParcelable(STATE_TABS_BACK_STACK, tabsBackStack)
  }

  override fun restoreState(savedState: Bundle) {
    savedState.getParcelable<TabsBackStack>(STATE_TABS_BACK_STACK)?.let { tabsBackStack = it }
  }

  private fun currentTabScreen(): Fragment? =
      fragmentManager.findFragmentById(containerViewId)

  companion object {
    private const val STATE_TABS_BACK_STACK = "tabs_back_stack"

    private fun Fragment.isRoot(tab: Tab): Boolean =
        tab.rootFragment == javaClass.name

    fun Navigator.currentTab(): Tab? =
        checkNotNull(findNavigation<TabsNavigation>()).currentTab

    fun Navigator.currentTabScreen(): Fragment? =
        checkNotNull(findNavigation<TabsNavigation>()).currentTabScreen()
  }
}
