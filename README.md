## S Pen Remote Open SDK

A [clean room design](https://en.wikipedia.org/wiki/Clean_room_design) of the
[Galaxy S Pen Remote SDK](https://developer.samsung.com/galaxy-spen-remote/overview.html)

This exists for compatibility between GPLv3 code and the S Pen.

The Galaxy S Pen Remote SDK is non-free and Samsung have [not replied to requests](https://forum.developer.samsung.com/t/disabling-spen-air-command-for-the-app/18585/2) 
relating to it.

The first commit of this repository contains a specification has been written via 
inspection of the functionality.

The specification contains the [public API](https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/package-summary.html) 
of the library in Java, converted to Kotlin and improved via the use of `enum class`

It is intended that other developer(s) implement this specification WITHOUT viewing any Samsung source/decompilation
 and submit changes as a pull request to this repository

## API Documentation

The API is publicly documented: https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/package-summary.html

## Order of implementation

When implementing functionality, the (rough) suggested order is:

* `AirMotionEvent` and `ButtonEvent` - trivial
* `SPenRemote`: `setConnectionStateChangeListener` - trivial
* `SPenRemote`: `isConnected` - initial value - trivial
* `SpenEvent`: standard (legacy) Android implementation of a `Parcelable`
* `SPenUnitManager`: implementation of a singleton which can't be accessed from outside the module
* `SPenUnit`: figure out the required constructor parameters
* `SPenUnit`: implementation of a listener to be passed into an `ISpenRemoteService`
* `SPenUnit`: functionality to accept `SPenEventListener` and adapt it to the listener
used for `ISpenRemoteService`
* `SPenUnitManager`: caching for `SPenUnit`
* `SPenUnitManager`: interfacing with `SPenUnit` to add and remove listeners
* `SPenRemote`: Validation of whether the current device is a Samsung device
* `SPenRemote`: Validation of whether the package exists
* `SPenRemote`: creation of the intent for binding
* `SPenRemote`: binding using the intent
* `SPenRemote`: Skeleton for `ServiceConnection`: connect/disconnect + ensuring it's called
* `SPenRemote`: implementation of `ServiceConnection`
* `SPenUnitManager`: Interaction between `SPenRemote` and `SPenUnitManager` once a service is running
* `SPenUnitManager`: handling `[un]registerSPenEventListener` once a service is obtained
* `SPenRemote`: handling `disconnect`
* Optional:
  * `SPenRemote`: `SemFeatures` - may or may not be hard to handle
    as it requires a real device + reflection to test out the API
     * Bluetooth Low Energy being enabled 
     * `isFeatureEnabled` 
* Test + release as library

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)