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
package no.digipost.tuple;

import no.digipost.function.DecaFunction;
import no.digipost.function.HexaFunction;
import no.digipost.function.NonaFunction;
import no.digipost.function.OctoFunction;
import no.digipost.function.PentaFunction;
import no.digipost.function.QuadFunction;
import no.digipost.function.SeptiFunction;
import no.digipost.function.TriFunction;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.function.Function.identity;

final class XTuple<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> implements Tuple<T1, T2>,
                                                                       Triple<T1, T2, T3>,
                                                                       Quadruple<T1, T2, T3, T4>,
                                                                       Pentuple<T1, T2, T3, T4, T5>,
                                                                       Hextuple<T1, T2, T3, T4, T5, T6>,
                                                                       Septuple<T1, T2, T3, T4, T5, T6, T7>,
                                                                       Octuple<T1, T2, T3, T4, T5, T6, T7, T8>,
                                                                       Nonuple<T1, T2, T3, T4, T5, T6, T7, T8, T9>,
                                                                       Decuple<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>,
                                                                       Serializable {

    private static final long serialVersionUID = 1L;

    static final Serializable TERMINATOR = new Serializable() {
        private Object readResolve() {
            return TERMINATOR;
        }

        @Override
        public String toString() {
            return "[" + XTuple.class.getSimpleName() + "-terminator element. Seeing this text indicates a bug in the Digg library.";
        };
    };



    private final T1 _1;
    private final T2 _2;
    private final T3 _3;
    private final T4 _4;
    private final T5 _5;
    private final T6 _6;
    private final T7 _7;
    private final T8 _8;
    private final T9 _9;
    private final T10 _10;


    XTuple(T1 _1, T2 _2, T3 _3, T4 _4, T5 _5, T6 _6, T7 _7, T8 _8, T9 _9, T10 _10) {
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
        this._4 = _4;
        this._5 = _5;
        this._6 = _6;
        this._7 = _7;
        this._8 = _8;
        this._9 = _9;
        this._10 = _10;
    }

    @Override
    public T1 first() {
        return _1;
    }

    @Override
    public T2 second() {
        return _2;
    }

    @Override
    public T3 third() {
        return _3;
    }

    @Override
    public T4 fourth() {
        return _4;
    }

    @Override
    public T5 fifth() {
        return _5;
    }

    @Override
    public T6 sixth() {
        return _6;
    }

    @Override
    public T7 seventh() {
        return _7;
    }

    @Override
    public T8 eighth() {
        return _8;
    }

    @Override
    public T9 ninth() {
        return _9;
    }

    @Override
    public T10 tenth() {
        return _10;
    }


    @Override
    public <S1> XTuple<S1, T2, T3, T4, T5, T6, T7, T8, T9, T10> mapFirst(Function<? super T1, ? extends S1> mapper) {
        return map(mapper, identity());
    }

    @Override
    public <S2> XTuple<T1, S2, T3, T4, T5, T6, T7, T8, T9, T10> mapSecond(Function<? super T2, ? extends S2> mapper) {
        return map(identity(), mapper);
    }

    @Override
    public <S3> XTuple<T1, T2, S3, T4, T5, T6, T7, T8, T9, T10> mapThird(Function<? super T3, ? extends S3> mapper) {
        return map(identity(), identity(), mapper);
    }

    @Override
    public <S4> XTuple<T1, T2, T3, S4, T5, T6, T7, T8, T9, T10> mapFourth(Function<? super T4, ? extends S4> mapper) {
        return map(identity(), identity(), identity(), mapper);
    }

    @Override
    public <S5> XTuple<T1, T2, T3, T4, S5, T6, T7, T8, T9, T10> mapFifth(Function<? super T5, ? extends S5> mapper) {
        return map(identity(), identity(), identity(), identity(), mapper);
    }

    @Override
    public <S6> XTuple<T1, T2, T3, T4, T5, S6, T7, T8, T9, T10> mapSixth(Function<? super T6, ? extends S6> mapper) {
        return map(identity(), identity(), identity(), identity(), identity(), mapper);
    }

    @Override
    public <S7> XTuple<T1, T2, T3, T4, T5, T6, S7, T8, T9, T10> mapSeventh(Function<? super T7, ? extends S7> mapper) {
        return map(identity(), identity(), identity(), identity(), identity(), identity(), mapper);
    }

    @Override
    public <S8> XTuple<T1, T2, T3, T4, T5, T6, T7, S8, T9, T10> mapEighth(Function<? super T8, ? extends S8> mapper) {
        return map(identity(), identity(), identity(), identity(), identity(), identity(), identity(), mapper);
    }

    @Override
    public <S9> XTuple<T1, T2, T3, T4, T5, T6, T7, T8, S9, T10> mapNinth(Function<? super T9, ? extends S9> mapper) {
        return map(identity(), identity(), identity(), identity(), identity(), identity(), identity(), identity(), mapper);
    }

    @Override
    public <S10> XTuple<T1, T2, T3, T4, T5, T6, T7, T8, T9, S10> mapTenth(Function<? super T10, ? extends S10> mapper) {
        return map(identity(), identity(), identity(), identity(), identity(), identity(), identity(), identity(), identity(), mapper);
    }


    @Override
    public <S1, S2> XTuple<S1, S2, T3, T4, T5, T6, T7, T8, T9, T10> map(Function<? super T1, ? extends S1> firstMapper,
                                                                        Function<? super T2, ? extends S2> secondMapper) {
        return map(firstMapper, secondMapper, identity());
    }

    @Override
    public <S1, S2, S3> XTuple<S1, S2, S3, T4, T5, T6, T7, T8, T9, T10> map(Function<? super T1, ? extends S1> firstMapper,
                                                                            Function<? super T2, ? extends S2> secondMapper,
                                                                            Function<? super T3, ? extends S3> thirdMapper) {
        return map(firstMapper, secondMapper, thirdMapper, identity());
    }

    @Override
    public <S1, S2, S3, S4> XTuple<S1, S2, S3, S4, T5, T6, T7, T8, T9, T10> map(Function<? super T1, ? extends S1> firstMapper,
                                                                                Function<? super T2, ? extends S2> secondMapper,
                                                                                Function<? super T3, ? extends S3> thirdMapper,
                                                                                Function<? super T4, ? extends S4> fourthMapper) {
        return map(firstMapper, secondMapper, thirdMapper, fourthMapper, identity());
    }

    @Override
    public <S1, S2, S3, S4, S5> XTuple<S1, S2, S3, S4, S5, T6, T7, T8, T9, T10> map(Function<? super T1, ? extends S1> firstMapper,
                                                                                    Function<? super T2, ? extends S2> secondMapper,
                                                                                    Function<? super T3, ? extends S3> thirdMapper,
                                                                                    Function<? super T4, ? extends S4> fourthMapper,
                                                                                    Function<? super T5, ? extends S5> fifthMapper) {
        return map(firstMapper, secondMapper, thirdMapper, fourthMapper, fifthMapper, identity());
    }

    @Override
    public <S1, S2, S3, S4, S5, S6> XTuple<S1, S2, S3, S4, S5, S6, T7, T8, T9, T10> map(Function<? super T1, ? extends S1> firstMapper,
                                                                                        Function<? super T2, ? extends S2> secondMapper,
                                                                                        Function<? super T3, ? extends S3> thirdMapper,
                                                                                        Function<? super T4, ? extends S4> fourthMapper,
                                                                                        Function<? super T5, ? extends S5> fifthMapper,
                                                                                        Function<? super T6, ? extends S6> sixthMapper) {
        return map(firstMapper, secondMapper, thirdMapper, fourthMapper, fifthMapper, sixthMapper, identity());
    }

    @Override
    public <S1, S2, S3, S4, S5, S6, S7> XTuple<S1, S2, S3, S4, S5, S6, S7, T8, T9, T10> map(Function<? super T1, ? extends S1> firstMapper,
                                                                                            Function<? super T2, ? extends S2> secondMapper,
                                                                                            Function<? super T3, ? extends S3> thirdMapper,
                                                                                            Function<? super T4, ? extends S4> fourthMapper,
                                                                                            Function<? super T5, ? extends S5> fifthMapper,
                                                                                            Function<? super T6, ? extends S6> sixthMapper,
                                                                                            Function<? super T7, ? extends S7> seventhMapper) {
        return map(firstMapper, secondMapper, thirdMapper, fourthMapper, fifthMapper, sixthMapper, seventhMapper, identity());
    }

    @Override
    public <S1, S2, S3, S4, S5, S6, S7, S8> XTuple<S1, S2, S3, S4, S5, S6, S7, S8, T9, T10> map(Function<? super T1, ? extends S1> firstMapper,
                                                                                                Function<? super T2, ? extends S2> secondMapper,
                                                                                                Function<? super T3, ? extends S3> thirdMapper,
                                                                                                Function<? super T4, ? extends S4> fourthMapper,
                                                                                                Function<? super T5, ? extends S5> fifthMapper,
                                                                                                Function<? super T6, ? extends S6> sixthMapper,
                                                                                                Function<? super T7, ? extends S7> seventhMapper,
                                                                                                Function<? super T8, ? extends S8> eightMapper) {
        return map(firstMapper, secondMapper, thirdMapper, fourthMapper, fifthMapper, sixthMapper, seventhMapper, eightMapper, identity());
    }

    @Override
    public <S1, S2, S3, S4, S5, S6, S7, S8, S9> XTuple<S1, S2, S3, S4, S5, S6, S7, S8, S9, T10> map(Function<? super T1, ? extends S1> firstMapper,
                                                                                                    Function<? super T2, ? extends S2> secondMapper,
                                                                                                    Function<? super T3, ? extends S3> thirdMapper,
                                                                                                    Function<? super T4, ? extends S4> fourthMapper,
                                                                                                    Function<? super T5, ? extends S5> fifthMapper,
                                                                                                    Function<? super T6, ? extends S6> sixthMapper,
                                                                                                    Function<? super T7, ? extends S7> seventhMapper,
                                                                                                    Function<? super T8, ? extends S8> eightMapper,
                                                                                                    Function<? super T9, ? extends S9> ninthMapper) {
        return map(firstMapper, secondMapper, thirdMapper, fourthMapper, fifthMapper, sixthMapper, seventhMapper, eightMapper, ninthMapper, identity());
    }

    @Override
    public <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10> XTuple<S1, S2, S3, S4, S5, S6, S7, S8, S9, S10> map(Function<? super T1, ? extends S1> firstMapper,
                                                                                                         Function<? super T2, ? extends S2> secondMapper,
                                                                                                         Function<? super T3, ? extends S3> thirdMapper,
                                                                                                         Function<? super T4, ? extends S4> fourthMapper,
                                                                                                         Function<? super T5, ? extends S5> fifthMapper,
                                                                                                         Function<? super T6, ? extends S6> sixthMapper,
                                                                                                         Function<? super T7, ? extends S7> seventhMapper,
                                                                                                         Function<? super T8, ? extends S8> eightMapper,
                                                                                                         Function<? super T9, ? extends S9> ninthMapper,
                                                                                                         Function<? super T10, ? extends S10> tenthMapper) {
        return new XTuple<>(firstMapper.apply(_1),
                            secondMapper.apply(_2),
                            thirdMapper.apply(_3),
                            fourthMapper.apply(_4),
                            fifthMapper.apply(_5),
                            sixthMapper.apply(_6),
                            seventhMapper.apply(_7),
                            eightMapper.apply(_8),
                            ninthMapper.apply(_9),
                            tenthMapper.apply(_10));
    }


    @Override
    public Decuple<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> asDecuple() {
        return this;
    }

    @Override
    public Nonuple<T1, T2, T3, T4, T5, T6, T7, T8, T9> asNonuple() {
        return this;
    }

    @Override
    public Octuple<T1, T2, T3, T4, T5, T6, T7, T8> asOctuple() {
        return this;
    }

    @Override
    public Septuple<T1, T2, T3, T4, T5, T6, T7> asSeptuple() {
        return this;
    }

    @Override
    public Hextuple<T1, T2, T3, T4, T5, T6> asHextuple() {
        return this;
    }

    @Override
    public Pentuple<T1, T2, T3, T4, T5> asPentuple() {
        return this;
    }

    @Override
    public Quadruple<T1, T2, T3, T4> asQuadruple() {
        return this;
    }

    @Override
    public Triple<T1, T2, T3> asTriple() {
        return this;
    }

    @Override
    public Tuple<T1, T2> asTuple() {
        return this;
    }

    @Override
    public <R> R to(BiFunction<? super T1, ? super T2, R> convertor) {
        return convertor.apply(_1, _2);
    }

    @Override
    public <R> R to(TriFunction<? super T1, ? super T2, ? super T3, R> convertor) {
        return convertor.apply(_1, _2, _3);
    }

    @Override
    public <R> R to(QuadFunction<? super T1, ? super T2, ? super T3, ? super T4, R> convertor) {
        return convertor.apply(_1, _2, _3, _4);
    }

    @Override
    public <R> R to(PentaFunction<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, R> convertor) {
        return convertor.apply(_1, _2, _3, _4, _5);
    }

    @Override
    public <R> R to(HexaFunction<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, R> convertor) {
        return convertor.apply(_1, _2, _3, _4, _5, _6);
    }

    @Override
    public <R> R to(SeptiFunction<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, R> convertor) {
        return convertor.apply(_1, _2, _3, _4, _5, _6, _7);
    }

    @Override
    public <R> R to(OctoFunction<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, R> convertor) {
        return convertor.apply(_1, _2, _3, _4, _5, _6, _7, _8);
    }

    @Override
    public <R> R to(NonaFunction<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, R> convertor) {
        return convertor.apply(_1, _2, _3, _4, _5, _6, _7, _8, _9);
    }

    @Override
    public <R> R to(DecaFunction<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? super T10, R> convertor) {
        return convertor.apply(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof XTuple) {
            XTuple<?, ?, ?, ?, ?, ?, ?, ?, ?, ?> that = (XTuple<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
            return Objects.equals(this._1, that._1) &&
                   Objects.equals(this._2, that._2) &&
                   Objects.equals(this._3, that._3) &&
                   Objects.equals(this._4, that._4) &&
                   Objects.equals(this._5, that._5) &&
                   Objects.equals(this._6, that._6) &&
                   Objects.equals(this._7, that._7) &&
                   Objects.equals(this._8, that._8) &&
                   Objects.equals(this._9, that._9) &&
                   Objects.equals(this._10, that._10);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (Object element : new Object[] {_1, _2, _3, _4, _5, _6, _7, _8, _9, _10}) {
            if (element == TERMINATOR) break;
            joiner.add(String.valueOf(element));
        }
        return joiner.toString();
    }

}
