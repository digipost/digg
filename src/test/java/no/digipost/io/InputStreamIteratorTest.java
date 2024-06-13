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
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
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
}
