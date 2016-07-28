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
import no.digipost.util.AttributesMap;
import no.digipost.util.GetsNamedValue.NotFound;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static no.digipost.jdbc.Mappers.*;
import static no.digipost.jdbc.ResultSetMock.mockResult;
import static no.digipost.jdbc.ResultSetMock.mockSingleRowResult;
import static no.digipost.util.AttributesMap.Config.ALLOW_NULL_VALUES;
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
    static final Attribute<Optional<WithTimestamp>> myOptionalTimestamp = new Attribute<>("myNullableTimestamp");

    private final AttributesRowMapper rowMapper = new AttributesRowMapper(
            getString.forColumn(myString),
            getLong.forColumn(myLong),
            getLong.andThen(id -> (WithId) () -> id).forColumn(myReference));

    @Test
    public void resultSetWithNoApplicableColumns() throws SQLException {
        try (MockResultSet rs = mockResult(Tuple.of("a", asList("valueA1")), Tuple.of("b", asList("valueB1")))) {
            AttributesMap attributes = rowMapper.map(rs);
            assertTrue(attributes.isEmpty());
        }
    }

    @Test
    public void populatesSpecifiedAttributes() throws SQLException {
        try (MockResultSet rs = mockSingleRowResult(myString.withValue("a").map(a -> a.name, identity()), myLong.withValue(42L).map(a -> a.name, identity()))) {
            AttributesMap attributes = rowMapper.map(rs);
            assertThat(attributes.get(myString), is("a"));
            assertThat(attributes.get(myLong), is(42L));
            assertThat(attributes.size(), is(2));
        }
    }

    @Test
    public void populatesNon() {

    }

    @Test
    public void mapsResultToAnotherType() throws SQLException {
        try (MockResultSet rs = mockResult(myReference.withValue(() -> 42L).mapSecond(WithId::getId).map(a -> a.name, Arrays::asList))) {
            AttributesMap attributes = rowMapper.map(rs);
            assertThat(attributes.get(myReference).getId(), is(42L));
            assertThat(attributes.size(), is(1));
        }
    }

    @Test
    public void nullFallthrough() throws SQLException {
        AttributesRowMapper rowMapper = new AttributesRowMapper(ALLOW_NULL_VALUES,
                getTimestamp.nullFallthrough().andThen(id -> (WithTimestamp) () -> { throw new AssertionError(id); }).forColumn(myNullableTimestamp));
        try (MockResultSet rs = mockSingleRowResult(Tuple.of(myNullableTimestamp.name, null))) {
            AttributesMap attributes = rowMapper.map(rs);
            assertThat(attributes.get(myNullableTimestamp), nullValue());
            assertThat(attributes.size(), is(1));
        }
    }

    @Test
    public void nullableColumnMapper() throws SQLException {
        AttributesRowMapper rowMapper = new AttributesRowMapper(
                getNullableTimestamp.andThen(id -> (WithTimestamp) () -> id).forColumn(myOptionalTimestamp));
        try (MockResultSet rs = mockSingleRowResult(Tuple.of(myNullableTimestamp.name, null))) {
            AttributesMap attributes = rowMapper.map(rs);
            assertThat(attributes.get(myOptionalTimestamp), is(Optional.empty()));
            assertThat(attributes.size(), is(1));
        }
    }


    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void doesNotIncludeNullValuesByDefault() throws SQLException {
        try (MockResultSet rs = mockResult(myString.withValue(null).map(a -> a.name, Arrays::asList))) {
            AttributesMap attributes = rowMapper.map(rs);
            expectedException.expect(NotFound.class);
            attributes.get(myString);
        }
    }

    @Test
    public void canBeSetToAllowNullValues() throws SQLException {
        AttributesRowMapper rowMapper = new AttributesRowMapper(ALLOW_NULL_VALUES, getString.forColumn(myString));

        try (MockResultSet rs = mockResult(myString.withValue(null).map(a -> a.name, Arrays::asList))) {
            AttributesMap attributes = rowMapper.map(rs);
            assertThat(attributes.get(myString), nullValue());
        }
    }

    @Test
    public void combineMappers() throws SQLException {
        try (MockResultSet rs = mockResult(myString.withValue("x").map(a -> a.name, Arrays::asList))) {
            Attribute<Integer> fortyTwo = new Attribute<Integer>("fortyTwo");
            AttributesMap attributes = rowMapper
                    .combinedWith((r, n) -> AttributesMap.with(fortyTwo, 42).build())
                    .andThen(results -> AttributesMap.buildNew().and(results.first()).and(results.second()).build())
                    .map(rs);
            assertThat(attributes.get(myString), is("x"));
            assertThat(attributes.get(fortyTwo), is(42));
        }
    }
}
