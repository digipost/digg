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

import no.digipost.DiggExceptions;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An {@link InputStream} getting its contents from consuming an {@link OutputStream}.
 * The InputStream will be immediately available for reading after construction, regardless
 * of how much data that will be produced by the OutputStream.
 */
public class ConsumingInputStream extends InputStream {


    private final PipedInputStream inputPipe = new PipedInputStream();

    private final Future<?> producing;

    private final AtomicBoolean attemptToCloseInputStream = new AtomicBoolean(false);



    public ConsumingInputStream(ExecutorService executorService, Consumer<? super OutputStream> write) {
        this(executorService, o -> o, write);
    }


    /**
     *
     * @param executorService The executorService to use to start producing data which will be readable by this inputstream.
     * @param outputStreamDecorator An {@link Fn} to wrap the outputstream to push data to. This <code>Fn</code> must
     *                              <em>always</em> wrap the given OutputStream in the OutputStream returned from this <code>Fn</code>,
     *                              i.e. the resulting OutputStream must be constructed by wrapping the given OutputStream as
     *                              a constructor argument. (E.g. <code>new ZipOutputStream(givenOutputStream)</code>.
     * @param write The data producing logic. This {@link Do} will be given the OutputStream resulting from the
     *              <code>outputStreamDecorator</code>.
     */
    public <S extends OutputStream> ConsumingInputStream(ExecutorService executorService, Function<OutputStream, S> outputStreamDecorator, Consumer<? super S> write) {
        PipedOutputStream outputPipe;
        S decoratedOutputPipe;
        try {
            outputPipe = new PipedOutputStream(inputPipe);
            decoratedOutputPipe = outputStreamDecorator.apply(outputPipe);
        } catch (IOException e) {
            throw DiggExceptions.asUnchecked(e);
        }
        this.producing = executorService.submit(new Producer<>(outputPipe, decoratedOutputPipe, write));
    }


    @Override
    public int read() throws IOException {
        failIfProducerFailed();
        return inputPipe.read();
    }


    @Override
    public void close() throws IOException {
        try {
            failIfProducerFailed();
            attemptToCloseInputStream.set(true);
            if (!producing.isDone()) {
                producing.cancel(true);
            }
        } finally {
            inputPipe.close();
        }
    }



    @Override
    public int read(byte[] b) throws IOException {
        failIfProducerFailed();
        return inputPipe.read(b);
    }


    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        failIfProducerFailed();
        return inputPipe.read(b, off, len);
    }


    @Override
    public long skip(long n) throws IOException {
        failIfProducerFailed();
        return inputPipe.skip(n);
    }


    @Override
    public int available() throws IOException {
        failIfProducerFailed();
        return inputPipe.available();
    }


    @Override
    public synchronized void mark(int readlimit) {
        inputPipe.mark(readlimit);
    }


    @Override
    public synchronized void reset() throws IOException {
        inputPipe.reset();
    }


    @Override
    public boolean markSupported() {
        return inputPipe.markSupported();
    }


    private void failIfProducerFailed() {
        if (producing.isDone()) {
            try {
                producing.get();
            } catch (Exception e) {
                throw new ProducerFailed(e);
            }
        }
    }



    public static final class ProducerFailed extends RuntimeException {
        private ProducerFailed(Throwable cause) {
            super("Tråden som skriver data for lesing av ConsumingInputStream har feilet. Cause: " + cause.getClass() + ": " + cause.getMessage(), cause);
        }
    }


    private class Producer<S extends OutputStream> implements Runnable {

        final PipedOutputStream outputPipe;
        final S decoratedOutputPipe;
        final Consumer<? super S> write;

        Producer(PipedOutputStream outputPipe, S decoratedOutputPipe, Consumer<? super S> write) {
            this.outputPipe = outputPipe;
            this.decoratedOutputPipe = decoratedOutputPipe;
            this.write = write;
        }


        @Override
        public void run() {
            try {
                write.accept(decoratedOutputPipe);
            } catch (Exception e) {
                throw DiggExceptions.asUnchecked(e);
            } finally {
                try {
                    try {
                        decoratedOutputPipe.close();
                    } finally {
                        outputPipe.close();
                    }
                } catch (IOException e) {
                    if (attemptToCloseInputStream.get()) {
                        return;
                    }
                    throw DiggExceptions.asUnchecked(e);
                }
            }
        }

    }


}
