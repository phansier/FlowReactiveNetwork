package ru.beryukhov.reactivenetwork.network.observing

import android.content.Context
import android.net.NetworkInfo
import androidx.test.core.app.ApplicationProvider
import ru.beryukhov.reactivenetwork.base.emission
import ru.beryukhov.reactivenetwork.base.expect
import ru.beryukhov.reactivenetwork.base.testIn
import kotlinx.coroutines.flow.map
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.beryukhov.reactivenetwork.base.BaseFlowTest
import ru.beryukhov.reactivenetwork.network.observing.strategy.LollipopNetworkObservingStrategy
import ru.beryukhov.reactivenetwork.network.observing.strategy.PreLollipopNetworkObservingStrategy

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
        val context = ApplicationProvider.getApplicationContext<Context>()
        //when

            val testFlow = strategy.observeNetworkConnectivity(context).map { it.state }.testIn(scope = testScopeRule)

            // then
            testFlow expect emission(index = 0, expected = NetworkInfo.State.CONNECTED)

    }
}