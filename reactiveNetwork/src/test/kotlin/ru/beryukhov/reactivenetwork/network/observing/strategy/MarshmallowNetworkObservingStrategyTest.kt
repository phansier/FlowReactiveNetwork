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
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkInfo
import android.os.Build
import android.os.PowerManager
import at.florianschuster.test.flow.*
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Spy
import org.mockito.junit.MockitoJUnit
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import ru.beryukhov.reactivenetwork.Connectivity

// we're suppressing PMD warnings because we want static imports in tests
@ObsoleteCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(
    RobolectricTestRunner::class
)
open class MarshmallowNetworkObservingStrategyTest {
    @get:Rule
    var rule = MockitoJUnit.rule()
    @Spy
    private val strategy =
        MarshmallowNetworkObservingStrategy()
    @Mock
    private val powerManager: PowerManager? = null
    @Mock
    private val connectivityManager: ConnectivityManager? = null
    @Mock
    private val contextMock: Context? = null
    @Mock
    private val intent: Intent? = null
    @Mock
    private val network: Network? = null
    @Spy
    private var context: Context? = null

    @Before
    fun setUp() {
        context = RuntimeEnvironment.application.applicationContext
    }

    @Test
    fun shouldObserveConnectivity() { // given
        val context =
            RuntimeEnvironment.application.applicationContext
        // when
        runBlockingTest {
            val testFlow =
                strategy.observeNetworkConnectivity(context).map { it.state() }.testIn(scope = this)
            // then
            testFlow expect emission(index = 0, expected = NetworkInfo.State.CONNECTED)
        }
    }
    //Rx specific test
    /*@Test
    fun shouldStopObservingConnectivity() { // given
        val observable: Observable<Connectivity> = strategy.observeNetworkConnectivity(context!!)
        // when
        val disposable: Disposable = observable.subscribe()
        disposable.dispose()
        // then
        assertThat(disposable.isDisposed()).isTrue()
    }*/

    @Test
    fun shouldCallOnError() { // given
        val message = "error message"
        val exception = Exception()
        // when
        strategy.onError(message, exception)
        // then
        Mockito.verify(strategy, Mockito.times(1))
            .onError(message, exception)
    }

    @Test
    fun shouldTryToUnregisterCallbackOnDispose() { // given
        // when
        runBlockingTest {
            val testFlow =
                strategy.observeNetworkConnectivity(context!!).testIn(scope = this)
            this.cancel()

            // then
            Mockito.verify(strategy).tryToUnregisterCallback(
                ArgumentMatchers.any(
                    ConnectivityManager::class.java
                )
            )
        }
    }

    @Test
    fun shouldTryToUnregisterReceiverOnDispose() { // given
        // when
        runBlockingTest {
            val testFlow =
                strategy.observeNetworkConnectivity(context!!).testIn(scope = this)
            this.cancel()

            // then
            Mockito.verify(strategy)
                .tryToUnregisterReceiver(context!!)
        }
    }

    @Test
    fun shouldNotBeInIdleModeWhenDeviceIsNotInIdleAndIsNotIgnoringBatteryOptimizations() { // given
        preparePowerManagerMocks(java.lang.Boolean.FALSE, java.lang.Boolean.FALSE)
        // when
        val isIdleMode = strategy.isIdleMode(contextMock)
        // then
        Truth.assertThat(isIdleMode).isFalse()
    }

    @Test
    fun shouldBeInIdleModeWhenDeviceIsNotIgnoringBatteryOptimizations() { // given
        preparePowerManagerMocks(java.lang.Boolean.TRUE, java.lang.Boolean.FALSE)
        // when
        val isIdleMode = strategy.isIdleMode(contextMock)
        // then
        Truth.assertThat(isIdleMode).isTrue()
    }

    @Test
    fun shouldNotBeInIdleModeWhenDeviceIsInIdleModeAndIgnoringBatteryOptimizations() { // given
        preparePowerManagerMocks(java.lang.Boolean.TRUE, java.lang.Boolean.TRUE)
        // when
        val isIdleMode = strategy.isIdleMode(contextMock)
        // then
        Truth.assertThat(isIdleMode).isFalse()
    }

