package ru.beryukhov.reactivenetwork

/**
 * A functional interface (callback) that returns true or false for the given input value.
 * @param <T> the first value
</T> */
interface Predicate<T> {
    /**
     * Test the given input value and return a boolean.
     * @param t the value
     * @return the boolean result
     * @throws Exception on error
     */
    @Throws(Exception::class)
    fun test(t: T): Boolean
}