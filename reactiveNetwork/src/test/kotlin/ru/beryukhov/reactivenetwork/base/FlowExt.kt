package ru.beryukhov.reactivenetwork.base

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.TestCoroutineScope

/**
 * Tests a [Flow] by creating and returning a [TestFlow] which caches all value
 * emissions, error and completion.
 */
fun <T> Flow<T>.test(): TestFlow<T> =
    TestFlow(this)

/**
 * Tests a [Flow] by creating and returning a [TestFlow] which caches all value
 * emissions, error and completion.
 *
 * The [TestFlow] is also launched inside the [scope].
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Flow<T>.testIn(scope: TestCoroutineScope): TestFlow<T> {
    val testFlow = TestFlow(this)
    testFlow.launchIn(scope)
    return testFlow
}
