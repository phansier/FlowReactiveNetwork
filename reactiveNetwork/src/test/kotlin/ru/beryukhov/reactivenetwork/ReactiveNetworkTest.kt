
package ru.beryukhov.reactivenetwork

import android.content.Context
import android.net.NetworkInfo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import ru.beryukhov.reactivenetwork.ReactiveNetwork.Companion.create
import ru.beryukhov.reactivenetwork.internet.observing.InternetObservingSettings.Companion.builder
import ru.beryukhov.reactivenetwork.internet.observing.InternetObservingStrategy
import ru.beryukhov.reactivenetwork.internet.observing.error.DefaultErrorHandler
import ru.beryukhov.reactivenetwork.internet.observing.error.ErrorHandler
import ru.beryukhov.reactivenetwork.internet.observing.strategy.SocketInternetObservingStrategy
import ru.beryukhov.reactivenetwork.network.observing.NetworkObservingStrategy
import ru.beryukhov.reactivenetwork.network.observing.strategy.LollipopNetworkObservingStrategy

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(RobolectricTestRunner::class)
class ReactiveNetworkTest: BaseFlowTest() {
    @Test
    fun testReactiveNetworkObjectShouldNotBeNull() { // given
        // when
        val reactiveNetwork = ReactiveNetwork()
        // then
        assertThat(reactiveNetwork).isNotNull()
    }

    @Test
    fun observeNetworkConnectivityShouldNotBeNull() { // given
        networkConnectivityObservableShouldNotBeNull()
    }

    @Test
    @Config(sdk = [23])
    fun observeNetworkConnectivityShouldNotBeNullForMarshmallow() { // given
        networkConnectivityObservableShouldNotBeNull()
    }

    @Test
    @Config(sdk = [21])
    fun observeNetworkConnectivityShouldNotBeNullForLollipop() {
        networkConnectivityObservableShouldNotBeNull()
    }

    private fun networkConnectivityObservableShouldNotBeNull() { // given
        val context: Context = RuntimeEnvironment.application
        // when
        val observable = ReactiveNetwork().observeNetworkConnectivity(context)
        // then
        assertThat(observable).isNotNull()
    }

    @Test
    fun observeNetworkConnectivityWithStrategyShouldNotBeNull() { // given
        val context: Context = RuntimeEnvironment.application
        val strategy: NetworkObservingStrategy = LollipopNetworkObservingStrategy()
        // when
        val observable = ReactiveNetwork().observeNetworkConnectivity(context, strategy)
        // then
        assertThat(observable).isNotNull()
    }

    @Test
    fun observeInternetConnectivityDefaultShouldNotBeNull() { // given
        // when
        val observable = ReactiveNetwork().observeInternetConnectivity()
        // then
        assertThat(observable).isNotNull()
    }

    @Test
    fun observeNetworkConnectivityShouldBeConnectedOnStartWhenNetworkIsAvailable() {
        runBlocking {
            // given
            val context = RuntimeEnvironment.application
            // when
            val connectivityFlow = ReactiveNetwork().observeNetworkConnectivity(context).map { it.state }
            // then
            connectivityFlow.expectFirst(NetworkInfo.State.CONNECTED)
        }
    }

    @Test
    fun observeInternetConnectivityShouldNotThrowAnExceptionWhenStrategyIsNotNull() { // given
        val strategy: InternetObservingStrategy = SocketInternetObservingStrategy()
        val errorHandler: ErrorHandler =
            DefaultErrorHandler()
        // when
        val observable = ReactiveNetwork().observeInternetConnectivity(
            strategy,
            TEST_VALID_INITIAL_INTERVAL,
            TEST_VALID_INTERVAL,
            TEST_VALID_HOST,
            TEST_VALID_PORT,
            TEST_VALID_TIMEOUT,
            TEST_VALID_HTTP_RESPONSE,
            errorHandler
        )
        // then
        assertThat(observable).isNotNull()
    }

    @Test
    fun shouldObserveInternetConnectivityWithCustomSettings() { // given
        val initialInterval = 1
        val interval = 2
        val host = "www.test.com"
        val port = 90
        val timeout = 3
        val testErrorHandler =
            createTestErrorHandler()
        val strategy = createTestInternetObservingStrategy()
        // when
        val settings = builder()
            .initialInterval(initialInterval)
            .interval(interval)
            .host(host)
            .port(port)
            .timeout(timeout)
            .errorHandler(testErrorHandler)
            .strategy(strategy)
            .build()
        // then
        val observable =
            ReactiveNetwork().observeInternetConnectivity(settings)
        assertThat(observable).isNotNull()
    }

    private fun createTestInternetObservingStrategy(): InternetObservingStrategy {
        return object : InternetObservingStrategy {
            override fun observeInternetConnectivity(
                initialIntervalInMs: Int,
                intervalInMs: Int,
                host: String,
                port: Int,
                timeoutInMs: Int,
                httpResponse: Int,
                errorHandler: ErrorHandler
            ): Flow<Boolean> {
                return flow {}
            }

            override suspend fun checkInternetConnectivity(
                host: String,
                port: Int,
                timeoutInMs: Int,
                httpResponse: Int,
                errorHandler: ErrorHandler
            ): Boolean {
                return true
            }

            override fun getDefaultPingHost(): String {
                return "null"
            }
        }
    }

    private fun createTestErrorHandler(): ErrorHandler {
        return object : ErrorHandler {
            override fun handleError(
                exception: Exception?,
                message: String?
            ) {
            }
        }
    }

    companion object {
        private const val TEST_VALID_HOST = "www.test.com"
        private const val TEST_VALID_PORT = 80
        private const val TEST_VALID_TIMEOUT = 1000
        private const val TEST_VALID_INTERVAL = 1000
        private const val TEST_VALID_INITIAL_INTERVAL = 1000
        private const val TEST_VALID_HTTP_RESPONSE = 204
    }
}