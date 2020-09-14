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
package no.digipost.jdbc;

import no.digipost.function.ThrowingBiFunction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;

/**
 * A {@code ColumnMapper} which is apropriate for retrieving {@code nullable} columns
 * from a {@link ResultSet}. If the result from a mapping (or any {@link Function} in the mapping chain)
 * is {@code null}, the result is always {@link Optional#empty()}, and the chain of any remaining
 * mapping functions are "short-circuited" and not invoked.
 *
 * @param <R> The type of the {@link Optional} result.
 */
@FunctionalInterface
public interface NullableColumnMapper<R> extends ColumnMapper<Optional<R>> {

    static <R> NullableColumnMapper<R> of(ThrowingBiFunction<String, ResultSet, R, ? extends SQLException> function) {
        return (name, rs) -> function.andThen(Optional::ofNullable).apply(name, rs);
    }


    /**
     * Create a new {@code NullableColumnMapper} which will map the result of this one
     * using the given {@link Function function}.
     *
     * @param <S> The type of {@code Optional<S>} the new mapper yields from a {@code ResultSet}
     * @param mapper the mapping function. This will not be called if the result of the mapping chain is already
     *               {@link Optional#empty()}.
     * @return the new mapper
     */
    default <S> NullableColumnMapper<S> andThen(Function<? super R, S> mapper) {
        return (name, rs) -> this.map(name, rs).map(mapper);
    }

}
