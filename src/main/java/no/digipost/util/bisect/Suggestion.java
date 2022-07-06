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
package no.digipost.util.bisect;

/**
 * A suggested value for a bisection search. A {@link Suggestion} is
 * AutoCloseable in order to support (if relevant) {@link Suggestion.Disposer disposal}
 * of dismissed values.
 *
 * @param <T> the type of the value contained in this suggestion
 */
public final class Suggestion<T> implements AutoCloseable {

    /**
     * Create a suggestion of a value. If the given value is {@link AutoCloseable}, it will,
     * if dismissed, it will have its {@link AutoCloseable#close() close method} invoked.
     *
     * @return a new suggestion containing the given value
     */
    public static <T> Suggestion<T> of(T value) {
        if (value instanceof AutoCloseable) {
            @SuppressWarnings("unchecked")
            Suggestion<T> suggestionDisposedByClosing = (Suggestion<T>) of((AutoCloseable) value);
            return suggestionDisposedByClosing;
        } else {
            return of(value, Disposer.NO_DISPOSING);
        }
    }

    /**
     * Create a suggestion of an {@link AutoCloseable} object, which, if dismissed,
     * will be disposed by having its {@link AutoCloseable#close() close method} invoked.
     *
     * @return a new suggestion containing the given value
     */
    public static <T extends AutoCloseable> Suggestion<T> of(T value) {
        return new Suggestion<>(value, Disposer.DISPOSE_BY_CLOSING);
    }

    /**
     * Create a suggestion of an object, with a custom operation for {@link Disposer disposing}
     * if the suggestion is dismissed.
     *
     * @return a new suggestion containing the given value
     */
    public static <T> Suggestion<T> of(T value, Disposer<? super T> dismissedSuggestionDisposer) {
        return new Suggestion<>(value, dismissedSuggestionDisposer);
    }


    /**
     * A disposing operation for a dismissed value of a {@link Suggestion}.
     *
     * @param <T> the type of the suggested value which are to be disposed
     */
    @FunctionalInterface
    public interface Disposer<T> {
        static final Disposer<Object> NO_DISPOSING = suggestion -> {};
        static final Disposer<AutoCloseable> DISPOSE_BY_CLOSING = AutoCloseable::close;

        void dispose(T suggestion) throws Exception;
    }



    private final T value;
    private final Disposer<? super T> disposer;
    private boolean accepted = false;

    private Suggestion(T value, Disposer<? super T> disposer) {
        this.value = value;
        this.disposer = disposer;
    }

    T peek() {
        return value;
    }

    T accepted() {
        accepted = true;
        return value;
    }

    @Override
    public void close() throws Exception {
        if (!accepted) {
            disposer.dispose(value);
        }
    }
}
