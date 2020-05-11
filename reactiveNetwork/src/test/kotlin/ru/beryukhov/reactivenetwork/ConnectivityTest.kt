/*
 * Copyright (C) 2016 Piotr Wittchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.beryukhov.reactivenetwork

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.NetworkInfo.DetailedState
import com.google.common.truth.Truth
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import ru.beryukhov.reactivenetwork.Connectivity.Companion.create
import ru.beryukhov.reactivenetwork.Connectivity.Companion.state
import ru.beryukhov.reactivenetwork.ConnectivityPredicate.appendUnknownNetworkTypeToTypes
import ru.beryukhov.reactivenetwork.ConnectivityPredicate.hasState
import ru.beryukhov.reactivenetwork.ConnectivityPredicate.hasType

@RunWith(RobolectricTestRunner::class)
class ConnectivityTest {
    @Test
    fun shouldCreateConnectivity() { // when
        val connectivity = create()
        // then
        Truth.assertThat(connectivity).isNotNull()
        Truth.assertThat(connectivity.state())
            .isEqualTo(NetworkInfo.State.DISCONNECTED)
        Truth.assertThat(connectivity.detailedState())
            .isEqualTo(DetailedState.IDLE)
        Truth.assertThat(connectivity.type()).isEqualTo(Connectivity.UNKNOWN_TYPE)
        Truth.assertThat(connectivity.subType()).isEqualTo(Connectivity.UNKNOWN_SUB_TYPE)
        Truth.assertThat(connectivity.available()).isFalse()
        Truth.assertThat(connectivity.failover()).isFalse()
        Truth.assertThat(connectivity.roaming()).isFalse()
        Truth.assertThat(connectivity.typeName())
            .isEqualTo(TYPE_NAME_NONE)
        Truth.assertThat(connectivity.subTypeName())
            .isEqualTo(TYPE_NAME_NONE)
        Truth.assertThat(connectivity.reason()).isEmpty()
        Truth.assertThat(connectivity.extraInfo()).isEmpty()
    }

    @Test
    @Throws(Exception::class)
    fun stateShouldBeEqualToGivenValue() { // given
        val connectivity = state(NetworkInfo.State.CONNECTED)
            .type(ConnectivityManager.TYPE_WIFI)
            .typeName(TYPE_NAME_WIFI)
            .build()
        // when
        val equalTo =
            hasState(connectivity.state())
        val shouldBeEqualToGivenStatus = equalTo.test(connectivity)
        // then
        Truth.assertThat(shouldBeEqualToGivenStatus).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun stateShouldBeEqualToOneOfGivenMultipleValues() { // given
        val connectivity = state(NetworkInfo.State.CONNECTING)
            .type(ConnectivityManager.TYPE_WIFI)
            .typeName(TYPE_NAME_WIFI)
            .build()
        val states = arrayOf(NetworkInfo.State.CONNECTED, NetworkInfo.State.CONNECTING)
        // when
        val equalTo = hasState(*states)
        val shouldBeEqualToGivenStatus = equalTo.test(connectivity)
        // then
        Truth.assertThat(shouldBeEqualToGivenStatus).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun stateShouldNotBeEqualToGivenValue() { // given
        val connectivity = state(NetworkInfo.State.DISCONNECTED)
            .type(ConnectivityManager.TYPE_WIFI)
            .typeName(TYPE_NAME_WIFI)
            .build()
        // when
        val equalTo = hasState(NetworkInfo.State.CONNECTED)
        val shouldBeEqualToGivenStatus = equalTo.test(connectivity)
        // then
        Truth.assertThat(shouldBeEqualToGivenStatus).isFalse()
    }

    @Test
    @Throws(Exception::class)
    fun typeShouldBeEqualToGivenValue() { // given
        val connectivity = state(NetworkInfo.State.CONNECTED)
            .type(ConnectivityManager.TYPE_WIFI)
            .typeName(TYPE_NAME_WIFI)
            .build()
        // note that unknown type is added initially by the ConnectivityPredicate#hasType method
        val givenTypes = intArrayOf(connectivity.type(), Connectivity.UNKNOWN_TYPE)
        // when
        val equalTo = hasType(*givenTypes)
        val shouldBeEqualToGivenStatus = equalTo.test(connectivity)
        // then
        Truth.assertThat(shouldBeEqualToGivenStatus).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun typeShouldBeEqualToOneOfGivenMultipleValues() { // given
        val connectivity = state(NetworkInfo.State.CONNECTING)
            .type(ConnectivityManager.TYPE_MOBILE)
            .typeName(TYPE_NAME_MOBILE)
            .build()
        // note that unknown type is added initially by the ConnectivityPredicate#hasType method
        val givenTypes = intArrayOf(
            ConnectivityManager.TYPE_WIFI,
            ConnectivityManager.TYPE_MOBILE,
            Connectivity.UNKNOWN_TYPE
        )
        // when
        val equalTo = hasType(*givenTypes)
        val shouldBeEqualToGivenStatus = equalTo.test(connectivity)
        // then
        Truth.assertThat(shouldBeEqualToGivenStatus).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun typeShouldNotBeEqualToGivenValue() { // given
        val connectivity = state(NetworkInfo.State.CONNECTED)
            .type(ConnectivityManager.TYPE_WIFI)
            .typeName(TYPE_NAME_WIFI)
            .build()
        // note that unknown type is added initially by the ConnectivityPredicate#hasType method
        val givenTypes = intArrayOf(ConnectivityManager.TYPE_MOBILE, Connectivity.UNKNOWN_TYPE)
        // when
        val equalTo = hasType(*givenTypes)
        val shouldBeEqualToGivenStatus = equalTo.test(connectivity)
        // then
        Truth.assertThat(shouldBeEqualToGivenStatus).isFalse()
    }

    //kotlin checks nullability by itself
    /*@Test(expected = KotlinNullPointerException::class)
    fun createShouldThrowAnExceptionWhenContextIsNull() { // given
        val context: Context? = null
        // when
        create(context)
        // then
        // an exception is thrown
    }*/

    @Test
    fun shouldReturnProperToStringValue() { // given
        val expectedToString = ("Connectivity{"
                + "state=DISCONNECTED, "
                + "detailedState=IDLE, "
                + "type=-1, "
                + "subType=-1, "
                + "available=false, "
                + "failover=false, "
                + "roaming=false, "
                + "typeName='NONE', "
                + "subTypeName='NONE', "
                + "reason='', "
                + "extraInfo=''}")
        // when
        val connectivity = create()
        // then
        Truth.assertThat(connectivity.toString()).isEqualTo(expectedToString)
    }

    @Test
    fun theSameConnectivityObjectsShouldBeEqual() { // given
        val connectivityOne = create()
        val connectivityTwo = create()
        // when
        val objectsAreEqual = connectivityOne.equals(connectivityTwo)
        // then
        Truth.assertThat(objectsAreEqual).isTrue()
    }

    @Test
    fun twoDefaultObjectsShouldBeInTheSameBucket() { // given
        val connectivityOne = create()
        val connectivityTwo = create()
        // when
        val hashCodesAreEqual = connectivityOne.hashCode() == connectivityTwo.hashCode()
        // then
        Truth.assertThat(hashCodesAreEqual).isTrue()
    }

    @Test
    fun shouldAppendUnknownTypeWhileFilteringNetworkTypesInsidePredicate() { // given
        val types =
            intArrayOf(ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI)
        val expectedOutputTypes = intArrayOf(
            ConnectivityManager.TYPE_MOBILE,
            ConnectivityManager.TYPE_WIFI,
            Connectivity.UNKNOWN_TYPE
        )
        // when
        val outputTypes =
            appendUnknownNetworkTypeToTypes(types)
        // then
        Truth.assertThat(outputTypes).isEqualTo(expectedOutputTypes)
    }

    @Test
    fun shouldAppendUnknownTypeWhileFilteringNetworkTypesInsidePredicateForEmptyArray() { // given
        val types = intArrayOf()
        val expectedOutputTypes = intArrayOf(Connectivity.UNKNOWN_TYPE)
        // when
        val outputTypes = appendUnknownNetworkTypeToTypes(types)
        // then
        Truth.assertThat(outputTypes).isEqualTo(expectedOutputTypes)
    }

    @Test
    fun shouldCreateConnectivityWithBuilder() { // given
        val state = NetworkInfo.State.CONNECTED
        val detailedState = DetailedState.CONNECTED
        val type = ConnectivityManager.TYPE_WIFI
        val subType = ConnectivityManager.TYPE_WIMAX
        val typeName = TYPE_NAME_WIFI
        val subTypeName = "test subType"
        val reason = "no reason"
        val extraInfo = "extra info"
        // when
        val connectivity = state(state)
            .detailedState(detailedState)
            .type(type)
            .subType(subType)
            .available(true)
            .failover(false)
            .roaming(true)
            .typeName(typeName)
            .subTypeName(subTypeName)
            .reason(reason)
            .extraInfo(extraInfo)
            .build()
        // then
        Truth.assertThat(connectivity.state()).isEqualTo(state)
        Truth.assertThat(connectivity.detailedState()).isEqualTo(detailedState)
        Truth.assertThat(connectivity.type()).isEqualTo(type)
        Truth.assertThat(connectivity.subType()).isEqualTo(subType)
        Truth.assertThat(connectivity.available()).isTrue()
        Truth.assertThat(connectivity.failover()).isFalse()
        Truth.assertThat(connectivity.roaming()).isTrue()
        Truth.assertThat(connectivity.typeName()).isEqualTo(typeName)
        Truth.assertThat(connectivity.subTypeName()).isEqualTo(subTypeName)
        Truth.assertThat(connectivity.reason()).isEqualTo(reason)
        Truth.assertThat(connectivity.extraInfo()).isEqualTo(extraInfo)
    }

    @Test
    fun connectivityShouldNotBeEqualToAnotherOne() { // given
        val connectivityOne = state(NetworkInfo.State.CONNECTED)
            .detailedState(DetailedState.CONNECTED)
            .type(ConnectivityManager.TYPE_WIFI)
            .subType(1)
            .available(true)
            .failover(true)
            .roaming(true)
            .typeName(TYPE_NAME_WIFI)
            .subTypeName("subtypeOne")
            .reason("reasonOne")
            .extraInfo("extraInfoOne")
            .build()
        val connectivityTwo = state(NetworkInfo.State.DISCONNECTED)
            .detailedState(DetailedState.DISCONNECTED)
            .type(ConnectivityManager.TYPE_MOBILE)
            .subType(2)
            .available(false)
            .failover(false)
            .roaming(false)
            .typeName(TYPE_NAME_MOBILE)
            .subTypeName("subtypeTwo")
            .reason("reasonTwo")
            .extraInfo("extraInfoTwo")
            .build()
        // when
        val isAnotherConnectivityTheSame = connectivityOne.equals(connectivityTwo)
        // then
        Truth.assertThat(isAnotherConnectivityTheSame).isFalse()
    }

    @Test
    fun shouldCreateDefaultConnectivityWhenConnectivityManagerIsNull() { // given
        val context =
            RuntimeEnvironment.application.applicationContext
        val connectivityManager: ConnectivityManager? = null
        // when
        val connectivity = create(context, connectivityManager)
        // then
        Truth.assertThat(connectivity.type()).isEqualTo(Connectivity.UNKNOWN_TYPE)
        Truth.assertThat(connectivity.subType()).isEqualTo(Connectivity.UNKNOWN_SUB_TYPE)
        Truth.assertThat(connectivity.state())
            .isEqualTo(NetworkInfo.State.DISCONNECTED)
        Truth.assertThat(connectivity.detailedState())
            .isEqualTo(DetailedState.IDLE)
        Truth.assertThat(connectivity.available()).isFalse()
        Truth.assertThat(connectivity.failover()).isFalse()
        Truth.assertThat(connectivity.roaming()).isFalse()
        Truth.assertThat(connectivity.typeName())
            .isEqualTo(TYPE_NAME_NONE)
        Truth.assertThat(connectivity.subTypeName())
            .isEqualTo(TYPE_NAME_NONE)
        Truth.assertThat(connectivity.reason()).isEmpty()
        Truth.assertThat(connectivity.extraInfo()).isEmpty()
    }

    companion object {
        private const val TYPE_NAME_WIFI = "WIFI"
        private const val TYPE_NAME_MOBILE = "MOBILE"
        private const val TYPE_NAME_NONE = "NONE"
    }
}