package ru.beryukhov.reactivenetwork.internet.observing.error

import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith

import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
open class DefaultErrorHandlerTest {

    private val handler = spyk(DefaultErrorHandler())

    @Test
    fun shouldHandleErrorDuringClosingSocket() { // given
        val errorMsg = "Could not close the socket"
        val exception = Exception(errorMsg)
        // when
        handler.handleError(exception, errorMsg)
        // then
        verify(exactly = 1){handler.handleError(exception, errorMsg)}
    }
}