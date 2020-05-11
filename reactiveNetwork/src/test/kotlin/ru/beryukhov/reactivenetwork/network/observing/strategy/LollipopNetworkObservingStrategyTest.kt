package ru.beryukhov.reactivenetwork.network.observing.strategy

import android.net.NetworkInfo
import at.florianschuster.test.flow.emission
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import ru.beryukhov.reactivenetwork.BaseFlowTest
import ru.beryukhov.reactivenetwork.network.observing.NetworkObservingStrategy

@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class LollipopNetworkObservingStrategyTest : BaseFlowTest() {

    @Test
    fun shouldObserveConnectivity() { // given
        val strategy: NetworkObservingStrategy = LollipopNetworkObservingStrategy()
        val context = RuntimeEnvironment.application.applicationContext
        // when

        val testFlow = strategy.observeNetworkConnectivity(context).map { it.state }
            .testIn(scope = testScopeRule)
        // then
        testFlow expect emission(index = 0, expected = NetworkInfo.State.CONNECTED)

    }

    //Rx specific
    /*@Test
    fun shouldStopObservingConnectivity() { // given
        val strategy: NetworkObservingStrategy = LollipopNetworkObservingStrategy()
        val context = RuntimeEnvironment.application
        val observable: Observable<Connectivity> = strategy.observeNetworkConnectivity(context)
        val observer: TestObserver<Connectivity> = TestObserver()
        // when
        observable.subscribe(observer)
        observer.dispose()
        // then
        assertThat(observer.isDisposed()).isTrue()
    }*/

    @Test
    fun shouldCallOnError() { // given
        val message = "error message"
        val exception = Exception()
        val strategy = spyk(LollipopNetworkObservingStrategy())
        // when
        strategy.onError(message, exception)
        // then
        verify(exactly = 1) { strategy.onError(message, exception) }
    }
}