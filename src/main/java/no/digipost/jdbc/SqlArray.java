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

import no.digipost.function.ThrowingFunction;

import java.sql.Array;
import java.sql.SQLException;

final class SqlArray {

    static SqlArray of(java.sql.Array array) {
        return new SqlArray(array);
    }


    private final Array array;

    private SqlArray(Array array) {
        this.array = array;
    }

    /**
     * Consume the contained {@link java.sql.Array} to produce a result, and ensure
     * {@link Array#free()} is invoked.
     *
     * @param <R> The result type,
     * @param sqlArrayConsumer the function to produce a result from the {@code Array}.
     *
     * @return the result from the given {@code sqlArrayConsumer}.
     */
    <R> R consume(ThrowingFunction<Array, R, SQLException> sqlArrayConsumer) throws SQLException {
        try {
            return sqlArrayConsumer.apply(array);
        } finally {
            array.free();
        }
    }

}
