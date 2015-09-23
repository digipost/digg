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

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static no.digipost.collection.BatchedIterable.batched;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


public class BatchedIterableTest {


    @Test
    public void emptyBatch() {
        Batches<Object> empty1 = new Batches<>();
        assertThat(batched(empty1, b -> false), is(emptyIterable()));
        assertThat(empty1.fetches(), is(1));

        Batches<Object> empty2 = new Batches<>();
        assertThat(batched(empty2, b -> true), is(emptyIterable()));
        assertThat(empty2.fetches(), is(2));
    }

    @Test
    public void singleBatch() {
        Batches<String> singleBatch = new Batches<String>(asList("a", "b"));
        assertThat(batched(singleBatch, b -> false), contains("a", "b"));
        assertThat(singleBatch.fetches(), is(1));
    }

    @Test
    public void twoBatches() {
        Batches<String> twoBatches = new Batches<String>(asList("a", "b", "c"), asList("d", "e"));
        assertThat(batched(twoBatches, batch -> batch.size() >= 3), contains("a", "b", "c", "d", "e"));
        assertThat(twoBatches.fetches(), is(2));
    }



    static class Batches<T> implements Supplier<List<T>> {

        private final Queue<List<T>> batches;
        private int fetches;

        @SuppressWarnings("unchecked")
        Batches() {
            this(new List[0]);
        }
        @SafeVarargs
        Batches(List<T> ... batches) {
            this.batches = new LinkedList<>();
            for (List<T> batch : batches) {
                this.batches.add(batch);
            }
        }

        @Override
        public List<T> get() {
            fetches++;
            return Optional.ofNullable(batches.poll()).orElse(emptyList());
        }

        public int fetches() {
            return fetches;
        }
    }

}
