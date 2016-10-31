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
 * which has the license as the Digg library, <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License 2.0</a>.
 */
public final class LimitedInputStream extends FilterInputStream implements Closeable {

    private final long sizeMax;

    private final Supplier<? extends Exception> throwIfTooManyBytes;

    private long count;


    /**
     * @see no.digipost.DiggIO#limit(InputStream, long, Supplier)
     */
    public LimitedInputStream(InputStream inputStream, long maxBytesToRead, Supplier<? extends Exception> throwIfTooManyBytes) {
        super(inputStream);
        this.sizeMax = maxBytesToRead;
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
            checkLimit();
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
            checkLimit();
        }
        return res;
    }


    private void checkLimit() throws IOException {
        if (count > sizeMax) {
            Exception tooManyBytes = throwIfTooManyBytes.get();
            if (tooManyBytes instanceof IOException) {
                throw (IOException) tooManyBytes;
            } else {
                throw asUnchecked(tooManyBytes);
            }
        }
    }

}
