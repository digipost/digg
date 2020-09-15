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
import java.util.function.BinaryOperator;
import java.util.function.Function;

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

    private DiggCompare() {}
}
