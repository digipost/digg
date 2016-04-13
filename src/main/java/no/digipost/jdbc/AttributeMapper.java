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
import no.digipost.util.AttributeMap;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An attribute mapper associates a {@link ColumnMapper} with an {@link Attribute}, and
 * is used internally by an {@link AttributesRowMapper} to produce an {@link AttributeMap} where
 * the values can be accessed in a type-safe manner.
 * <p>
 * An {@link AttributeMapper} is constructed from an existing {@code ColumnMapper} by calling
 * {@link ColumnMapper#forAttribute(Attribute) .forAttribute(..)}.
 * <p>
 * Usually, instances are directly given to a constructor of an {@link AttributesRowMapper}.
 *
 * @param <R> The type of the {@code Attribute} and the result yielded from the {@code ColumnMapper}
 */
public final class AttributeMapper<R> {
    private final Attribute<R> attribute;
    private final ColumnMapper<R> mapper;

    AttributeMapper(Attribute<R> attribute, ColumnMapper<R> mapper) {
        this.attribute = attribute;
        this.mapper = mapper;
    }

    Tuple<Attribute<R>, R> map(ResultSet resultSet) throws SQLException {
        return attribute.withValue(mapper.map(attribute.name, resultSet));
    }

    String getAttributeName() {
        return attribute.name;
    }
}