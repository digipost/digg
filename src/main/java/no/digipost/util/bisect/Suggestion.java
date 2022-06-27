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

import no.digipost.util.bisect.BisectSearch.Disposer;

import static no.digipost.util.bisect.BisectSearch.Disposer.DISPOSE_BY_CLOSING;
import static no.digipost.util.bisect.BisectSearch.Disposer.NO_DISPOSING;

public final class Suggestion<T> implements AutoCloseable {

    public static <T> Suggestion<T> of(T value) {
        if (value instanceof AutoCloseable) {
            @SuppressWarnings("unchecked")
            Suggestion<T> suggestionDisposedByClosing = (Suggestion<T>) of((AutoCloseable) value);
            return suggestionDisposedByClosing;
        } else {
            return of(value, NO_DISPOSING);
        }
    }

    public static <T extends AutoCloseable> Suggestion<T> of(T value) {
        return new Suggestion<>(value, DISPOSE_BY_CLOSING);
    }

    public static <T> Suggestion<T> of(T value, Disposer<? super T> dismissedSuggestionDisposer) {
        return new Suggestion<>(value, dismissedSuggestionDisposer);
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
