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

data class Tab(
    val id: Int,
    val tag: String,
    val rootFragment: String
) : Parcelable {

  private constructor(parcel: Parcel) : this(
      id = parcel.readInt(),
      tag = checkNotNull(parcel.readString()),
      rootFragment = checkNotNull(parcel.readString())
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeInt(id)
    parcel.writeString(tag)
    parcel.writeString(rootFragment)
  }

  override fun describeContents() = 0

  companion object CREATOR : Parcelable.Creator<Tab> {
    override fun createFromParcel(parcel: Parcel) = Tab(parcel)

    override fun newArray(size: Int) = arrayOfNulls<Tab>(size)
  }
}
