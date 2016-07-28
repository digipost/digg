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
package no.digipost.jdbc;

import no.digipost.function.*;
import no.digipost.tuple.Tuple;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Defines how to map a {@link java.sql.ResultSet} to an object.
 * The given {@code ResultSet} is expected to be positioned at the "current" row, i.e. the {@code RowMapper}
 * is not expected to be required to do any cursor placement before doing any value extraction.
 * <p>
 * <strong>Please see the depraction notes on {@link #fromResultSet(ResultSet, int)}.</strong>
 *
 * @param <R> The type of object this {@code RowMapper} yields from a {@code ResultSet}
 */
@FunctionalInterface
public interface RowMapper<R> {

    static <R> RowMapper<R> of(ThrowingFunction<ResultSet, R, SQLException> mapper) {
        return of((rs, n) -> mapper.apply(rs));
    }

    static <R> RowMapper<R> of(RowMapper<R> mapper) {
        return mapper;
    }

    /**
     * @deprecated This method was originally included as an intended convenience to be
     *             {@link FunctionalInterface}-signature compatible with Spring's {@code RowMapper},
     *             but the value of this has shown to be limited. This method will be removed, and
     *             the lambda signature of a {@link no.digipost.jdbc.RowMapper} will then be {@code ResultSet -> R}.
     *             Assigning an instance of this to Spring's {@code RowMapper} can be achieved with a
     *             lambda like this (omit types for the lambda arguments if you prefer):
     *             {@code org.springframework.jdbc.core.RowMapper<R> springMapper = (ResultSet rs, int n) -> diggRowMapper.map(rs);}
     */
    @Deprecated
    R fromResultSet(ResultSet resultSet, int rowNum) throws SQLException;

    /**
     * Obtain the result from the current row of a {@code ResultSet}.
     *
     * @param resultSet the {@link ResultSet}
     * @return the result
     *
     * @throws SQLException if any error happens when processing the {@link ResultSet}. May be if the name/label is not valid,
     *                      if a database access error occurs, or this method is called on a closed result set.
     */
    default R map(ResultSet resultSet) throws SQLException {
        return fromResultSet(resultSet, resultSet.getRow());
    }

    /**
     * @deprecated Use {@link #map(ResultSet)} instead.
     */
    @Deprecated
    default R fromResultSet(ResultSet resultSet) throws SQLException {
        return map(resultSet);
    }


    /**
     * Create a new mapper which takes the result of this mapper and applies
     * the given {@link Function} to yield another result.
     *
     * @param <S> the output type of the given {@code resultMapper} function,
     *            which also become the type of the result produced by the new
     *            row mapper.
     * @param resultMapper the function to apply to the result
     * @return the new row mapper
     */
    default <S> RowMapper<S> andThen(Function<? super R, S> resultMapper) {
        return (rs, n) -> resultMapper.apply(fromResultSet(rs, n));
    }


    /**
     * Create a new row mapper which first runs this mapper and then the given mapper, and
     * combines the two results into a tuple-type container.
     *
     * @param otherMapper the mapper to run in addition to this.
     * @return the new mapper
     */
    default <S> RowMapper.Tupled<R, S> combinedWith(RowMapper<S> otherMapper) {
        return (rs, n) -> Tuple.of(this.fromResultSet(rs, n), otherMapper.fromResultSet(rs, n));
    }




    interface Tupled<T, U> extends RowMapper<Tuple<T, U>> {

        /**
         * Create a new mapper which takes the two results of this mapper and applies
         * the given {@link BiFunction} to yield another result.
         *
         * @param <S> the output type of the given {@code resultMapper} function,
         *            which also become the type of the result produced by the new
         *            row mapper.
         * @param resultMapper the function to apply to the result
         * @return the new row mapper
         */
        default <S> RowMapper<S> andThen(BiFunction<? super T, ? super U, S> resultMapper) {
            return andThen(tu -> resultMapper.apply(tu.first(), tu.second()));
        }

        @Override
        default <V> RowMapper.Tripled<T, U, V> combinedWith(RowMapper<V> otherMapper) {
            return (rs, n) -> Tuple.of(this.fromResultSet(rs, n), otherMapper.fromResultSet(rs, n));
        }

    }

    interface Tripled<T, U, V> extends RowMapper.Tupled<Tuple<T, U>, V> {

        /**
         * Create a new mapper which takes the three results of this mapper and applies
         * the given {@link TriFunction} to yield another result.
         *
         * @param <S> the output type of the given {@code resultMapper} function,
         *            which also become the type of the result produced by the new
         *            row mapper.
         * @param resultMapper the function to apply to the result
         * @return the new row mapper
         */
        default <S> RowMapper<S> andThen(TriFunction<? super T, ? super U, ? super V, S> resultMapper) {
            return andThen(tuv -> {
                Tuple<T, U> tu = tuv.first();
                return resultMapper.apply(tu.first(), tu.second(), tuv.second());
            });
        }

        @Override
        default <W> RowMapper.Quadrupled<T, U, V, W> combinedWith(RowMapper<W> otherMapper) {
            return (rs, n) -> Tuple.of(this.fromResultSet(rs, n), otherMapper.fromResultSet(rs, n));
        }

    }

    interface Quadrupled<T, U, V, W> extends RowMapper.Tripled<Tuple<T, U>, V, W> {

        /**
         * Create a new mapper which takes the four results of this mapper and applies
         * the given {@link QuadFunction} to yield another result.
         *
         * @param <S> the output type of the given {@code resultMapper} function,
         *            which also become the type of the result produced by the new
         *            row mapper.
         * @param resultMapper the function to apply to the result
         * @return the new row mapper
         */
        default <S> RowMapper<S> andThen(QuadFunction<? super T, ? super U, ? super V, ? super W, S> resultMapper) {
            return andThen(tuvw -> {
                Tuple<Tuple<T, U>, V> tuv = tuvw.first();
                Tuple<T, U> tu = tuv.first();
                return resultMapper.apply(tu.first(), tu.second(), tuv.second(), tuvw.second());
            });
        }

        @Override
        default <X> RowMapper.Pentupled<T, U, V, W, X> combinedWith(RowMapper<X> otherMapper) {
            return (rs, n) -> Tuple.of(this.fromResultSet(rs, n), otherMapper.fromResultSet(rs, n));
        }

    }


    interface Pentupled<T, U, V, W, X> extends RowMapper.Quadrupled<Tuple<T, U>, V, W, X> {

        /**
         * Create a new mapper which takes the five results of this mapper and applies
         * the given {@link PentaFunction} to yield another result.
         *
         * @param <S> the output type of the given {@code resultMapper} function,
         *            which also become the type of the result produced by the new
         *            row mapper.
         * @param resultMapper the function to apply to the result
         * @return the new row mapper
         */
        default <S> RowMapper<S> andThen(PentaFunction<? super T, ? super U, ? super V, ? super W, ? super X, S> resultMapper) {
            return andThen(tuvwx -> {
                Tuple<Tuple<Tuple<T, U>, V>, W> tuvw = tuvwx.first();
                Tuple<Tuple<T, U>, V> tuv = tuvw.first();
                Tuple<T, U> tu = tuv.first();
                return resultMapper.apply(tu.first(), tu.second(), tuv.second(), tuvw.second(), tuvwx.second());
            });
        }

        @Override
        default <Z> RowMapper.Hextupled<T, U, V, W, X, Z> combinedWith(RowMapper<Z> otherMapper) {
            return (rs, n) -> Tuple.of(this.fromResultSet(rs, n), otherMapper.fromResultSet(rs, n));
        }

    }

    interface Hextupled<T, U, V, W, X, Z> extends RowMapper.Pentupled<Tuple<T, U>, V, W, X, Z> {

        /**
         * Create a new mapper which takes the six results of this mapper and applies
         * the given {@link HexaFunction} to yield another result.
         *
         * @param <S> the output type of the given {@code resultMapper} function,
         *            which also become the type of the result produced by the new
         *            row mapper.
         * @param resultMapper the function to apply to the result
         * @return the new row mapper
         */
        default <S> RowMapper<S> andThen(HexaFunction<? super T, ? super U, ? super V, ? super W, ? super X, ? super Z, S> resultMapper) {
            return andThen(tuvwxz -> {
                Tuple<Tuple<Tuple<Tuple<T, U>, V>, W>, X> tuvwx = tuvwxz.first();
                Tuple<Tuple<Tuple<T, U>, V>, W> tuvw = tuvwx.first();
                Tuple<Tuple<T, U>, V> tuv = tuvw.first();
                Tuple<T, U> tu = tuv.first();
                return resultMapper.apply(tu.first(), tu.second(), tuv.second(), tuvw.second(), tuvwx.second(), tuvwxz.second());
            });
        }

        @Override
        default <A> Septupled<T, U, V, W, X, Z, A> combinedWith(RowMapper<A> otherMapper) {
            return (rs, n) -> Tuple.of(this.fromResultSet(rs, n), otherMapper.fromResultSet(rs, n));
        }

    }

    interface Septupled<T, U, V, W, X, Z, A> extends RowMapper.Hextupled<Tuple<T, U>, V, W, X, Z, A> {

        /**
         * Create a new mapper which takes the seven results of this mapper and applies
         * the given {@link SeptiFunction} to yield another result.
         *
         * @param <S> the output type of the given {@code resultMapper} function,
         *            which also become the type of the result produced by the new
         *            row mapper.
         * @param resultMapper the function to apply to the result
         * @return the new row mapper
         */
        default <S> RowMapper<S> andThen(SeptiFunction<? super T, ? super U, ? super V, ? super W, ? super X, ? super Z, ? super A, S> resultMapper) {
            return andThen(tuvwxza -> {
                Tuple<Tuple<Tuple<Tuple<Tuple<T, U>, V>, W>, X>, Z> tuvwxz = tuvwxza.first();
                Tuple<Tuple<Tuple<Tuple<T, U>, V>, W>, X> tuvwx = tuvwxz.first();
                Tuple<Tuple<Tuple<T, U>, V>, W> tuvw = tuvwx.first();
                Tuple<Tuple<T, U>, V> tuv = tuvw.first();
                Tuple<T, U> tu = tuv.first();
                return resultMapper.apply(tu.first(), tu.second(), tuv.second(), tuvw.second(), tuvwx.second(), tuvwxz.second(), tuvwxza.second());
            });
        }

        @Override
        default <B> Octupled<T, U, V, W, X, Z, A, B> combinedWith(RowMapper<B> otherMapper) {
            return (rs, n) -> Tuple.of(this.fromResultSet(rs, n), otherMapper.fromResultSet(rs, n));
        }

    }

    interface Octupled<T, U, V, W, X, Z, A, B> extends RowMapper.Septupled<Tuple<T, U>, V, W, X, Z, A, B> {

        /**
         * Create a new mapper which takes the eight results of this mapper and applies
         * the given {@link OctoFunction} to yield another result.
         *
         * @param <S> the output type of the given {@code resultMapper} function,
         *            which also become the type of the result produced by the new
         *            row mapper.
         * @param resultMapper the function to apply to the result
         * @return the new row mapper
         */
        default <S> RowMapper<S> andThen(OctoFunction<? super T, ? super U, ? super V, ? super W, ? super X, ? super Z, ? super A, ? super B, S> resultMapper) {
            return andThen(tuvwxzab -> {
                Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<T, U>, V>, W>, X>, Z>, A> tuvwxza = tuvwxzab.first();
                Tuple<Tuple<Tuple<Tuple<Tuple<T, U>, V>, W>, X>, Z> tuvwxz = tuvwxza.first();
                Tuple<Tuple<Tuple<Tuple<T, U>, V>, W>, X> tuvwx = tuvwxz.first();
                Tuple<Tuple<Tuple<T, U>, V>, W> tuvw = tuvwx.first();
                Tuple<Tuple<T, U>, V> tuv = tuvw.first();
                Tuple<T, U> tu = tuv.first();
                return resultMapper.apply(tu.first(), tu.second(), tuv.second(), tuvw.second(), tuvwx.second(), tuvwxz.second(), tuvwxza.second(), tuvwxzab.second());
            });
        }

        @Override
        default <C> Nonupled<T, U, V, W, X, Z, A, B, C> combinedWith(RowMapper<C> otherMapper) {
            return (rs, n) -> Tuple.of(this.fromResultSet(rs, n), otherMapper.fromResultSet(rs, n));
        }

    }

    interface Nonupled<T, U, V, W, X, Z, A, B, C> extends RowMapper.Octupled<Tuple<T, U>, V, W, X, Z, A, B, C> {

        /**
         * Create a new mapper which takes the nine results of this mapper and applies
         * the given {@link NonaFunction} to yield another result.
         *
         * @param <S> the output type of the given {@code resultMapper} function,
         *            which also become the type of the result produced by the new
         *            row mapper.
         * @param resultMapper the function to apply to the result
         * @return the new row mapper
         */
        default <S> RowMapper<S> andThen(NonaFunction<? super T, ? super U, ? super V, ? super W, ? super X, ? super Z, ? super A, ? super B, ? super C, S> resultMapper) {
            return andThen(tuvwxzabc -> {
                Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<T, U>, V>, W>, X>, Z>, A>, B> tuvwxzab = tuvwxzabc.first();
                Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<T, U>, V>, W>, X>, Z>, A> tuvwxza = tuvwxzab.first();
                Tuple<Tuple<Tuple<Tuple<Tuple<T, U>, V>, W>, X>, Z> tuvwxz = tuvwxza.first();
                Tuple<Tuple<Tuple<Tuple<T, U>, V>, W>, X> tuvwx = tuvwxz.first();
                Tuple<Tuple<Tuple<T, U>, V>, W> tuvw = tuvwx.first();
                Tuple<Tuple<T, U>, V> tuv = tuvw.first();
                Tuple<T, U> tu = tuv.first();
                return resultMapper.apply(tu.first(), tu.second(), tuv.second(), tuvw.second(), tuvwx.second(), tuvwxz.second(), tuvwxza.second(), tuvwxzab.second(), tuvwxzabc.second());
            });
        }

        @Override
        default <D> Decupled<T, U, V, W, X, Z, A, B, C, D> combinedWith(RowMapper<D> otherMapper) {
            return (rs, n) -> Tuple.of(this.fromResultSet(rs, n), otherMapper.fromResultSet(rs, n));
        }

    }

    interface Decupled<T, U, V, W, X, Z, A, B, C, D> extends RowMapper.Nonupled<Tuple<T, U>, V, W, X, Z, A, B, C, D> {

        /**
         * Create a new mapper which takes the ten results of this mapper and applies
         * the given {@link DecaFunction} to yield another result.
         *
         * @param <S> the output type of the given {@code resultMapper} function,
         *            which also become the type of the result produced by the new
         *            row mapper.
         * @param resultMapper the function to apply to the result
         * @return the new row mapper
         */
        default <S> RowMapper<S> andThen(DecaFunction<? super T, ? super U, ? super V, ? super W, ? super X, ? super Z, ? super A, ? super B, ? super C, ? super D, S> resultMapper) {
            return andThen(tuvwxzabcd -> {
                Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<T, U>, V>, W>, X>, Z>, A>, B>, C> tuvwxzabc = tuvwxzabcd.first();
                Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<T, U>, V>, W>, X>, Z>, A>, B> tuvwxzab = tuvwxzabc.first();
                Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<T, U>, V>, W>, X>, Z>, A> tuvwxza = tuvwxzab.first();
                Tuple<Tuple<Tuple<Tuple<Tuple<T, U>, V>, W>, X>, Z> tuvwxz = tuvwxza.first();
                Tuple<Tuple<Tuple<Tuple<T, U>, V>, W>, X> tuvwx = tuvwxz.first();
                Tuple<Tuple<Tuple<T, U>, V>, W> tuvw = tuvwx.first();
                Tuple<Tuple<T, U>, V> tuv = tuvw.first();
                Tuple<T, U> tu = tuv.first();
                return resultMapper.apply(tu.first(), tu.second(), tuv.second(), tuvw.second(), tuvwx.second(), tuvwxz.second(), tuvwxza.second(), tuvwxzab.second(), tuvwxzabc.second(), tuvwxzabcd.second());
            });
        }

    }


}
