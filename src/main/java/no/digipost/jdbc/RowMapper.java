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

    R fromResultSet(ResultSet resultSet, int rowNum) throws SQLException;

    /**
     * Obtain the result from the current row of a {@code ResultSet}.
     *
     * @param resultSet the {@link ResultSet}
     * @return the result
     *
     * @throws SQLException if an error happen when retrieving data from the {@code ResultSet}.
     */
    default R fromResultSet(ResultSet resultSet) throws SQLException {
        return fromResultSet(resultSet, resultSet.getRow());
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

    }


}
