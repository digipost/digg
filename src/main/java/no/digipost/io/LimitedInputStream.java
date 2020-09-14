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

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import static no.digipost.DiggExceptions.asUnchecked;

/**
 * An {@link InputStream} which limits how many bytes which can be read.
 * <p>
 * This class is based on the
 * <a href="https://commons.apache.org/proper/commons-fileupload/apidocs/org/apache/commons/fileupload/util/LimitedInputStream.html">LimitedInputStream from Apache Commons Fileupload</a> (v1.3.2),
 * which has the license as the Digg library, <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License 2.0</a>, but also supports
 * to {@link #SILENTLY_EOF_ON_REACHING_LIMIT silently treat the limit as EOF} without any signal to distinguish between the EOF of the wrapped stream and
 * the <em>limited</em> stream.
 */
public final class LimitedInputStream extends FilterInputStream implements Closeable {

    private static final class SilentlyEofWhenReachingLimit implements Supplier<Exception> {
        @Override
        public Exception get() {
            throw new UnsupportedOperationException("Should not call get() on instance of " + SilentlyEofWhenReachingLimit.class.getSimpleName() + ", this indicates a bug.");
        }
        private SilentlyEofWhenReachingLimit() {}
    }

    /**
     * Supply this instead of an {@link Supplier exception supplier} as parameter when contructing
     * a new {@code LimitedInputStream} to instruct it to
     * treat the limit as an ordinary EOF, and <em>not</em> throw any exception to signal that the
     * limit was reached during consumption of the stream.
     * <p>
     * Invoking {@link Supplier#get() get()} on this will throw an exception.
     */
    public static final Supplier<Exception> SILENTLY_EOF_ON_REACHING_LIMIT = new SilentlyEofWhenReachingLimit();


    private final DataSize limit;
    private final Supplier<? extends Exception> throwIfTooManyBytes;
    private long count;


    /**
     * @see no.digipost.DiggIO#limit(InputStream, DataSize, Supplier)
     */
    public LimitedInputStream(InputStream inputStream, DataSize maxDataToRead, Supplier<? extends Exception> throwIfTooManyBytes) {
        super(inputStream);
        this.limit = maxDataToRead;
        this.throwIfTooManyBytes = throwIfTooManyBytes;
    }


    /**
     * Reads the next byte of data from this input stream. The value
     * byte is returned as an <code>int</code> in the range
     * <code>0</code> to <code>255</code>. If no byte is available
     * because the end of the stream has been reached, the value
     * <code>-1</code> is returned. This method blocks until input data
     * is available, the end of the stream is detected, or an exception
     * is thrown.
     * <p>
     * This method
     * simply performs <code>in.read()</code> and returns the result.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    @Override
    public int read() throws IOException {
        int res = super.read();
        if (res != -1) {
            count++;
            if (hasReachedLimit()) {
                return -1;
            }
        }
        return res;
    }

    /**
     * Reads up to <code>len</code> bytes of data from this input stream
     * into an array of bytes. If <code>len</code> is not zero, the method
     * blocks until some input is available; otherwise, no
     * bytes are read and <code>0</code> is returned.
     * <p>
     * This method simply performs <code>in.read(b, off, len)</code>
     * and returns the result.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   The start offset in the destination array
     *                   <code>b</code>.
     * @param      len   the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  NullPointerException If <code>b</code> is <code>null</code>.
     * @exception  IndexOutOfBoundsException If <code>off</code> is negative,
     * <code>len</code> is negative, or <code>len</code> is greater than
     * <code>b.length - off</code>
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int res = super.read(b, off, len);
        if (res > 0) {
            count += res;
            if (hasReachedLimit()) {
                return -1;
            }
        }
        return res;
    }


    private boolean hasReachedLimit() throws IOException {
        if (count > limit.toBytes()) {
            if (throwIfTooManyBytes == SILENTLY_EOF_ON_REACHING_LIMIT) {
                return true;
            }
            Exception tooManyBytes = throwIfTooManyBytes.get();
            if (tooManyBytes instanceof IOException) {
                throw (IOException) tooManyBytes;
            } else {
                throw asUnchecked(tooManyBytes);
            }
        } else {
            return false;
        }
    }

}
