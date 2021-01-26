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

import no.digipost.stream.NonEmptyStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

/**
 * A non-empty list. Implementations of this interface must
 * take care to ensure that instances will <em>never</em> in any
 * circumstance be allowed to be empty.
 *
 * @param <E> the type of elements in this list
 */
public interface NonEmptyList<E> extends List<E> {

    /**
     * Utility for safe retrieval of the first element from an arbitrary list.
     *
     * @param <E> the type of the elements in the list
     * @param list the list to get the first element from
     *
     * @return an {@link Optional} with the first element of the list,
     *         or {@link Optional#empty() empty} if the list was empty.
     */
    static <E> Optional<E> firstOf(List<E> list) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }


    /**
     * Construct a non-empty list with one single element.
     *
     * @param <E> the type of the element
     * @param singleElement the element to include in the list
     *
     * @return the non-empty list
     */
    static <E> NonEmptyList<E> of(E singleElement) {
        return new NonEmptyHeadTailList<>(singleElement);
    }


    /**
     * Construct a non-empty list containing a specific object as its
     * first element, and any provided remaining elements.
     *
     * @param <E> the type of the elements
     * @param firstElement the first element to include
     * @param remainingElements the remaining elements to include
     *
     * @return the non-empty list
     */
    @SafeVarargs
    public static <E> NonEmptyList<E> of(E firstElement, E ... remainingElements) {
        return of(firstElement, asList(remainingElements));
    }


    /**
     * Construct a non-empty list containing a specific object as its
     * first element, and any elements contained in another list as
     * its remaining elements.
     *
     * @param <E> the type of the elements
     * @param firstElement the first element to include
     * @param remainingElements the remaining elements to include
     *
     * @return the non-empty list
     */
    static <E> NonEmptyList<E> of(E firstElement, List<E> remainingElements) {
        return new NonEmptyHeadTailList<>(firstElement, remainingElements);
    }


    /**
     * Try to construct a non-empty list from a given list, which may be empty.
     *
     * @param <E> the type of elements in the list.
     * @param list the list, which may be empty
     *
     * @return the resulting non-empty list, or {@link Optional#empty()} if the
     *         given list is empty
     */
    static <E> Optional<NonEmptyList<E>> of(List<E> list) {
        return firstOf(list).map(first -> NonEmptyList.of(first, list.subList(1, list.size())));
    }


    /**
     * <strong>Unsafe</strong> construction of non-empty list from a possibly empty list.
     * <p>
     * This method should only be used when the given list is <em>guarantied</em>
     * to be empty, and thus offers a fail-fast way to introduce the non-empty
     * quality on a type level. Use {@link #copyOf(List)} if you need
     * more flexibility in handling of a possible empty list.
     *
     * @param <E> the type of elements in the list.
     * @param list the list
     *
     * @return the resulting non-empty list
     *
     * @throws IllegalArgumentException if the given list is empty
     *
     * @see #copyOf(List)
     */
    static <E> NonEmptyList<E> ofUnsafe(List<E> list) {
        return copyOf(list).orElseThrow(() -> new IllegalArgumentException("empty list"));
    }


    /**
     * Try to construct a non-empty list from copying the elements
     * of a given list, which may be empty.
     *
     * @param <E> the type of elements in the list.
     * @param list the list, which may be empty
     *
     * @return the resulting non-empty list,
     *         or {@link Optional#empty()} if the given list is empty
     */
    static <E> Optional<NonEmptyList<E>> copyOf(List<E> list) {
        return firstOf(list).map(first -> NonEmptyList.of(first, new ArrayList<>(list.subList(1, list.size()))));
    }


    /**
     * <strong>Unsafe</strong> construction of non-empty list from copying the
     * elements of a possibly empty list.
     * <p>
     * This method should only be used when the given list is <em>guarantied</em>
     * to be empty, and thus offers a fail-fast way to introduce the non-empty
     * quality on a type level. Use {@link #copyOf(List)} if you need
     * more flexibility in handling of a possible empty list.
     *
     * @param <E> the type of elements in the list.
     * @param nonEmptyList the list
     *
     * @return the resulting non-empty list
     *
     * @throws IllegalArgumentException if the given list is empty
     *
     * @see #copyOf(List)
     */
    static <E> NonEmptyList<E> copyOfUnsafe(List<E> nonEmptyList) {
        return copyOf(nonEmptyList).orElseThrow(() -> new IllegalArgumentException("empty list"));
    }


    @Override
    default NonEmptyStream<E> stream() {
        return NonEmptyStream.<E>of(this::first, this.subList(1, size()).stream());
    }


    /**
     * Get the first element of this non-empty list.
     * This method should never fail with an exception.
     *
     * @return the first element.
     */
    default E first() {
        return get(0);
    }

    /**
     * A non-empty list is never empty, so this always returns {@code true}.
     *
     * @return always {@code true}
     */
    @Override
    default boolean isEmpty() {
        return false;
    }

    /**
     * Checks if the non-empty list contains <em>only</em> the required
     * singular element, i.e. if the size of the list is exactly 1.
     *
     * @return {@code true} if the list contains only one element,
     *         {@code false} otherwise.
     */
    default boolean isSingular() {
        return !hasMultipleElements();
    }

    /**
     * Checks if this non-empty list has multiple elements, i.e. 2 or
     * more elements. This is functionally equivalent of checking if
     * {@code NonEmptyList.size() > 1}, but this method should be preferred
     * as implementations may provide more efficient way than checking the
     * actual amount.
     *
     * @implNote Implementations of {@code NonEmptyList} should override this
     *           default method if they can provide a more efficient way to
     *           determine if there are more than one element.
     * @return {@code true} if the list contains more than one element,
     *         or {@code false} if only a {@link #isSingular() single} element is contained.
     */
    default boolean hasMultipleElements() {
        return size() > 1;
    }

}
