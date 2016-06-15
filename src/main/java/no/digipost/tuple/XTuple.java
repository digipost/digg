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

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;

final class XTuple<T1, T2, T3, T4, T5> implements Tuple<T1, T2>, Triple<T1, T2, T3>, Quadruple<T1, T2, T3, T4>, Quintuple<T1, T2, T3, T4, T5>, Serializable {

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

    XTuple(T1 _1, T2 _2, T3 _3, T4 _4, T5 _5) {
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
        this._4 = _4;
        this._5 = _5;
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
    public <S1> XTuple<S1, T2, T3, T4, T5> mapFirst(Function<? super T1, ? extends S1> mapper) {
        return map(mapper, Function.identity());
    }

    @Override
    public <S2> XTuple<T1, S2, T3, T4, T5> mapSecond(Function<? super T2, ? extends S2> mapper) {
        return map(Function.identity(), mapper);
    }

    @Override
    public <S3> XTuple<T1, T2, S3, T4, T5> mapThird(Function<? super T3, ? extends S3> mapper) {
        return map(Function.identity(), Function.identity(), mapper);
    }

    @Override
    public <S4> XTuple<T1, T2, T3, S4, T5> mapFourth(Function<? super T4, ? extends S4> mapper) {
        return map(Function.identity(), Function.identity(), Function.identity(), mapper);
    }

    @Override
    public <S5> XTuple<T1, T2, T3, T4, S5> mapFifth(Function<? super T5, ? extends S5> mapper) {
        return map(Function.identity(), Function.identity(), Function.identity(), Function.identity(), mapper);
    }


    @Override
    public <S1, S2> XTuple<S1, S2, T3, T4, T5> map(Function<? super T1, ? extends S1> firstMapper,
                                                   Function<? super T2, ? extends S2> secondMapper) {
        return map(firstMapper, secondMapper, Function.identity());
    }

    @Override
    public <S1, S2, S3> XTuple<S1, S2, S3, T4, T5> map(Function<? super T1, ? extends S1> firstMapper,
                                                       Function<? super T2, ? extends S2> secondMapper,
                                                       Function<? super T3, ? extends S3> thirdMapper) {
        return map(firstMapper, secondMapper, thirdMapper, Function.identity());
    }

    @Override
    public <S1, S2, S3, S4> XTuple<S1, S2, S3, S4, T5> map(Function<? super T1, ? extends S1> firstMapper,
                                                           Function<? super T2, ? extends S2> secondMapper,
                                                           Function<? super T3, ? extends S3> thirdMapper,
                                                           Function<? super T4, ? extends S4> fourthMapper) {
        return map(firstMapper, secondMapper, thirdMapper, fourthMapper, Function.identity());
    }

    @Override
    public <S1, S2, S3, S4, S5> XTuple<S1, S2, S3, S4, S5> map(Function<? super T1, ? extends S1> firstMapper,
                                                               Function<? super T2, ? extends S2> secondMapper,
                                                               Function<? super T3, ? extends S3> thirdMapper,
                                                               Function<? super T4, ? extends S4> fourthMapper,
                                                               Function<? super T5, ? extends S5> fifthMapper) {
        return new XTuple<>(firstMapper.apply(first()), secondMapper.apply(second()), thirdMapper.apply(_3), fourthMapper.apply(_4), fifthMapper.apply(_5));
    }


    @Override
    public Quintuple<T1, T2, T3, T4, T5> asQuintuple() {
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
    public boolean equals(Object obj) {
        if (obj instanceof XTuple) {
            XTuple<?, ?, ?, ?, ?> that = (XTuple<?, ?, ?, ?, ?>) obj;
            return Objects.equals(this._1, that._1) &&
                   Objects.equals(this._2, that._2) &&
                   Objects.equals(this._3, that._3) &&
                   Objects.equals(this._4, that._4) &&
                   Objects.equals(this._5, that._5);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_1, _2, _3, _4, _5);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (Object element : new Object[] {_1, _2, _3, _4, _5}) {
            if (element == TERMINATOR) break;
            joiner.add(String.valueOf(element));
        }
        return joiner.toString();
    }

}
