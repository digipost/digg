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
package no.digipost.io;

import java.io.Serializable;
import java.util.Objects;

import static no.digipost.io.DataSizeUnit.BYTES;
import static no.digipost.io.DataSizeUnit.KILOBYTES;
import static no.digipost.io.DataSizeUnit.MEGABYTES;

public final class DataSize implements Serializable, Comparable<DataSize> {

    private static final long serialVersionUID = 1L;

    public static final DataSize ZERO = new DataSize(0);
    public static final DataSize MAX = new DataSize(Long.MAX_VALUE);

    public static DataSize of(long value, DataSizeUnit unit) {
        if (value == 0) {
            return ZERO;
        } else if (unit.asBytes(value) == MAX.bytes) {
            return MAX;
        } else if (value < 0) {
            throw new IllegalArgumentException("Size can not be negative! (" + value + " " + unit + ")");
        }
        return new DataSize(unit.asBytes(value));
    }

    public static DataSize bytes(long value) {
        return of(value, BYTES);
    }

    public static DataSize kB(long value) {
        return of(value, KILOBYTES);
    }

    public static DataSize MB(long value) {
        return of(value, MEGABYTES);
    }



    private final long bytes;

    private DataSize(long bytes) {
        this.bytes = bytes;
    }

    public double get(DataSizeUnit unit) {
        return unit.fromBytes(bytes);
    }

    public long toBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return String.format("%,d ", bytes) + DataSizeUnit.BYTES;
    }

    @Override
    public int compareTo(DataSize other) {
        return Long.compare(this.bytes, other.bytes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataSize) {
            DataSize that = (DataSize) obj;
            return Objects.equals(this.bytes, that.bytes);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(bytes);
    }

}
