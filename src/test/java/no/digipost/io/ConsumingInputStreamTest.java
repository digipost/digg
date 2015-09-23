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

import com.google.common.io.ByteStreams;
import no.digipost.io.ConsumingInputStream.ProducerFailed;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.time.Duration.ofMillis;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ConsumingInputStreamTest {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private ConsumingInputStream input;

    @Test
    public void readingWhatIsWrittenToOutputStream() throws Exception {
        Future<String> futureString = executorService.submit(delayed(ofMillis(750), "Hello"));

        input = new ConsumingInputStream(executorService, writeWhenReady(futureString));

        assertThat(input.available(), is(0));

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            assertThat("Blocks until string is ready", reader.readLine(), is("Hello"));
        }
    }



    @Test
    public void zippingAnOutputStreamThenUnzippingTheInputStream() throws Exception {
        final long expectedEntriesAmount = 100;
        input = new ConsumingInputStream(executorService, ZipOutputStream::new, zip -> {
            try {
                for (int i = 0; i < expectedEntriesAmount; i++) {
                    ZipEntry entry = new ZipEntry("file" + i);
                    entry.setMethod(ZipEntry.DEFLATED);
                    entry.setExtra("Extra bytes".getBytes());
                    entry.setComment(
                            "This will be stored in central directory at the end, and not retrieved when " +
                            "reading the zip with ZipInputStream");
                    zip.putNextEntry(entry);
                    zip.write((entry.getName() + " content").getBytes());
                }
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });


        LongAdder entriesAmount = new LongAdder();
        try (ZipInputStream zip = new ZipInputStream(input)) {

            for (ZipEntry entry : Zip.entriesIn(zip)) {
                assertThat(entry.getName(), is("file" + entriesAmount));
                assertThat(entry.getExtra(), is("Extra bytes".getBytes()));
                assertThat(ByteStreams.toByteArray(zip), is((entry.getName() + " content").getBytes()));
                entriesAmount.increment();
            }
        }
        assertThat(entriesAmount.sum(), is(expectedEntriesAmount));
    }



    @Test
    public void exceptionInProducerPropagatesToInputStream() throws Exception {
        IllegalStateException producerFailure = new IllegalStateException("Cannot write data");
        input = new ConsumingInputStream(executorService, out -> { throw producerFailure; });
        Thread.sleep(200);

        List<Throwable> failures = new ArrayList<>();
        try { input.read();	} catch (ProducerFailed e) { failures.add(e.getCause().getCause()); }
        try { input.read(new byte[10]);	} catch (ProducerFailed e) { failures.add(e.getCause().getCause()); }
        try { input.read(new byte[10], 1, 3); } catch (ProducerFailed e) { failures.add(e.getCause().getCause()); }
        try { input.skip(1); } catch (ProducerFailed e) { failures.add(e.getCause().getCause()); }
        try { input.available(); } catch (ProducerFailed e) { failures.add(e.getCause().getCause()); }
        try { input.close(); } catch (ProducerFailed e) { failures.add(e.getCause().getCause()); }

        assertThat(failures, hasSize(6));
        assertThat(failures, everyItem(sameInstance((Throwable) producerFailure)));
    }


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void failFastForOutputStreamDecorator() {
        Function<OutputStream, OutputStream> failStreamWrapping = out -> {
            throw new IllegalStateException("unable to wrap piped stream");
        };
        expectedException.expect(IllegalStateException.class);
        input = new ConsumingInputStream(executorService, failStreamWrapping, out -> {});
    }



    private Consumer<OutputStream> writeWhenReady(final Future<String> futureString) {
        return out -> {
            String s;
            try {
                s = futureString.get(4, TimeUnit.SECONDS);
                out.write(s.getBytes());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        };
    }



    private Callable<String> delayed(final Duration delay, final String expectedString) {
        return () -> {
            Thread.sleep(delay.toMillis());
            return expectedString;
        };
    }



    @AfterClass
    public static void shutdownExecutorService() {
        executorService.shutdownNow();
    }
}
