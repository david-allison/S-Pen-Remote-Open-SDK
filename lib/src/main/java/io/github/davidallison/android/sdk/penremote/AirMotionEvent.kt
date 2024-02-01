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
 * **Warning**: AirMotion events are battery intensive. Remember to call
 * [SPenUnitManager.unregisterSpenEventListener] when finished listening
 *
 * Usage:
 *
 * ```kotlin
 * val unitManager: SPenUnitManager = ...
 * unitManager.registerSpenEventListener(unitManager.getUnit(TYPE_AIR_MOTION)) { event ->
 *     val airMotionEvent = AirMotionEvent(event)
 *
 *     val deltaX = airMotionEvent.deltaX
 *     val deltaY = airMotionEvent.deltaY
 *
 *     Timber.d("Air Motion = $deltaX, $deltaY")
 * }
 * ```
 *
 * https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/AirMotionEvent.html
 */
class AirMotionEvent(event: SPenEvent) {
    init {
        TODO("set variables using the SpenEvent")
    }
    val deltaX: Float // index 0 in the provided SPenEvent
    val deltaY: Float // index 1 in the provided SPenEvent
    val timeStamp: Long
}