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
 * A hextuple is a simple composition of five arbitrary values (objects). A hextuple
 * captures no semantics of the five values, and they are only referred to as
 * "the first", "the second", "the third", "the fourth", "the fifth", and "the sixth" value.
 *
 * @param <T1> The type of the first value
 * @param <T2> The type of the second value
 * @param <T3> The type of the third value
 * @param <T4> The type of the fourth value
 * @param <T5> The type of the fifth value
 * @param <T6> The type of the sixth value
 */
public interface Hextuple<T1, T2, T3, T4, T5, T6> extends ViewableAsHextuple<T1, T2, T3, T4, T5, T6> {

    static <T1, T2, T3, T4, T5, T6> Hextuple<T1, T2, T3, T4, T5, T6> of(T1 first, T2 second, T3 third, T4 fourth, T5 fifth, T6 sixth) {
        return new XTuple<>(first, second, third, fourth, fifth, sixth, TERMINATOR, null, null, null);
    }

    static <T1, T2, T3, T4, T5, T6> Hextuple<T1, T2, T3, T4, T5, T6> flatten(Tuple<Tuple<Tuple<Tuple<Tuple<T1, T2>, T3>, T4>, T5>, T6> nestedTuple) {
        return Hextuple.of(nestedTuple.first().first().first().first().first(),
                           nestedTuple.first().first().first().first().second(),
                           nestedTuple.first().first().first().second(),
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
     * @return the fifth value
     */
    T5 fifth();

    /**
     * @return the fifth value
     */
    T6 sixth();


    /**
     * Create a new hextuple by applying a function to the first element, and putting the
     * result as the first element of the new hextuple.
     *
     * @param mapper the function to apply to the first element
     * @return the new hextuple
     */
    <S1> Hextuple<S1, T2, T3, T4, T5, T6> mapFirst(Function<? super T1, ? extends S1> mapper);


    /**
     * Create a new hextuple by applying a function to the second element, and putting the
     * result as the second element of the new hextuple.
     *
     * @param mapper the function to apply to the second element
     * @return the new hextuple
     */
    <S2> Hextuple<T1, S2, T3, T4, T5, T6> mapSecond(Function<? super T2, ? extends S2> mapper);


    /**
     * Create a new hextuple by applying a function to the third element, and putting the
     * result as the third element of the new hextuple.
     *
     * @param mapper the function to apply to the third element
     * @return the new hextuple
     */
    <S3> Hextuple<T1, T2, S3, T4, T5, T6> mapThird(Function<? super T3, ? extends S3> mapper);


    /**
     * Create a new hextuple by applying a function to the fourth element, and putting the
     * result as the fourth element of the new hextuple.
     *
     * @param mapper the function to apply to the fourth element
     * @return the new hextuple
     */
    <S4> Hextuple<T1, T2, T3, S4, T5, T6> mapFourth(Function<? super T4, ? extends S4> mapper);


    /**
     * Create a new hextuple by applying a function to the fifth element, and putting the
     * result as the fifth element of the new hextuple.
     *
     * @param mapper the function to apply to the fifth element
     * @return the new hextuple
     */
    <S5> Hextuple<T1, T2, T3, T4, S5, T6> mapFifth(Function<? super T5, ? extends S5> mapper);


    /**
     * Create a new hextuple by applying a function to the sixth element, and putting the
     * result as the sixth element of the new hextuple.
     *
     * @param mapper the function to apply to the sixth element
     * @return the new hextuple
     */
    <S6> Hextuple<T1, T2, T3, T4, T5, S6> mapSixth(Function<? super T6, ? extends S6> mapper);


    /**
     * Create a new hextuple by applying a function to each element, and putting the
     * results into a new hextuple.
     *
     * @param firstMapper  the function to apply to the first element
     * @param secondMapper the function to apply to the second element
     * @param thirdMapper  the function to apply to the third element
     * @param fourthMapper the function to apply to the fourth element
     * @param fifthMapper  the function to apply to the fifth element
     * @param sixthMapper  the function to apply to the sixth element
     * @return the new hextuple
     */
    <S1, S2, S3, S4, S5, S6> Hextuple<S1, S2, S3, S4, S5, S6> map(Function<? super T1, ? extends S1> firstMapper,
                                                                  Function<? super T2, ? extends S2> secondMapper,
                                                                  Function<? super T3, ? extends S3> thirdMapper,
                                                                  Function<? super T4, ? extends S4> fourthMapper,
                                                                  Function<? super T5, ? extends S5> fifthMapper,
                                                                  Function<? super T6, ? extends S6> sixthMapper);


    /**
     * @return this hextuple instance
     */
    @Override
    Hextuple<T1, T2, T3, T4, T5, T6> asHextuple();

}
