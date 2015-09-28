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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.function.Consumer;

import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.*;

public class ThrowingRunnableTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final RuntimeException ex = new RuntimeException();

    private final Error err = new Error();

    @Test
    public void rethrowOriginalRuntimeException() {
        ThrowingRunnable<Exception> fn = () -> {throw ex;};
        expectedException.expect(sameInstance(ex));
        fn.asUnchecked().run();
    }

    @Test
    public void rethrowOriginalError() {
        ThrowingRunnable<Exception> fn = () -> {throw err;};
        expectedException.expect(sameInstance(err));
        fn.asUnchecked().run();
    }

    @Test
    public void translateToEmptyOptionalAndDelegateExceptionToHandler() {
        ThrowingRunnable<Exception> fn = () -> {throw ex;};
        @SuppressWarnings("unchecked")
        Consumer<Exception> handler = mock(Consumer.class);

        fn.ifException(handler).run();
        verify(handler, times(1)).accept(ex);
    }
}
