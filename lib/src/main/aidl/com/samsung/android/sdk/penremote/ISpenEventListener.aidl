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

package com.samsung.android.sdk.penremote;

parcelable SpenEvent;

/**
 * This service should be viewed as a member of ISpenRemoteService
 *
 * Receives the events sent by the S Pen
 *
 * There are two types of events:
 * * Button presses [Up/Down]
 * * AirMotionEvents [deltaX, deltaY]
 *
 * An event listener will only have one event type provided.
 * The type of event is specified in the call to ISpenRemoteService
 */
interface ISpenEventListener {
    void onEvent(out SpenEvent event);
}