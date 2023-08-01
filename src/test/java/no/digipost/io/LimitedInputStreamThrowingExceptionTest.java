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

import static org.apache.commons.io.IOUtils.toByteArray;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.GeneralSecurityException;
import java.util.function.Supplier;

import static uk.co.probablyfine.matchers.Java8Matchers.where;
import static java.nio.charset.StandardCharsets.UTF_8;
import static no.digipost.DiggIO.limit;
import static no.digipost.io.DataSize.bytes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LimitedInputStreamThrowingExceptionTest {

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


    private String testLimitedStream(String content, Supplier<? extends Exception> throwIfTooManyBytes) throws Exception {
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

    static byte[] toByteArrayUsingSingleByteReads(InputStream toRead, DataSize amountToRead) throws IOException {
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
}
