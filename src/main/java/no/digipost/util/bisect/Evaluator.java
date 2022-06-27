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
package no.digipost.util.bisect;

import no.digipost.function.ThrowingFunction;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

@FunctionalInterface
public interface Evaluator<T> {

    /**
     * Evaluate suggestions using an extracted {@link Comparable comparable} property of the suggestions.
     *
     * @param propertyExtractor the function to resolve/extract the property
     * @param comparableTarget the ideal target to search for
     *
     * @return the evaluator
     */
    static <T, U extends Comparable<? super U>, X extends Exception> Evaluator<T> having(ThrowingFunction<? super T, ? extends U, X> propertyExtractor, U comparableTarget) {
        return suggestion -> Result.fromComparatorResult(propertyExtractor.apply(suggestion).compareTo(comparableTarget));
    }

    /**
     * Evaluate suggestions based on how many bytes is written from a suggestion.
     *
     * @param targetBytes the ideal target byte amount
     * @param serializer the consumer function which defines how a suggestion is written
     *                   as a series of bytes to a {@link ByteCounter} (an {@link OutputStream}).
     *
     * @return the evaluator
     */
    static <T> Evaluator<T> byteCount(long targetBytes, OutputStreamObjectWriter<? super T, ? super ByteCounter> serializer) {
        return having(suggestion -> {
            ByteCounter counter = new ByteCounter();
            serializer.write(suggestion, counter);
            return counter.getByteCount();
        }, targetBytes);
    }


    /**
     * Evaluate a suggestion as either {@link Result#TOO_LOW}, {@link Result#TOO_HIGH},
     * or {@link Result#FOUND}.
     *
     * @param suggestion the suggestion to evaluate
     * @return the result of the evaluation
     */
    Result evaluate(T suggestion) throws Exception;


    public enum Result {
        TOO_LOW, TOO_HIGH, FOUND;

        public static Result fromComparatorResult(int comparatorResult) {
            if (comparatorResult < 0) {
                return TOO_LOW;
            } else if (comparatorResult > 0) {
                return TOO_HIGH;
            } else {
                return FOUND;
            }
        }
    }



    @FunctionalInterface
    interface OutputStreamObjectWriter<T, O extends OutputStream> {
        void write(T object, O output) throws Exception;
    }


    final class ByteCounter extends OutputStream {

        private long count;

        private ByteCounter() {
        }

        public long getByteCount() {
            return count;
        }

        @Override
        public void write(int b) {
            count++;
        }

        @Override
        public void write(byte[] b, int off, int len) {
            try {
                super.write(b, off, len);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void write(byte[] b) {
            try {
                super.write(b);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

}
