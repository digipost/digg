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

import com.mockrunner.mock.jdbc.MockResultSet;
import no.digipost.tuple.Tuple;
import no.digipost.util.Attribute;
import no.digipost.util.AttributeMap;
import no.digipost.util.GetsNamedValue.NotFound;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static no.digipost.jdbc.Mappers.*;
import static no.digipost.util.AttributeMap.Config.ALLOW_NULL_VALUES;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AttributesRowMapperTest {
    @FunctionalInterface interface WithId { Long getId(); }
    @FunctionalInterface interface WithTimestamp { Timestamp getInstant(); }

    static final Attribute<String> myString = new Attribute<>("myString");
    static final Attribute<Long> myLong = new Attribute<>("myLong");
    static final Attribute<WithId> myReference = new Attribute<>("myReference");
    static final Attribute<WithTimestamp> myNullableTimestamp = new Attribute<>("myNullableTimestamp");

    private final AttributesRowMapper rowMapper = new AttributesRowMapper(
            getString.forAttribute(myString),
            getLong.forAttribute(myLong),
            getLong.andThen(id -> (WithId) () -> id).forAttribute(myReference));

    @Test
    public void resultSetWithNoApplicableColumns() throws SQLException {
        try (MockResultSet rs = populateResultSet(Tuple.of("a", asList("valueA1")), Tuple.of("b", asList("valueB1")))) {
            AttributeMap attributes = rowMapper.fromResultSet(rs);
            assertTrue(attributes.isEmpty());
        }
    }

    @Test
    public void populatesSpecifiedAttributes() throws SQLException {
        try (MockResultSet rs = populateResultSet(myString.withValue("a").map(a -> a.name, Arrays::asList), myLong.withValue(42L).map(a -> a.name, Arrays::asList))) {
            AttributeMap attributes = rowMapper.fromResultSet(rs);
            assertThat(attributes.get(myString), is("a"));
            assertThat(attributes.get(myLong), is(42L));
            assertThat(attributes.size(), is(2));
        }
    }

    @Test
    public void mapsResultToAnotherType() throws SQLException {
        try (MockResultSet rs = populateResultSet(myReference.withValue(() -> 42L).mapSecond(WithId::getId).map(a -> a.name, Arrays::asList))) {
            AttributeMap attributes = rowMapper.fromResultSet(rs);
            assertThat(attributes.get(myReference).getId(), is(42L));
            assertThat(attributes.size(), is(1));
        }
    }

    @Test
    public void nullFallthrough() throws SQLException {
        AttributesRowMapper rowMapper = new AttributesRowMapper(ALLOW_NULL_VALUES,
                getTimestamp.nullFallthrough().andThen(id -> (WithTimestamp) () -> { throw new AssertionError(id); }).forAttribute(myNullableTimestamp));
        try (MockResultSet rs = populateResultSet(Tuple.of(myNullableTimestamp.name, asList(new Object[]{null})))) {
            AttributeMap attributes = rowMapper.fromResultSet(rs);
            assertThat(attributes.get(myNullableTimestamp), nullValue());
            assertThat(attributes.size(), is(1));
        }
    }


    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void doesNotIncludeNullValuesByDefault() throws SQLException {
        try (MockResultSet rs = populateResultSet(myString.withValue(null).map(a -> a.name, Arrays::asList))) {
            AttributeMap attributes = rowMapper.fromResultSet(rs);
            expectedException.expect(NotFound.class);
            attributes.get(myString);
        }
    }

    @Test
    public void canBeSetToAllowNullValues() throws SQLException {
        AttributesRowMapper rowMapper = new AttributesRowMapper(ALLOW_NULL_VALUES, getString.forAttribute(myString));

        try (MockResultSet rs = populateResultSet(myString.withValue(null).map(a -> a.name, Arrays::asList))) {
            AttributeMap attributes = rowMapper.fromResultSet(rs);
            assertThat(attributes.get(myString), nullValue());
        }
    }

    @Test
    public void combineMappers() throws SQLException {
        try (MockResultSet rs = populateResultSet(myString.withValue("x").map(a -> a.name, Arrays::asList))) {
            Tuple<AttributeMap, Integer> attributes = rowMapper.combinedWith((r, n) -> 42).fromResultSet(rs);
            assertThat(attributes.first().get(myString), is("x"));
            assertThat(attributes.second(), is(42));
        }
    }


    @SafeVarargs
    private final MockResultSet populateResultSet(Tuple<String, List<Object>> ... columns) throws SQLException {
        MockResultSet rs = new MockResultSet("mock-" + LocalDate.now());
        for (Tuple<String, List<Object>> col : columns) {
            rs.addColumn(col.first(), col.second());
        }
        rs.next();
        return rs;
    }
}
