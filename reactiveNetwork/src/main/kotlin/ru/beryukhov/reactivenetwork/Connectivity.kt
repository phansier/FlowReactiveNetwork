package ru.beryukhov.reactivenetwork

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.NetworkInfo.DetailedState

/**
 * Connectivity class represents current connectivity status. It wraps NetworkInfo object.
 */
data class Connectivity(
    val state : NetworkInfo.State = NetworkInfo.State.DISCONNECTED,
    val detailedState : DetailedState? = DetailedState.IDLE,
    val type : Int = UNKNOWN_TYPE,
    val subType : Int = UNKNOWN_SUB_TYPE,
    val available : Boolean = false,
    val failover : Boolean = false,
    val roaming : Boolean = false,
    val typeName : String? = "NONE",
    val subTypeName : String? = "NONE",
    val reason : String? = "",
    val extraInfo : String? = ""
){
    companion object {
        const val UNKNOWN_TYPE = -1
        const val UNKNOWN_SUB_TYPE = -1

        fun create(context: Context): Connectivity {
            Preconditions.checkNotNull(context, "context == null")
            return create(
                context,
                getConnectivityManager(context)
            )
        }

        private fun getConnectivityManager(context: Context): ConnectivityManager {
            val service = Context.CONNECTIVITY_SERVICE
            return context.getSystemService(service) as ConnectivityManager
        }

        internal fun create(
            context: Context,
            manager: ConnectivityManager?
        ): Connectivity {
            Preconditions.checkNotNull(context, "context == null")
            if (manager == null) {
                return Connectivity()
            }
            val networkInfo = manager.activeNetworkInfo
            return networkInfo?.let { create(it) } ?: Connectivity()
        }

        private fun create(networkInfo: NetworkInfo): Connectivity {
            return Connectivity(
                state=networkInfo.state,
                detailedState=networkInfo.detailedState,
                type=networkInfo.type,
                subType=networkInfo.subtype,
                available=networkInfo.isAvailable,
                failover=networkInfo.isFailover,
                roaming=networkInfo.isRoaming,
                typeName=networkInfo.typeName,
                subTypeName=networkInfo.subtypeName,
                reason=networkInfo.reason,
                extraInfo=networkInfo.extraInfo
            )
        }
    }
}