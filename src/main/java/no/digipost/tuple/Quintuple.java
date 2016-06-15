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

/**
 * A quintuple is a simple composition of five arbitrary values (objects). A quintuple
 * captures no semantics of the five values, and they are only referred to as
 * "the first", "the second", "the third", "the fourth", and "the fifth" value.
 *
 * @param <T1> The type of the first value
 * @param <T2> The type of the second value
 * @param <T3> The type of the third value
 * @param <T4> The type of the fourth value
 * @param <T5> The type of the fifth value
 */
public interface Quintuple<T1, T2, T3, T4, T5> extends ViewableAsQuintuple<T1, T2, T3, T4, T5> {

    static <T1, T2, T3, T4, T5> Quintuple<T1, T2, T3, T4, T5> of(T1 first, T2 second, T3 third, T4 fourth, T5 fifth) {
        return new XTuple<>(first, second, third, fourth, fifth);
    }

    static <T1, T2, T3, T4, T5> Quintuple<T1, T2, T3, T4, T5> flatten(Tuple<Tuple<Tuple<Tuple<T1, T2>, T3>, T4>, T5> nestedTuple) {
        return Quintuple.of(nestedTuple.first().first().first().first(), nestedTuple.first().first().first().second(), nestedTuple.first().first().second(), nestedTuple.first().second(), nestedTuple.second());
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
     * @return the fifth value
     */
    T5 fifth();


    /**
     * Create a new quintuple by applying a function to the first element, and putting the
     * result as the first element of the new quintuple.
     *
     * @param mapper the function to apply to the first element
     * @return the new quintuple
     */
    <S1> Quintuple<S1, T2, T3, T4, T5> mapFirst(Function<? super T1, ? extends S1> mapper);


    /**
     * Create a new quintuple by applying a function to the second element, and putting the
     * result as the second element of the new quintuple.
     *
     * @param mapper the function to apply to the second element
     * @return the new quintuple
     */
    <S2> Quintuple<T1, S2, T3, T4, T5> mapSecond(Function<? super T2, ? extends S2> mapper);


    /**
     * Create a new quintuple by applying a function to the third element, and putting the
     * result as the third element of the new quintuple.
     *
     * @param mapper the function to apply to the third element
     * @return the new quintuple
     */
    <S3> Quintuple<T1, T2, S3, T4, T5> mapThird(Function<? super T3, ? extends S3> mapper);


    /**
     * Create a new quintuple by applying a function to the fourth element, and putting the
     * result as the fourth element of the new quintuple.
     *
     * @param mapper the function to apply to the fourth element
     * @return the new quintuple
     */
    <S4> Quintuple<T1, T2, T3, S4, T5> mapFourth(Function<? super T4, ? extends S4> mapper);


    /**
     * Create a new quintuple by applying a function to the fifth element, and putting the
     * result as the fifth element of the new quintuple.
     *
     * @param mapper the function to apply to the fifth element
     * @return the new quintuple
     */
    <S5> Quintuple<T1, T2, T3, T4, S5> mapFifth(Function<? super T5, ? extends S5> mapper);


    /**
     * Create a new quintuple by applying a function to each element, and putting the
     * results into a new quintuple.
     *
     * @param firstMapper  the function to apply to the first element
     * @param secondMapper the function to apply to the second element
     * @param thirdMapper  the function to apply to the third element
     * @param fourthMapper the function to apply to the fourth element
     * @param fifthMapper  the function to apply to the fifth element
     * @return the new quintuple
     */
    <S1, S2, S3, S4, S5> Quintuple<S1, S2, S3, S4, S5> map(Function<? super T1, ? extends S1> firstMapper,
                                                           Function<? super T2, ? extends S2> secondMapper,
                                                           Function<? super T3, ? extends S3> thirdMapper,
                                                           Function<? super T4, ? extends S4> fourthMapper,
                                                           Function<? super T5, ? extends S5> fifthMapper);


    /**
     * @return this quintuple instance
     */
    @Override
    Quintuple<T1, T2, T3, T4, T5> asQuintuple();

}
