package ru.beryukhov.reactivenetwork.internet.observing.error

import android.util.Log
import ru.beryukhov.reactivenetwork.ReactiveNetwork

class DefaultErrorHandler :
    ErrorHandler {
    override fun handleError(
        exception: Exception?,
        message: String?
    ) {
        Log.e(ReactiveNetwork.LOG_TAG, message, exception)
    }
}