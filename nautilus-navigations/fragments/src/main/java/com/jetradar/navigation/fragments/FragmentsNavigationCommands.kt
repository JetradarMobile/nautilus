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

import androidx.fragment.app.Fragment
import com.jetradar.navigation.NavigationCommand

class ForwardCommand(
    val fragment: Fragment,
    val tag: String = fragment.javaClass.name,
    val animations: AnimationHolder? = null
) : NavigationCommand

class ReplaceCommand(
    val fragment: Fragment,
    val tag: String = fragment.javaClass.name,
    val animations: AnimationHolder? = null
) : NavigationCommand

class BackToCommand(val tag: String) : NavigationCommand