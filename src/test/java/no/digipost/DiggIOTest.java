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
package no.digipost;

import no.digipost.function.ThrowingConsumer;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static no.digipost.DiggIO.autoClosing;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DiggIOTest {

    @Test
    public void closesResourceAfterSuccess() throws Exception {
        AutoCloseable resource = mock(AutoCloseable.class);
        autoClosing(r -> {}).accept(resource);
        verify(resource, times(1)).close();
    }

    @Test
    public void closesResourceAfterFailure() throws Exception {
        AutoCloseable resource = mock(AutoCloseable.class);
        Consumer<AutoCloseable> autoClosingConsumer = autoClosing((ThrowingConsumer<AutoCloseable, Exception>) r -> { throw new Exception(); });
        assertThrows(RuntimeException.class, () -> autoClosingConsumer.accept(resource));
        verify(resource, times(1)).close();
    }

}
