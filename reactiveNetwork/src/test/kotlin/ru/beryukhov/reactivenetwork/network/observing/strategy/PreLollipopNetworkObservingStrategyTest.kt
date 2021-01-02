package ru.beryukhov.reactivenetwork.network.observing.strategy

import android.content.BroadcastReceiver
import android.content.Context
import android.net.NetworkInfo
import androidx.test.core.app.ApplicationProvider
import at.florianschuster.test.flow.emission
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.beryukhov.reactivenetwork.network.observing.NetworkObservingStrategy

// We are suppressing PMD here because we want static imports in unit tests
@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
open class PreLollipopNetworkObservingStrategyTest {

    @Ignore
    @Test
    fun shouldObserveConnectivity() { // given
        val strategy: NetworkObservingStrategy = PreLollipopNetworkObservingStrategy()
        val context = ApplicationProvider.getApplicationContext<Context>()
        // when
        runBlockingTest {
            val testFlow =
                strategy.observeNetworkConnectivity(context).map { it.state }.testIn(scope = this)
            advanceTimeBy(1000)
            // then
            testFlow expect emission(index = 0, expected = NetworkInfo.State.CONNECTED)

        }
    }

    //Rx specific test
    /*@Test
    fun shouldStopObservingConnectivity() { // given
        val strategy: NetworkObservingStrategy = PreLollipopNetworkObservingStrategy()
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
        val strategy = spyk(PreLollipopNetworkObservingStrategy())
        // when
        strategy.onError(message, exception)
        // then
        verify(exactly = 1) { strategy.onError(message, exception) }
    }

    @Test
    fun shouldTryToUnregisterReceiver() { // given
        val strategy = PreLollipopNetworkObservingStrategy()
        val context = spyk(ApplicationProvider.getApplicationContext())
        val broadcastReceiver = mockk<BroadcastReceiver>(relaxed = true)
        // when
        strategy.tryToUnregisterReceiver(context, broadcastReceiver)
        // then
        verify { context.unregisterReceiver(broadcastReceiver) }
    }

    @Ignore
    @Test
    fun shouldTryToUnregisterReceiverAfterDispose() { // given
        val context = ApplicationProvider.getApplicationContext<Context>()
        val strategy = spyk(PreLollipopNetworkObservingStrategy())
        // when
        runBlockingTest {

            val testFlow = strategy.observeNetworkConnectivity(context).testIn(scope = this)
            this.cancel()

            // then
            verify { strategy.tryToUnregisterReceiver(context, any()) }

        }
    }
}