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
 * A nonuple is a simple composition of nine arbitrary values (objects). A nonuple
 * captures no semantics of the nine values, and they are only referred to as
 * "the first", "the second", "the third", "the fourth", "the fifth", "the sixth", "the seventh",
 * "the eighth", and "the ninth" value.
 *
 * @param <T1> The type of the first value
 * @param <T2> The type of the second value
 * @param <T3> The type of the third value
 * @param <T4> The type of the fourth value
 * @param <T5> The type of the fifth value
 * @param <T6> The type of the sixth value
 * @param <T7> The type of the seventh value
 * @param <T8> The type of the eighth value
 * @param <T9> The type of the ninth value
 */
public interface Nonuple<T1, T2, T3, T4, T5, T6, T7, T8, T9> extends ViewableAsNonuple<T1, T2, T3, T4, T5, T6, T7, T8, T9> {

    static <T1, T2, T3, T4, T5, T6, T7, T8, T9> Nonuple<T1, T2, T3, T4, T5, T6, T7, T8, T9> of(T1 first, T2 second, T3 third, T4 fourth, T5 fifth, T6 sixth, T7 seventh, T8 eighth, T9 ninth) {
        return new XTuple<>(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, TERMINATOR);
    }

    static <T1, T2, T3, T4, T5, T6, T7, T8, T9> Nonuple<T1, T2, T3, T4, T5, T6, T7, T8, T9> flatten(Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<T1, T2>, T3>, T4>, T5>, T6>, T7>, T8>, T9> nestedTuple) {
        return Nonuple.of(nestedTuple.first().first().first().first().first().first().first().first(),
                          nestedTuple.first().first().first().first().first().first().first().second(),
                          nestedTuple.first().first().first().first().first().first().second(),
                          nestedTuple.first().first().first().first().first().second(),
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
     * @return the sixth value
     */
    T6 sixth();

    /**
     * @return the seventh value
     */
    T7 seventh();

    /**
     * @return the eighth value
     */
    T8 eighth();

    /**
     * @return the ninth value
     */
    T9 ninth();


    /**
     * Create a new nonuple by applying a function to the first element, and putting the
     * result as the first element of the new nonuple.
     *
     * @param mapper the function to apply to the first element
     * @return the new nonuple
     */
    <S1> Nonuple<S1, T2, T3, T4, T5, T6, T7, T8, T9> mapFirst(Function<? super T1, ? extends S1> mapper);


    /**
     * Create a new nonuple by applying a function to the second element, and putting the
     * result as the second element of the new nonuple.
     *
     * @param mapper the function to apply to the second element
     * @return the new nonuple
     */
    <S2> Nonuple<T1, S2, T3, T4, T5, T6, T7, T8, T9> mapSecond(Function<? super T2, ? extends S2> mapper);


    /**
     * Create a new nonuple by applying a function to the third element, and putting the
     * result as the third element of the new nonuple.
     *
     * @param mapper the function to apply to the third element
     * @return the new nonuple
     */
    <S3> Nonuple<T1, T2, S3, T4, T5, T6, T7, T8, T9> mapThird(Function<? super T3, ? extends S3> mapper);


    /**
     * Create a new nonuple by applying a function to the fourth element, and putting the
     * result as the fourth element of the new nonuple.
     *
     * @param mapper the function to apply to the fourth element
     * @return the new nonuple
     */
    <S4> Nonuple<T1, T2, T3, S4, T5, T6, T7, T8, T9> mapFourth(Function<? super T4, ? extends S4> mapper);


    /**
     * Create a new nonuple by applying a function to the fifth element, and putting the
     * result as the fifth element of the new nonuple.
     *
     * @param mapper the function to apply to the fifth element
     * @return the new nonuple
     */
    <S5> Nonuple<T1, T2, T3, T4, S5, T6, T7, T8, T9> mapFifth(Function<? super T5, ? extends S5> mapper);


    /**
     * Create a new nonuple by applying a function to the sixth element, and putting the
     * result as the sixth element of the new nonuple.
     *
     * @param mapper the function to apply to the sixth element
     * @return the new nonuple
     */
    <S6> Nonuple<T1, T2, T3, T4, T5, S6, T7, T8, T9> mapSixth(Function<? super T6, ? extends S6> mapper);


    /**
     * Create a new nonuple by applying a function to the seventh element, and putting the
     * result as the seventh element of the new nonuple.
     *
     * @param mapper the function to apply to the seventh element
     * @return the new nonuple
     */
    <S7> Nonuple<T1, T2, T3, T4, T5, T6, S7, T8, T9> mapSeventh(Function<? super T7, ? extends S7> mapper);


    /**
     * Create a new nonuple by applying a function to the eighth element, and putting the
     * result as the eighth element of the new nonuple.
     *
     * @param mapper the function to apply to the eighth element
     * @return the new nonuple
     */
    <S8> Nonuple<T1, T2, T3, T4, T5, T6, T7, S8, T9> mapEighth(Function<? super T8, ? extends S8> mapper);


    /**
     * Create a new nonuple by applying a function to the ninth element, and putting the
     * result as the ninth element of the new nonuple.
     *
     * @param mapper the function to apply to the ninth element
     * @return the new nonuple
     */
    <S9> Nonuple<T1, T2, T3, T4, T5, T6, T7, T8, S9> mapNinth(Function<? super T9, ? extends S9> mapper);


    /**
     * Create a new nonuple by applying a function to each element, and putting the
     * results into a new nonuple.
     *
     * @param firstMapper   the function to apply to the first element
     * @param secondMapper  the function to apply to the second element
     * @param thirdMapper   the function to apply to the third element
     * @param fourthMapper  the function to apply to the fourth element
     * @param fifthMapper   the function to apply to the fifth element
     * @param sixthMapper   the function to apply to the sixth element
     * @param seventhMapper the function to apply to the seventh element
     * @param eighthMapper  the function to apply to the eighth element
     * @param ninthMapper   the function to apply to the ninth element
     * @return the new nonuple
     */
    <S1, S2, S3, S4, S5, S6, S7, S8, S9> Nonuple<S1, S2, S3, S4, S5, S6, S7, S8, S9> map(Function<? super T1, ? extends S1> firstMapper,
                                                                                         Function<? super T2, ? extends S2> secondMapper,
                                                                                         Function<? super T3, ? extends S3> thirdMapper,
                                                                                         Function<? super T4, ? extends S4> fourthMapper,
                                                                                         Function<? super T5, ? extends S5> fifthMapper,
                                                                                         Function<? super T6, ? extends S6> sixthMapper,
                                                                                         Function<? super T7, ? extends S7> seventhMapper,
                                                                                         Function<? super T8, ? extends S8> eighthMapper,
                                                                                         Function<? super T9, ? extends S9> ninthMapper);


    /**
     * @return this nonuple instance
     */
    @Override
    Nonuple<T1, T2, T3, T4, T5, T6, T7, T8, T9> asNonuple();

}
