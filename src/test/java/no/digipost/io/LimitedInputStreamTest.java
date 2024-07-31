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
package no.digipost.io;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.quicktheories.core.Gen;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static no.digipost.DiggIO.limit;
import static no.digipost.io.DataSize.bytes;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.generators.SourceDSL.strings;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class LimitedInputStreamTest {

    @Nested
    class YieldingEofWhenReachingLimit {

        @Test
        void readsJustTheBytesWithoutReachingTheLimit() throws IOException {
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
        void neverReadsMoreThanTheSetLimit() {
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



    @Nested
    class ThrowingExceptionWhenReachingLimit {

        @Test
        public void readsAnInputStreamToTheEnd() throws Exception {
            assertThat(testLimitedStream("xyz", null), is("xyz"));
        }

        @Test
        public void throwsIOExceptionIfTooManyBytes() throws Exception {
            IOException tooManyBytes = new IOException();
            assertThat(assertThrows(IOException.class, () -> testLimitedStream("xyz", () -> tooManyBytes)), sameInstance(tooManyBytes));
        }

        @Test
        public void throwsRuntimeExceptionIfTooManyBytes() throws Exception {
            RuntimeException tooManyBytes = new IllegalStateException();
            assertThat(assertThrows(RuntimeException.class, () -> testLimitedStream("xyz", () -> tooManyBytes)), sameInstance(tooManyBytes));
        }

        @Test
        public void wrapsOtherCheckedExceptionsThanIOExceptionAsRuntimeException() throws Exception {
            GeneralSecurityException tooManyBytes = new DigestException();
            assertThat(assertThrows(RuntimeException.class, () -> testLimitedStream("xyz", () -> tooManyBytes)), where(Exception::getCause, sameInstance(tooManyBytes)));
        }

    }


    private static String testLimitedStream(String content, Supplier<? extends Exception> throwIfTooManyBytes) throws Exception {
        final byte[] contentBytes = content.getBytes(UTF_8);
        DataSize contentSize = DataSize.bytes(contentBytes.length);
        final InputStream limitedInputStream1;
        final InputStream limitedInputStream2;
        if (throwIfTooManyBytes == null) {
            limitedInputStream1 = limit(new ByteArrayInputStream(contentBytes), contentSize, () -> { throw new AssertionError("Not expected to fail"); });
            limitedInputStream2 = limit(new ByteArrayInputStream(contentBytes), contentSize, () -> { throw new AssertionError("Not expected to fail"); });
        } else {
            limitedInputStream1 = limit(new ByteArrayInputStream(contentBytes), bytes(contentSize.toBytes() - 1), throwIfTooManyBytes);
            limitedInputStream2 = limit(new ByteArrayInputStream(contentBytes), bytes(contentSize.toBytes() - 1), throwIfTooManyBytes);
        }

        Exception e1 = null;
        String readUsingByteBuffers = null;
        try (InputStream in = limitedInputStream1) {
            readUsingByteBuffers = new String(toByteArray(in), UTF_8);
        } catch (Exception e) {
            e1 = e;
        }

        Exception e2 = null;
        String readUsingSingleBytes = null;
        try (InputStream toRead = limitedInputStream2) {
            byte[] readBytes = toByteArrayUsingSingleByteReads(toRead, contentSize);
            readUsingSingleBytes = new String(readBytes, UTF_8);
        } catch (Exception e) {
            e2 = e;
        }

        if (e1 != null) {
            assertThat(e2, instanceOf(e1.getClass()));
            assertThat(e2, either(sameInstance(e1)).or(where(Exception::getCause, sameInstance(e1.getCause()))));
            throw e1;
        } else {
            assertThat(readUsingByteBuffers, equalTo(readUsingSingleBytes));
            return readUsingByteBuffers;
        }

    }

    private static byte[] toByteArrayUsingSingleByteReads(InputStream toRead, DataSize amountToRead) throws IOException {
        int next;
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) amountToRead.toBytes());
        while ((next = toRead.read()) != -1) {
            if (!byteBuffer.hasRemaining()) {
                break;
            }
            byteBuffer.put((byte) next);
        }
        byte[] readBytes = byteBuffer.array();
        return readBytes;
    }

    @Test
    void doesNotConsumeMoreFromUnderlyingInputStreamThanGivenLimit() throws IOException {
        byte[] readBytes = new byte[3];
        try (
                InputStream threeBytes = new ByteArrayInputStream(new byte[] {65, 66, 67});
                InputStream maxTwoBytes = limit(threeBytes, bytes(2))) {

            assertThat(maxTwoBytes.read(readBytes), is(2));
        }
        assertArrayEquals(new byte[] {65, 66, 0}, readBytes);
    }

    @Test
    void ableToResetBufferedStreamWhenLimitedStreamIsExhausted() throws IOException {
        byte[] oneKiloByte = new byte[1024];
        Arrays.fill(oneKiloByte, (byte) 65);

        byte[] readFromLimitedStream = new byte[800];
        byte[] readFromBufferedStream = new byte[oneKiloByte.length];
        int limit = 600;
        try (
                InputStream source = new ByteArrayInputStream(oneKiloByte);
                InputStream bufferedSource = new BufferedInputStream(source, 400)) {

            bufferedSource.mark(limit);
            try (InputStream limitedStream = limit(bufferedSource, DataSize.bytes(limit), () -> new IllegalStateException("Reached limit!"))) {
                assertThat(limitedStream.read(readFromLimitedStream), is(limit));
                bufferedSource.reset();
                bufferedSource.read(readFromBufferedStream);
            }
        }
        assertArrayEquals(readFromBufferedStream, oneKiloByte);
        assertAll(
                () -> assertEquals(readFromLimitedStream[0], (byte)65),
                () -> assertEquals(readFromLimitedStream[limit - 1], (byte)65),
                () -> assertEquals(readFromLimitedStream[limit], (byte)0),
                () -> assertEquals(readFromLimitedStream[readFromLimitedStream.length - 1], (byte)0)
                );
    }

    @Test
    void rewindWhenReachingLimit() throws IOException {
        byte[] twoKiloByte = new byte[2048];
        Arrays.fill(twoKiloByte, (byte) 65);

        int limit = 1024;
        try (
                InputStream source = new ByteArrayInputStream(twoKiloByte);
                InputStream bufferedSource = new BufferedInputStream(source, 512)) {

            bufferedSource.mark(limit + 1); //  <-- :(
            try (InputStream limitedStream = limit(bufferedSource, DataSize.bytes(limit))) {
                byte[] readFromLimitedStream = toByteArray(limitedStream);
                assertThat(readFromLimitedStream.length, is(limit));

                bufferedSource.reset();
                byte[] readFromBufferedStream = toByteArray(bufferedSource);
                assertArrayEquals(readFromBufferedStream, twoKiloByte);
            }
        }

    }

}
