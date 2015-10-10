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

import no.digipost.function.*;

import java.util.function.Consumer;
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
     * Immediately {@link ThrowingFunction#apply apply} the given {@code function} with the given {@code argument},
     * and if needed, convert any thrown exceptions to unckecked.
     *
     * @param function The {@link ThrowingFunction}.
     * @param argument The argument to pass to {@link ThrowingFunction#apply(Object)}
     * @return the result.
     */
    public static <T, R> R applyUnchecked(ThrowingFunction<T, R, ? extends Throwable> function, T argument) {
        return function.asUnchecked().apply(argument);
    }

    /**
     * Immediately {@link ThrowingSupplier#get get} a result from the given {@code supplier},
     * and if needed, convert any thrown exceptions to unckecked.
     *
     * @param supplier The {@link ThrowingSupplier}.
     * @return the result.
     */
    public static <T> T getUnchecked(ThrowingSupplier<T, ? extends Throwable> supplier) {
        return supplier.asUnchecked().get();
    }

    /**
     * Immediately {@link ThrowingRunnable#run() run} the given {@code runnable},
     * and if needed, convert any thrown exceptions to unckecked.
     *
     * @param runnable The {@link ThrowingRunnable}.
     */
    public static void runUnchecked(ThrowingRunnable<? extends Throwable> runnable) {
        runnable.asUnchecked().run();
    }


    /**
     * Convenience to acquire a {@link ThrowingFunction}-reference from a lambda expression.
     */
    public static final <T, R, X extends Throwable> ThrowingFunction<T, R, X> mayThrow(ThrowingFunction<T, R, X> function) {
        return function;
    }

    /**
     * Convenience to acquire a {@link ThrowingBiFunction}-reference from a lambda expression.
     */
    public static final <T, U, R, X extends Throwable> ThrowingBiFunction<T, U, R, X> mayThrow(ThrowingBiFunction<T, U, R, X> bifunction) {
        return bifunction;
    }

    /**
     * Convenience to acquire a {@link ThrowingSupplier}-reference from a lambda expression.
     */
    public static final <T, X extends Throwable> ThrowingSupplier<T, X> mayThrow(ThrowingSupplier<T, X> supplier) {
        return supplier;
    }

    /**
     * Convenience to acquire a {@link ThrowingConsumer}-reference from a lambda expression.
     */
    public static final <T, X extends Throwable> ThrowingConsumer<T, X> mayThrow(ThrowingConsumer<T, X> consumer) {
        return consumer;
    }

    /**
     * Convenience to acquire a {@link ThrowingBiConsumer}-reference from a lambda expression.
     */
    public static final <T, U, X extends Throwable> ThrowingBiConsumer<T, U, X> mayThrow(ThrowingBiConsumer<T, U, X> consumer) {
        return consumer;
    }

    /**
     * Convenience to acquire a {@link ThrowingRunnable}-reference from a lambda expression.
     */
    public static final <T, X extends Throwable> ThrowingRunnable<X> mayThrow(ThrowingRunnable<X> runnable) {
        return runnable;
    }


    /**
     * This consumer rethrows any given {@link Exception} as an unchecked {@link RuntimeException}.
     */
    public static final Consumer<Exception> rethrowAnyException = rethrow(Exceptions::asUnchecked);


    /**
     * Create a exception handler ({@link Consumer}) which simply rethrows given exceptions.
     *
     * @param createUnchecked a function to convert a (typically) checked exception to {@link RuntimeException}.
     * @return the {@link Consumer} which rethrows any given exception.
     */
    public static final <T extends Throwable> Consumer<T> rethrow(Function<T, ? extends RuntimeException> createUnchecked) {
        return e -> { throw createUnchecked.apply(e); };
    }



    private Exceptions() {}
}
