package ru.beryukhov.reactivenetwork.network.observing.strategy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.beryukhov.reactivenetwork.Connectivity
import ru.beryukhov.reactivenetwork.ReactiveNetwork
import ru.beryukhov.reactivenetwork.network.observing.NetworkObservingStrategy

/**
 * Network observing strategy for Android devices before Lollipop (API 20 or lower).
 * Uses Broadcast Receiver.
 */
class PreLollipopNetworkObservingStrategy : NetworkObservingStrategy {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeNetworkConnectivity(context: Context): Flow<Connectivity> {
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        return callbackFlow {
            val receiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context,
                    intent: Intent
                ) {
                    trySend(Connectivity.create(context))
                }
            }
            context.registerReceiver(receiver, filter)
            awaitClose {
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        tryToUnregisterReceiver(context, receiver)
                    }
                }
            }
        }
    }

    internal fun tryToUnregisterReceiver(
        context: Context,
        receiver: BroadcastReceiver?
    ) {
        try {
            context.unregisterReceiver(receiver)
        } catch (exception: Exception) {
            onError("receiver was already unregistered", exception)
        }
    }

    override fun onError(
        message: String,
        exception: Exception
    ) {
        Log.e(ReactiveNetwork.LOG_TAG, message, exception)
    }
}