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

import androidx.fragment.app.Fragment
import com.jetradar.navigation.NavigationCommand

class OpenTabScreenCommand(
    val tab: Tab? = null,
    val fragment: Fragment,
    val addToBackStack: Boolean = true
) : NavigationCommand

class SwitchTabCommand(
    val tab: Tab
) : NavigationCommand

object ReselectTabCommand : NavigationCommand

class ClearTabBackStackCommand(
    val tab: Tab? = null
) : NavigationCommand

object BackToRootTabScreenCommand : NavigationCommand
