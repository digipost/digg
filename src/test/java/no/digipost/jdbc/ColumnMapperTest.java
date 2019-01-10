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

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static co.unruly.matchers.OptionalMatchers.contains;
import static co.unruly.matchers.OptionalMatchers.empty;
import static java.util.function.Function.identity;
import static no.digipost.jdbc.ResultSetMock.mockResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ColumnMapperTest {

    @Test
    public void basicColumnMapperPassesNullDownTheMappingChain() throws SQLException {
        ColumnMapper<?> mapper = BasicColumnMapper.of((__, ___) -> null).andThen(identity()).andThen(identity());
        assertThat(mapper.map(null, mockResult()), nullValue());
    }

    @Test
    public void basicColumnMapperCanShortCircuitNulls() throws SQLException {
        ColumnMapper<?> mapper = BasicColumnMapper.of((__, ___) -> null).andThen(identity())
                .nullFallthrough().andThen(__ -> { throw new AssertionError("Should not be called"); });
        assertThat(mapper.map(null, mockResult()), nullValue());
    }

    @Test
    public void nullableColumnMapperAlwaysShortCurcuitsNull() throws SQLException {
        ColumnMapper<Optional<String>> mapper = NullableColumnMapper
                .of((__, ___) -> null).andThen(__ -> { throw new AssertionError("Should not be called"); });
        assertThat(mapper.map(null, mockResult()), empty());
    }

    @Test
    public void nullableColumnMapperShortCurcuitsNullFromComposedMappingFunction() throws SQLException {
        ColumnMapper<Optional<String>> mapper = NullableColumnMapper.of((__, ___) -> "a")
                .andThen(__ -> null).andThen(__ -> { throw new AssertionError("Should not be called"); });
        assertThat(mapper.map(null, mockResult()), empty());
    }

    @Test
    public void basicColumnMapperChain() throws SQLException {
        ColumnMapper<String> mapper = BasicColumnMapper.of((__, ___) -> -42).nullFallthrough()
                .andThen(Math::abs).andThen(String::valueOf);
        assertThat(mapper.map(null, mockResult()), is("42"));
    }

    @Test
    public void nullableColumnMapperChain() throws SQLException {
        ColumnMapper<Optional<String>> mapper = NullableColumnMapper.of((__, ___) -> -42).andThen(Math::abs).andThen(String::valueOf).andThen(identity());
        assertThat(mapper.map(null, mockResult()), contains("42"));
    }
}
