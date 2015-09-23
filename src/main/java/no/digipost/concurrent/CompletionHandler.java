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
package no.digipost.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.function.*;

/**
 * Convenience utility to build various handler functions which will be called with
 * <em>either</em> the first argument set to a value of any type, <em>or</em>
 * the second argument as a Throwable indicating an error. Usable for
 * {@link CompletableFuture#whenComplete(BiConsumer)},
 * {@link CompletableFuture#handle(BiFunction)}, or any of their async variants.
 * <p>
 * The built handling functions will <em>always</em> prioritize exception instances
 * in case it is (erroneously) invoked with both a result value and an exception.
 */
public final class CompletionHandler {


    public static final ConsumerBuilder<Object> doNothingOnSuccess = onSuccess(() -> {});

    public static <R> ConsumerBuilder<R> onSuccess(Runnable runOnSuccess) {
        return onSuccess((Consumer<R>) result -> runOnSuccess.run());
    }

    public static <R> ConsumerBuilder<R> onSuccess(Consumer<R> resultConsumer) {
        return new Builder<>(result -> { resultConsumer.accept(result); return null; });
    }

    public static <R, U> FunctionBuilder<R, U> onSuccess(U successValue) {
        return onSuccess(() -> successValue);
    }

    public static <R, U> FunctionBuilder<R, U> onSuccess(Supplier<U> getOnSuccess) {
        return onSuccess((Function<R, U>) result -> getOnSuccess.get());
    }

    public static <R, U> FunctionBuilder<R, U> onSuccess(Function<R, U> resultMapper) {
        return new Builder<>(resultMapper);
    }



    public static interface ConsumerBuilder<R> {
        <E extends Throwable> BiConsumer<R, E> orCatch(Consumer<E> exceptionConsumer);
    }

    public interface FunctionBuilder<R, U> {
        <E extends Throwable> BiFunction<R, E, U> orCatch(Function<E, U> exceptionMapper);
    }

    private static class Builder<R, U> implements ConsumerBuilder<R>, FunctionBuilder<R, U> {

        private final Function<R, U> resultMapper;

        Builder(Function<R, U> function) {
            this.resultMapper = function;
        }

        @Override
        public <E extends Throwable> BiConsumer<R, E> orCatch(Consumer<E> exceptionConsumer) {
            return (result, thrown) -> {
                orCatch((E t) -> { exceptionConsumer.accept(t);	return null; }).apply(result, thrown);
            };
        }

        @Override
        public <E extends Throwable> BiFunction<R, E, U> orCatch(Function<E, U> exceptionMapper) {
            return (result, thrown) -> thrown != null ? exceptionMapper.apply(thrown) : resultMapper.apply(result);
        }
    }

    private CompletionHandler() {}

}
