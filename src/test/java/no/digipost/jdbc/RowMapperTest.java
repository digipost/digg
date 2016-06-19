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

import no.digipost.tuple.*;
import no.digipost.util.Attribute;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static co.unruly.matchers.OptionalMatchers.contains;
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
        final Optional<File> avatar;

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
            this(name, age, memberSince, profileCompleteness, active, Optional.empty());
        }

        User(String name, int age, Instant memberSince, double profileCompleteness, boolean active, Optional<File> avatar) {
            this.name = name;
            this.age = age;
            this.memberSince = memberSince;
            this.profileCompleteness = profileCompleteness;
            this.active = active;
            this.avatar = avatar;
        }
    }

    final Attribute<String> name = new Attribute<>("name");
    final Attribute<Integer> age = new Attribute<>("age");
    final Attribute<Instant> memberSince = new Attribute<>("member_since");
    final Attribute<Double> profileCompleteness = new Attribute<>("profile_completeness");
    final Attribute<Boolean> active = new Attribute<>("active");
    final Attribute<Optional<File>> avatar = new Attribute<>("avatar");

    final RowMapper.Tupled<String, Integer> twoColumns =
            getString.forAttribute(name).combinedWith(getInt.forAttribute(age));

    final RowMapper.Tripled<String, Integer, Instant> threeColumns = twoColumns.combinedWith(
            getTimestamp.andThen(Timestamp::toInstant).forAttribute(memberSince));

    final RowMapper.Quadrupled<String, Integer, Instant, Double> fourColumns = threeColumns.combinedWith(
            getDouble.forAttribute(profileCompleteness));

    final RowMapper.Pentupled<String, Integer, Instant, Double, Boolean> fiveColumns = fourColumns.combinedWith(
            getBoolean.forAttribute(active));

    final RowMapper.Hextupled<String, Integer, Instant, Double, Boolean, Optional<File>> sixColumns = fiveColumns.combinedWith(
            getNullableString.andThen(File::new).forAttribute(avatar));


    private ResultSetMock rs;

    @Before
    public void mockDatabaseResultSet() {
        rs = mockSingleRowResult(
                Tuple.of(name.name, "John Doe"),
                Tuple.of(age.name, 30),
                Tuple.of(memberSince.name, Timestamp.from(EPOCH)),
                Tuple.of(profileCompleteness.name, 0.5),
                Tuple.of(active.name, true),
                Tuple.of(avatar.name, "johndoe.png"));
    }

    @Test
    public void combineTwoMappers() throws SQLException {
        Tuple<String, Integer> row = twoColumns.fromResultSet(rs);
        User user = twoColumns.andThen(User::new).fromResultSet(rs);

        assertThat(row.first(), both(is(user.name)).and(is("John Doe")));
        assertThat(row.second(), both(is(user.age)).and(is(30)));
    }

    @Test
    public void combineAndFlattenThreeMappers() throws SQLException {
        Triple<String, Integer, Instant> row = threeColumns.andThen(Triple::flatten).fromResultSet(rs);
        User user = threeColumns.andThen((n, a, ms) -> new User(n, a, ms)).fromResultSet(rs);

        assertThat(row.first(), both(is(user.name)).and(is("John Doe")));
        assertThat(row.second(), both(is(user.age)).and(is(30)));
        assertThat(row.third(), both(is(user.memberSince)).and(is(EPOCH)));
    }


    @Test
    public void combineAndFlattenFourMappers() throws SQLException {
        Quadruple<String, Integer, Instant, Double> row = fourColumns.andThen(Quadruple::flatten).fromResultSet(rs);
        User user = fourColumns.andThen((n, a, ms, pc) -> new User(n, a, ms, pc)).fromResultSet(rs);

        assertThat(row.first(), both(is(user.name)).and(is("John Doe")));
        assertThat(row.second(), both(is(user.age)).and(is(30)));
        assertThat(row.third(), both(is(user.memberSince)).and(is(EPOCH)));
        assertThat(row.fourth(), both(is(user.profileCompleteness)).and(is(0.5)));
    }

    @Test
    public void combineAndFlattenFiveMappers() throws SQLException {
        Pentuple<String, Integer, Instant, Double, Boolean> row = fiveColumns.andThen(Pentuple::flatten).fromResultSet(rs);
        User user = fiveColumns.andThen((n, a, ms, pc, act) -> new User(n, a, ms, pc, act)).fromResultSet(rs);

        assertThat(row.first(), both(is(user.name)).and(is("John Doe")));
        assertThat(row.second(), both(is(user.age)).and(is(30)));
        assertThat(row.third(), both(is(user.memberSince)).and(is(EPOCH)));
        assertThat(row.fourth(), both(is(user.profileCompleteness)).and(is(0.5)));
        assertThat(row.fifth(), both(is(user.active)).and(is(true)));
    }

    @Test
    public void combineAndFlattenSixMappers() throws SQLException {
        Hextuple<String, Integer, Instant, Double, Boolean, Optional<File>> row = sixColumns.andThen(Hextuple::flatten).fromResultSet(rs);
        User user = sixColumns.andThen((n, a, ms, pc, act, av) -> new User(n, a, ms, pc, act, av)).fromResultSet(rs);

        assertThat(row.first(), both(is(user.name)).and(is("John Doe")));
        assertThat(row.second(), both(is(user.age)).and(is(30)));
        assertThat(row.third(), both(is(user.memberSince)).and(is(EPOCH)));
        assertThat(row.fourth(), both(is(user.profileCompleteness)).and(is(0.5)));
        assertThat(row.fifth(), both(is(user.active)).and(is(true)));
        assertThat(row.sixth(), both(is(user.avatar)).and(contains(new File("johndoe.png"))));
    }


    @After
    public void closeDatabaseResultMock() {
        rs.close();
    }
}
