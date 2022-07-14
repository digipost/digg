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
package no.digipost;

import no.digipost.function.ThrowingBiConsumer;
import no.digipost.function.ThrowingBiFunction;
import no.digipost.function.ThrowingConsumer;
import no.digipost.function.ThrowingFunction;
import no.digipost.function.ThrowingRunnable;
import no.digipost.function.ThrowingSupplier;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static no.digipost.DiggBase.friendlyName;

public final class DiggExceptions {

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

    /**
     * Generate a concise description of an exception/throwable
     * containing the {@link DiggBase#friendlyName(Class) "friendly name"}
     * of the exception's type, and its {@link Throwable#getMessage() message}.
     *
     * @param t the exception/throwable
     *
     * @return the description
     */
    public static String exceptionNameAndMessage(Throwable t) {
        return friendlyName(t.getClass()) + ": '" + t.getMessage() + "'";
    }


    /**
     * Utility for acquiring a {@link RuntimeException} from any {@link Throwable}.
     * This method is appropriate to use when you have caught an exception which you have no
     * ability to handle, and you need to just throw it from a context where you are
     * not allowed to.
     * <p>
     * If you want to add more context to the thrown exception (which you probably should), consider
     * using {@link #asUnchecked(Throwable, String)} or {@link #asUnchecked(Throwable, Function)}.
     *
     * @param t the exception (Throwable)
     *
     * @return a new {@link RuntimeException} which has the given exception as its {@link Throwable#getCause() cause},
     *         or {@code t} itself casted to {@code RuntimeException} if possible
     */
    public static RuntimeException asUnchecked(Throwable t) {
        return t instanceof RuntimeException ? (RuntimeException) t : asUnchecked(t, DiggExceptions::exceptionNameAndMessage);
    }


    /**
     * Utility for acquiring a {@link RuntimeException} with a custom message from any {@link Throwable}.
     * The result from this method will <em>always</em> be a new exception instance.
     *
     * @param <X> the type of the given exception
     * @param t the exception (Throwable)
     * @param messageCreator a function which returns the message for the new exception
     *
     * @return a new {@link RuntimeException} which has the result from the {@code messageCreator} function as its
     *         {@link Throwable#getMessage() message}, and the given exception as its {@link Throwable#getCause() cause}
     */
    public static <X extends Throwable> RuntimeException asUnchecked(X t, Function<? super X, String> messageCreator) {
        return asUnchecked(t, messageCreator.apply(t));
    }


    /**
     * Utility for acquiring a {@link RuntimeException} with a custom message from any {@link Throwable}.
     * The result from this method will <em>always</em> be a new exception instance.
     *
     * @param t the exception (Throwable)
     * @param message the message for the new exception
     *
     * @return a new {@link RuntimeException} which has the given {@code message},
     *         and the given exception as its {@link Throwable#getCause() cause}
     */
    public static RuntimeException asUnchecked(Throwable t, String message) {
        if (t instanceof IOException) {
            return new UncheckedIOException(message, (IOException) t);
        } else {
            return new RuntimeException(message, t);
        }
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
    public static final Consumer<Exception> rethrowAnyException = rethrow(DiggExceptions::asUnchecked);


    /**
     * Create a exception handler ({@link Consumer}) which simply rethrows given exceptions.
     *
     * @param createUnchecked a function to convert a (typically) checked exception to {@link RuntimeException}.
     * @return the {@link Consumer} which rethrows any given exception.
     */
    public static final <T extends Throwable> Consumer<T> rethrow(Function<T, ? extends RuntimeException> createUnchecked) {
        return e -> { throw createUnchecked.apply(e); };
    }



    private DiggExceptions() {}
}
