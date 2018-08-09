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
package no.digipost.collection;

import no.digipost.DiggExceptions;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * SimpleIterator offers an easier way to implement the
 * {@link java.util.Iterator} interface, by requiring only
 * one method, {@link #nextIfAvailable()}, to be implemented instead of both
 * {@link Iterator#hasNext() hasNext()} and {@link Iterator#next() next()}.
 *
 * In addition, implementations are free to throw any exception, which,
 * if is a checked exception, will be rethrown wrapped in a <code>RuntimeException</code>.
 *
 * @param <T> The type of elements yielded by this iterator.
 * @see #nextIfAvailable()
 */
public abstract class SimpleIterator<T> implements Iterator<T> {

    private Optional<? extends T> next = Optional.empty();

    /**
     * @return The next element if any, or {@link Optional#empty() empty} if there are no more elements.
     */
    protected abstract Optional<? extends T> nextIfAvailable() throws Exception;

    @Override
    public final boolean hasNext() {
        try {
            next = nextIfAvailable();
        } catch (Exception e) {
            throw DiggExceptions.asUnchecked(e);
        }
        return next.isPresent();
    }

    @Override
    public final T next() {
        if (!next.isPresent() && !hasNext()) throw new NoSuchElementException();
        T toReturn = next.get();
        next = Optional.empty();
        return toReturn;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() method is not supported");
    }

}
