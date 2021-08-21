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
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Socket strategy for monitoring connectivity with the Internet.
 * It monitors Internet connectivity via opening socket connection with the remote host.
 */
class SocketInternetObservingStrategy : InternetObservingStrategy {
    override fun getDefaultPingHost(): String {
        return DEFAULT_HOST
    }

    @ExperimentalCoroutinesApi
    override fun observeInternetConnectivity(
        initialIntervalInMs: Int,
        intervalInMs: Int,
        host: String,
        port: Int,
        timeoutInMs: Int,
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
        checkGeneralPreconditions(host, port, timeoutInMs, errorHandler)
        val adjustedHost = adjustHost(host)
        return tickerFlow(
            period = intervalInMs.toLong(),
            initialDelay = initialIntervalInMs.toLong()
        ).map { isConnected(adjustedHost, port, timeoutInMs, errorHandler) }.distinctUntilChanged()

    }

    override suspend fun checkInternetConnectivity(
        host: String,
        port: Int,
        timeoutInMs: Int,
        httpResponse: Int,
        errorHandler: ErrorHandler
    ): Boolean {
        checkGeneralPreconditions(host, port, timeoutInMs, errorHandler)
        return isConnected(host, port, timeoutInMs, errorHandler)
    }

    /**
     * adjusts host to needs of SocketInternetObservingStrategy
     *
     * @return transformed host
     */
    internal fun adjustHost(host: String): String {
        if (host.startsWith(HTTP_PROTOCOL)) {
            return host.replace(
                HTTP_PROTOCOL,
                EMPTY_STRING
            )
        } else if (host.startsWith(HTTPS_PROTOCOL)) {
            return host.replace(
                HTTPS_PROTOCOL,
                EMPTY_STRING
            )
        }
        return host
    }

    private fun checkGeneralPreconditions(
        host: String, port: Int, timeoutInMs: Int,
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
    }

    /**
     * checks if device is connected to given host at given port
     *
     * @param host to connect
     * @param port to connect
     * @param timeoutInMs connection timeout
     * @param errorHandler error handler for socket connection
     * @return boolean true if connected and false if not
     */
    internal fun isConnected(
        host: String?, port: Int, timeoutInMs: Int,
        errorHandler: ErrorHandler
    ): Boolean {
        val socket = Socket()
        return isConnected(socket, host, port, timeoutInMs, errorHandler)
    }

    /**
     * checks if device is connected to given host at given port
     *
     * @param socket to connect
     * @param host to connect
     * @param port to connect
     * @param timeoutInMs connection timeout
     * @param errorHandler error handler for socket connection
     * @return boolean true if connected and false if not
     */
    internal fun isConnected(
        socket: Socket,
        host: String?,
        port: Int,
        timeoutInMs: Int,
        errorHandler: ErrorHandler
    ): Boolean {
        return try {
            socket.connect(InetSocketAddress(host, port), timeoutInMs)
            socket.isConnected
        } catch (e: IOException) {
            java.lang.Boolean.FALSE
        } finally {
            try {
                socket.close()
            } catch (exception: IOException) {
                errorHandler.handleError(exception, "Could not close the socket")
            }
        }
    }

    companion object {
        private const val EMPTY_STRING = ""
        private const val DEFAULT_HOST = "www.google.com"
        private const val HTTP_PROTOCOL = "http://"
        private const val HTTPS_PROTOCOL = "https://"
    }
}