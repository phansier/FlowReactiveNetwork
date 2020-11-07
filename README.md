# FlowReactiveNetwork on Coroutines
[![Download](https://api.bintray.com/packages/andreyberyukhov/FlowReactiveNetwork/FlowReactiveNetwork/images/download.svg) ](https://bintray.com/andreyberyukhov/FlowReactiveNetwork/FlowReactiveNetwork/_latestVersion)
[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.4.10-blue.svg)](https://kotlinlang.org)

[![kotlinweekly](https://img.shields.io/badge/kotlinweekly.net-204-blue.svg)](https://mailchi.mp/kotlinweekly/kotlin-weekly-204)
![CI](https://github.com/AndreySBer/FlowReactiveNetwork/workflows/Android%20CI/badge.svg)

FlowReactiveNetwork is an Android library listening **network connection state** and **Internet connectivity** with Coroutines Flow. It's a port of [ReactiveNetwork](https://github.com/pwittchen/ReactiveNetwork) library rewritten with Reactive Programming approach. Library supports both new and legacy network monitoring strategies. Min sdk version = 14.

Usage
-----
See [ReactiveNetwork](https://github.com/pwittchen/ReactiveNetwork) docs for Usage. API is the same except for return data types:
- `Observable<T>` replaced by `Flow<T>`
- `Single<T>` replaced by `suspend fun():T`

Download
--------

You can depend on the library through Gradle:

```groovy
dependencies {
    implementation 'ru.beryukhov:flowreactivenetwork:1.0.2'
}
// as for the library is not available in jcenter yet
allprojects {
    repositories {
        //...
        maven {
            url "https://dl.bintray.com/andreyberyukhov/FlowReactiveNetwork"
        }
    }
}
```

Tests
-----

Tests are available in `reactiveNetwork/src/test/kotlin/` directory and can be executed on JVM without any emulator or Android device from Android Studio or CLI with the following command:

```
./gradlew test
```

Warning
-----

There are some problems with working on PreLollipop devices visible by unit-tests and tests on cancellation of Flow.
