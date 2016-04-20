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
import no.digipost.tuple.Quadruple;
import no.digipost.tuple.Triple;
import no.digipost.tuple.Tuple;
import no.digipost.util.Attribute;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import static java.time.Instant.EPOCH;
import static no.digipost.jdbc.Mappers.*;
import static no.digipost.jdbc.ResultSetMock.mockSingleRowResult;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RowMapperTest {

    final Attribute<String> name = new Attribute<>("name");
    final Attribute<Integer> age = new Attribute<>("age");
    final Attribute<Instant> memeberSince = new Attribute<>("member_since");
    final Attribute<Double> profileCompleteness = new Attribute<>("profile_completeness");

    final RowMapper<Tuple<Tuple<String, Integer>, Instant>> mapper =
            getString.forAttribute(name).combinedWith(
            getInt.forAttribute(age)).combinedWith(
            getTimestamp.andThen(Timestamp::toInstant).forAttribute(memeberSince));

    @Test
    public void combineAndFlattenThreeMappers() throws SQLException {
        try (MockResultSet rs = mockSingleRowResult(Tuple.of("name", "John Doe"), Tuple.of("age", 30), Tuple.of("member_since", Timestamp.from(EPOCH)))) {
            Triple<String, Integer, Instant> row = mapper.andThen(Triple::flatten).fromResultSet(rs);
            assertThat(row.first(), is("John Doe"));
            assertThat(row.second(), is(30));
            assertThat(row.third(), is(EPOCH));
        }
    }


    @Test
    public void combineAndFlattenFourMappers() throws SQLException {
        try (MockResultSet rs = mockSingleRowResult(Tuple.of("name", "John Doe"), Tuple.of("age", 30), Tuple.of("member_since", Timestamp.from(EPOCH)), Tuple.of("profile_completeness", 0.5))) {
            Quadruple<String, Integer, Instant, Double> row = mapper.combinedWith(getDouble.forAttribute(profileCompleteness)).andThen(Quadruple::flatten).fromResultSet(rs);
            assertThat(row.first(), is("John Doe"));
            assertThat(row.second(), is(30));
            assertThat(row.third(), is(EPOCH));
            assertThat(row.fourth(), is(0.5));
        }
    }
}
