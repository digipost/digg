/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.function;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ThrowingSupplierTest {

    private final RuntimeException ex = new RuntimeException();

    private final Error err = new Error();

    @Test
    public void rethrowOriginalRuntimeException() {
        ThrowingSupplier<?, Exception> fn = () -> {throw ex;};
        assertThat(assertThrows(RuntimeException.class, fn.asUnchecked()::get), sameInstance(ex));
    }

    @Test
    public void rethrowOriginalError() {
        ThrowingSupplier<?, Exception> fn = () -> {throw err;};
        assertThat(assertThrows(Error.class, fn.asUnchecked()::get), sameInstance(err));
    }

    @Test
    public void translateToEmptyOptionalAndDelegateExceptionToHandler() {
        ThrowingSupplier<?, Exception> fn = () -> {throw ex;};
        @SuppressWarnings("unchecked")
        Consumer<Exception> handler = mock(Consumer.class);

        assertThat(fn.ifException(handler).get(), is(empty()));
        verify(handler, times(1)).accept(ex);
    }
}
