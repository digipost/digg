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
import no.digipost.tuple.Pentuple;
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
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RowMapperTest {

    static class User {
        final String name;
        final int age;
        final Instant memberSince;
        final double profileCompleteness;
        final boolean active;

        User(String name, int age) {
            this(name, age, Instant.now());
        }

        User(String name, int age, Instant memberSince) {
            this(name, age, memberSince, 0);
        }

        User(String name, int age, Instant memberSince, double profileCompleteness) {
            this(name, age, memberSince, profileCompleteness, false);
        }

        User(String name, int age, Instant memberSince, double profileCompleteness, boolean active) {
            this.name = name;
            this.age = age;
            this.memberSince = memberSince;
            this.profileCompleteness = profileCompleteness;
            this.active = active;
        }
    }

    final Attribute<String> name = new Attribute<>("name");
    final Attribute<Integer> age = new Attribute<>("age");
    final Attribute<Instant> memberSince = new Attribute<>("member_since");
    final Attribute<Double> profileCompleteness = new Attribute<>("profile_completeness");
    final Attribute<Boolean> active = new Attribute<>("active");

    final RowMapper.Tupled<String, Integer> twoColumns =
            getString.forAttribute(name).combinedWith(getInt.forAttribute(age));

    final RowMapper.Tripled<String, Integer, Instant> threeColumns = twoColumns.combinedWith(
            getTimestamp.andThen(Timestamp::toInstant).forAttribute(memberSince));

    final RowMapper.Quadrupled<String, Integer, Instant, Double> fourColumns = threeColumns.combinedWith(
            getDouble.forAttribute(profileCompleteness));

    final RowMapper.Pentupled<String, Integer, Instant, Double, Boolean> fiveColumns = fourColumns.combinedWith(
            getBoolean.forAttribute(active));

    @Test
    public void combineTwoMappers() throws SQLException {
        try (MockResultSet rs = mockSingleRow()) {
            Tuple<String, Integer> row = twoColumns.fromResultSet(rs);
            User user = twoColumns.andThen(User::new).fromResultSet(rs);
            assertThat(row.first(), both(is(user.name)).and(is("John Doe")));
            assertThat(row.second(), both(is(user.age)).and(is(30)));
        }
    }

    @Test
    public void combineAndFlattenThreeMappers() throws SQLException {
        try (MockResultSet rs = mockSingleRow()) {
            Triple<String, Integer, Instant> row = threeColumns.andThen(Triple::flatten).fromResultSet(rs);
            User user = threeColumns.andThen((name, age, memberSince) -> new User(name, age, memberSince)).fromResultSet(rs);
            assertThat(row.first(), both(is(user.name)).and(is("John Doe")));
            assertThat(row.second(), both(is(user.age)).and(is(30)));
            assertThat(row.third(), both(is(user.memberSince)).and(is(EPOCH)));
        }
    }


    @Test
    public void combineAndFlattenFourMappers() throws SQLException {
        try (MockResultSet rs = mockSingleRow()) {
            Quadruple<String, Integer, Instant, Double> row = fourColumns.andThen(Quadruple::flatten).fromResultSet(rs);
            User user = fourColumns.andThen((name, age, memberSince, profileCompleteness) -> new User(name, age, memberSince, profileCompleteness)).fromResultSet(rs);
            assertThat(row.first(), both(is(user.name)).and(is("John Doe")));
            assertThat(row.second(), both(is(user.age)).and(is(30)));
            assertThat(row.third(), both(is(user.memberSince)).and(is(EPOCH)));
            assertThat(row.fourth(), both(is(user.profileCompleteness)).and(is(0.5)));
        }
    }

    @Test
    public void combineAndFlattenFiveMappers() throws SQLException {
        try (MockResultSet rs = mockSingleRow()) {
            Pentuple<String, Integer, Instant, Double, Boolean> row = fiveColumns.andThen(Pentuple::flatten).fromResultSet(rs);
            User user = fiveColumns.andThen((name, age, memberSince, profileCompleteness, active) -> new User(name, age, memberSince, profileCompleteness, active)).fromResultSet(rs);
            assertThat(row.first(), both(is(user.name)).and(is("John Doe")));
            assertThat(row.second(), both(is(user.age)).and(is(30)));
            assertThat(row.third(), both(is(user.memberSince)).and(is(EPOCH)));
            assertThat(row.fourth(), both(is(user.profileCompleteness)).and(is(0.5)));
            assertThat(row.fifth(), both(is(user.active)).and(is(true)));
        }
    }


    private MockResultSet mockSingleRow() throws SQLException {
        return mockSingleRowResult(
                Tuple.of(name.name, "John Doe"),
                Tuple.of(age.name, 30),
                Tuple.of(memberSince.name, Timestamp.from(EPOCH)),
                Tuple.of(profileCompleteness.name, 0.5),
                Tuple.of(active.name, true));
    }
}
