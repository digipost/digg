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
package no.digipost.collection;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BatchedIterable<T, BATCH extends Iterable<? extends T>> implements Iterable<T> {

	private Supplier<BATCH> batchFetcher;
	private Predicate<? super BATCH> canFetchNextBatch;


	public static <T, BATCH extends Iterable<? extends T>> Iterable<T> batched(Supplier<BATCH> batchFetcher, Predicate<? super BATCH> fetchNextBatch) {
		return new BatchedIterable<>(batchFetcher, fetchNextBatch);
	}

	private BatchedIterable(Supplier<BATCH> batchFetcher, Predicate<? super BATCH> fetchNextBatch) {
		this.batchFetcher = batchFetcher;
		this.canFetchNextBatch = fetchNextBatch;
	}


	@Override
    public Iterator<T> iterator() {
		return new Iterator<T>() {
			BATCH currentBatch = batchFetcher.get();
			Iterator<? extends T> currentIterator = currentBatch.iterator();

			@Override
            public boolean hasNext() {
				if (currentIterator.hasNext()) return true;
				if (canFetchNextBatch.test(currentBatch)) {
					currentBatch = batchFetcher.get();
					currentIterator = currentBatch.iterator();
					return currentIterator.hasNext();
				}
				return false;
            }

			@Override
            public T next() {
				return currentIterator.next();
            }
		};
    }

}
