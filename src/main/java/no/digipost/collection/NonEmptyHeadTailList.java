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
package no.digipost.collection;

import java.util.AbstractList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Implementation of a {@link NonEmptyList non-empty list}, which
 * simply delegates to an underlying arbitrary list for the elements
 * after the first one, which is enforced to exist.
 *
 * @param <E> the type of elements in this list
 */
final class NonEmptyHeadTailList<E> extends AbstractList<E> implements NonEmptyList<E> {

    private final E head;
    private final List<E> tail;

    NonEmptyHeadTailList(E singleElement) {
        this(singleElement, emptyList());
    }

    NonEmptyHeadTailList(E head, List<E> tail) {
        this.head = head;
        this.tail = tail;
    }

    @Override
    public E get(int index) {
        return index == 0 ? head : tail.get(index - 1);
    }

    @Override
    public int size() {
        return tail.size() + 1;
    }

    @Override
    public boolean hasMultipleElements() {
        return !tail.isEmpty();
    }

}
