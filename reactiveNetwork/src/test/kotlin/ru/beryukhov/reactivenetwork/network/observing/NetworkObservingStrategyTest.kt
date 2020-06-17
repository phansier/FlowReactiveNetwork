package ru.beryukhov.reactivenetwork.network.observing

import android.net.NetworkInfo
import at.florianschuster.test.flow.emission
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.map
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import ru.beryukhov.reactivenetwork.BaseFlowTest
import ru.beryukhov.reactivenetwork.network.observing.strategy.LollipopNetworkObservingStrategy
import ru.beryukhov.reactivenetwork.network.observing.strategy.PreLollipopNetworkObservingStrategy

@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class NetworkObservingStrategyTest: BaseFlowTest() {
    @Test
    fun lollipopObserveNetworkConnectivityShouldBeConnectedWhenNetworkIsAvailable() { // given
        val strategy: NetworkObservingStrategy = LollipopNetworkObservingStrategy()
        // when
        assertThatIsConnected(strategy)
    }

    @Ignore
    @Test
    fun preLollipopObserveNetworkConnectivityShouldBeConnectedWhenNetworkIsAvailable() { // given
        val strategy: NetworkObservingStrategy = PreLollipopNetworkObservingStrategy()
        // when
        assertThatIsConnected(strategy)
    }

    private fun assertThatIsConnected(strategy: NetworkObservingStrategy) { // given
        val context = RuntimeEnvironment.application.applicationContext
        //when

            val testFlow = strategy.observeNetworkConnectivity(context).map { it.state }.testIn(scope = testScopeRule)

            // then
            testFlow expect emission(index = 0, expected = NetworkInfo.State.CONNECTED)

    }
}