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
package no.digipost.concurrent;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static no.digipost.concurrent.CompletionHandler.onSuccess;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompletionHandlerTest {

    @Test
    public void biConsumerBuilder() {
        AtomicReference<Boolean> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        BiConsumer<Object, Throwable> biConsumer = onSuccess(() -> result.set(true)).orCatch(e -> failure.set(e));
        biConsumer.accept("ignored", new IOException());
        biConsumer.accept("result", null);
        assertTrue(result.get());
        assertThat(failure.get(), instanceOf(IOException.class));
    }

    @Test
    public void biFunctionBuilder() {
        BiFunction<Object, Throwable, Object> handler = CompletionHandler.<Object, Object>onSuccess("result").orCatch(e -> e);
        assertThat(handler.apply("any", null), is("result"));
        assertThat(handler.apply("ignored", new IOException()), instanceOf(IOException.class));
    }
}
