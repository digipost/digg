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

import com.mockrunner.mock.jdbc.MockResultSet;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.co.probablyfine.matchers.StreamMatchers.allMatch;
import static no.digipost.DiggExceptions.applyUnchecked;
import static no.digipost.jdbc.Mappers.getBoolean;
import static no.digipost.jdbc.Mappers.getByte;
import static no.digipost.jdbc.Mappers.getDouble;
import static no.digipost.jdbc.Mappers.getFloat;
import static no.digipost.jdbc.Mappers.getInt;
import static no.digipost.jdbc.Mappers.getLong;
import static no.digipost.jdbc.Mappers.getNullableBoolean;
import static no.digipost.jdbc.Mappers.getNullableByte;
import static no.digipost.jdbc.Mappers.getNullableDouble;
import static no.digipost.jdbc.Mappers.getNullableFloat;
import static no.digipost.jdbc.Mappers.getNullableInt;
import static no.digipost.jdbc.Mappers.getNullableLong;
import static no.digipost.jdbc.Mappers.getNullableShort;
import static no.digipost.jdbc.Mappers.getShort;
import static no.digipost.jdbc.ResultSetMock.mockSingleColumnResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class MappersTest {

    @Test
    public void recognizesSQL_NULLsInPrimitiveMappers() throws SQLException {
        try (MockResultSet rs = mockSingleColumnResult("value", new Object[] { null })) {
            Stream<Optional<?>> results =
                    Stream.of(getNullableInt, getNullableBoolean, getNullableByte, getNullableDouble, getNullableFloat, getNullableLong, getNullableShort)
                        .map((NullableColumnMapper<?> nullableMapper) -> applyUnchecked(mapper -> mapper.map("value", rs), nullableMapper));
            assertThat(results, allMatch(is(Optional.empty())));
        }
    }

    @Test
    public void correctlyHandlesResultSetIckySQL_NULLHandling() throws SQLException {
        try (MockResultSet rs = mockSingleColumnResult("value", new Object[] { null })) {
            Stream<Object> results =
                    Stream.of(getInt, getBoolean, getByte, getDouble, getFloat, getLong, getShort)
                        .map((BasicColumnMapper<?> basicMapper) -> applyUnchecked(mapper -> mapper.map("value", rs), basicMapper));
            assertThat(results, allMatch(nullValue()));
        }
    }
}
