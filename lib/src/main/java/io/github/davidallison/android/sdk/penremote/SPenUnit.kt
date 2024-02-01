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

/*
 * This class manages instances of specific S Pen embedded unit.
 *
 * It maps calls from the remote [ISpenEventListener] to the user's [SPenEventListener]
 *
 * This class primarily acts to manage an instance of a [ISpenEventListener.Stub] which is
 * provided to [ISpenRemoteService.registerSpenEventListener] and [ISpenRemoteService.unregisterSpenEventListener]
 *
 * After a [SPenEventListener] is provided to this class by [SPenUnitManager],
 * the [ISpenEventListener.Stub] listens to events and delegates them to the supplied [SPenEventListener]
 *
 * The [SPenUnitManager] also controls removing the [SPenEventListener], in which case the
 * listener is unregistered from the remoteService and the reference to the [SPenEventListener] is
 * removed
 *
 * There will typically be a max of two instances of this class managed by [SPenUnitManager]:
 * * [SPenUnitType.TYPE_BUTTON]
 * * [SPenUnitType.TYPE_AIR_MOTION]
 *
 * @param type The type of event that the unit is listening to. Passed to
 * [ISpenRemoteService.registerSpenEventListener]
 * @param remoteService either a reference to the remote service, or `null`.
 * If `null` then throw a [RemoteException] for [SPenUnitManager.registerSPenEventListener]
 */
// the constructor to this class has required parameters
/**
 * https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/SpenUnit.html
 */
class SPenUnit internal constructor()