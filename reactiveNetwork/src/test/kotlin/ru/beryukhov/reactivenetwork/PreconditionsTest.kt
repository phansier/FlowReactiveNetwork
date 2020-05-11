package ru.beryukhov.reactivenetwork

import com.google.common.truth.Truth
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import ru.beryukhov.reactivenetwork.Preconditions.checkGreaterOrEqualToZero
import ru.beryukhov.reactivenetwork.Preconditions.checkGreaterThanZero
import ru.beryukhov.reactivenetwork.Preconditions.checkNotNullOrEmpty
import ru.beryukhov.reactivenetwork.Preconditions.isAtLeastAndroidLollipop
import ru.beryukhov.reactivenetwork.Preconditions.isAtLeastAndroidMarshmallow

@RunWith(RobolectricTestRunner::class)
class PreconditionsTest {
    @Test
    @Config(sdk = [21])
    fun shouldBeAtLeastAndroidLollipop() {
        val isAtLeastAndroidLollipop = isAtLeastAndroidLollipop()
        Truth.assertThat(isAtLeastAndroidLollipop).isTrue()
    }

    @Test
    @Config(sdk = [22])
    fun shouldBeAtLeastAndroidLollipopForHigherApi() {
        val isAtLeastAndroidLollipop = isAtLeastAndroidLollipop()
        Truth.assertThat(isAtLeastAndroidLollipop).isTrue()
    }

    @Test
    @Config(sdk = [22])
    fun shouldNotBeAtLeastAndroidMarshmallowForLowerApi() {
        val isAtLeastAndroidMarshmallow = isAtLeastAndroidMarshmallow()
        Truth.assertThat(isAtLeastAndroidMarshmallow).isFalse()
    }

    @Test
    @Config(sdk = [23])
    fun shouldBeAtLeastAndroidMarshmallow() {
        val isAtLeastAndroidMarshmallow = isAtLeastAndroidMarshmallow()
        Truth.assertThat(isAtLeastAndroidMarshmallow).isTrue()
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowAnExceptionWhenStringIsNull() {
        checkNotNullOrEmpty(
            null,
            MSG_STRING_IS_NULL
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowAnExceptionWhenStringIsEmpty() {
        checkNotNullOrEmpty(
            "",
            MSG_STRING_IS_NULL
        )
    }

    @Test
    fun shouldNotThrowAnythingWhenStringIsNotEmpty() {
        checkNotNullOrEmpty(
            "notEmpty",
            MSG_STRING_IS_NULL
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowAnExceptionWhenValueIsZero() {
        checkGreaterThanZero(
            0,
            MSG_VALUE_IS_NOT_GREATER_THAN_ZERO
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowAnExceptionWhenValueLowerThanZero() {
        checkGreaterThanZero(
            -1,
            MSG_VALUE_IS_NOT_GREATER_THAN_ZERO
        )
    }

    @Test
    fun shouldNotThrowAnythingWhenValueIsGreaterThanZero() {
        checkGreaterThanZero(
            1,
            MSG_VALUE_IS_NOT_GREATER_THAN_ZERO
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowAnExceptionWhenValueLowerThanZeroForGreaterOrEqualCheck() {
        checkGreaterOrEqualToZero(
            -1,
            MSG_VALUE_IS_NOT_GREATER_THAN_ZERO
        )
    }

    @Test
    fun shouldNotThrowAnythingWhenValueIsGreaterThanZeroForGreaterOrEqualCheck() {
        checkGreaterOrEqualToZero(
            1,
            MSG_VALUE_IS_NOT_GREATER_THAN_ZERO
        )
    }

    @Test
    fun shouldNotThrowAnythingWhenValueIsEqualToZero() {
        checkGreaterOrEqualToZero(
            0,
            MSG_VALUE_IS_NOT_GREATER_THAN_ZERO
        )
    }

    companion object {
        private const val MSG_STRING_IS_NULL = "String is null"
        private const val MSG_VALUE_IS_NOT_GREATER_THAN_ZERO =
            "value is not greater than zero"
    }
}