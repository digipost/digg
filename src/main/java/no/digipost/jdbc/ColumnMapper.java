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

import no.digipost.util.Attribute;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A {@code ColumnMapper} differ from a {@link RowMapper} in that it is also given a column label/name
 * together with the {@link ResultSet}, which can be used to process specific column(s).
 * Typically, a {@code ColumnMapper} defines how a extract the value from a <em>certain type of columns</em>,
 * say {@code INT}, {@code VARCHAR}, or {@code TIMESTAMP} columns. Mappers for these value types and other common basic
 * mappers can be found in the {@link Mappers} class. A {@code ColumnMapper} can be specialized to handle one specific column
 * with {@link #forColumn(Attribute)}, which turns it into a {@link RowMapper}.
 *
 * @param <R> The type of object this {@code ColumnMapper} yields from a {@code ResultSet}
 *
 * @see BasicColumnMapper
 * @see NullableColumnMapper
 */
@FunctionalInterface
public interface ColumnMapper<R> {

    /**
     * Extract a value from a {@link ResultSet} based on the given {@code name}. Usually the name is the
     * column name, or column label if an SQL {@code AS} clause was used in the query. The given {@code ResultSet}
     * is expected to be positioned at the "current" row, i.e. the {@code ColumnMapper} is not expected to be
     * required to do any cursor placement before doing any value extraction.
     *
     * @param name the name of the value to extract, usually the column name, or column label if an
     *             SQL {@code AS} clause was used in the query.
     * @param resultSet the {@code ResultSet} to extract from.
     * @return The reulting extracted value.
     *
     * @throws SQLException if any error happens when processing the {@link ResultSet}. May be if the name/label is not valid,
     *                      if a database access error occurs, or this method is called on a closed result set.
     */
    R map(String name, ResultSet resultSet) throws SQLException;


    /**
     * Associate the column mapper with an {@link Attribute}. This will create a row mapper
     * for the column named by the given {@code attribute}.
     *
     * @return a new {@link AttributeMapper}, which is a row mapper.
     */
    default AttributeMapper<R> forColumn(Attribute<R> attribute) {
        return new AttributeMapper<R>(attribute, this);
    }

}