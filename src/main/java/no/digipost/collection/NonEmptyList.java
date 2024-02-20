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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

/**
 * A non-empty list. Implementations of this interface must
 * take care to ensure that instances will <em>never</em> in any
 * circumstance be allowed to be empty.
 *
 * @implNote In general, constructing a {@code NonEmptyList} from another
 * mutable container source (e.g. an existing list or array), and then changing the
 * contents of the source, results in undefined behavior of the non-empty list. The
 * only guarantee is that the non-empty list will <em>never</em> be empty, but the
 * behavior when mutating a list on which the non-empty list is based on should not be
 * relied on.
 * <p>
 * If you need to construct non-empty lists based on another container you
 * need to further mutate, consider using one of the {@link #copyOf(Collection) copyOf}
 * constructors, which will copy the given references so that any subsequent changes to the
 * to the given source container will not be reflected in the created non-empty list. (As usual,
 * this is a shallow copy, meaning that the instances themselves can of course be mutated by anything.)
 *
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
    @SuppressWarnings("varargs")
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
     * Try to construct a non-empty list from a given array, which may be empty.
     *
     * @param <E> the type of elements in the array.
     * @param array the array, which may be empty
     *
     * @return the resulting non-empty list, or {@link Optional#empty()} if the
     *         given array is empty
     */
    static <E> Optional<NonEmptyList<E>> of(E[] array) {
        return of(asList(array));
    }


    /**
     * Try to construct a non-empty list from a given list, which is assumed is not empty.
     * <p>
     * This method should only be used when the given list is <em>guarantied</em>
     * to be empty, and thus offers a fail-fast way to introduce the non-empty
     * quality on a type level. Use {@link #of(List)} if you need
     * more flexibility in handling of a possible empty list.
     *
     * @param <E> the type of elements in the list.
     * @param nonEmptyList the list, which is assumed not to be empty
     *
     * @return the resulting non-empty list, or {@link Optional#empty()} if the
     *         given list is empty
     *
     * @throws IllegalArgumentException if the given list is empty
     */
    static <E> NonEmptyList<E> ofUnsafe(List<E> nonEmptyList) {
        return of(nonEmptyList).orElseThrow(() -> new IllegalArgumentException("empty list"));
    }


    /**
     * Try to construct a non-empty list from a given array, which is assumed is not empty.
     * <p>
     * This method should only be used when the given list is <em>guarantied</em>
     * to be empty, and thus offers a fail-fast way to introduce the non-empty
     * quality on a type level. Use {@link #of(Object[])} if you need
     * more flexibility in handling of a possible empty list.
     *
     * @param <E> the type of elements in the array.
     * @param nonEmptyArray the array, which is assumed not to be empty
     *
     * @return the resulting non-empty list, or {@link Optional#empty()} if the
     *         given array is empty
     *
     * @throws IllegalArgumentException if the given array is empty
     */
    static <E> NonEmptyList<E> ofUnsafe(E[] nonEmptyArray) {
        return of(nonEmptyArray).orElseThrow(() -> new IllegalArgumentException("empty array"));
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
        return copyOf((Collection<E>) list);
    }


    /**
     * Try to construct a non-empty list from copying the elements
     * of a given collection, which may be empty.
     *
     * @param <E> the type of elements in the collection.
     * @param collection the collection, which may be empty
     *
     * @return the resulting non-empty list,
     *         or {@link Optional#empty()} if the given collection is empty
     */
    static <E> Optional<NonEmptyList<E>> copyOf(Collection<E> collection) {
        return of(new ArrayList<>(collection));
    }


    /**
     * Try to construct a non-empty list from copying the elements
     * of a given array, which may be empty.
     *
     * @param <E> the type of elements in the array.
     * @param array the array, which may be empty
     *
     * @return the resulting non-empty list,
     *         or {@link Optional#empty()} if the given array is empty
     */
    static <E> Optional<NonEmptyList<E>> copyOf(E[] array) {
        return copyOf(asList(array));
    }


    /**
     * <strong>Unsafe</strong> construction of non-empty list from copying the
     * elements of a list assumed to be non-empty.
     * <p>
     * This method should only be used when the given list is <em>guarantied</em>
     * to be empty, and thus offers a fail-fast way to introduce the non-empty
     * quality on a type level. Use {@link #copyOf(List)} if you need
     * more flexibility in handling of a possible empty list.
     *
     * @param <E> the type of elements in the list.
     * @param nonEmptyList the list, which is assumed not to be empty
     *
     * @return the resulting non-empty list
     *
     * @throws IllegalArgumentException if the given list is empty
     *
     * @see #copyOf(List)
     */
    static <E> NonEmptyList<E> copyOfUnsafe(List<E> nonEmptyList) {
        return copyOfUnsafe((Collection<E>) nonEmptyList);
    }


    /**
     * <strong>Unsafe</strong> construction of non-empty list from copying the
     * elements of a collection assumed to be non-empty.
     * <p>
     * This method should only be used when the given collection is <em>guarantied</em>
     * to be empty, and thus offers a fail-fast way to introduce the non-empty
     * quality on a type level. Use {@link #copyOf(Collection)} if you need
     * more flexibility in handling of a possible empty list.
     *
     * @param <E> the type of elements in the list.
     * @param nonEmptyCollection the collection, which is assumed not to be empty
     *
     * @return the resulting non-empty list
     *
     * @throws IllegalArgumentException if the given list is empty
     *
     * @see #copyOf(Collection)
     */
    static <E> NonEmptyList<E> copyOfUnsafe(Collection<E> nonEmptyCollection) {
        return copyOf(nonEmptyCollection).orElseThrow(() -> new IllegalArgumentException("empty list"));
    }


    /**
     * <strong>Unsafe</strong> construction of non-empty list from copying the
     * elements of an array assumed to be non-empty.
     * <p>
     * This method should only be used when the given array is <em>guaranteed</em>
     * to be empty, and thus offers a fail-fast way to introduce the non-empty
     * quality on a type level. Use {@link #copyOf(Object[])} if you need
     * more flexibility in handling of a possible empty array.
     *
     * @param <E> the type of elements in the array.
     * @param nonEmptyArray the array, which is assumed not to be empty
     *
     * @return the resulting non-empty list
     *
     * @throws IllegalArgumentException if the given array is empty
     *
     * @see #copyOf(Object[])
     */
    static <E> NonEmptyList<E> copyOfUnsafe(E[] nonEmptyArray) {
        return copyOf(nonEmptyArray).orElseThrow(() -> new IllegalArgumentException("empty array"));
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
