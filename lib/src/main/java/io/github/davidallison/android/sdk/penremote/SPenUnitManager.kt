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

import android.os.RemoteException
import android.util.Log
import com.samsung.android.sdk.penremote.ISPenRemoteService
import java.util.EnumMap

/**
 * Allows a user to set [event listeners][SPenEventListener] for specific [events][SPenUnitType]
 * sent by the S Pen:
 *
 * * [registerSPenEventListener]
 * * [unregisterSpenEventListener]
 *
 * An instance is obtained after a connection to the S Pen Remote Service is established
 * @see [SPenRemote.ConnectionResultCallback.onSuccess]
 *
 * This manages a cache of [SPenUnit] instances
 * At any given time, there is at most one [SPenUnit] for each [SPenUnitType]
 *
 * https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/SpenUnitManager.html
 */
/*
 * This is a singleton, but getting the instance directly should not be possible outside of
 * the `io.github.davidallison.penremote` module.
 *
 * A user should only be able to access this class from
 * [SPenRemote.ConnectionResultCallback.onSuccess]
 */
// The constructor has no functionality
class SPenUnitManager private constructor() {

    internal var remoteService: ISPenRemoteService? = null
        set(value) {
            field = value
            unitCache.clear()
        }

    private var unitCache: MutableMap<SPenUnitType, SPenUnit> = EnumMap(SPenUnitType::class.java)

    /**
     * Obtains a [SPenUnit] of the provided [unitType]
     */
    fun getUnit(unitType: SPenUnitType): SPenUnit {
        if (remoteService == null) throw RemoteException("Service not connected")
        // NOTE: The original code accepted an int and had the possibility of returning null
        // The new code accepts an enum, and each value should be handled

        // for either: SPenUnitType.TYPE_BUTTON or SPenUnitType.TYPE_AIR_MOTION
        // Provides a [SPenUnit] associated with [unitType] from the cache, or creates it and
        // returns it if it was not previously cached

        // The SPenUnit should have knowledge of its type, and maintain its own reference to the
        // [ISpenRemoteService]

        return unitCache[unitType] ?: SPenUnit(unitType, remoteService!!).also {
            unitCache[unitType] = it
        }
    }

    /**
     * Associates the provided listener with the given [unit] and starts listening for the event
     *
     * If called multiple times, only the last listener receives events
     */
    fun registerSPenEventListener(unit: SPenUnit, listener: SPenEventListener) {
        // If a RemoteException is thrown inside this method, it is logged and ignored.
        // A RemoteException is explicitly thrown if remoteService inside [unit] is null

        // inside the SPenUnit:
        /*
         * Listens to events from the remote service for [type] and forwards them to [listener]
         *
         * Only one listener may be used at a time within a [SPenUnit], if this is called
         * multiple times, the previous listener is discarded
         *
         * @throws RemoteException if [remoteService] is null
         */

        // ensure the Unit's ISpenEventListener will forward events to [listener] and and previous
        // listeners are discarded and no longer receive events

        // call [unit.remoteService.registerSpenEventListener] using the Stub + type
        // a further explanation of the functionality is below

        // ----

        // It is important to understand that SPenUnit maintains the mapping between a Stub (ISpenEventListener.Stub)
        // and a user-supplied SPenEventListener

        // The stub is connected to the remote service via [registerSpenEventListener] each time
        // this method is called

        // The stub is disconnected when [removeSPenEventListener] is called

        // If [registerSPenEventListener] is called multiple times:
        // the old [SPenEventListener] is replaced with the new listener
        // [remoteService.registerSpenEventListener] is called again on the remote service, using the same Stub listener
        // !! [remoteService.unregisterSpenEventListener] is NOT called on the remote service

        // The purpose of the stub is only to forward events on to the user-supplied [SPenEventListener]
        // If the user-supplied SPenEventListener is null, the stub does nothing

        // NOTE: in the original implementation, the Stub forwarded the same event to SPenEventListener
        // in this implementation, we should convert from [com.samsung.android.sdk.penremote.SpenEvent]
        // to [io.github.davidallison.penremote.SPenEvent]
        if (remoteService == null) throw RemoteException("Service not connected")
        try {
            unit.registerSpenEventListener(listener)
        } catch (e: RemoteException) {
            Log.e("Spen", "Error when registering listener", e)
        }
    }

    /**
     * [unit] stops listening to any events from the remote service
     */
    fun unregisterSpenEventListener(unit: SPenUnit) {
        // remove the listener added to the unit in [registerSPenEventListener]
        // interface with the [remoteService] associated with the unit and remove the listener
        unit.unregisterSpenEventListener()
    }

    internal fun clearListeners() {
        unitCache.values.forEach { unregisterSpenEventListener(it) }
    }

    companion object {
        internal val instance = SPenUnitManager()
    }
}