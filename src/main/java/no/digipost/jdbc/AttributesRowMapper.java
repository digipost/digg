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

import no.digipost.util.AttributesMap;
import no.digipost.util.AttributesMap.Builder;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.rangeClosed;
import static no.digipost.DiggExceptions.asUnchecked;

/**
 * A {@link RowMapper} producing {@link AttributesMap}s.
 */
public class AttributesRowMapper implements RowMapper<AttributesMap> {

    private final Supplier<Builder> attributeMapBuilderSupplier;
    private final Map<String, ? extends AttributeMapper<?>> mappers;

    public AttributesRowMapper(AttributeMapper<?> ... mappers) {
        this(AttributesMap::buildNew, mappers);
    }

    public AttributesRowMapper(Supplier<AttributesMap.Builder> attributeMapBuilderSupplier, AttributeMapper<?> ... mappers) {
        this(attributeMapBuilderSupplier, Stream.of(mappers));
    }

    public AttributesRowMapper(Stream<AttributeMapper<?>> mappers) {
        this(AttributesMap::buildNew, mappers);
    }

    public AttributesRowMapper(Supplier<AttributesMap.Builder> attributeMapBuilderSupplier, Stream<AttributeMapper<?>> mappers) {
        this.attributeMapBuilderSupplier = attributeMapBuilderSupplier;
        this.mappers = mappers.collect(toMap(AttributeMapper::getAttributeName, Function.identity()));
    }


    @Override
    public AttributesMap fromResultSet(ResultSet rs, int rowNum) throws SQLException {
        AttributesMap.Builder attributes = attributeMapBuilderSupplier.get();
        for(AttributeMapper<?> mapper : applicableMappers(rs.getMetaData()).collect(toList())) {
            attributes.and(mapper.attributeAndValue(rs));
        }
        return attributes.build();
    }

    private Stream<AttributeMapper<?>> applicableMappers(ResultSetMetaData metadata) throws SQLException {
        return rangeClosed(1, metadata.getColumnCount())
                .limit(metadata.getColumnCount())
                .mapToObj(i -> {
                    try {
                        return metadata.getColumnLabel(i);
                    } catch (SQLException e) {
                        throw asUnchecked(e);
                    }
                })
                .filter(mappers::containsKey)
                .map(mappers::get);
    }
}
