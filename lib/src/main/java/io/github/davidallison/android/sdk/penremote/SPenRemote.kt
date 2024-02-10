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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.samsung.android.sdk.penremote.ISPenRemoteService
import io.github.davidallison.android.sdk.penremote.SPenRemote.ConnectionResultCallback
import io.github.davidallison.android.sdk.penremote.SPenRemote.connect
import io.github.davidallison.android.sdk.penremote.SPenRemote.disconnect
import io.github.davidallison.android.sdk.penremote.SPenRemote.isConnected
import io.github.davidallison.android.sdk.penremote.SPenRemote.isFeatureEnabled

/**
 * [SPenRemote] handles the lifecycle of connecting to the S Pen Remote Service (supplied by the Samsung OS)
 *
 * If a successful connection is made, a [SPenUnitManager] is provided to the user
 * via [ConnectionResultCallback.onSuccess]. The unit manager allows a user to listen to S Pen events
 *
 * [SPenRemote] handles the [ServiceConnection] to
 * `com.samsung.android.service.aircommand.remotespen.external.RemoteSpenBindingService`
 *
 * The class allows a user to:
 * * [connect] and [disconnect] to the `RemoteSpenBindingService`
 * * Check connection status: [isConnected]
 * * Check the availability of features: [isFeatureEnabled]
 *
 * The class is responsible for pre-validation checks, ensuring that the device is
 * a Samsung with S Pen support
 *
 * ```kotlin
 *     val unitManagerToUse: SPenUnitManager? = null
 *     val activity = requireActivity()
 *
 *     if (!SPenRemote.isConnected) {
 *         SPenRemote.connect(activity, object: ConnectionResultCallback {
 *             override fun onSuccess(unitManager: SPenUnitManager) {
 *                 // the device is S Pen capable. See [SPenUnitManager] for how to listen to events
 *                 unitManagerToUse = unitManager
 *             }
 *
 *             override fun onFailure(code: ConnectionResultCallback.Error) {
 *                 // the device is not S Pen capable, or an error occurred
 *             }
 *         })
 *     }
 *  ```
 *
 *  https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/SpenRemote.html
 */
// The service is also responsible for
//      service connection, via [context.bindService] and similar APIs
//      **sometimes**  ConnectionResultCallback and ConnectionStateChangeListener are called
//      asynchronously, rather than synchronously
//      Knowing whether the connection is active, and returning the value via [isConnected]
object SPenRemote {
    const val VERSION_CODE = 16777217
    /** MAJOR.MINOR.REVISION */
    const val VERSION_NAME = "1.0.1"

    private const val SERVICE_CLASS_NAME = "com.samsung.android.service.aircommand.remotespen.external.RemoteSpenBindingService"

    private var stateChangeListener: ConnectionStateChangeListener? = null

    // This is initially false
    // This is true immediately after `context.bindService` is called [connect]
    // This is false immediately after `context.unbindService` is called [disconnect]
    // Probable BUG: The value of this is not affected by [ServiceConnection]
    //   onServiceDisconnected or errors in onServiceConnected will not set this to false
    var isConnected: Boolean = false
        private set

    private var iSpenRemoteService: ISPenRemoteService? = null

    private var connectionResultCallback: ConnectionResultCallback? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            Log.i("Spen", "Service connected")
            if (service == null) {
                connectionResultCallback?.onFailure(ConnectionResultCallback.Error.CONNECTION_FAILED)
                return
            }
            iSpenRemoteService = ISPenRemoteService.Stub.asInterface(service)

            SPenUnitManager.instance.remoteService = iSpenRemoteService
            connectionResultCallback?.onSuccess(SPenUnitManager.instance)
            stateChangeListener?.onChange(ConnectionStateChangeListener.State.CONNECTED)
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            Log.i("Spen", "Service disconnected")
            iSpenRemoteService = null
            SPenUnitManager.instance.remoteService = null
            isConnected = false

