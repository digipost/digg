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
package no.digipost;

import no.digipost.function.ObjIntFunction;
import no.digipost.function.ObjLongFunction;

import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Utilities for working with {@link Stream}s.
 */
public final class DiggStreams {

    /**
     * Stream the elements from a container which allows access to the elements by an {@code int} index, but does not itself
     * offer any means for traversal/iteration/streaming. This method will stream all elements from index 0 up to,
     * but not including, the given {@code endExclusive} index.
     *
     * @param <V> The type of the resolved elements yielded by the {@link Stream}.
     * @param <S> The type of the source object which yields elements of type {@code V} by index.
     *
     * @param source The source object which yields the elements by an index
     * @param endExclusive The end index. This is also effectively the resulting size of the stream
     * @param resolveValue How to query the {@code source} object for each index. This can usually be a method
     *                     reference like {@code S::get} or similar.
     *
     * @return the stream yielding the elements
     */
    public static final <S, V> Stream<V> streamByIntIndex(S source, int endExclusive, ObjIntFunction<S, V> resolveValue) {
        return streamByIndex(source, IntStream.range(0, endExclusive), resolveValue);
    }

    /**
     * Stream the elements from a container which allows access to the by through an {@code int} index, but does not itself
     * offer any means for traversal/iteration/streaming. This method will stream the elements with indexes from
     * the given {@link IntStream}.
     *
     *
     * @param <V> The type of the resolved elements yielded by the {@link Stream}.
     * @param <S> The type of the source object which yields elements of type {@code V} by index.
     *
     * @param source The source object which yields the elements by an index
     * @param indexes The indexes
     * @param resolveValue How to query the {@code source} object for each index. This can usually be a method
     *                     reference like {@code S::get} or similar.
     *
     * @return the stream yielding the elements
     */
    public static final <S, K, V> Stream<V> streamByIndex(S source, IntStream indexes, ObjIntFunction<S, V> resolveValue) {
        return indexes.mapToObj(index -> resolveValue.apply(source, index));
    }

    /**
     * Stream the elements from a container which allows access to the elements by a {@code long} index, but does not itself
     * offer any means for traversal/iteration/streaming. This method will stream all elements from index 0 up to,
     * but not including, the given {@code endExclusive} index.
     *
     * @param <V> The type of the resolved elements yielded by the {@link Stream}.
     * @param <S> The type of the source object which yields elements of type {@code V} by index.
     *
     * @param source The source object which yields the elements by an index
     * @param endExclusive The end index. This is also effectively the resulting size of the stream
     * @param resolveValue How to query the {@code source} object for each index. This can usually be a method
     *                     reference like {@code S::get} or similar.
     *
     * @return the stream yielding the elements
     */
    public static final <S, V> Stream<V> streamByLongIndex(S source, long endExclusive, ObjLongFunction<S, V> resolveValue) {
        return streamByIndex(source, LongStream.range(0, endExclusive), resolveValue);
    }

    /**
     * Stream the elements from a container which allows access to the by through a {@code long} index, but does not itself
     * offer any means for traversal/iteration/streaming. This method will stream the elements with indexes from
     * the given {@link IntStream}.
     *
     *
     * @param <V> The type of the resolved elements yielded by the {@link Stream}.
     * @param <S> The type of the source object which yields elements of type {@code V} by index.
     *
     * @param source The source object which yields the elements by an index
     * @param indexes The indexes
     * @param resolveValue How to query the {@code source} object for each index. This can usually be a method
     *                     reference like {@code S::get} or similar.
     *
     * @return the stream yielding the elements
     */
    public static final <S, K, V> Stream<V> streamByIndex(S source, LongStream indexes, ObjLongFunction<S, V> resolveValue) {
        return indexes.mapToObj(index -> resolveValue.apply(source, index));
    }


    /**
     * Stream the elements from a container which allows access to the by through a key, but does not itself
     * offer any means for traversal/iteration/streaming. This method will stream the elements with keys from
     * the given {@link Stream Stream&lt;K&gt;}.
     *
     *
     * @param <V> The type of the resolved elements yielded by the {@link Stream}.
     * @param <S> The type of the source object which yields elements of type {@code V} by key.
     * @param <K> The type of the key used to query elements of type {@code V}.
     *
     * @param source The source object which yields the elements by a key
     * @param keys The keys
     * @param resolveValue How to query the {@code source} object for each key. This can usually be a method
     *                     reference like {@code S::get} or similar.
     *
     * @return the stream yielding the elements
     */
    public static final <S, K, V> Stream<V> streamByKey(S source, Stream<K> keys, BiFunction<S, K, V> resolveValue) {
        return keys.map(key -> resolveValue.apply(source, key));
    }


    private DiggStreams() {
    }
}
