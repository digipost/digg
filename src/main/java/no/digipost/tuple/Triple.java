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
package no.digipost.tuple;

import java.util.function.Function;

import static no.digipost.tuple.XTuple.TERMINATOR;

/**
 * A triple is a simple composition of three arbitrary values (objects). A triple
 * captures no semantics of the three values, and they are only referred to as
 * "the first", "the second", and "the third" value.
 *
 * @param <T1> The type of the first value
 * @param <T2> The type of the second value
 * @param <T3> The type of the third value
 */
public interface Triple<T1, T2, T3> extends ViewableAsTriple<T1, T2, T3> {

    static <T1, T2, T3> Triple<T1, T2, T3> of(T1 first, T2 second, T3 third) {
        return new XTuple<>(first, second, third, TERMINATOR, null, null);
    }

    static <T1, T2, T3> Triple<T1, T2, T3> flatten(Tuple<Tuple<T1, T2>, T3> nestedTuple) {
        return Triple.of(nestedTuple.first().first(),
                         nestedTuple.first().second(),
                         nestedTuple.second());
    }

    /**
     * @return the first value
     */
    T1 first();

    /**
     * @return the second value
     */
    T2 second();

    /**
     * @return the third value
     */
    T3 third();


    /**
     * Create a new triple by applying a function to the first element, and putting the
     * result as the first element of the new triple.
     *
     * @param mapper the function to apply to the first element
     * @return the new triple
     */
    <S1> Triple<S1, T2, T3> mapFirst(Function<? super T1, ? extends S1> mapper);


    /**
     * Create a new triple by applying a function to the second element, and putting the
     * result as the second element of the new triple.
     *
     * @param mapper the function to apply to the second element
     * @return the new triple
     */
    <S2> Triple<T1, S2, T3> mapSecond(Function<? super T2, ? extends S2> mapper);


    /**
     * Create a new triple by applying a function to the third element, and putting the
     * result as the third element of the new triple.
     *
     * @param mapper the function to apply to the third element
     * @return the new triple
     */
    <S3> Triple<T1, T2, S3> mapThird(Function<? super T3, ? extends S3> mapper);


    /**
     * Create a new triple by applying a function to each element, and putting the
     * results into a new triple.
     *
     * @param firstMapper  the function to apply to the first element
     * @param secondMapper the function to apply to the second element
     * @param thirdMapper  the function to apply to the third element
     * @return the new triple
     */
    <S1, S2, S3> Triple<S1, S2, S3> map(Function<? super T1, ? extends S1> firstMapper,
                                        Function<? super T2, ? extends S2> secondMapper,
                                        Function<? super T3, ? extends S3> thirdMapper);


    /**
     * @return this triple instance
     */
    @Override
    Triple<T1, T2, T3> asTriple();
}