    @Test
    fun shouldNotBeInIdleModeWhenDeviceIsNotInIdleMode() { // given
        preparePowerManagerMocks(java.lang.Boolean.FALSE, java.lang.Boolean.TRUE)
        // when
        val isIdleMode = strategy.isIdleMode(contextMock)
        // then
        Truth.assertThat(isIdleMode).isFalse()
    }

    @Test
    fun shouldReceiveIntentInIdleMode() { // given
        preparePowerManagerMocks(java.lang.Boolean.TRUE, java.lang.Boolean.FALSE)
        val broadcastReceiver = strategy.createIdleBroadcastReceiver()
        // when
        broadcastReceiver.onReceive(contextMock, intent)
        // then
        Mockito.verify(strategy).onNext(
            ArgumentMatchers.any(
                Connectivity::class.java
            )
        )
    }

    @Test
    fun shouldReceiveIntentWhenIsNotInIdleMode() { // given
        preparePowerManagerMocks(java.lang.Boolean.FALSE, java.lang.Boolean.FALSE)
        val broadcastReceiver = strategy.createIdleBroadcastReceiver()
        // when
        broadcastReceiver.onReceive(contextMock, intent)
        // then
        Mockito.verify(strategy).onNext(
            ArgumentMatchers.any(
                Connectivity::class.java
            )
        )
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun preparePowerManagerMocks(
        idleMode: Boolean,
        ignoreOptimizations: Boolean
    ) {
        val packageName = "com.github.pwittchen.test"
        Mockito.`when`(contextMock!!.packageName).thenReturn(packageName)
        Mockito.`when`(contextMock.getSystemService(Context.POWER_SERVICE))
            .thenReturn(powerManager)
        Mockito.`when`(powerManager!!.isDeviceIdleMode).thenReturn(idleMode)
        Mockito.`when`(powerManager.isIgnoringBatteryOptimizations(packageName))
            .thenReturn(ignoreOptimizations)
    }

    @Test
    fun shouldCreateNetworkCallbackOnSubscribe() {
        // when
        runBlockingTest {
            val testFlow =
                strategy.observeNetworkConnectivity(context!!).testIn(scope = this)

            // then
            Mockito.verify(strategy).createNetworkCallback(context!!)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Test
    fun shouldInvokeOnNextOnNetworkAvailable() { // given
        val networkCallback = strategy.createNetworkCallback(context!!)
        // when
        networkCallback.onAvailable(network)
        // then
        Mockito.verify(strategy).onNext(
            ArgumentMatchers.any(
                Connectivity::class.java
            )
        )
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Test
    fun shouldInvokeOnNextOnNetworkLost() { // given
        val networkCallback = strategy.createNetworkCallback(context!!)
        // when
        networkCallback.onLost(network)
        // then
        Mockito.verify(strategy).onNext(
            ArgumentMatchers.any(
                Connectivity::class.java
            )
        )
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Test
    fun shouldHandleErrorWhileTryingToUnregisterCallback() { // given
        strategy.observeNetworkConnectivity(context!!)
        val exception = IllegalArgumentException()
        Mockito.doThrow(exception).`when`(connectivityManager)
            ?.unregisterNetworkCallback(ArgumentMatchers.any(NetworkCallback::class.java))
        // when
        strategy.tryToUnregisterCallback(connectivityManager)
        // then
        Mockito.verify(strategy).onError(
            MarshmallowNetworkObservingStrategy.ERROR_MSG_NETWORK_CALLBACK,
            exception
        )
    }

    @Test
    fun shouldHandleErrorWhileTryingToUnregisterReceiver() { // given
        strategy.observeNetworkConnectivity(context!!)
        val exception = RuntimeException()
        Mockito.doThrow(exception).`when`(contextMock)!!.unregisterReceiver(
            ArgumentMatchers.any(
                BroadcastReceiver::class.java
            )
        )
        // when
        strategy.tryToUnregisterReceiver(contextMock!!)
        // then
        Mockito.verify(strategy)
            .onError(MarshmallowNetworkObservingStrategy.ERROR_MSG_RECEIVER, exception)
    }

    @Test
    fun shouldPropagateCurrentAndLastConnectivityWhenSwitchingFromWifiToMobile() {
        val lastType = ConnectivityManager.TYPE_WIFI
        val currentType = ConnectivityManager.TYPE_MOBILE
        assertThatConnectivityIsPropagatedDuringChange(lastType, currentType)
    }

    @Test
    fun shouldPropagateCurrentAndLastConnectivityWhenSwitchingFromMobileToWifi() {
        val lastType = ConnectivityManager.TYPE_MOBILE
        val currentType = ConnectivityManager.TYPE_WIFI
        assertThatConnectivityIsPropagatedDuringChange(lastType, currentType)
    }

    private fun assertThatConnectivityIsPropagatedDuringChange(
        lastType: Int, currentType: Int
    ) { // given
        val last = Connectivity.Builder()
            .type(lastType)
            .state(NetworkInfo.State.CONNECTED)
            .build()
        val current = Connectivity.Builder()
            .type(currentType)
            .state(NetworkInfo.State.DISCONNECTED)
            .detailedState(NetworkInfo.DetailedState.CONNECTED)
            .build()
        // when
        runBlockingTest {
            val testFlow =
                strategy.propagateAnyConnectedState(last, current).testIn(scope = this)
            // then
            testFlow expect emissionCount(2)
            testFlow expect emissions(current, last)
        }
    }

    @Test
    fun shouldNotPropagateLastConnectivityEventWhenTypeIsNotChanged() { // given
        val last = Connectivity.Builder()
            .type(ConnectivityManager.TYPE_WIFI)
            .state(NetworkInfo.State.CONNECTED)
            .build()
        val current = Connectivity.Builder()
            .type(ConnectivityManager.TYPE_WIFI)
            .state(NetworkInfo.State.DISCONNECTED)
            .detailedState(NetworkInfo.DetailedState.CONNECTED)
            .build()
        // when
        runBlockingTest {
            val testFlow =
                strategy.propagateAnyConnectedState(last, current).testIn(scope = this)
            // then
            testFlow expect emissionCount(1)
            testFlow expect emissions(current)

        }
    }

    @Test
    fun shouldNotPropagateLastConnectivityWhenWasNotConnected() { // given
        val last = Connectivity.Builder()
            .type(ConnectivityManager.TYPE_WIFI)
            .state(NetworkInfo.State.DISCONNECTED)
            .build()
        val current = Connectivity.Builder()
            .type(ConnectivityManager.TYPE_MOBILE)
            .state(NetworkInfo.State.CONNECTED)
            .detailedState(NetworkInfo.DetailedState.CONNECTED)
            .build()
        // when
        runBlockingTest {
            val testFlow =
                strategy.propagateAnyConnectedState(last, current).testIn(scope = this)
            // then

            testFlow expect emissionCount(1)
            testFlow expect emissions(current)
        }
    }

    @Test
    fun shouldNotPropagateLastConnectivityWhenIsConnected() { // given
        val last = Connectivity.Builder()
            .type(ConnectivityManager.TYPE_WIFI)
            .state(NetworkInfo.State.CONNECTED)
            .build()
        val current = Connectivity.Builder()
            .type(ConnectivityManager.TYPE_MOBILE)
            .state(NetworkInfo.State.CONNECTED)
            .detailedState(NetworkInfo.DetailedState.CONNECTED)
            .build()
        // when
        runBlockingTest {
            val testFlow =
                strategy.propagateAnyConnectedState(last, current).testIn(scope = this)
            // then

            testFlow expect emissionCount(1)
            testFlow expect emissions(current)
        }
    }

    @Test
    fun shouldNotPropagateLastConnectivityWhenIsIdle() { // given
        val last = Connectivity.Builder()
            .type(ConnectivityManager.TYPE_WIFI)
            .state(NetworkInfo.State.CONNECTED)
            .build()
        val current = Connectivity.Builder()
            .type(ConnectivityManager.TYPE_MOBILE)
            .state(NetworkInfo.State.DISCONNECTED)
            .detailedState(NetworkInfo.DetailedState.IDLE)
            .build()
        // when
        runBlockingTest {
            val testFlow = strategy.propagateAnyConnectedState(last, current).testIn(scope = this)
            // then

            testFlow expect emissionCount(1)
            testFlow expect emissions(current)
        }
    }
}