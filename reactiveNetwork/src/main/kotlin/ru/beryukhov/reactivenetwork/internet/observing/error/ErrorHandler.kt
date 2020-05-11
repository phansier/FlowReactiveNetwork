package ru.beryukhov.reactivenetwork.internet.observing.error

interface ErrorHandler {
    fun handleError(exception: Exception?, message: String?)
}