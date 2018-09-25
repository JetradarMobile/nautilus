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

import android.os.Parcel
import android.os.Parcelable
import java.util.EmptyStackException
import java.util.LinkedList

class TabsBackStack() : Parcelable {
  private val backStack = LinkedList<TabsBackStackEntry>()

  constructor(parcel: Parcel) : this() {
    parcel.readTypedList(backStack, TabsBackStackEntry.CREATOR)
  }

  fun push(entry: TabsBackStackEntry) {
    backStack.push(entry)
  }

  fun pop(tab: Tab): TabsBackStackEntry? =
      tabBackStack(tab).firstOrNull()?.takeIf { backStack.remove(it) }

  fun pop(): TabsBackStackEntry? =
      if (backStack.isEmpty()) null
      else backStack.pop()

  fun clear(tab: Tab) {
    backStack.removeAll(tabBackStack(tab))
  }

  fun clear() {
    backStack.clear()
  }

  fun resetToRoot(tab: Tab) {
    val tabBackStack = tabBackStack(tab)
    if (tabBackStack.isEmpty()) throw EmptyStackException()
    backStack.removeAll(tabBackStack.dropLast(1))
  }

  /* experimental */
  fun resetTo(tab: Tab, fragmentName: String) {
    val tabBackStack = tabBackStack(tab)
    if (tabBackStack.isEmpty()) throw EmptyStackException()
    backStack.removeAll(tabBackStack.take(tabBackStack.indexOfFirst { it.fragmentName == fragmentName }))
  }

  fun size(tab: Tab) = tabBackStack(tab).size

  fun empty(tab: Tab) = tabBackStack(tab).isEmpty()

  fun empty() = backStack.isEmpty()

  private fun tabBackStack(tab: Tab) = backStack.filter { it.tab == tab }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeTypedList(backStack)
  }

  override fun describeContents() = 0

  companion object CREATOR : Parcelable.Creator<TabsBackStack> {
    override fun createFromParcel(parcel: Parcel) = TabsBackStack(parcel)

    override fun newArray(size: Int) = arrayOfNulls<TabsBackStack>(size)
  }
}
