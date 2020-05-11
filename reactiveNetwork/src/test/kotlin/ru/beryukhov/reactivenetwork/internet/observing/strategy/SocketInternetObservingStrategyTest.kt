/*
 * Copyright (C) 2016 Piotr Wittchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.beryukhov.reactivenetwork.internet.observing.strategy

import at.florianschuster.test.flow.emission
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Spy
import org.mockito.junit.MockitoJUnit
import org.robolectric.RobolectricTestRunner
import ru.beryukhov.reactivenetwork.internet.observing.error.ErrorHandler
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
open class SocketInternetObservingStrategyTest {
    @get:Rule
    var rule = MockitoJUnit.rule()
    @Spy
    private val strategy: SocketInternetObservingStrategy? = null
    @Mock
    private val errorHandler: ErrorHandler? =
        null
    @Mock
    private val socket: Socket? = null

    private val host: String
         get() = strategy!!.getDefaultPingHost()

    @Test
    fun shouldBeConnectedToTheInternet() { // given
        Mockito.`when`(
            strategy!!.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                errorHandler!!
            )
        ).thenReturn(true)
        // when
        runBlockingTest {
            val testFlow = strategy.observeInternetConnectivity(
                INITIAL_INTERVAL_IN_MS,
                INTERVAL_IN_MS,
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandler
            ).testIn(scope = this)

            // then
            testFlow expect emission(index = 0, expected = true)
        }
    }

    @Test
    fun shouldNotBeConnectedToTheInternet() { // given
        Mockito.`when`(
            strategy!!.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                errorHandler!!
            )
        ).thenReturn(false)
        // when
        runBlockingTest {
            val testFlow = strategy.observeInternetConnectivity(
                INITIAL_INTERVAL_IN_MS,
                INTERVAL_IN_MS,
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandler
            ).testIn(scope = this)

            // then
            testFlow expect emission(index = 0, expected = false)
        }
    }

    @Test
    @Throws(IOException::class)
    fun shouldNotBeConnectedToTheInternetWhenSocketThrowsAnExceptionOnConnect() { // given
        val address = InetSocketAddress(
            host,
            PORT
        )
        Mockito.doThrow(IOException()).`when`(socket)
            ?.connect(address, TIMEOUT_IN_MS)
        // when
        val isConnected = strategy!!.isConnected(
            socket!!,
            host,
            PORT,
            TIMEOUT_IN_MS,
            errorHandler!!
        )
        // then
        Truth.assertThat(isConnected).isFalse()
    }

    @Test
    @Throws(IOException::class)
    fun shouldHandleAnExceptionThrownDuringClosingTheSocket() { // given
        val errorMsg = "Could not close the socket"
        val givenException = IOException(errorMsg)
        Mockito.doThrow(givenException).`when`(socket)!!.close()
        // when
        strategy!!.isConnected(
            socket!!,
            host,
            PORT,
            TIMEOUT_IN_MS,
            errorHandler!!
        )
        // then
        Mockito.verify(
            errorHandler,
            Mockito.times(1)
        )!!.handleError(givenException, errorMsg)
    }

    //Single methods are commented out
    /*@Test
    fun shouldBeConnectedToTheInternetViaSingle() { // given
        Mockito.`when`(
            strategy!!.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                errorHandler!!
            )
        ).thenReturn(true)
        // when
        val observable: Single<Boolean> = strategy.checkInternetConnectivity(
            host,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandler
        )
        val isConnected: Boolean = observable.blockingGet()
        // then
        Truth.assertThat(isConnected).isTrue()
    }

    @Test
    fun shouldNotBeConnectedToTheInternetViaSingle() { // given
        Mockito.`when`(
            strategy!!.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                errorHandler!!
            )
        ).thenReturn(false)
        // when
        val observable: Single<Boolean> = strategy.checkInternetConnectivity(
            host,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandler
        )
        val isConnected: Boolean = observable.blockingGet()
        // then
        Truth.assertThat(isConnected).isFalse()
    }*/

    @Test
    fun shouldNotTransformHost() { // when
        val transformedHost =
            strategy!!.adjustHost(HOST_WITHOUT_HTTP)
        // then
        Truth.assertThat(transformedHost)
            .isEqualTo(HOST_WITHOUT_HTTP)
    }

    @Test
    fun shouldRemoveHttpProtocolFromHost() { // when
        val transformedHost =
            strategy!!.adjustHost(HOST_WITH_HTTP)
        // then
        Truth.assertThat(transformedHost)
            .isEqualTo(HOST_WITHOUT_HTTP)
    }

    @Test
    fun shouldRemoveHttpsProtocolFromHost() { // when
        val transformedHost =
            strategy!!.adjustHost(HOST_WITH_HTTP)
        // then
        Truth.assertThat(transformedHost)
            .isEqualTo(HOST_WITHOUT_HTTP)
    }

    @Test
    fun shouldAdjustHostDuringCheckingConnectivity() { // given
        val host = host
        Mockito.`when`(
            strategy!!.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                errorHandler!!
            )
        ).thenReturn(true)
        // when
        runBlockingTest {
            strategy.observeInternetConnectivity(
                INITIAL_INTERVAL_IN_MS,
                INTERVAL_IN_MS,
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandler
            ).testIn(scope = this)
            // then
            Mockito.verify(strategy)!!.adjustHost(host)
        }
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