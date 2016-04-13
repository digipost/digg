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
import no.digipost.util.Attribute;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

/**
 * A column mapper differ from a {@link RowMapper} in that it is also given a column label/name
 * together with the {@link ResultSet}, which can be used to process specific column(s).
 *
 * @param <R> The type of object this {@code ColumnMapper} yields from a {@code ResultSet}
 */
@FunctionalInterface
public interface ColumnMapper<R> {

    static <R> ColumnMapper<R> of(ThrowingBiFunction<String, ResultSet, R, ? extends SQLException> function) {
        return (name, rs) -> function.apply(name, rs);
    }

    R map(String name, ResultSet resultSet) throws SQLException;

    /**
     * Create a new {@code ColumnMapper} which will map the result of this one
     * using the given {@link Function function}.
     *
     * @param <S> The type of object the new {@code ColumnMapper} yields from a {@code ResultSet}
     * @param mapper the mapping function
     * @return the new mapper
     *
     * @see #nullFallthrough()
     */
    default <S> ColumnMapper<S> andThen(Function<? super R, S> mapper) {
        return (a, rs) -> mapper.apply(map(a, rs));
    }

    /**
     * Associate the column mapper with an {@link Attribute}.
     *
     * @return a new {@link AttributeMapper}
     */
    default AttributeMapper<R> forAttribute(Attribute<R> attribute) {
        return new AttributeMapper<R>(attribute, this);
    }

    /**
     * Specify that a retrieved {@code null} value will short circuit any
     * chain from this point. A mapping chain can be constructed with
     * {@link #andThen(Function)}, and if a value retrieved from a {@link ResultSet}
     * is <em>nullable</em>, usually this should pass unchanged through a chain of mappers
     * as {@code null}. This eliminates the need for the mapping functions to handle
     * {@code null}s.
     *
     * @return a new {@code ColumnMapper}
     */
    default ColumnMapper<R> nullFallthrough() {
        ColumnMapper<R> parent = this;
        return new ColumnMapper<R>() {
            @Override
            public R map(String name, ResultSet resultSet) throws SQLException {
                return parent.map(name, resultSet);
            }

            @Override
            public <S> ColumnMapper<S> andThen(Function<? super R, S> mapper) {
                return (a, rs) -> {
                    R maybeResult = map(a, rs);
                    return maybeResult != null ? mapper.apply(maybeResult) : null;
                };
            }
        };
    }
}