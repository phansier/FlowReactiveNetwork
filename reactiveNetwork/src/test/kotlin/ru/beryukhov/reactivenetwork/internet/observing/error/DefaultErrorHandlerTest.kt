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
package ru.beryukhov.reactivenetwork.internet.observing.error

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Spy
import org.mockito.junit.MockitoJUnit
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
open class DefaultErrorHandlerTest {
    @get:Rule
    val rule = MockitoJUnit.rule()
    @Spy
    private val handler = DefaultErrorHandler()

    @Test
    fun shouldHandleErrorDuringClosingSocket() { // given
        val errorMsg = "Could not close the socket"
        val exception = Exception(errorMsg)
        // when
        handler.handleError(exception, errorMsg)
        // then
        Mockito.verify(
            handler,
            Mockito.times(1)
        ).handleError(exception, errorMsg)
    }
}