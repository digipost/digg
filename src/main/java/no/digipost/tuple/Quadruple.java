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
 * A quadruple is a simple composition of four arbitrary values (objects). A quadruple
 * captures no semantics of the four values, and they are only referred to as
 * "the first", "the second", "the third", and "the fourth" value.
 *
 * @param <T1> The type of the first value
 * @param <T2> The type of the second value
 * @param <T3> The type of the third value
 * @param <T4> The type of the fourth value
 */
public interface Quadruple<T1, T2, T3, T4> extends ViewableAsQuadruple<T1, T2, T3, T4> {

    static <T1, T2, T3, T4> Quadruple<T1, T2, T3, T4> of(T1 first, T2 second, T3 third, T4 fourth) {
        return new XTuple<>(first, second, third, fourth, TERMINATOR, null);
    }

    static <T1, T2, T3, T4> Quadruple<T1, T2, T3, T4> flatten(Tuple<Tuple<Tuple<T1, T2>, T3>, T4> nestedTuple) {
        return Quadruple.of(nestedTuple.first().first().first(),
                            nestedTuple.first().first().second(),
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
     * @return the fourth value
     */
    T4 fourth();


    /**
     * Create a new quadruple by applying a function to the first element, and putting the
     * result as the first element of the new quadruple.
     *
     * @param mapper the function to apply to the first element
     * @return the new quadruple
     */
    <S1> Quadruple<S1, T2, T3, T4> mapFirst(Function<? super T1, ? extends S1> mapper);


    /**
     * Create a new quadruple by applying a function to the second element, and putting the
     * result as the second element of the new quadruple.
     *
     * @param mapper the function to apply to the second element
     * @return the new quadruple
     */
    <S2> Quadruple<T1, S2, T3, T4> mapSecond(Function<? super T2, ? extends S2> mapper);


    /**
     * Create a new quadruple by applying a function to the third element, and putting the
     * result as the third element of the new quadruple.
     *
     * @param mapper the function to apply to the third element
     * @return the new quadruple
     */
    <S3> Quadruple<T1, T2, S3, T4> mapThird(Function<? super T3, ? extends S3> mapper);


    /**
     * Create a new quadruple by applying a function to the fourth element, and putting the
     * result as the fourth element of the new quadruple.
     *
     * @param mapper the function to apply to the fourth element
     * @return the new quadruple
     */
    <S4> Quadruple<T1, T2, T3, S4> mapFourth(Function<? super T4, ? extends S4> mapper);


    /**
     * Create a new quadruple by applying a function to each element, and putting the
     * results into a new quadruple.
     *
     * @param firstMapper  the function to apply to the first element
     * @param secondMapper the function to apply to the second element
     * @param thirdMapper  the function to apply to the third element
     * @param fourthMapper the function to apply to the fourth element
     * @return the new quadruple
     */
    <S1, S2, S3, S4> Quadruple<S1, S2, S3, S4> map(Function<? super T1, ? extends S1> firstMapper,
                                                   Function<? super T2, ? extends S2> secondMapper,
                                                   Function<? super T3, ? extends S3> thirdMapper,
                                                   Function<? super T4, ? extends S4> fourthMapper);


    /**
     * @return this quadruple instance
     */
    @Override
    Quadruple<T1, T2, T3, T4> asQuadruple();

}
