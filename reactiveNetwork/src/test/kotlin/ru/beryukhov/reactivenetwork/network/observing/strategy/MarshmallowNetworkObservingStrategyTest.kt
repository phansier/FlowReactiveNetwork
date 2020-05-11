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
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import ru.beryukhov.reactivenetwork.BaseFlowTest
import ru.beryukhov.reactivenetwork.Connectivity

// we're suppressing PMD warnings because we want static imports in tests
@ObsoleteCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
open class MarshmallowNetworkObservingStrategyTest : BaseFlowTest() {

    private val strategy = spyk(MarshmallowNetworkObservingStrategy())

    private val powerManager = mockk<PowerManager>(relaxed = true)
    private val connectivityManager = mockk<ConnectivityManager>(relaxed = true)
    private val contextMock = mockk<Context>(relaxed = true)
    private val intent = mockk<Intent>(relaxed = true)
    private val network = mockk<Network>(relaxed = true)

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = spyk(RuntimeEnvironment.application.applicationContext)
    }

    @Test
    fun shouldObserveConnectivity() { // given
        val context = RuntimeEnvironment.application.applicationContext
        // when

        val testFlow = strategy.observeNetworkConnectivity(context).map { it.state }
            .testIn(scope = testScopeRule)
        // then
        testFlow expect emission(index = 0, expected = NetworkInfo.State.CONNECTED)
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
        verify(exactly = 1) { strategy.onError(message, exception) }
    }

    @Test
    fun shouldTryToUnregisterCallbackOnDispose() { // given
        // when
        runBlockingTest {
            val testFlow = strategy.observeNetworkConnectivity(context).testIn(scope = this)
            this.cancel()

            // then
            verify { strategy.tryToUnregisterCallback(any()) }
        }
    }

    @Test
    fun shouldTryToUnregisterReceiverOnDispose() { // given
        // when
        runBlockingTest {
            val testFlow = strategy.observeNetworkConnectivity(context).testIn(scope = this)
            this.cancel()

            // then
            verify { strategy.tryToUnregisterReceiver(context) }
        }
    }

    @Test
    fun shouldNotBeInIdleModeWhenDeviceIsNotInIdleAndIsNotIgnoringBatteryOptimizations() { // given
        preparePowerManagerMocks(false, false)
        // when
        val isIdleMode = strategy.isIdleMode(contextMock)
        // then
        Truth.assertThat(isIdleMode).isFalse()
    }

    @Test
    fun shouldBeInIdleModeWhenDeviceIsNotIgnoringBatteryOptimizations() { // given
        preparePowerManagerMocks(true, false)
        // when
        val isIdleMode = strategy.isIdleMode(contextMock)
        // then
        Truth.assertThat(isIdleMode).isTrue()
    }

    @Test
    fun shouldNotBeInIdleModeWhenDeviceIsInIdleModeAndIgnoringBatteryOptimizations() { // given
        preparePowerManagerMocks(true, true)
        // when
        val isIdleMode = strategy.isIdleMode(contextMock)
        // then
        Truth.assertThat(isIdleMode).isFalse()
    }

    @Test
    fun shouldNotBeInIdleModeWhenDeviceIsNotInIdleMode() { // given
        preparePowerManagerMocks(false, true)
        // when
        val isIdleMode = strategy.isIdleMode(contextMock)
        // then
        Truth.assertThat(isIdleMode).isFalse()
    }

    @Test
    fun shouldReceiveIntentInIdleMode() { // given
        preparePowerManagerMocks(true, false)
        val broadcastReceiver = strategy.createIdleBroadcastReceiver()
        // when
        broadcastReceiver.onReceive(contextMock, intent)
        // then
        verify { strategy.onNext(any()) }
    }

    @Test
    fun shouldReceiveIntentWhenIsNotInIdleMode() { // given
        preparePowerManagerMocks(false, false)
        val broadcastReceiver = strategy.createIdleBroadcastReceiver()
        // when
        broadcastReceiver.onReceive(contextMock, intent)
        // then
        verify { strategy.onNext(any()) }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun preparePowerManagerMocks(
        idleMode: Boolean,
        ignoreOptimizations: Boolean
    ) {
        val packageName = "com.github.pwittchen.test"
        every { contextMock.packageName } returns packageName
        every { contextMock.getSystemService(Context.POWER_SERVICE) } returns powerManager
        every { powerManager.isDeviceIdleMode } returns idleMode
        every { powerManager.isIgnoringBatteryOptimizations(packageName) } returns ignoreOptimizations
    }

    @Test
    fun shouldCreateNetworkCallbackOnSubscribe() {
        // when
        val testFlow = strategy.observeNetworkConnectivity(context).testIn(scope = testScopeRule)

        // then
        verify { strategy.createNetworkCallback(context) }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Test
    fun shouldInvokeOnNextOnNetworkAvailable() { // given
        val networkCallback = strategy.createNetworkCallback(context)
        // when
        networkCallback.onAvailable(network)
        // then
        verify { strategy.onNext(any()) }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Test
    fun shouldInvokeOnNextOnNetworkLost() { // given
        val networkCallback = strategy.createNetworkCallback(context)
        // when
        networkCallback.onLost(network)
        // then
        verify { strategy.onNext(any()) }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Test
    fun shouldHandleErrorWhileTryingToUnregisterCallback() { // given
        strategy.observeNetworkConnectivity(context)
        val exception = IllegalArgumentException()
        every { connectivityManager.unregisterNetworkCallback(any<NetworkCallback>()) } throws exception
        // when
        strategy.tryToUnregisterCallback(connectivityManager)
        // then
        verify {
            strategy.onError(
                MarshmallowNetworkObservingStrategy.ERROR_MSG_NETWORK_CALLBACK,
                exception
            )
        }
    }

    @Test
    fun shouldHandleErrorWhileTryingToUnregisterReceiver() { // given
        strategy.observeNetworkConnectivity(context)
        val exception = RuntimeException()
        every { contextMock.unregisterReceiver(any()) } throws exception
        // when
        strategy.tryToUnregisterReceiver(contextMock)
        // then
        verify {
            strategy.onError(
                MarshmallowNetworkObservingStrategy.ERROR_MSG_RECEIVER,
                exception
            )
        }
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
        val last = Connectivity(
            type = lastType,
            state = NetworkInfo.State.CONNECTED
        )
        val current = Connectivity(
            type = currentType,
            state = NetworkInfo.State.DISCONNECTED,
            detailedState = NetworkInfo.DetailedState.CONNECTED
        )
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
        val last = Connectivity(
            type = ConnectivityManager.TYPE_WIFI,
            state = NetworkInfo.State.CONNECTED
        )
        val current = Connectivity(
            type = ConnectivityManager.TYPE_WIFI,
            state = NetworkInfo.State.DISCONNECTED,
            detailedState = NetworkInfo.DetailedState.CONNECTED
        )
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
        val last = Connectivity(
            type = ConnectivityManager.TYPE_WIFI,
            state = NetworkInfo.State.DISCONNECTED
        )
        val current = Connectivity(
            type = ConnectivityManager.TYPE_MOBILE,
            state = NetworkInfo.State.CONNECTED,
            detailedState = NetworkInfo.DetailedState.CONNECTED
        )
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
        val last = Connectivity(
            type = ConnectivityManager.TYPE_WIFI,
            state = NetworkInfo.State.CONNECTED
        )
        val current = Connectivity(
            type = ConnectivityManager.TYPE_MOBILE,
            state = NetworkInfo.State.CONNECTED,
            detailedState = NetworkInfo.DetailedState.CONNECTED
        )
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
        val last = Connectivity(
            type = ConnectivityManager.TYPE_WIFI,
            state = NetworkInfo.State.CONNECTED
        )
        val current = Connectivity(
            type = ConnectivityManager.TYPE_MOBILE,
            state = NetworkInfo.State.DISCONNECTED,
            detailedState = NetworkInfo.DetailedState.IDLE
        )
        // when
        runBlockingTest {
            val testFlow = strategy.propagateAnyConnectedState(last, current).testIn(scope = this)
            // then

            testFlow expect emissionCount(1)
            testFlow expect emissions(current)
        }
    }
}