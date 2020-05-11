# ReactiveNetwork on Coroutines

ReactiveNetwork is an Android library listening **network connection state** and **Internet connectivity** with Coroutines Flow. It's a port of [ReactiveNetwork](https://github.com/pwittchen/ReactiveNetwork) library rewritten with Reactive Programming approach. Library supports both new and legacy network monitoring strategies. Min sdk version = 14.

Usage
-----
See [ReactiveNetwork](https://github.com/pwittchen/ReactiveNetwork) docs for Usage. API is the same except for return data types:
- `Observable<T>` replaced by `Flow<T>`
- `Single<T>` replaced by `suspend fun():T`

Tests
-----

Tests are available in `reactiveNetwork/src/test/kotlin/` directory and can be executed on JVM without any emulator or Android device from Android Studio or CLI with the following command:

```
./gradlew test
```

Warning
-----

There are some problems with working on PreLollipop devices visible by unit-tests and tests on cancellation of Flow.
