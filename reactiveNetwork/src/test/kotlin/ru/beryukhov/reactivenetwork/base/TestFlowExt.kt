package ru.beryukhov.reactivenetwork.base

import kotlinx.coroutines.flow.Flow
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

typealias TestFlowExpectation<T> = (TestFlow<T>) -> Unit

/**
 * Expects a certain [TestFlowExpectation] from the [TestFlow].
 */
infix fun <T> TestFlow<T>.expect(expectation: TestFlowExpectation<T>): TestFlow<T> {
    expectation(this)
    return this
}

/**
 * Asserts that no errors occurred during collection the the [Flow].
 */

fun noError(): TestFlowExpectation<*> = { testFlow ->
    assertNull(testFlow.error, "${testFlow.tag} has error")
}

/**
 * Asserts that an errors occurred during collection the the [Flow].
 */

fun anyError(): TestFlowExpectation<*> = { testFlow ->
    assertNotNull(testFlow.error, "${testFlow.tag} has no error")
}

/**
 * Asserts that an [T] error occurred during collection of the [Flow].
 */

inline fun <reified T : Throwable> error(): TestFlowExpectation<*> = { testFlow ->
    assertNotNull(testFlow.error, "${testFlow.tag} has no error")
    assertEquals(T::class, testFlow.error!!::class, "${testFlow.tag} has wrong error")
}

/**
 * Asserts that no emissions occurred during collection the the [Flow].
 */

fun noEmissions(): TestFlowExpectation<*> = { testFlow ->
    assertEquals(0, testFlow.emissions.count(), "${testFlow.tag} has values")
}

/**
 * Asserts that any emissions occurred during collection the the [Flow].
 */

fun anyEmission(): TestFlowExpectation<*> = { testFlow ->
    testFlow.assertNotEmpty()
}

/**
 * Asserts that [expected] count of emissions occurred during collection the the [Flow].
 */

fun emissionCount(expected: Int): TestFlowExpectation<*> = { testFlow ->
    assertEquals(
        expected,
        testFlow.emissions.count(),
        "${testFlow.tag} has wrong emissions count"
    )
}

/**
 * Asserts that [expected] emissions occurred during collection the the [Flow].
 */

fun <T> emissions(vararg expected: T): TestFlowExpectation<T> = { testFlow ->
    testFlow.assertNotEmpty()
    assertEquals(expected.toList(), testFlow.emissions, "${testFlow.tag} has wrong emissions")
}

/**
 * Asserts that [expected] emissions occurred during collection the the [Flow].
 */

fun <T> emissions(expected: List<T>): TestFlowExpectation<T> = { testFlow ->
    testFlow.assertNotEmpty()
    assertEquals(expected, testFlow.emissions, "${testFlow.tag} has wrong emissions")
}

/**
 * Asserts the [predicate] for every emission that occurred during collection the the [Flow].
 */

fun <T> allEmissions(predicate: (T) -> Boolean): TestFlowExpectation<T> = { testFlow ->
    testFlow.assertNotEmpty()
    testFlow.emissions.forEachIndexed { index, emission ->
        assertTrue(
            predicate.invoke(emission),
            "${testFlow.tag} emission at $index does not match predicate"
        )
    }
}

/**
 * Asserts that [expected] emission occurred at [index] in the [emissions] collection.
 */

fun <T> emission(index: Int, expected: T): TestFlowExpectation<T> = { testFlow ->
    testFlow.assertNotEmpty()
    testFlow.hasEmissionAt(index)
    assertEquals(
        expected,
        testFlow.emissions[index],
        "${testFlow.tag} emission at $index is not $expected"
    )
}

/**
 * Asserts the [predicate] for the emission at [index] in the [emissions] collection.
 */

fun <T> emission(index: Int, predicate: (T) -> Boolean): TestFlowExpectation<T> = { testFlow ->
    testFlow.assertNotEmpty()
    testFlow.hasEmissionAt(index)
    assertTrue(
        predicate.invoke(testFlow.emissions[index]),
        "${testFlow.tag} emission at $index does not match predicate"
    )
}

/**
 * Asserts that [expected] emission occurred first in the [emissions] collection.
 */

fun <T> firstEmission(expected: T): TestFlowExpectation<T> = { testFlow ->
    testFlow.assertNotEmpty()
    assertEquals(expected, testFlow.emissions.first(), "${testFlow.tag} has wrong first emission")
}

/**
 * Asserts the [predicate] for the first emission in the [emissions] collection.
 */

fun <T> firstEmission(predicate: (T) -> Boolean): TestFlowExpectation<T> = { testFlow ->
    testFlow.assertNotEmpty()
    assertTrue(
        predicate.invoke(testFlow.emissions.first()),
        "${testFlow.tag} first emission does not match predicate"
    )
}

/**
 * Asserts that [expected] emission occurred last in the [emissions] collection.
 */

fun <T> lastEmission(expected: T): TestFlowExpectation<T> = { testFlow ->
    testFlow.assertNotEmpty()
    assertEquals(expected, testFlow.emissions.last(), "${testFlow.tag} has wrong last emission")
}

/**
 * Asserts the [predicate] for the last emission in the [emissions] collection.
 */

fun <T> lastEmission(predicate: (T) -> Boolean): TestFlowExpectation<T> = { testFlow ->
    testFlow.assertNotEmpty()
    assertTrue(
        predicate.invoke(testFlow.emissions.last()),
        "${testFlow.tag} has wrong last emission matching predicate"
    )
}

/**
 * Asserts that the [TestFlow] has not completed emission collection.
 */

fun noCompletion(): TestFlowExpectation<*> = { testFlow ->
    assertFalse(testFlow.completed, "${testFlow.tag} has completion")
}

/**
 * Asserts that the [TestFlow] has completed emission collection.
 */

fun anyCompletion(): TestFlowExpectation<*> = { testFlow ->
    assertTrue(testFlow.completed, "${testFlow.tag} has no completion")
}

/**
 * Asserts that the [TestFlow] has completed emission collection with no error.
 */

fun regularCompletion(): TestFlowExpectation<*> = { testFlow ->
    testFlow expect anyCompletion()
    testFlow expect noError()
}

/**
 * Asserts that the [TestFlow] has completed emission collection with an error of type [T].
 */

inline fun <reified T : Throwable> exceptionalCompletion(): TestFlowExpectation<*> = { testFlow ->
    testFlow expect anyCompletion()
    testFlow expect error<T>()
}


private fun TestFlow<*>.assertNotEmpty() {
    assertNotEquals(0, emissions.count(), "$tag has no values")
}


private fun TestFlow<*>.hasEmissionAt(index: Int) {
    assertNotNull(emissions.getOrNull(index), "$tag has no emission at $index")
}