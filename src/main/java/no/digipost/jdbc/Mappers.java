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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;

import static java.util.Optional.ofNullable;

/**
 * Various predefined mappers used to extract result(s) from a {@link ResultSet}.
 *
 * @see ColumnMapper
 * @see RowMapper
 */
public final class Mappers {

    /** @see ResultSet#getBoolean(String) */
    public static final BasicColumnMapper<Boolean> getBoolean = (name, rs) -> rs.getBoolean(name);

    /** @see ResultSet#getByte(String) */
    public static final BasicColumnMapper<Byte> getByte = (name, rs) -> rs.getByte(name);

    /** @see ResultSet#getInt(String) */
    public static final BasicColumnMapper<Integer> getInt = (name, rs) -> rs.getInt(name);

    /** @see ResultSet#getLong(String) */
    public static final BasicColumnMapper<Long> getLong = (name, rs) -> rs.getLong(name);

    /** @see ResultSet#getFloat(String) */
    public static final BasicColumnMapper<Float> getFloat = (name, rs) -> rs.getFloat(name);

    /** @see ResultSet#getDouble(String) */
    public static final BasicColumnMapper<Double> getDouble = (name, rs) -> rs.getDouble(name);


    /** @see ResultSet#getBigDecimal(String) */
    public static final BasicColumnMapper<BigDecimal> getBigDecimal = (name, rs) -> rs.getBigDecimal(name);
    /** @see ResultSet#getBigDecimal(String) */
    public static final NullableColumnMapper<BigDecimal> getNullableBigDecimal = (name, rs) -> ofNullable(rs.getBigDecimal(name));

    /** @see ResultSet#getBytes(String) */
    public static final BasicColumnMapper<byte[]> getBytes = (name, rs) -> rs.getBytes(name);
    /** @see ResultSet#getBytes(String) */
    public static final NullableColumnMapper<byte[]> getNullableBytes = (name, rs) -> ofNullable(rs.getBytes(name));

    /** @see ResultSet#getString(String) */
    public static final BasicColumnMapper<String> getString = (name, rs) -> rs.getString(name);
    /** @see ResultSet#getString(String) */
    public static final NullableColumnMapper<String> getNullableString = (name, rs) -> ofNullable(rs.getString(name));

    /** @see ResultSet#getURL(String) */
    public static final BasicColumnMapper<URL> getURL = (name, rs) -> rs.getURL(name);
    /** @see ResultSet#getURL(String) */
    public static final NullableColumnMapper<URL> getNullableURL = (name, rs) -> ofNullable(rs.getURL(name));

    /** @see ResultSet#getDate(String) */
    public static final BasicColumnMapper<Date> getDate = (name, rs) -> rs.getDate(name);
    /** @see ResultSet#getDate(String) */
    public static final NullableColumnMapper<Date> getNullableDate = (name, rs) -> ofNullable(rs.getDate(name));

    /** @see ResultSet#getTimestamp(String) */
    public static final BasicColumnMapper<Timestamp> getTimestamp = (name, rs) -> rs.getTimestamp(name);
    /** @see ResultSet#getTimestamp(String) */
    public static final NullableColumnMapper<Timestamp> getNullableTimestamp = (name, rs) -> ofNullable(rs.getTimestamp(name));

    /**
     * Combination of {@link #getTimestamp} and a conversion to an {@link Instant} using {@link Timestamp#toInstant()}.
     */
    public static final BasicColumnMapper<Instant> getInstant = getTimestamp.andThen(Timestamp::toInstant);
    /**
     * Combination of {@link #getNullableTimestamp} and a conversion to an {@link Instant}
     * using {@link Timestamp#toInstant()}.
     */
    public static final NullableColumnMapper<Instant> getNullableInstant = getNullableTimestamp.andThen(Timestamp::toInstant);


    /** @see ResultSet#getAsciiStream(String) */
    public static final BasicColumnMapper<InputStream> getAsciiStream = (name, rs) -> rs.getAsciiStream(name);
    /** @see ResultSet#getAsciiStream(String) */
    public static final NullableColumnMapper<InputStream> getNullableAsciiStream = (name, rs) -> ofNullable(rs.getAsciiStream(name));

    /** @see ResultSet#getBinaryStream(String) */
    public static final BasicColumnMapper<InputStream> getBinaryStream = (name, rs) -> rs.getBinaryStream(name);
    /** @see ResultSet#getBinaryStream(String) */
    public static final NullableColumnMapper<InputStream> getNullableBinaryStream = (name, rs) -> ofNullable(rs.getBinaryStream(name));

    /** @see ResultSet#getCharacterStream(String) */
    public static final BasicColumnMapper<Reader> getCharacterStream = (name, rs) -> rs.getCharacterStream(name);
    /** @see ResultSet#getCharacterStream(String) */
    public static final NullableColumnMapper<Reader> getNullableCharacterStream = (name, rs) -> ofNullable(rs.getCharacterStream(name));


    private Mappers() {}
}
