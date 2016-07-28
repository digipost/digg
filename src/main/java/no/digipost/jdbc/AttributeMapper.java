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

import no.digipost.tuple.Tuple;
import no.digipost.util.Attribute;
import no.digipost.util.AttributesMap;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An attribute mapper associates a {@link ColumnMapper} with an {@link Attribute}, and
 * is used internally by an {@link AttributesRowMapper} to produce an {@link AttributesMap} where
 * the values can be accessed in a type-safe manner.
 * <p>
 * An {@link AttributeMapper} is constructed from an existing {@code ColumnMapper} by calling
 * {@link ColumnMapper#forColumn(Attribute) .forColumn(..)}.
 * <p>
 * Instances may be directly given to a constructor of an {@link AttributesRowMapper}, but can
 * also be used as a standalone {@link RowMapper}.
 *
 * @param <R> The type of the {@code Attribute} and the result yielded from the {@code ColumnMapper}
 */
public final class AttributeMapper<R> implements RowMapper<R> {
    private final Attribute<R> attribute;
    private final ColumnMapper<R> mapper;

    AttributeMapper(Attribute<R> attribute, ColumnMapper<R> mapper) {
        this.attribute = attribute;
        this.mapper = mapper;
    }

    /**
     * For the current row of the given {@link ResultSet}, map the specific column(s) this {@code AttributeMapper} is set to handle.
     * The mapper does not move the cursor of the {@code ResultSet}.
     *
     * @param resultSet the {@link ResultSet} to retrieve necessary data from in order to yield
     *                  the result of type {@code R}.
     * @param rowNum the current row of the {@code ResultSet} cursor. Not used by this mapper.
     * @return the result
     */
    @Override
    public R fromResultSet(ResultSet resultSet, int rowNum) throws SQLException {
        return mapper.map(attribute.name, resultSet);
    }


    Tuple<Attribute<R>, R> attributeAndValue(ResultSet resultSet) throws SQLException {
        return attribute.withValue(map(resultSet));
    }

    String getAttributeName() {
        return attribute.name;
    }
}