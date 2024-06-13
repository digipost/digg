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

import no.digipost.io.InputStreamIterator.WrappedInputStreamFailed;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class InputStreamIteratorTest {

    @Test
    void should_read_the_input_stream_fully() throws Exception {
        StringBuilder sb = new StringBuilder();

        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream("Some data".getBytes(StandardCharsets.UTF_8));) {
            InputStreamIterator iterator = new InputStreamIterator(inputStream, 2);

            while (iterator.hasNext()) {
                sb.append(new String(iterator.next()));
            }
        }

        assertEquals("Some data", sb.toString());
    }

    @Test
    void should_read_the_input_stream_fully_with_datasize() throws Exception {
        StringBuilder sb = new StringBuilder();

        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream("Some data".getBytes(StandardCharsets.UTF_8));) {

            InputStreamIterator iterator = new InputStreamIterator(inputStream, DataSize.bytes(2));
            while (iterator.hasNext()) {
                sb.append(new String(iterator.next()));
            }
        }

        assertEquals("Some data", sb.toString());
    }

    @Test
    void too_big_data_size_will_throw_NegativeArraySizeException() throws Exception {
        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream("Some data".getBytes(StandardCharsets.UTF_8))) {
            InputStreamIterator iterator = new InputStreamIterator(inputStream, DataSize.MAX);

            assertThrows(NegativeArraySizeException.class, iterator::hasNext);
        }
    }

    @Test
    void should_throw_if_next_is_called_with_no_more_elements() throws Exception {
        StringBuilder sb = new StringBuilder();

        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream("Some data".getBytes(StandardCharsets.UTF_8));) {

            InputStreamIterator iterator = new InputStreamIterator(inputStream, 2);

            while (iterator.hasNext()) {
                sb.append(new String(iterator.next()));
            }

            assertThrows(NoSuchElementException.class, iterator::next);
        }

        assertEquals("Some data", sb.toString());
    }

    @Test
    void should_throw_exception_if_input_stream_fails() throws Exception {
        try (final InputStream failingInputStream = new InputStream() {

            @Override
            public int read() throws IOException {
                throw new IOException("This input stream is broken");
            }
        }) {
            InputStreamIterator iterator = new InputStreamIterator(failingInputStream, 1);

            final WrappedInputStreamFailed ex = assertThrows(WrappedInputStreamFailed.class, iterator::next);
            assertThat(ex, where(Exception::getMessage, containsString("InputStreamIteratorTest.")));
        }

    }

    @Test
    void worksWithInputStreamHavingMultipleEntries() throws IOException {
        ZipEntryContent file1 = new ZipEntryContent("file1.txt", "This is file1");
        ZipEntryContent file2 = new ZipEntryContent("file2.txt", "This is file2");
        byte[] zipFile = zip(file1, file2);


        List<ZipEntryContent> entriesReadConventionally = new ArrayList<>();
        try (ZipInputStream zipReader = new ZipInputStream(new ByteArrayInputStream(zipFile))) {
            for (ZipEntry nextEntry = zipReader.getNextEntry(); nextEntry != null; nextEntry = zipReader.getNextEntry()) {
                entriesReadConventionally.add(ZipEntryContent.read(nextEntry, zipReader));
            }
        }

        assertThat(entriesReadConventionally, containsInAnyOrder(file1, file2));


        List<ZipEntryContent> entriesReadInChunks = new ArrayList<>();
        try (ZipInputStream zipReader = new ZipInputStream(new ByteArrayInputStream(zipFile))) {
            for (ZipEntry nextEntry = zipReader.getNextEntry(); nextEntry != null; nextEntry = zipReader.getNextEntry()) {
                ByteArrayOutputStream entryConsumer = new ByteArrayOutputStream();
                for (byte[] chunk : (Iterable<byte[]>) () -> new InputStreamIterator(zipReader, DataSize.bytes(2))) {
                    entryConsumer.write(chunk);
                }
                entriesReadInChunks.add(ZipEntryContent.read(nextEntry, entryConsumer.toByteArray()));
            }
        }

        assertThat(entriesReadInChunks, containsInAnyOrder(file1, file2));
    }


    private static final class ZipEntryContent {
        static ZipEntryContent read(ZipEntry entry, byte[] content) throws IOException {
            return read(entry, new ByteArrayInputStream(content));
        }

        static ZipEntryContent read(ZipEntry entry, InputStream contentStream) throws IOException {
            return new ZipEntryContent(entry.getName(), IOUtils.toString(contentStream, UTF_8));
        }

        final String name;
        final String content;

        ZipEntryContent(String name, String content) {
            this.name = name;
            this.content = content;
        }

        public void writeTo(ZipOutputStream zip) throws IOException {
            zip.putNextEntry(new ZipEntry(name));
            zip.write(content.getBytes(UTF_8));
        }

        @Override
        public String toString() {
            return "zip entry '" + name + "': " + content;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ZipEntryContent) {
                ZipEntryContent that = (ZipEntryContent) o;
                return Objects.equals(this.name, that.name) && Objects.equals(this.content, that.content);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, content);
        }

    }

    private static byte[] zip(ZipEntryContent ... entries) {
        ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();
        try (ZipOutputStream zipWriter = new ZipOutputStream(zipOutput)) {
            for (ZipEntryContent entry : entries) {
                entry.writeTo(zipWriter);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return zipOutput.toByteArray();
    }

}