            // [ConnectionStateChangeListener] is called with [State.DISCONNECTED_BY_UNKNOWN_REASON] (??)
            stateChangeListener?.onChange(ConnectionStateChangeListener.State.DISCONNECTED_BY_UNKNOWN_REASON)
        }
    }

    private var semFeatureList: List<String>? = null

    fun isFeatureEnabled(feature: Feature): Boolean {
        // interestingly, this is ONLY user-facing, [SPenUnitManager] will allow listening to
        // a feature even if [isFeatureEnabled] returns false. It's up to the user to check
        // before listening

        if (semFeatureList != null){
            return semFeatureList!!.contains(feature.samsungFeatureName)
        }

        // PROCEDURE: loading supported features - only called once
        // Note that loading the features may occur without a call to isFeatureEnabled [if inside connect()]

        val featureList = FloatingFeatureReflected().getString(SemFeatures.SPEN_FEATURE_LIST)
        semFeatureList = featureList?.split(",") ?: listOf()
        return isFeatureEnabled(feature)
    }


    /**
     * @throws java.lang.NoClassDefFoundError: Failed resolution of: Lcom/samsung/android/feature/SemFloatingFeature;
     * This is a bug: https://forum.developer.samsung.com/t/spenremotesdk-on-device-without-samsung-android-feature/7939
     */
    fun connect(activity: Activity, listener: ConnectionResultCallback) {

        fun unsupported() {
            Log.e("SPen", "Unsupported device")
            listener.onFailure(ConnectionResultCallback.Error.UNSUPPORTED_DEVICE)
        }

        // NOTE: Validation that `context` is an `Activity` has been removed, in favour of accepting a non-null Context

        // NOTE: this routine seems buggy:
        // * if a failure was obtained, the method continued executing
        // * [listener] may not be invoked on failure, even though it is intended to be
        // I strongly recommend fixing the above bugs in the reimplementation
        // and have therefore modified the below definitions to specify the behaviour which I would
        // prefer

        Log.i("Spen", VERSION_NAME)


        if (!(Build.BRAND.equals("Samsung", true) &&
                    Build.MANUFACTURER.equals("Samsung", true))
        ) {
            unsupported()
            return
        }

//        TODO("Validate that air commands + S Pen Framework exist as a package")
        // use INTENT_PACKAGE_NAME and FLAG_GRANT_PREFIX_URI_PERMISSION
        // If an exception is not returned, the check succeeded
        // If a NameNotFoundException is obtained, log, send UNSUPPORTED_DEVICE and return

        try {
            activity.packageManager.getApplicationInfo(
                Constants.INTENT_PACKAGE_NAME,
                PackageManager.GET_META_DATA
            )
        } catch (e: PackageManager.NameNotFoundException) {
            unsupported()
            return
        }


        if (!FloatingFeatureReflected().getBoolean(SemFeatures.HAS_BLUETOOTH_LOW_ENERGY)) {
            unsupported()
            return
        }

        if (!isFeatureEnabled(Feature.FEATURE_TYPE_BUTTON)) {
            unsupported()
            return
        }

        connectionResultCallback = listener

        @SuppressLint("WrongConstant")
        val intent = Intent()
            .addFlags(Constants.INTENT_FLAGS)
            .setClassName(Constants.INTENT_PACKAGE_NAME, Constants.INTENT_CLASS_NAME)
            .putExtra(Constants.INTENT_EXTRA_BINDER_TYPE, Constants.INTENT_EXTRA_BINDER_TYPE_VALUE)
            .putExtra(Constants.INTENT_EXTRA_CLIENT_VERSION, Constants.INTENT_EXTRA_CLIENT_VERSION_VALUE)
            .putExtra(Constants.INTENT_EXTRA_CLIENT_PACKAGE_NAME, activity.packageName)

        try {
            activity.bindService(intent, serviceConnection, BIND_AUTO_CREATE)
            isConnected = true
        } catch (e: SecurityException) {
            Log.e(
                "Spen",
                "Permission com.samsung.android.sdk.penremote.BIND_SPEN_REMOTE is required"
            )
            //    BUG: if this error occurs neither ConnectionResultCallback nor
            //    ConnectionStateChangeListener are called
        }
    }

    /** @see ConnectionStateChangeListener */
    // unusually: visible setter and non-visible getter
    fun setConnectionStateChangeListener(listener: ConnectionStateChangeListener) {
        this.stateChangeListener = listener
    }

    fun disconnect(context: Context) {
        if (!isConnected) return

        Log.i("Spen", "Service is disconnecting")
        // The Unit Manager should have ALL event listeners unbound, but cached entries are NOT yet removed
        SPenUnitManager.instance.clearListeners()
        context.unbindService(serviceConnection)
        // inside the onServiceDisconnected call, the Unit Manager should have its caches removed, and should no longer reference the service
        stateChangeListener?.onChange(ConnectionStateChangeListener.State.DISCONNECTED)

        // at this point, the service should be disconnected and the class state should be updated
    }

    /*
     * This is likely going to be the hardest part of the reverse engineering
     *
     * NOTE: skipping this check and assuming it succeeds would be an acceptable 'first draft'
     * of this functionality
     *
     * com.samsung.android.feature.SemFloatingFeature is a class which exists at RUNTIME on
     * select Samsung Devices
     *
     * BUG: if this API does not exist, then [connect] throws
     * java.lang.NoClassDefFoundError: Failed resolution of: Lcom/samsung/android/feature/SemFloatingFeature;
     *
     * When a feature in this Enum is desired:
     * * Get an instance of `SemFloatingFeature` [probably via reflection?]
     * * Use reflection to determine the available methods on the instance
     * * call the appropriate method returning a boolean/string for the below constants
     */
    private enum class SemFeatures(val feature: String) {
        /**
         * Obtains a list of [Feature], as a comma separated string
         *
         * @see Feature.samsungFeatureName for expected features
         */
        // string
        SPEN_FEATURE_LIST("SEC_FLOATING_FEATURE_COMMON_CONFIG_BLE_SPEN_SPEC"),
        // boolean
        HAS_BLUETOOTH_LOW_ENERGY("SEC_FLOATING_FEATURE_COMMON_SUPPORT_BLE_SPEN"),
    }

    /** For use in [isFeatureEnabled] */
    enum class Feature(val code: Int, val samsungFeatureName: String) {
        FEATURE_TYPE_BUTTON(0, "button"),
        FEATURE_TYPE_AIR_MOTION(1, "airmotion"),
    }

    /**
     * Allows a user to listen to connection/disconnection events to the S Pen Remote Service
     *
     * Usage is optional.
     *
     * Setter: [setConnectionStateChangeListener]
     *
     * https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/SpenRemote.ConnectionStateChangeListener.html
     */
    // ALL calls to this are performed asynchronously
    interface ConnectionStateChangeListener {
        fun onChange(state: State)

        /**
         * https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/SpenRemote.State.html
         */
        enum class State(val code: Int) {
            /**
             * When the service is connected in [ServiceConnection.onServiceConnected]
             */
            // This is called AFTER ConnectionResultCallback.onSuccess
            CONNECTED(0),
            /** When [SPenRemote.disconnect] is called */
            DISCONNECTED(-1),
            /**
             * If the service is disconnected unexpectedly by [ServiceConnection.onServiceDisconnected]
             *
             * [disconnect] should have been used instead
             */
            DISCONNECTED_BY_UNKNOWN_REASON(-2),
        }
    }


    /**
     * Passed to [connect] and handles the success or failure of the connection to the S Pen
     *
     * This allows a user to obtain an instance of the [Unit Manager][SPenUnitManager]
     *
     * https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/SpenRemote.ConnectionResultCallback.html
     */
    // Calls to ConnectionResultCallback made by SPenRemote can either be async or sync
    // If they are async, the call is made inside a lambda and handled at some point in the future
    // If they are sync, the call is made directly
    // all calls to ConnectionResultCallback inside [onServiceConnected] are sync
    // all other calls are async
    interface ConnectionResultCallback {
        /** A [SPenUnitManager] can be used */
        fun onSuccess(unitManager: SPenUnitManager)
        /**
         * A failure occurred when connecting to the service
         *
         * This occurs normally if the user's device does not support the S Pen
         * @se [Error.UNSUPPORTED_DEVICE]
         */
        fun onFailure(code: Error)

        /**
         * https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/SpenRemote.Error.html
         */
        enum class Error(val code: Int) {
            /**
             * One of:
             * * The user is not using a Samsung device
             * * The S Pen framework is not found
             * * Bluetooth Low Energy is not supported
             * * The device does not list any S Pen Remote features
             */
            UNSUPPORTED_DEVICE(-1),
            /** The second parameter to [ServiceConnection.onServiceConnected] was null */
            CONNECTION_FAILED(-2),
            /** Unused */
            UNKNOWN(-100),
        }
    }

    private object Constants {
        /** Data for the call to [Context.bindService] */
        const val INTENT_PACKAGE_NAME = "com.samsung.android.service.aircommand"
        const val INTENT_CLASS_NAME = SERVICE_CLASS_NAME
        const val INTENT_EXTRA_BINDER_TYPE = "binderType"
        const val INTENT_EXTRA_BINDER_TYPE_VALUE = 2
        const val INTENT_EXTRA_CLIENT_VERSION = "clientVersion"
        const val INTENT_EXTRA_CLIENT_VERSION_VALUE = VERSION_CODE
        const val INTENT_EXTRA_CLIENT_PACKAGE_NAME = "clientPackageName"
        const val INTENT_FLAGS = BIND_AUTO_CREATE

        /** Used when checking whether [SERVICE_CLASS_NAME] exists */
        private const val FLAG_GRANT_PREFIX_URI_PERMISSION = Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
    }

    private class FloatingFeatureReflected {
        val className = "com.samsung.android.feature.SemFloatingFeature"

        var clazz: Class<*>?
        var instance: Any?

        init {
            val classLoader = ClassLoader.getSystemClassLoader()
            clazz = classLoader.loadClass(className)
            instance = clazz?.getDeclaredMethod("getInstance")?.invoke(null, *arrayOf())
        }

        fun getString(feature: SemFeatures): String? {
            return clazz?.getDeclaredMethod("getString", java.lang.String::class.java)
                ?.invoke(instance, feature.feature) as String?
        }

        fun getBoolean(feature: SemFeatures): Boolean {
            return clazz?.getDeclaredMethod(
                "getBoolean",
                java.lang.String::class.java,
                Boolean::class.javaPrimitiveType
            )?.invoke(instance, feature.feature, false) as Boolean? ?: false
        }
    }
}