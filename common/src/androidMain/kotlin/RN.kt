import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.beryukhov.reactivenetwork.ReactiveNetwork

actual class RN(private val context: Context) {

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    actual fun observeNetworkConnectivity(): Flow<RNConnectivity> {
        return ReactiveNetwork().observeNetworkConnectivity(context).map {
            RNConnectivity(it.available)
        }
    }
}