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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.lang.Math.toIntExact;
import static no.digipost.DiggBase.friendlyName;
import static no.digipost.DiggExceptions.exceptionNameAndMessage;

/**
 * InputStreamIterator is an {@link Iterator} reading from an {@link InputStream} in chunks
 * where each chunk is returned as the next element in the iterable.
 * When the input stream is fully consumed the iterator has no more elements.
 */
public class InputStreamIterator implements Iterator<byte[]> {
    private final InputStream inputStream;
    private final int chunkSizeBytes;
    private byte[] next;
    private Boolean hasNext;

    /**
     * @param inputStream The input stream to iterate over
     * @param chunkSize   DataSize should not be too big since that defeats the purpose of this iterator.
     */
    public InputStreamIterator(InputStream inputStream, DataSize chunkSize) {
        this(inputStream, toIntExact(chunkSize.toBytes()));
    }

    public InputStreamIterator(InputStream inputStream, int chunkSizeBytes) {
        this.inputStream = inputStream;
        this.chunkSizeBytes   = chunkSizeBytes;
    }

    private byte[] loadNextChunk() {
        byte[] chunk = new byte[chunkSizeBytes];
        int bytesRead = 0;
        try {
            bytesRead = inputStream.read(chunk);
            if (bytesRead == -1) {
                return null;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed reading next chunk of up to " + chunkSizeBytes +
                    " bytes from " + friendlyName(inputStream.getClass()) +
                    " because " + exceptionNameAndMessage(e), e);
        }

        if (bytesRead < chunkSizeBytes) {
            // resize the buffer if less data was read
            byte[] smallerBuffer = new byte[bytesRead];
            System.arraycopy(chunk, 0, smallerBuffer, 0, bytesRead);
            chunk = smallerBuffer;
        }

        return chunk;
    }

    /**
     *
     * @return true if the iteration has more elements
     *
     * @throws UncheckedIOException if the wrapped InputStream throws an IOException
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

}
