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
package ru.beryukhov.reactivenetwork.network.observing

import android.net.NetworkInfo
import at.florianschuster.test.flow.emission
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import ru.beryukhov.reactivenetwork.network.observing.strategy.LollipopNetworkObservingStrategy
import ru.beryukhov.reactivenetwork.network.observing.strategy.PreLollipopNetworkObservingStrategy

@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class NetworkObservingStrategyTest {
    @Test
    fun lollipopObserveNetworkConnectivityShouldBeConnectedWhenNetworkIsAvailable() { // given
        val strategy: NetworkObservingStrategy = LollipopNetworkObservingStrategy()
        // when
        assertThatIsConnected(strategy)
    }

    @Test
    fun preLollipopObserveNetworkConnectivityShouldBeConnectedWhenNetworkIsAvailable() { // given
        val strategy: NetworkObservingStrategy = PreLollipopNetworkObservingStrategy()
        // when
        assertThatIsConnected(strategy)
    }

    private fun assertThatIsConnected(strategy: NetworkObservingStrategy) { // given
        val context = RuntimeEnvironment.application.applicationContext
        //when
        runBlockingTest {
            val testFlow = strategy.observeNetworkConnectivity(context).map { it.state }.testIn(scope = this)

            // then
            testFlow expect emission(index = 0, expected = NetworkInfo.State.CONNECTED)


        }
    }
}