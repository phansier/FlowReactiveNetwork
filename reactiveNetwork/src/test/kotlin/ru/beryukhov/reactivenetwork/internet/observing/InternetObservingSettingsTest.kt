package ru.beryukhov.reactivenetwork.internet.observing

import com.google.common.truth.Truth
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.beryukhov.reactivenetwork.internet.observing.InternetObservingSettings.Companion.builder
import ru.beryukhov.reactivenetwork.internet.observing.InternetObservingSettings.Companion.create
import ru.beryukhov.reactivenetwork.internet.observing.error.DefaultErrorHandler
import ru.beryukhov.reactivenetwork.internet.observing.error.ErrorHandler
import ru.beryukhov.reactivenetwork.internet.observing.strategy.SocketInternetObservingStrategy
import ru.beryukhov.reactivenetwork.internet.observing.strategy.WalledGardenInternetObservingStrategy

@RunWith(RobolectricTestRunner::class)
class InternetObservingSettingsTest {
    @Test
    fun shouldCreateSettings() { // when
        val settings = create()
        // then
        Truth.assertThat(settings).isNotNull()
    }

    @Test
    fun shouldBuildSettingsWithDefaultValues() { // when
        val settings = create()
        // then
        Truth.assertThat(settings.initialInterval()).isEqualTo(0)
        Truth.assertThat(settings.interval()).isEqualTo(2000)
        Truth.assertThat(settings.host()).isEqualTo("http://clients3.google.com/generate_204")
        Truth.assertThat(settings.port()).isEqualTo(80)
        Truth.assertThat(settings.timeout()).isEqualTo(2000)
        Truth.assertThat(settings.httpResponse()).isEqualTo(204)
        Truth.assertThat(settings.errorHandler())
            .isInstanceOf(DefaultErrorHandler::class.java)
        Truth.assertThat(settings.strategy())
            .isInstanceOf(WalledGardenInternetObservingStrategy::class.java)
    }

    @Test
    fun shouldBuildSettings() { // given
        val initialInterval = 1
        val interval = 2
        val host = "www.test.com"
        val port = 90
        val timeout = 3
        val httpResponse = 200
        val testErrorHandler =
            createTestErrorHandler()
        val strategy = SocketInternetObservingStrategy()
        // when
        val settings = builder()
            .initialInterval(initialInterval)
            .interval(interval)
            .host(host)
            .port(port)
            .timeout(timeout)
            .httpResponse(httpResponse)
            .errorHandler(testErrorHandler)
            .strategy(strategy)
            .build()
        // then
        Truth.assertThat(settings.initialInterval()).isEqualTo(initialInterval)
        Truth.assertThat(settings.interval()).isEqualTo(interval)
        Truth.assertThat(settings.host()).isEqualTo(host)
        Truth.assertThat(settings.port()).isEqualTo(port)
        Truth.assertThat(settings.timeout()).isEqualTo(timeout)
        Truth.assertThat(settings.httpResponse()).isEqualTo(httpResponse)
        Truth.assertThat(settings.errorHandler()).isNotNull()
        Truth.assertThat(settings.errorHandler())
            .isNotInstanceOf(DefaultErrorHandler::class.java)
        Truth.assertThat(settings.strategy())
            .isInstanceOf(SocketInternetObservingStrategy::class.java)
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
}