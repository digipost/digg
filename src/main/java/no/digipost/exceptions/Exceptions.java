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
package no.digipost.exceptions;

import no.digipost.function.ThrowingRunnable;
import no.digipost.function.ThrowingSupplier;

import java.util.function.Function;
import java.util.stream.Stream;

public final class Exceptions {

    /**
     * @param t the Throwable to get the causal chain from.
     * @return the entire chain of causes, including the given Throwable.
     */
    public static Stream<Throwable> causalChainOf(Throwable t) {
        Stream.Builder<Throwable> causes = Stream.builder();
        for (Throwable cause = t; cause != null; cause = cause.getCause()) {
            causes.add(cause);
        }
        return causes.build();
    }

    public static String exceptionNameAndMessage(Throwable t) {
        return t.getClass().getSimpleName() + ": '" + t.getMessage() + "'";
    }

    public static RuntimeException asUnchecked(Throwable t) {
        return asUnchecked(t, Exceptions::exceptionNameAndMessage);
    }

    public static <X extends Throwable> RuntimeException asUnchecked(X t, Function<? super X, String> message) {
        return t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(message.apply(t), t);
    }


    /**
     * Immediately get a value from the given {@link ThrowingSupplier supplier},
     * and if needed, convert any thrown exceptions to unckecked.
     *
     * @param supplier The {@link ThrowingSupplier}.
     * @return the value.
     */
    public static <T> T supplyUnchecked(ThrowingSupplier<T, ? extends Throwable> supplier) {
        return supplier.asUnchecked().get();
    }

    /**
     * Immediately {@link Runnable#run() run} the given {@code runnable},
     * and if needed, convert any thrown exceptions to unckecked.
     *
     * @param runnable The {@link ThrowingRunnable}.
     * @return the value.
     */
    public static void runUnchecked(ThrowingRunnable<? extends Throwable> runnable) {
        runnable.asUnchecked().run();
    }

    private Exceptions() {}
}
