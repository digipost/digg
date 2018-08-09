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

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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



    /**
     * Stream elements retrieved from resolved collections while they are non-empty.
     * The first empty collection will end the stream.
     *
     * @param <E> The type of the elements in the resolved collections, and elements in the resulting stream
     *
     * @param resolveCollection a function accepting an int indicating the page number and returns a {@link Collection} with
     *                          elements to include in the resulting stream
     *
     * @return the stream yielding the elements of the resolved collections
     */
    public static <E> Stream<E> streamWhileNonEmpty(IntFunction<? extends Collection<E>> resolveCollection) {
        return streamPages(resolveCollection, c -> !c.isEmpty()).flatMap(Collection::stream);
    }


    /**
     * Generate a stream of objects resolved from an incrementing int (page number), while a predicate is accepting
     * the resolved objects. The first object not accepted by the predicate will end the stream.
     *
     * @param resolvePage a function accepting an int indicating the page number and returns a page to include
     *                    in the resulting stream
     * @param includeWhile the predicate accepting the objects to include in the stream
     *
     * @return the stream of the resolved objects
     */
    public static <P> Stream<P> streamPages(IntFunction<P> resolvePage, Predicate<? super P> includeWhile) {
        return streamPages(0, resolvePage, includeWhile);
    }

    /**
     * Generate a stream of objects resolved from an incrementing int (page number), while a predicate is accepting
     * the resolved objects. The first object not accepted by the predicate will end the stream.
     *
     * @param firstPageNum the initial page number
     * @param resolvePage a function accepting an int indicating the page number and returns a page to include
     *                    in the resulting stream
     * @param includeWhile the predicate accepting the objects to include in the stream
     *
     * @return the stream of the resolved objects
     */
    public static <P> Stream<P> streamPages(int firstPageNum, IntFunction<P> resolvePage, Predicate<? super P> includeWhile) {
        Spliterator<P> spliterator = new Spliterators.AbstractSpliterator<P>(Long.MAX_VALUE, 0) {
            final AtomicInteger pageNum = new AtomicInteger(firstPageNum);
            @Override
            public boolean tryAdvance(Consumer<? super P> action) {
                P nextPage = resolvePage.apply(pageNum.getAndIncrement());
                if (includeWhile.test(nextPage)) {
                    action.accept(nextPage);
                    return true;
                } else {
                    return false;
                }
            }
        };
        return StreamSupport.stream(spliterator, false);
    }


    private DiggStreams() {
    }

}
