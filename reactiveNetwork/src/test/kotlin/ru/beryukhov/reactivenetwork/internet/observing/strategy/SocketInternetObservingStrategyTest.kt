package ru.beryukhov.reactivenetwork.internet.observing.strategy

import at.florianschuster.test.flow.emission
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

import org.robolectric.RobolectricTestRunner
import ru.beryukhov.reactivenetwork.BaseFlowTest
import ru.beryukhov.reactivenetwork.internet.observing.error.ErrorHandler
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class SocketInternetObservingStrategyTest : BaseFlowTest() {

    private val strategy = spyk(SocketInternetObservingStrategy())
    private val errorHandler = mockk<ErrorHandler>(relaxed = true)
    private val socket = mockk<Socket>(relaxed = true)

    private val host: String = strategy.getDefaultPingHost()

    @Ignore
    @Test
    fun shouldBeConnectedToTheInternet() { // given
        every {
            strategy.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                errorHandler
            )
        } returns true

        // when

        val testFlow = strategy.observeInternetConnectivity(
            INITIAL_INTERVAL_IN_MS,
            INTERVAL_IN_MS,
            host,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandler
        ).testIn(scope = testScopeRule)

        // then
        testFlow expect emission(index = 0, expected = true)

    }

    @Ignore
    @Test
    fun shouldNotBeConnectedToTheInternet() { // given
        every {
            strategy.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                errorHandler
            )
        } returns false
        // when

        val testFlow = strategy.observeInternetConnectivity(
            INITIAL_INTERVAL_IN_MS,
            INTERVAL_IN_MS,
            host,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandler
        ).testIn(scope = testScopeRule)

        // then
        testFlow expect emission(index = 0, expected = false)

    }

    @Test
    @Throws(IOException::class)
    fun shouldNotBeConnectedToTheInternetWhenSocketThrowsAnExceptionOnConnect() { // given
        val address = InetSocketAddress(
            host,
            PORT
        )
        every { socket.connect(address, TIMEOUT_IN_MS) } throws (IOException())

        // when
        val isConnected = strategy.isConnected(
            socket,
            host,
            PORT,
            TIMEOUT_IN_MS,
            errorHandler
        )
        // then
        Truth.assertThat(isConnected).isFalse()
    }

    @Test
    @Throws(IOException::class)
    fun shouldHandleAnExceptionThrownDuringClosingTheSocket() { // given
        val errorMsg = "Could not close the socket"
        val givenException = IOException(errorMsg)
        every { socket.close() } throws (givenException)

        // when
        strategy.isConnected(
            socket,
            host,
            PORT,
            TIMEOUT_IN_MS,
            errorHandler
        )
        // then
        verify(exactly = 1) { errorHandler.handleError(givenException, errorMsg) }
    }

    @Test
    fun shouldBeConnectedToTheInternetViaSingle() { // given
        every {
            strategy.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                errorHandler
            )
        } returns true
        runBlocking {
            // when
            val isConnected = strategy.checkInternetConnectivity(
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandler
            )
            // then
            Truth.assertThat(isConnected).isTrue()
        }
    }

    @Test
    fun shouldNotBeConnectedToTheInternetViaSingle() { // given
        every {
            strategy.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                errorHandler
            )
        } returns false
        runBlocking {
            // when
            val isConnected = strategy.checkInternetConnectivity(
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandler
            )
            // then
            Truth.assertThat(isConnected).isFalse()
        }
    }

    @Test
    fun shouldNotTransformHost() { // when
        val transformedHost =
            strategy.adjustHost(HOST_WITHOUT_HTTP)
        // then
        Truth.assertThat(transformedHost)
            .isEqualTo(HOST_WITHOUT_HTTP)
    }

    @Test
    fun shouldRemoveHttpProtocolFromHost() { // when
        val transformedHost =
            strategy.adjustHost(HOST_WITH_HTTP)
        // then
        Truth.assertThat(transformedHost)
            .isEqualTo(HOST_WITHOUT_HTTP)
    }

    @Test
    fun shouldRemoveHttpsProtocolFromHost() { // when
        val transformedHost =
            strategy.adjustHost(HOST_WITH_HTTP)
        // then
        Truth.assertThat(transformedHost)
            .isEqualTo(HOST_WITHOUT_HTTP)
    }

    @Test
    fun shouldAdjustHostDuringCheckingConnectivity() { // given
        val host = host
        every {
            strategy.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                errorHandler
            )
        } returns true

        // when

        strategy.observeInternetConnectivity(
            INITIAL_INTERVAL_IN_MS,
            INTERVAL_IN_MS,
            host,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandler
        ).testIn(scope = testScopeRule)
        // then
        verify { strategy.adjustHost(host) }

    }

    companion object {
        private const val INITIAL_INTERVAL_IN_MS = 0
        private const val INTERVAL_IN_MS = 2000
        private const val PORT = 80
        private const val TIMEOUT_IN_MS = 30
        private const val HTTP_RESPONSE = 204
        private const val HOST_WITH_HTTP = "http://www.website.com"
        private const val HOST_WITHOUT_HTTP = "www.website.com"
    }
}