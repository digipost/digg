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

import no.digipost.function.ThrowingBiFunction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

/**
 * The basic {@link ColumnMapper} retrieves a value from a {@link ResultSet}, and offers a way
 * to compose a chain of {@link Function functions} that maps the value. As the value retrieved
 * from the {@code ResultSet} is unconditionally passed down the chain of mapping functions, if
 * retrieving a value from a {@code nullable} column, consider using a {@link NullableColumnMapper}
 * instead.
 *
 * @param <R> The type of the result.
 * @see NullableColumnMapper
 */
@FunctionalInterface
public interface BasicColumnMapper<R> extends ColumnMapper<R> {

    static <R> BasicColumnMapper<R> of(ThrowingBiFunction<String, ResultSet, R, ? extends SQLException> function) {
        return (name, rs) -> function.apply(name, rs);
    }


    /**
     * Create a new {@code BasicColumnMapper} which will map the result of this one
     * using the given {@link Function function}.
     *
     * @param <S> The type of object the new {@code ColumnMapper} yields from a {@code ResultSet}
     * @param mapper the mapping function
     * @return the new mapper
     *
     * @see #nullFallthrough()
     */
    default <S> BasicColumnMapper<S> andThen(Function<? super R, S> mapper) {
        return (a, rs) -> mapper.apply(map(a, rs));
    }


    /**
     * Specify that a retrieved {@code null} value will short circuit any
     * chain from this point. A mapping chain can be constructed with
     * {@link #andThen(Function)}, and if a value retrieved from a {@link ResultSet}
     * is <em>nullable</em>, usually this should pass unchanged through a chain of mappers
     * as {@code null}. This eliminates the need for the mapping functions to handle
     * {@code null}s.
     * <p>
     * This should be used for <em>rare</em> circumstances where the result from a function passed to
     * {@link #andThen(Function)} can be {@code null}. If mapping a {@code nullable} database
     * column, consider using a {@link NullableColumnMapper} instead.
     *
     * @return a new {@code ColumnMapper}
     * @see NullableColumnMapper
     */
    default BasicColumnMapper<R> nullFallthrough() {
        ColumnMapper<R> parent = this;
        return new BasicColumnMapper<R>() {
            @Override
            public R map(String name, ResultSet resultSet) throws SQLException {
                return parent.map(name, resultSet);
            }

            @Override
            public <S> BasicColumnMapper<S> andThen(Function<? super R, S> mapper) {
                return (a, rs) -> {
                    R maybeResult = map(a, rs);
                    return maybeResult != null ? mapper.apply(maybeResult) : null;
                };
            }
        };
    }
}
