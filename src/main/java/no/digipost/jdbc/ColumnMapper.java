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

import no.digipost.util.Attribute;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A column mapper differ from a {@link RowMapper} in that it is also given a column label/name
 * together with the {@link ResultSet}, which can be used to process specific column(s).
 *
 * @param <R> The type of object this {@code ColumnMapper} yields from a {@code ResultSet}
 *
 * @see BasicColumnMapper
 * @see NullableColumnMapper
 */
@FunctionalInterface
public interface ColumnMapper<R> {

    R map(String name, ResultSet resultSet) throws SQLException;


    /**
     * Associate the column mapper with an {@link Attribute}.
     *
     * @return a new {@link AttributeMapper}
     */
    default AttributeMapper<R> forAttribute(Attribute<R> attribute) {
        return new AttributeMapper<R>(attribute, this);
    }

}