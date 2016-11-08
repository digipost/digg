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

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.io.ByteStreams.toByteArray;
import static no.digipost.DiggIO.limit;
import static no.digipost.io.LimitedInputStreamThrowingExceptionTest.toByteArrayUsingSingleByteReads;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

@RunWith(JUnitQuickcheck.class)
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


    @Property
    public void neverReadsMoreThanTheSetLimit(String dataString, @InRange(minInt=0, maxInt=16) int byteLimit) throws IOException {
        byte[] data = dataString.getBytes();
        DataSize limit = DataSize.bytes(byteLimit);
        try (InputStream in1 = limit(new ByteArrayInputStream(data), limit);
             InputStream in2 = limit(new ByteArrayInputStream(data), limit)) {
            byte[] readBytes = toByteArray(in1);
            byte[] fromSingleByteReads = toByteArrayUsingSingleByteReads(in2, DataSize.bytes(readBytes.length));
            assertThat(readBytes.length, lessThanOrEqualTo(byteLimit));
            assertArrayEquals(readBytes, fromSingleByteReads);
        }
    }
}
