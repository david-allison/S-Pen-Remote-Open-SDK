## S Pen Remote Open SDK

A [clean room design](https://en.wikipedia.org/wiki/Clean_room_design) of the
[Galaxy S Pen Remote SDK](https://developer.samsung.com/galaxy-spen-remote/overview.html)

This exists for compatibility between GPLv3 code and the S Pen.

## API Documentation

The API is publicly documented: https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/package-summary.html

## Contributing

This library must be contributed to WITHOUT having viewed any source/decompilation related to
Samsung's Galaxy S Pen Remote SDK

## Library History

The Galaxy S Pen Remote SDK was non-free and Samsung had [not replied to requests](https://forum.developer.samsung.com/t/disabling-spen-air-command-for-the-app/18585/2) 
relating to it.

The first commit defined a specification of the library, written via inspection of the functionality.

The specification contained the [public API](https://developer.samsung.com/galaxy-spen-remote/api-reference/com/samsung/android/sdk/penremote/package-summary.html) 
of the library in Java, converted to Kotlin and improved via the use of `enum class`

The second commit of this library implemented the specification

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
