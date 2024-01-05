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

import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;


/**
 * Utilities for comparing values.
 */
public final class DiggCompare {


    /**
     * Choose the lesser of two {@link Comparable} objects.
     * In the case where the objects are considered equal by
     * {@link Comparable#compareTo(Object)}, the first argument
     * is returned.
     *
     * @return the least of the two given objects.
     */
    public static <T extends Comparable<T>> T min(T t1, T t2) {
        return t1.compareTo(t2) > 0 ? t2 : t1;
    }


    /**
     * Choose the greater of two {@link Comparable} objects.
     * In the case where the objects are considered equal by
     * {@link Comparable#compareTo(Object)}, the first argument
     * is returned.
     *
     * @return the greatest of the two given objects.
     */
    public static <T extends Comparable<T>> T max(T t1, T t2) {
        return t2.compareTo(t1) > 0 ? t2 : t1;
    }


    /**
     * Choose the lesser of two objects by using a given {@link Comparator}.
     *
     * In the case where the objects are considered equal by
     * {@link Comparable#compareTo(Object)}, the first argument
     * is returned.
     *
     * <p>
     * This is the static method equivalent of applying the resulting function from
     * {@link BinaryOperator#minBy(Comparator)} with the given comparator as argument.
     *
     * @return the least of the two given objects.
     */
    public static <T> T minBy(Comparator<? super T> propertyComparator, T t1, T t2) {
        return propertyComparator.compare(t1, t2) > 0 ? t2 : t1;
    }


    /**
     * Choose the lesser of two objects by comparing a
     * {@link Comparable} value resolved from each object.
     *
     * In the case where the objects are considered equal by
     * {@link Comparable#compareTo(Object)}, the first argument
     * is returned.
     *
     * <p>
     * This is the static method equivalent of applying the resulting function from
     * {@link BinaryOperator#minBy(Comparator)} with
     * {@link Comparator#comparing(Function, Comparator) comparing(propertyExtractor)}
     * as argument.
     *
     * @return the least of the two given objects.
     */
    public static <T, U extends Comparable<? super U>> T minBy(Function<? super T, U> propertyExtractor, T t1, T t2) {
        return minBy(comparing(propertyExtractor), t1, t2);
    }


    /**
     * Choose the greater of two objects by using a {@link Comparator}.
     *
     * In the case where the objects are considered equal by
     * {@link Comparable#compareTo(Object)}, the first argument
     * is returned.
     *
     * <p>
     * This is the static method equivalent of applying the resulting function from
     * {@link BinaryOperator#maxBy(Comparator)} with the given comparator as argument.
     *
     * @return the least of the two given objects.
     */
    public static <T> T maxBy(Comparator<? super T> comparator, T t1, T t2) {
        return comparator.compare(t2, t1) > 0 ? t2 : t1;
    }


    /**
     * Choose the greater of two objects by comparing a
     * {@link Comparable} value resolved from each object.
     *
     * In the case where the objects are considered equal by
     * {@link Comparable#compareTo(Object)}, the first argument
     * is returned.
     *
     * <p>
     * This is the static method equivalent of applying the resulting function from
     * {@link BinaryOperator#maxBy(Comparator)} with
     * {@link Comparator#comparing(Function) comparing(propertyExtractor)}
     * as argument.
     *
     * @return the least of the two given objects.
     */
    public static <T, U extends Comparable<? super U>> T maxBy(Function<? super T, U> propertyExtractor, T t1, T t2) {
        return maxBy(comparing(propertyExtractor), t1, t2);
    }


    /**
     * Create a comparator which will use a given list of already prioritized elements
     * to determine the order of given elements. Two elements present in the prioritized list
     * are compared according to their position in the list. An element not present in the
     * elements is always considered as less prioritized than a present element. Two elements not present
     * are considered equal.
     *
     * @param elementsOrderedByPriority The elements which order is used to determine the order
     *                                  of elements given to the comparator.
     *
     * @return the comparator
     *
     * @see #prioritize(List)
     */
    @SafeVarargs
    @SuppressWarnings({"varargs"})
    public static <T> Comparator<T> prioritize(T ... elementsOrderedByPriority) {
        return prioritize(asList(elementsOrderedByPriority));
    }


    /**
     * Create a comparator which will use a given list of already prioritized elements
     * to determine the order of given elements. Two elements present in the prioritized list
     * are compared according to their position in the list. An element not present in the
     * elements is always considered as less prioritized than a present element. Two elements not present
     * are considered equal.
     *
     * @param elementsOrderedByPriority The elements which order is used to determine the order
     *                                  of elements given to the comparator.
     *
     * @return the comparator
     */
    public static <T> Comparator<T> prioritize(List<T> elementsOrderedByPriority) {
        return elementsOrderedByPriority.isEmpty() ? PrioritizeByIndex.noPrioritization() : new PrioritizeByIndex<>(elementsOrderedByPriority::indexOf);
    }


    /**
     * Create a comparator which will use a given list of predicates in prioritized order
     * to determine the order of given elements. The indexes of the first predicates which pass
     * two compared elements will determine the comparison result. An element not passing any of the predicates
     * is always considered as less prioritized than a passing element. Two elements not passing any predicate
     * are considered equal.
     *
     * @param predicatesOrderedByPriority The predicates which order is used to determine the order
     *                                    of elements given to the comparator.
     *
     * @return the comparator
     */
    public static <T> Comparator<T> prioritizeIf(List<? extends Predicate<? super T>> predicatesOrderedByPriority) {
        @SuppressWarnings("all")
        Predicate<T>[] targetArray = new Predicate[predicatesOrderedByPriority.size()];
        return prioritizeIf(predicatesOrderedByPriority.toArray(targetArray));
    }


    /**
     * Create a comparator which will use the given predicates in prioritized order
     * to determine the order of any given elements. The indexes of the first predicates which pass
     * two compared elements will determine the comparison result. An element not passing any of the predicates
     * is always considered as less prioritized than a passing element. Two elements not passing any predicate
     * are considered equal.
     *
     * @param predicatesOrderedByPriority The predicates which order is used to determine the order
     *                                    of elements given to the comparator.
     *
     * @return the comparator
     */
    @SafeVarargs
    public static <T> Comparator<T> prioritizeIf(Predicate<? super T> ... predicatesOrderedByPriority) {
        return predicatesOrderedByPriority.length == 0 ? PrioritizeByIndex.noPrioritization() : new PrioritizeByIndex<>(element -> {
            for (int index = 0; index < predicatesOrderedByPriority.length; index++) {
                if (predicatesOrderedByPriority[index].test(element)) {
                    return index;
                }
            }
            return -1;
        });
    }

    private static final class PrioritizeByIndex<T> implements Comparator<T> {

        @SuppressWarnings("unchecked")
        public static <T> Comparator<T> noPrioritization() {
            return (Comparator<T>) NO_PRIORITIZATION;
        }

        private static Comparator<?> NO_PRIORITIZATION = (t1, t2) -> 0;


        private final ToIntFunction<T> indexResolver;

        public PrioritizeByIndex(ToIntFunction<T> indexResolver) {
            this.indexResolver = indexResolver;
        }

        @Override
        public int compare(T t1, T t2) {
            int firstIndex = indexResolver.applyAsInt(t1);
            int secondIndex = indexResolver.applyAsInt(t2);
            if (firstIndex < 0 && secondIndex >= 0) {
                return 1;
            } else if (secondIndex < 0 && firstIndex >= 0) {
                return -1;
            } else {
                return Integer.compare(firstIndex, secondIndex);
            }
        }
    }


    private DiggCompare() {}
}
