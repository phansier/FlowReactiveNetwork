package ru.beryukhov.reactivenetwork.network.observing

import android.content.Context
import kotlinx.coroutines.flow.Flow
import ru.beryukhov.reactivenetwork.Connectivity

/**
 * Network observing strategy allows to implement different strategies for monitoring network
 * connectivity change. Network monitoring API may differ depending of specific Android version.
 */
interface NetworkObservingStrategy {
    /**
     * Observes network connectivity
     *
     * @param context of the Activity or an Application
     * @return Observable representing stream of the network connectivity
     */
    fun observeNetworkConnectivity(context: Context): Flow<Connectivity>

    /**
     * Handles errors, which occurred during observing network connectivity
     *
     * @param message to be processed
     * @param exception which was thrown
     */
    fun onError(message: String, exception: Exception)
}