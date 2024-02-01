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

// this class is inside com.samsung so it can be used by the AIDL
package com.samsung.android.sdk.penremote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
 * A [parcel][Parcelable] used to efficiently receive S Pen events from [ISpenEventListener]
 *
 * It consists of two values: a timestamp and a collection of float values
 *
 * This is written to the parcel in the following order:
 * * timestamp (as long)
 * * length of array
 * * array contents (as float)
 *
 * This class needs to be manually written to handle the Parcelable implementation
 *
 * This is a standard pattern in Android and Google will point you in the right direction
 * https://medium.com/techmacademy/how-to-implement-and-use-a-parcelable-class-in-android-part-1-28cca73fc2d1
 *
 * [Parcelable.describeContents] returns 0
 */

/**
 * Internal class used for bindings. Use [io.github.davidallison.android.sdk.penremote.SPenEvent]
 *
 * If you see this: `Preferences -> Code Completion -> Exclude classes from code completion
 *
 * https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/SpenEvent.html
 *
 * @hide
 */
@Parcelize
data class SpenEvent(val timeStamp: Long, val values: Array<Float>) : Parcelable {
    init {
        TODO("Stop using Parcelize and implement Parcelable manually; remove the library reference to kotlin-parcelize")
    }
}