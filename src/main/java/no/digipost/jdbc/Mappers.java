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
import java.sql.Array;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * Various predefined mappers used to extract result(s) from a {@link ResultSet}.
 *
 * @see ColumnMapper
 * @see RowMapper
 */
public final class Mappers {

    /**
     * Gets the value of a given column as a Java {@code boolean}. Unlike the {@link ResultSet#getBoolean(String)},
     * this mapper yields {@code null} for SQL {@code NULL}, as the returned value is a boxed {@link java.lang.Boolean}.
     * However, for <em>nullable</em> columns, the {@link #getNullableBoolean} mapper should be preferred over this.
     *
     * @see ResultSet#getBoolean(String)
     */
    public static final BasicColumnMapper<Boolean> getBoolean = (name, rs) -> {
        boolean value = rs.getBoolean(name);
        return rs.wasNull() ? null : value;
    };

    /** @see ResultSet#getBoolean(String) */
    public static final NullableColumnMapper<Boolean> getNullableBoolean = (name, rs) -> {
        boolean value = rs.getBoolean(name);
        return rs.wasNull() ? empty() : Optional.of(value);
    };

    /**
     * Gets the value of a given column as a Java {@code byte}. Unlike the {@link ResultSet#getByte(String)},
     * this mapper yields {@code null} for SQL {@code NULL}, as the returned value is a boxed {@link java.lang.Byte}.
     * However, for <em>nullable</em> columns, the {@link #getNullableByte} mapper should be preferred over this.
     *
     * @see ResultSet#getByte(String)
     */
    public static final BasicColumnMapper<Byte> getByte = (name, rs) -> {
        byte value = rs.getByte(name);
        return rs.wasNull() ? null : value;
    };

    /** @see ResultSet#getByte(String) */
    public static final NullableColumnMapper<Byte> getNullableByte = (name, rs) -> {
        byte value = rs.getByte(name);
        return rs.wasNull() ? empty() : Optional.of(value);
    };

    /**
     * Gets the value of a given column as a Java {@code short}. Unlike the {@link ResultSet#getShort(String)},
     * this mapper yields {@code null} for SQL {@code NULL}, as the returned value is a boxed {@link java.lang.Short}.
     * However, for <em>nullable</em> columns, the {@link #getNullableShort} mapper should be preferred over this.
     *
     * @see ResultSet#getShort(String)
     */
    public static final BasicColumnMapper<Short> getShort = (name, rs) -> {
        short value = rs.getShort(name);
        return rs.wasNull() ? null : value;
    };

    /** @see ResultSet#getShort(String) */
    public static final NullableColumnMapper<Short> getNullableShort = (name, rs) -> {
        short value = rs.getShort(name);
        return rs.wasNull() ? empty() : Optional.of(value);
    };

    /**
     * Gets the value of a given column as a Java {@code int}. Unlike the {@link ResultSet#getInt(String)},
     * this mapper yields {@code null} for SQL {@code NULL}, as the returned value is a boxed {@link java.lang.Integer}.
     * However, for <em>nullable</em> columns, the {@link #getNullableInt} mapper should be preferred over this.
     *
     * @see ResultSet#getInt(String)
     */
    public static final BasicColumnMapper<Integer> getInt = (name, rs) -> {
        int value = rs.getInt(name);
        return rs.wasNull() ? null : value;
    };

    /** @see ResultSet#getInt(String) */
    public static final NullableColumnMapper<Integer> getNullableInt = (name, rs) -> {
        int value = rs.getInt(name);
        return rs.wasNull() ? empty() : Optional.of(value);
    };

    /**
     * Gets the value of a given column as a Java {@code long}. Unlike the {@link ResultSet#getLong(String)},
     * this mapper yields {@code null} for SQL {@code NULL}, as the returned value is a boxed {@link java.lang.Long}.
     * However, for <em>nullable</em> columns, the {@link #getNullableLong} mapper should be preferred over this.
     *
     * @see ResultSet#getLong(String)
     */
    public static final BasicColumnMapper<Long> getLong = (name, rs) -> {
        long value = rs.getLong(name);
        return rs.wasNull() ? null : value;
    };

    /** @see ResultSet#getLong(String) */
    public static final NullableColumnMapper<Long> getNullableLong = (name, rs) -> {
        long value = rs.getLong(name);
        return rs.wasNull() ? empty() : Optional.of(value);
    };

    /**
     * Gets the value of a given column as a Java {@code float}. Unlike the {@link ResultSet#getFloat(String)},
     * this mapper yields {@code null} for SQL {@code NULL}, as the returned value is a boxed {@link java.lang.Float}.
     * However, for <em>nullable</em> columns, the {@link #getNullableFloat} mapper should be preferred over this.
     *
     * @see ResultSet#getFloat(String)
     */
    public static final BasicColumnMapper<Float> getFloat = (name, rs) -> {
        float value = rs.getFloat(name);
        return rs.wasNull() ? null : value;
    };

    /** @see ResultSet#getFloat(String) */
    public static final NullableColumnMapper<Float> getNullableFloat = (name, rs) -> {
        float value = rs.getFloat(name);
        return rs.wasNull() ? empty() : Optional.of(value);
    };

    /**
     * Gets the value of a given column as a Java {@code double}. Unlike the {@link ResultSet#getDouble(String)},
     * this mapper yields {@code null} for SQL {@code NULL}, as the returned value is a boxed {@link java.lang.Double}.
     * However, for <em>nullable</em> columns, the {@link #getNullableDouble} mapper should be preferred over this.
     *
     * @see ResultSet#getDouble(String)
     */
    public static final BasicColumnMapper<Double> getDouble = (name, rs) -> {
        double value = rs.getDouble(name);
        return rs.wasNull() ? null : value;
    };

    /** @see ResultSet#getDouble(String) */
    public static final NullableColumnMapper<Double> getNullableDouble = (name, rs) -> {
        double value = rs.getDouble(name);
        return rs.wasNull() ? empty() : Optional.of(value);
    };


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

    /** @see ResultSet#getArray(String) */
    public static final BasicColumnMapper<Array> getSqlArray = (name, rs) -> rs.getArray(name);

    /**
     * Gets the value of a given SQL {@code ARRAY} column as an uncasted Java array object. The result should be casted to
     * the applicable specific array type.
     *
     * @see #getStringArray
     * @see #getIntArray
     * @see #getLongArray
     */
    public static final BasicColumnMapper<Object> getArray = (name, rs) -> {
        Array sqlArray = getSqlArray.map(name, rs);
        try {
            return sqlArray.getArray();
        } finally {
            sqlArray.free();
        }
    };

    /**
     * Gets the value of a given SQL {@code ARRAY} column as an {@code String[]} array.
     */
    public static final BasicColumnMapper<String[]> getStringArray = getArray.andThen(String[].class::cast);

    /**
     * Gets the value of a given SQL {@code ARRAY} column as an {@code int[]} array.
     */
    public static final BasicColumnMapper<int[]> getIntArray = getArray.andThen(int[].class::cast);

    /**
     * Gets the value of a given SQL {@code ARRAY} column as an {@code long[]} array.
     */
    public static final BasicColumnMapper<long[]> getLongArray = getArray.andThen(long[].class::cast);


    private Mappers() {}
}
