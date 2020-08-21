package ru.beryukhov.reactivenetwork.network.observing.strategy

import android.annotation.TargetApi
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import ru.beryukhov.reactivenetwork.Connectivity
import ru.beryukhov.reactivenetwork.ReactiveNetwork
import ru.beryukhov.reactivenetwork.network.observing.NetworkObservingStrategy

/**
 * Network observing strategy for devices with Android Lollipop (API 21) or higher.
 * Uses Network Callback API.
 */
@TargetApi(21)
class LollipopNetworkObservingStrategy : NetworkObservingStrategy {
    // it has to be initialized in the Observable due to Context
    private lateinit var networkCallback: NetworkCallback

    @ExperimentalCoroutinesApi
    override fun observeNetworkConnectivity(context: Context): Flow<Connectivity> {
        val service = Context.CONNECTIVITY_SERVICE
        val manager = context.getSystemService(service) as ConnectivityManager
        return callbackFlow<Connectivity> {
            networkCallback = object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    offer(Connectivity.create(context))
                }

                override fun onLost(network: Network) {
                    offer(Connectivity.create(context))
                }
            }

            val networkRequest = NetworkRequest.Builder().build()
            manager.registerNetworkCallback(networkRequest, networkCallback)

            awaitClose { tryToUnregisterCallback(manager) }
        }.onStart { emit(Connectivity.create(context)) }.distinctUntilChanged()
    }

    private fun tryToUnregisterCallback(manager: ConnectivityManager) {
        try {
            manager.unregisterNetworkCallback(networkCallback)
        } catch (exception: Exception) {
            onError("could not unregister network callback", exception)
        }
    }

    override fun onError(
        message: String,
        exception: Exception
    ) {
        Log.e(ReactiveNetwork.LOG_TAG, message, exception)
    }

}