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

/**
 * Various predefined mappers used to extract result(s) from a {@link ResultSet}.
 *
 * @see ColumnMapper
 * @see RowMapper
 */
public final class Mappers {

    public static final ColumnMapper<Boolean> getBoolean = (name, rs) -> rs.getBoolean(name);
    public static final ColumnMapper<Byte> getByte = (name, rs) -> rs.getByte(name);
    public static final ColumnMapper<byte[]> getBytes = (name, rs) -> rs.getBytes(name);
    public static final ColumnMapper<Integer> getInt = (name, rs) -> rs.getInt(name);
    public static final ColumnMapper<Long> getLong = (name, rs) -> rs.getLong(name);
    public static final ColumnMapper<Float> getFloat = (name, rs) -> rs.getFloat(name);
    public static final ColumnMapper<Double> getDouble = (name, rs) -> rs.getDouble(name);
    public static final ColumnMapper<BigDecimal> getBigDecimal = (name, rs) -> rs.getBigDecimal(name);

    public static final ColumnMapper<String> getString = (name, rs) -> rs.getString(name);
    public static final ColumnMapper<URL> getURL = (name, rs) -> rs.getURL(name);

    public static final ColumnMapper<Date> getDate = (name, rs) -> rs.getDate(name);
    public static final ColumnMapper<Timestamp> getTimestamp = (name, rs) -> rs.getTimestamp(name);
    public static final ColumnMapper<Instant> getInstant = getTimestamp.andThen(Timestamp::toInstant);

    public static final ColumnMapper<InputStream> getAsciiStream = (name, rs) -> rs.getAsciiStream(name);
    public static final ColumnMapper<InputStream> getBinaryStream = (name, rs) -> rs.getBinaryStream(name);
    public static final ColumnMapper<Reader> getCharacterStream = (name, rs) -> rs.getCharacterStream(name);


    private Mappers() {}
}
