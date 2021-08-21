package ru.beryukhov.reactivenetwork.network.observing.strategy

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import ru.beryukhov.reactivenetwork.Connectivity
import ru.beryukhov.reactivenetwork.ReactiveNetwork
import ru.beryukhov.reactivenetwork.network.observing.NetworkObservingStrategy

/**
 * Network observing strategy for devices with Android Marshmallow (API 23) or higher.
 * Uses Network Callback API and handles Doze mode.
 */
@TargetApi(23)
class MarshmallowNetworkObservingStrategy : NetworkObservingStrategy {
    // it has to be initialized in the Observable due to Context
    private var networkCallback: NetworkCallback? = null
    private var connectivitySubject: MutableStateFlow<Connectivity>? = null
    private val idleReceiver: BroadcastReceiver = createIdleBroadcastReceiver()
    private var lastConnectivity = Connectivity()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun observeNetworkConnectivity(context: Context): Flow<Connectivity> {
        val service = Context.CONNECTIVITY_SERVICE
        val manager = context.getSystemService(service) as ConnectivityManager
        networkCallback = createNetworkCallback(context)
        registerIdleReceiver(context)
        val request =
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                .build()
        manager.registerNetworkCallback(request, networkCallback!!)
        connectivitySubject = MutableStateFlow(Connectivity.create(context))
        return connectivitySubject!!.flatMapConcat { connectivity ->
            propagateAnyConnectedState(lastConnectivity, connectivity)
        }.onCompletion {
            tryToUnregisterCallback(manager)
            tryToUnregisterReceiver(context)
        }
    }

    internal fun propagateAnyConnectedState(
        last: Connectivity,
        current: Connectivity
    ): Flow<Connectivity> {
        val typeChanged = last.type != current.type
        val wasConnected = last.state == NetworkInfo.State.CONNECTED
        val isDisconnected = current.state == NetworkInfo.State.DISCONNECTED
        val isNotIdle = current.detailedState != NetworkInfo.DetailedState.IDLE
        return if (typeChanged && wasConnected && isDisconnected && isNotIdle) {
            flowOf(current, last)
        } else {
            flowOf(current)
        }
    }

    private fun registerIdleReceiver(context: Context?) {
        val filter = IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED)
        context!!.registerReceiver(idleReceiver, filter)
    }

    internal fun createIdleBroadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {
                if (isIdleMode(context)) {
                    onNext(Connectivity())
                } else {
                    onNext(Connectivity.create(context!!))
                }
            }
        }
    }

    internal fun isIdleMode(context: Context?): Boolean {
        val packageName = context?.packageName
        val manager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringOptimizations = manager.isIgnoringBatteryOptimizations(packageName)
        return manager.isDeviceIdleMode && !isIgnoringOptimizations
    }

    internal fun tryToUnregisterCallback(manager: ConnectivityManager?) {
        try {
            networkCallback?.let { manager?.unregisterNetworkCallback(it) }
        } catch (exception: Exception) {
            onError(
                ERROR_MSG_NETWORK_CALLBACK,
                exception
            )
        }
    }

    internal fun tryToUnregisterReceiver(context: Context) {
        try {
            context.unregisterReceiver(idleReceiver)
        } catch (exception: Exception) {
            onError(ERROR_MSG_RECEIVER, exception)
        }
    }

    override fun onError(
        message: String,
        exception: Exception
    ) {
        Log.e(ReactiveNetwork.LOG_TAG, message, exception)
    }

    internal fun createNetworkCallback(context: Context): NetworkCallback {
        return object : NetworkCallback() {
            override fun onAvailable(network: Network?) {
                onNext(Connectivity.create(context))
            }

            override fun onLost(network: Network?) {
                onNext(Connectivity.create(context))
            }
        }
    }

    internal fun onNext(connectivity: Connectivity) {
        connectivitySubject?.tryEmit(connectivity)
    }

    companion object {
        internal const val ERROR_MSG_NETWORK_CALLBACK: String =
            "could not unregister network callback"
        internal const val ERROR_MSG_RECEIVER: String = "could not unregister receiver"
    }
}