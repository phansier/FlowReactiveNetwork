import kotlinx.coroutines.flow.Flow

expect class RN {
    fun observeNetworkConnectivity(): Flow<RNConnectivity>
}

data class RNConnectivity(
    val available: Boolean
)