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

public enum DataSizeUnit {

    /**
     * The byte unit.
     */
    BYTES(1, "bytes"),

    /**
     * The kilobyte unit, i.e. 1024 {@link #BYTES}.
     */
    KILOBYTES(BYTES.numberOfBytes * 1024, "kB"),

    /**
     * The Megabyte unit, i.e. 1024 {@link #KILOBYTES}, i.e. 1 048 576 bytes.
     */
    MEGABYTES(KILOBYTES.numberOfBytes * 1024, "MB"),

    /**
     * The Gigabyte unit, i.e. 1024 {@link #MEGABYTES }, i.e. 1 048 576 kilobytes.
     */
    GIGABYTES(MEGABYTES.numberOfBytes * 1024, "GB");

    /**
     * Alias for {@link #BYTES}.
     */
    public static final DataSizeUnit B = BYTES;

    /**
     * Alias for {@link #KILOBYTES}.
     */
    public static final DataSizeUnit kB = KILOBYTES;

    /**
     * Alias for {@link #MEGABYTES}
     */
    public static final DataSizeUnit MB = MEGABYTES;



    private final int numberOfBytes;
    private final String symbol;

    DataSizeUnit(int numberOfBytes, String symbol) {
        this.numberOfBytes = numberOfBytes;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }


    long asBytes(long value) {
        return value * this.numberOfBytes;
    }

    double fromBytes(long value) {
        return this == BYTES ? value : (double) value / this.numberOfBytes;
    }
}