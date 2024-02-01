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
 * A user-defined event listener allowing a user to listen to events from the S Pen
 *
 * 'See Also' contains usage samples
 *
 * @see AirMotionEvent Usage sample
 * @see ButtonEvent Usage sample
 *
 * https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/SpenEventListener.html
 */
/*
 Similar to [ISpenEventListener], but a simple interface for library users, rather than a proxy
 */
fun interface SPenEventListener {
    fun onEvent(event: SPenEvent)
}