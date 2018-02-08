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

import org.junit.Test;
import org.quicktheories.core.Gen;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static com.google.common.io.ByteStreams.toByteArray;
import static no.digipost.DiggIO.limit;
import static no.digipost.io.LimitedInputStreamThrowingExceptionTest.toByteArrayUsingSingleByteReads;
import static org.junit.Assert.assertArrayEquals;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.generators.SourceDSL.strings;

public class LimitedInputStreamYieldingEofTest {

    @Test
    public void readsJustTheBytesWithoutReachingTheLimit() throws IOException {
        String data = "Hello World!";
        DataSize size = DataSize.bytes(data.getBytes().length);
        try (InputStream in1 = limit(new ByteArrayInputStream(data.getBytes()), size);
             InputStream in2 = limit(new ByteArrayInputStream(data.getBytes()), size)) {

            byte[] readBytes = toByteArray(in1);
            byte[] fromSingleByteReads = toByteArrayUsingSingleByteReads(in2, size);
            assertArrayEquals(data.getBytes(), readBytes);
            assertArrayEquals(readBytes, fromSingleByteReads);
        }
    }


    @Test
    public void neverReadsMoreThanTheSetLimit() {
        Gen<byte[]> byteData = strings().allPossible().ofLengthBetween(0, 256).map(s -> s.getBytes());
        Gen<DataSize> limits = integers().between(0, 16).map(DataSize::bytes);

        qt()
            .forAll(byteData, limits)
            .check((data, limit) -> {
                try (InputStream in1 = limit(new ByteArrayInputStream(data), limit);
                     InputStream in2 = limit(new ByteArrayInputStream(data), limit)) {
                    byte[] readBytes = toByteArray(in1);
                    byte[] fromSingleByteReads = toByteArrayUsingSingleByteReads(in2, DataSize.bytes(readBytes.length));
                    return readBytes.length <= limit.toBytes() &&
                           Arrays.equals(readBytes, fromSingleByteReads);
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            });
    }
}
