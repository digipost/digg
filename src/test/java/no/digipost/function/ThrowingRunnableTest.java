/*
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ThrowingRunnableTest {

    private final RuntimeException ex = new RuntimeException();

    private final Error err = new Error();

    @Test
    public void rethrowOriginalRuntimeException() {
        ThrowingRunnable<Exception> fn = () -> {throw ex;};
        assertThat(assertThrows(RuntimeException.class, fn.asUnchecked()::run), sameInstance(ex));
    }

    @Test
    public void rethrowOriginalError() {
        ThrowingRunnable<Exception> fn = () -> {throw err;};
        assertThat(assertThrows(Error.class, fn.asUnchecked()::run), sameInstance(err));
    }

    @Test
    public void translateToEmptyOptionalAndDelegateExceptionToHandler(@Mock Consumer<Exception> handler) {
        ThrowingRunnable<Exception> fn = () -> {throw ex;};

        fn.ifException(handler).run();
        verify(handler, times(1)).accept(ex);
    }
}
