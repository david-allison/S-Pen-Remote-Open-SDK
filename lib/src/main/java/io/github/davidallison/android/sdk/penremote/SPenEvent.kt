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

import com.samsung.android.sdk.penremote.SpenEvent

// This class is not in the original package.
// It exists because [com.samsung.android.sdk.penremote.SpenEvent] is used by the AIDL
// and therefore should be in the com.samsung.android.sdk.penremote package
// We do not want users of the library to import classes under com.samsung.android.sdk
/**
 * Data for either [AirMotionEvent] or [ButtonEvent]
 *
 * https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/SpenEvent.html
 */
class SPenEvent internal constructor(event: SpenEvent) {
    val timeStamp: Long = event.timeStamp
    val values: Array<Float> = event.values
}