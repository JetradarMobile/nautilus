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

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.Fragment.SavedState
import androidx.fragment.app.FragmentManager

class TabsBackStackEntry(
    val tab: Tab,
    val fragmentName: String,
    val args: Bundle? = null,
    val state: SavedState? = null
) : Parcelable {

  private constructor(parcel: Parcel) : this(
      tab = checkNotNull(parcel.readParcelable(Tab::class.java.classLoader)),
      fragmentName = checkNotNull(parcel.readString()),
      args = parcel.readBundle(Bundle::class.java.classLoader),
      state = parcel.readParcelable(SavedState::class.java.classLoader)
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(fragmentName)
    parcel.writeBundle(args)
    parcel.writeParcelable(state, flags)
  }

  override fun describeContents() = 0

  fun recreateFragment(context: Context): Fragment = Fragment.instantiate(context, fragmentName, args).apply {
    setInitialSavedState(state)
  }

  companion object {
    @JvmField val CREATOR = object : Parcelable.Creator<TabsBackStackEntry> {
      override fun createFromParcel(parcel: Parcel) = TabsBackStackEntry(parcel)

      override fun newArray(size: Int) = arrayOfNulls<TabsBackStackEntry>(size)
    }

    fun create(tab: Tab, fragment: Fragment, fragmentManager: FragmentManager) = with(fragment) {
      TabsBackStackEntry(
          tab = tab,
          fragmentName = javaClass.name,
          args = arguments,
          state = fragmentManager.saveFragmentInstanceState(this)
      )
    }
  }
}
