package ru.beryukhov.reactivenetwork.internet.observing.strategy

import at.florianschuster.test.flow.testIn
import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import ru.beryukhov.reactivenetwork.BaseFlowTest
import ru.beryukhov.reactivenetwork.internet.observing.error.ErrorHandler
import java.io.IOException
import java.net.HttpURLConnection

@FlowPreview
@ExperimentalCoroutinesApi
@Config(manifest = Config.NONE)
@RunWith(
    RobolectricTestRunner::class
)
class WalledGardenInternetObservingStrategyTest : BaseFlowTest() {

    private val errorHandler = mockk<ErrorHandler>(relaxed = true)
    private val strategy = spyk(WalledGardenInternetObservingStrategy())

    private val host: String = strategy.getDefaultPingHost()

    @Test
    fun shouldBeConnectedToTheInternet() { // given
        val errorHandlerStub = createErrorHandlerStub()
        every {
            strategy.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandlerStub
            )
        } returns true

        // when
        runBlocking {
            val testFlow = strategy.observeInternetConnectivity(
                INITIAL_INTERVAL_IN_MS,
                INTERVAL_IN_MS,
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandlerStub
            )

            // then
            testFlow.expectFirst(true)
        }
    }

    @Test
    fun shouldNotBeConnectedToTheInternet() { // given
        val errorHandlerStub =
            createErrorHandlerStub()
        every {
            strategy.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandlerStub
            )
        } returns false
        // when
        runBlocking {
            val testFlow = strategy.observeInternetConnectivity(
                INITIAL_INTERVAL_IN_MS,
                INTERVAL_IN_MS,
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandlerStub
            )

            // then
            testFlow.expectFirst(false)
        }
    }

    //Single methods are commented
    /*@Test
    fun shouldBeConnectedToTheInternetViaSingle() { // given
        val errorHandlerStub =
            createErrorHandlerStub()
        Mockito.`when`(
            strategy!!.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandlerStub
            )
        ).thenReturn(true)
        // when
        val observable: Single<Boolean> = strategy.checkInternetConnectivity(
            host,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandlerStub
        )
        val isConnected: Boolean = observable.blockingGet()
        // then
        Truth.assertThat(isConnected).isTrue()
    }

    @Test
    fun shouldNotBeConnectedToTheInternetViaSingle() { // given
        val errorHandlerStub =
            createErrorHandlerStub()
        Mockito.`when`(
            strategy!!.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandlerStub
            )
        ).thenReturn(false)
        // when
        val observable: Single<Boolean> = strategy.checkInternetConnectivity(
            host,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandlerStub
        )
        val isConnected: Boolean = observable.blockingGet()
        // then
        Truth.assertThat(isConnected).isFalse()
    }*/

    @Test
    @Throws(IOException::class)
    fun shouldCreateHttpUrlConnection() { // given
        val parsedDefaultHost = "clients3.google.com"
        // when
        val connection = strategy.createHttpUrlConnection(
            host,
            PORT,
            TIMEOUT_IN_MS
        )
        // then
        Truth.assertThat(connection).isNotNull()
        Truth.assertThat(connection.url.host).isEqualTo(parsedDefaultHost)
        Truth.assertThat(connection.url.port)
            .isEqualTo(PORT)
        Truth.assertThat(connection.connectTimeout)
            .isEqualTo(TIMEOUT_IN_MS)
        Truth.assertThat(connection.readTimeout)
            .isEqualTo(TIMEOUT_IN_MS)
        Truth.assertThat(connection.instanceFollowRedirects).isFalse()
        Truth.assertThat(connection.useCaches).isFalse()
    }

    @Test
    @Throws(IOException::class)
    fun shouldHandleAnExceptionWhileCreatingHttpUrlConnection() { // given
        val errorMsg = "Could not establish connection with WalledGardenStrategy"
        val givenException = IOException(errorMsg)
        every {
            strategy.createHttpUrlConnection(
                HOST_WITH_HTTP,
                PORT,
                TIMEOUT_IN_MS
            )
        } throws (givenException)
        // when
        strategy.isConnected(
            HOST_WITH_HTTP,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandler
        )
        // then
        verify { errorHandler.handleError(givenException, errorMsg) }
    }

    @Test
    @Throws(IOException::class)
    fun shouldCreateHttpsUrlConnection() { // given
        val parsedDefaultHost = "clients3.google.com"
        // when
        val connection: HttpURLConnection = strategy.createHttpsUrlConnection(
            "https://clients3.google.com",
            PORT,
            TIMEOUT_IN_MS
        )
        // then
        Truth.assertThat(connection).isNotNull()
        Truth.assertThat(connection.url.host).isEqualTo(parsedDefaultHost)
        Truth.assertThat(connection.url.port)
            .isEqualTo(PORT)
        Truth.assertThat(connection.connectTimeout)
            .isEqualTo(TIMEOUT_IN_MS)
        Truth.assertThat(connection.readTimeout)
            .isEqualTo(TIMEOUT_IN_MS)
        Truth.assertThat(connection.instanceFollowRedirects).isFalse()
        Truth.assertThat(connection.useCaches).isFalse()
    }

    @Test
    @Throws(IOException::class)
    fun shouldHandleAnExceptionWhileCreatingHttpsUrlConnection() { // given
        val errorMsg = "Could not establish connection with WalledGardenStrategy"
        val givenException = IOException(errorMsg)
        val host = "https://clients3.google.com"
        every {
            strategy.createHttpsUrlConnection(
                host,
                PORT,
                TIMEOUT_IN_MS
            )
        } throws (givenException)
        // when
        strategy.isConnected(
            host,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandler
        )
        // then
        verify { errorHandler.handleError(givenException, errorMsg) }
    }

    @Test
    fun shouldNotTransformHttpHost() { // when
        val transformedHost = strategy.adjustHost(HOST_WITH_HTTPS)
        // then
        Truth.assertThat(transformedHost).isEqualTo(HOST_WITH_HTTPS)
    }

    @Test
    fun shouldNotTransformHttpsHost() { // when
        val transformedHost = strategy.adjustHost(HOST_WITH_HTTPS)
        // then
        Truth.assertThat(transformedHost)
            .isEqualTo(HOST_WITH_HTTPS)
    }

    @Test
    fun shouldAddHttpsProtocolToHost() { // when
        val transformedHost = strategy.adjustHost(HOST_WITHOUT_HTTPS)
        // then
        Truth.assertThat(transformedHost).isEqualTo(HOST_WITH_HTTPS)
    }

    @Test
    fun shouldAdjustHostWhileCheckingConnectivity() { // given
        val errorHandlerStub =
            createErrorHandlerStub()
        val host = host
        every {
            strategy.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandlerStub
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
            errorHandlerStub
        ).testIn(scope = testScopeRule)
        // then
        verify { strategy.adjustHost(host) }

    }

    private fun createErrorHandlerStub(): ErrorHandler {
        return object : ErrorHandler {
            override fun handleError(
                exception: Exception?,
                message: String?
            ) {
            }
        }
    }

    companion object {
        private const val INITIAL_INTERVAL_IN_MS = 0
        private const val INTERVAL_IN_MS = 2000
        private const val PORT = 80
        private const val TIMEOUT_IN_MS = 30
        private const val HTTP_RESPONSE = 204
        private const val HOST_WITH_HTTP = "http://www.website.com"
        private const val HOST_WITH_HTTPS = "https://www.website.com"
        private const val HOST_WITHOUT_HTTPS = "www.website.com"
    }
}