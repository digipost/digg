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

import no.digipost.DiggBase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * InputStreamIterator is an {@link Iterator} reading from an {@link InputStream} in chunks
 * where each chunk is returned as the next element in the iterable.
 * When the input stream is fully consumed the iterator has no more elements.
 */
public class InputStreamIterator implements Iterator<byte[]> {
    private final InputStream inputStream;
    private final int chunkSize;
    private byte[] next;
    private Boolean hasNext;
    private boolean endOfStreamReached = false;

    /**
     * @param inputStream The input stream to iterate over
     * @param chunkSize   DataSize should not be too big since that defeats the purpose of this iterator.
     */
    public InputStreamIterator(InputStream inputStream, DataSize chunkSize) {
        this.inputStream = inputStream;
        this.chunkSize   = (int) chunkSize.toBytes();
    }

    public InputStreamIterator(InputStream inputStream, int chunkSizeBytes) {
        this.inputStream = inputStream;
        this.chunkSize   = chunkSizeBytes;
    }

    private byte[] loadNextChunk() {
        if (endOfStreamReached) return null;

        byte[] chunk = new byte[chunkSize];
        int bytesRead = 0;
        try {
            bytesRead = inputStream.read(chunk);
            if (bytesRead == -1) {
                endOfStreamReached = true;
                return null;
            }
        } catch (IOException e) {
            throw new WrappedInputStreamFailed(e, inputStream);
        }

        if (bytesRead < chunkSize) {
            // resize the buffer if less data was read
            byte[] smallerBuffer = new byte[bytesRead];
            System.arraycopy(chunk, 0, smallerBuffer, 0, bytesRead);
            chunk = smallerBuffer;
        }

        return chunk;
    }

    /**
     * If the iterator fails reading from the wrapped InputStream an
     * {@link  InputStreamIterator.WrappedInputStreamFailed} runtime exception is thrown.
     *
     * @return true if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        if (hasNext == null) {
            next    = loadNextChunk();
            hasNext = (next != null);
        }

        return hasNext;
    }

    @Override
    public byte[] next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more data to read");
        }

        byte[] result = next;
        hasNext = null;
        next    = null;
        return result;
    }

    public static final class WrappedInputStreamFailed extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private WrappedInputStreamFailed(Throwable cause, InputStream inputStream) {
            super("The InputStream " + DiggBase.friendlyName(inputStream.getClass()) +
                    " read failed. Cause: " + cause.getClass() + ": " + cause.getMessage(), cause);
        }
    }
}
