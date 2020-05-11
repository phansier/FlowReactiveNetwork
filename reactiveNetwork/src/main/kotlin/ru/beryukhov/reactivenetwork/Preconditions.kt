package ru.beryukhov.reactivenetwork

import android.os.Build

object Preconditions {
    /**
     * Validation method, which checks if an object is null
     *
     * @param object to verify
     * @param message to be thrown in exception
     */
    fun checkNotNull(o: Any?, message: String) {
        if (o == null) {
            throw IllegalArgumentException(message);
        }
    }

    /**
     * Validation method, which checks if a string is null or empty
     *
     * @param string to verify
     * @param message to be thrown in exception
     */
    fun checkNotNullOrEmpty(string: String?,  message:String) {
        if (string == null || string.isEmpty()) {
            throw IllegalArgumentException(message);
        }
    }

    /**
     * Validation method, which checks is an integer number is positive
     *
     * @param number integer to verify
     * @param message to be thrown in exception
     */
    fun checkGreaterOrEqualToZero(number: Int,  message: String) {
        if (number < 0) {
            throw IllegalArgumentException(message);
        }
    }

    /**
     * Validation method, which checks is an integer number is non-zero or positive
     *
     * @param number integer to verify
     * @param message to be thrown in exception
     */
    fun checkGreaterThanZero(number: Int,  message: String) {
        if (number <= 0) {
            throw IllegalArgumentException(message);
        }
    }

    /**
     * Validation method, which checks if current Android version is at least Lollipop (API 21) or
     * higher
     *
     * @return boolean true if current Android version is Lollipop or higher
     */
    fun isAtLeastAndroidLollipop(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Validation method, which checks if current Android version is at least Marshmallow (API 23) or
     * higher
     *
     * @return boolean true if current Android version is Marshmallow or higher
     */
    fun isAtLeastAndroidMarshmallow():Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}