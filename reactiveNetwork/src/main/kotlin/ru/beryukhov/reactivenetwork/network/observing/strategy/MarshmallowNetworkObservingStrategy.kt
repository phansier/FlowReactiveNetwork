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
package ru.beryukhov.reactivenetwork.network.observing.strategy

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import ru.beryukhov.reactivenetwork.Connectivity
import ru.beryukhov.reactivenetwork.ReactiveNetwork
import ru.beryukhov.reactivenetwork.network.observing.NetworkObservingStrategy

/**
 * Network observing strategy for devices with Android Marshmallow (API 23) or higher.
 * Uses Network Callback API and handles Doze mode.
 */
@FlowPreview
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@TargetApi(23)
class MarshmallowNetworkObservingStrategy : NetworkObservingStrategy {
    // it has to be initialized in the Observable due to Context
    private var networkCallback: NetworkCallback? = null
    private val connectivitySubject: /*Subject<Connectivity?>?*/ BroadcastChannel<Connectivity>
    private val idleReceiver: BroadcastReceiver?
    private var lastConnectivity = Connectivity.create()
    override fun observeNetworkConnectivity(context: Context): Flow<Connectivity> {
        val service = Context.CONNECTIVITY_SERVICE
        val manager = context.getSystemService(service) as ConnectivityManager
        networkCallback = createNetworkCallback(context)
        registerIdleReceiver(context)
        val request =
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                .build()
        manager.registerNetworkCallback(request, networkCallback)
        return callbackFlow {
            connectivitySubject.consumeEach {
                offer(it)
                print("offer")
                lastConnectivity = it
            }
            awaitClose {
                tryToUnregisterCallback(manager)
                tryToUnregisterReceiver(context)
            }
        }.flatMapConcat { connectivity ->
            propagateAnyConnectedState(lastConnectivity, connectivity)
        }.onStart { emit(Connectivity.create(context)) }.distinctUntilChanged()

            /*.toFlowable(BackpressureStrategy.LATEST)
            .doOnCancel(object : Action() {
                fun run() {
                    tryToUnregisterCallback(manager)
                    tryToUnregisterReceiver(context)
                }
            })
            .doAfterNext(object : Consumer<Connectivity?>() {
                fun accept(connectivity: Connectivity?) {
                    lastConnectivity = connectivity
                }
            })*/
            /*.flatMap(object : Function<Connectivity?, Publisher<Connectivity?>?>() {
                fun apply(connectivity: Connectivity?): Publisher<Connectivity?>? {
                    val typeChanged = lastConnectivity!!.type() != connectivity!!.type()
                    val wasConnected = lastConnectivity.state() == NetworkInfo.State.CONNECTED
                    val isDisconnected = connectivity.state() == NetworkInfo.State.DISCONNECTED
                    val isNotIdle = connectivity.detailedState() != NetworkInfo.DetailedState.IDLE
                    return if (typeChanged && wasConnected && isDisconnected && isNotIdle) {
                        Flowable.fromArray(connectivity, lastConnectivity)
                    } else {
                        Flowable.fromArray(connectivity)
                    }
                }
            })*/

    }

    internal fun propagateAnyConnectedState(last: Connectivity, current: Connectivity): Flow<Connectivity> {
        val typeChanged = last.type() != current.type()
        val wasConnected = last.state() == NetworkInfo.State.CONNECTED
        val isDisconnected = current.state() == NetworkInfo.State.DISCONNECTED
        val isNotIdle = current.detailedState() != NetworkInfo.DetailedState.IDLE
        return if (typeChanged && wasConnected && isDisconnected && isNotIdle) {
            flowOf(current, last)
        } else {
            flowOf(current)
        }
    }

    protected fun registerIdleReceiver(context: Context?) {
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
                    onNext(Connectivity.create())
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
            manager?.unregisterNetworkCallback(networkCallback)
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
        connectivitySubject.offer(connectivity)
    }

    companion object {
        internal val ERROR_MSG_NETWORK_CALLBACK: String = "could not unregister network callback"
        internal val ERROR_MSG_RECEIVER: String = "could not unregister receiver"
    }

    init {
        idleReceiver = createIdleBroadcastReceiver()
        connectivitySubject = BroadcastChannel(Channel.CONFLATED)
    }
}