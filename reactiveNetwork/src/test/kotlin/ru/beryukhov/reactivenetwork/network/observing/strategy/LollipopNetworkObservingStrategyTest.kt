package ru.beryukhov.reactivenetwork.network.observing.strategy

import android.content.Context
import android.net.NetworkInfo
import androidx.test.core.app.ApplicationProvider
import ru.beryukhov.reactivenetwork.base.emission
import ru.beryukhov.reactivenetwork.base.expect
import ru.beryukhov.reactivenetwork.base.testIn
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.map
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.beryukhov.reactivenetwork.base.BaseFlowTest
import ru.beryukhov.reactivenetwork.network.observing.NetworkObservingStrategy

@RunWith(RobolectricTestRunner::class)
class LollipopNetworkObservingStrategyTest : BaseFlowTest() {

    @Test
    fun shouldObserveConnectivity() { // given
        val strategy: NetworkObservingStrategy = LollipopNetworkObservingStrategy()
        val context = ApplicationProvider.getApplicationContext<Context>()
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
        val context = ApplicationProvider.getApplicationContext<Context>()
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