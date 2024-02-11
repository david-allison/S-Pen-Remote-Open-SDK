/**
 * Copyright 2024 David Allison <davidallisongithub@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.davidallison.android.sdk.penremote

/**
 * Usage:
 * ```kotlin
 * val unitManager: SPenUnitManager = ...
 * unitManager.registerSPenEventListener(unitManager.getUnit(TYPE_BUTTON)) { event ->
 *     val buttonEvent = ButtonEvent(event)
 *
 *     when (buttonEvent.action) {
 *         ButtonAction.ACTION_DOWN -> Timber.d("S Pen Button Pressed")
 *         ButtonAction.ACTION_UP -> Timber.d("S Pen Button Released")
 *     }
 * }
 * ```
 *
 * https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/ButtonEvent.html
 */
class ButtonEvent(event: SPenEvent) {
    val timeStamp: Long = event.timeStamp
    val action: ButtonAction =
        if (event.values[1].toInt() == 0) ButtonAction.ACTION_DOWN else ButtonAction.ACTION_UP

    enum class ButtonAction(val code: Int) {
        ACTION_DOWN(0), ACTION_UP(1),
    }
}