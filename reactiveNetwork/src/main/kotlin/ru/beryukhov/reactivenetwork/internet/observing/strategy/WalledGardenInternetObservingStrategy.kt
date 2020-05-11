package ru.beryukhov.reactivenetwork.internet.observing.strategy

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import ru.beryukhov.reactivenetwork.Preconditions
import ru.beryukhov.reactivenetwork.internet.observing.InternetObservingStrategy
import ru.beryukhov.reactivenetwork.internet.observing.error.ErrorHandler
import ru.beryukhov.reactivenetwork.tickerFlow
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Walled Garden Strategy for monitoring connectivity with the Internet.
 * This strategy handle use case of the countries behind Great Firewall (e.g. China),
 * which does not has access to several websites like Google. It such case, different HTTP responses
 * are generated. Instead HTTP 200 (OK), we got HTTP 204 (NO CONTENT), but it still can tell us
 * if a device is connected to the Internet or not.
 */
class WalledGardenInternetObservingStrategy : InternetObservingStrategy {
    override fun getDefaultPingHost(): String {
        return DEFAULT_HOST
    }

    @ExperimentalCoroutinesApi
    override fun observeInternetConnectivity(
        initialIntervalInMs: Int,
        intervalInMs: Int, host: String, port: Int, timeoutInMs: Int,
        httpResponse: Int,
        errorHandler: ErrorHandler
    ): Flow<Boolean> {
        Preconditions.checkGreaterOrEqualToZero(
            initialIntervalInMs,
            "initialIntervalInMs is not a positive number"
        )
        Preconditions.checkGreaterThanZero(
            intervalInMs,
            "intervalInMs is not a positive number"
        )
        checkGeneralPreconditions(host, port, timeoutInMs, httpResponse, errorHandler)
        val adjustedHost = adjustHost(host)
        return tickerFlow(period = intervalInMs.toLong(), initialDelay = initialIntervalInMs.toLong()).map{isConnected(
            adjustedHost,
            port,
            timeoutInMs,
            httpResponse,
            errorHandler
        )}.distinctUntilChanged()
    }

    override suspend fun checkInternetConnectivity(
        host: String,
        port: Int,
        timeoutInMs: Int,
        httpResponse: Int,
        errorHandler: ErrorHandler
    ): Boolean {
        checkGeneralPreconditions(host, port, timeoutInMs, httpResponse, errorHandler)
        return isConnected(host, port, timeoutInMs, httpResponse, errorHandler)
    }

    internal fun adjustHost(host: String): String {
        return if (!host.startsWith(HTTP_PROTOCOL) && !host.startsWith(
                HTTPS_PROTOCOL
            )
        ) {
            HTTPS_PROTOCOL + host
        } else host
    }

    private fun checkGeneralPreconditions(
        host: String,
        port: Int,
        timeoutInMs: Int,
        httpResponse: Int,
        errorHandler: ErrorHandler
    ) {
        Preconditions.checkNotNullOrEmpty(
            host,
            "host is null or empty"
        )
        Preconditions.checkGreaterThanZero(
            port,
            "port is not a positive number"
        )
        Preconditions.checkGreaterThanZero(
            timeoutInMs,
            "timeoutInMs is not a positive number"
        )
        Preconditions.checkNotNull(
            errorHandler,
            "errorHandler is null"
        )
        Preconditions.checkNotNull(
            httpResponse,
            "httpResponse is null"
        )
        Preconditions.checkGreaterThanZero(
            httpResponse,
            "httpResponse is not a positive number"
        )
    }

    internal fun isConnected(
        host: String,
        port: Int,
        timeoutInMs: Int,
        httpResponse: Int,
        errorHandler: ErrorHandler
    ): Boolean {
        var urlConnection: HttpURLConnection? = null
        return try {
            urlConnection = if (host.startsWith(HTTPS_PROTOCOL)) {
                createHttpsUrlConnection(host, port, timeoutInMs)
            } else {
                createHttpUrlConnection(host, port, timeoutInMs)
            }
            urlConnection.responseCode == httpResponse
        } catch (e: IOException) {
            errorHandler.handleError(e, "Could not establish connection with WalledGardenStrategy")
            java.lang.Boolean.FALSE
        } finally {
            urlConnection?.disconnect()
        }
    }

    @Throws(IOException::class)
    internal fun createHttpUrlConnection(
        host: String?,
        port: Int,
        timeoutInMs: Int
    ): HttpURLConnection {
        val initialUrl = URL(host)
        val url = URL(initialUrl.protocol, initialUrl.host, port, initialUrl.file)
        val urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.connectTimeout = timeoutInMs
        urlConnection.readTimeout = timeoutInMs
        urlConnection.instanceFollowRedirects = false
        urlConnection.useCaches = false
        return urlConnection
    }

    @Throws(IOException::class)
    internal fun createHttpsUrlConnection(
        host: String?,
        port: Int,
        timeoutInMs: Int
    ): HttpsURLConnection {
        val initialUrl = URL(host)
        val url =
            URL(initialUrl.protocol, initialUrl.host, port, initialUrl.file)
        val urlConnection =
            url.openConnection() as HttpsURLConnection
        urlConnection.connectTimeout = timeoutInMs
        urlConnection.readTimeout = timeoutInMs
        urlConnection.instanceFollowRedirects = false
        urlConnection.useCaches = false
        return urlConnection
    }

    companion object {
        private const val DEFAULT_HOST = "http://clients3.google.com/generate_204"
        private const val HTTP_PROTOCOL = "http://"
        private const val HTTPS_PROTOCOL = "https://"
    }
}