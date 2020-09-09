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
package no.digipost.collection;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class SimpleIteratorTest {


    @Test
    public void nextThrowsNoSuchElement() {
        assertThrows(NoSuchElementException.class, empty.iterator()::next);
    }

    @Test
    public void nextReturnsAnElementThenThrowsNoSuchElement() {
        Iterator<String> iterator = oneElement.iterator();
        assertThat(iterator.next(), is("x"));
        try { iterator.next(); } catch (NoSuchElementException e) { return; }
        fail("Should have thrown " + NoSuchElementException.class);
    }

    @Test
    public void iteratingUsingForLoop() {
        for (Object o : empty) fail("Should not yield any object, but got " + o);
        for (String s : oneElement) {
            assertThat(s, is("x"));
            return;
        }
        fail("Did not iterate");
    }


    @Test
    public void iteratorMayThrowException() {
        Iterator<Object> iterator = fails.iterator();
        try {
            iterator.next();
        } catch (Exception e) {
            assertThat(e, instanceOf(RuntimeException.class));
            assertThat(e.getCause(), instanceOf(IOException.class));
            return;
        }
        fail("Should throw exception");
    }

    @Test
    public void removeIsUnsupported() {
        Iterator<String> iterator = oneElement.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }






    private final Iterable<String> oneElement = new Iterable<String>() {
        @Override
        public Iterator<String> iterator() {
            return new SimpleIterator<String>() {
                boolean returned;
                @Override
                protected Optional<String> nextIfAvailable() {
                    if (returned) return Optional.empty();
                    returned = true;
                    return Optional.of("x");
                }
            };
        }
    };


    private final Iterable<Object> empty = new Iterable<Object>() {
        @Override
        public Iterator<Object> iterator() {
            return new SimpleIterator<Object>() {
                @Override
                protected Optional<Object> nextIfAvailable() {
                    return Optional.empty();
                }
            };
        }
    };

    private final Iterable<Object> fails = new Iterable<Object>() {
        @Override
        public Iterator<Object> iterator() {
            return new SimpleIterator<Object>() {
                @Override
                protected Optional<? extends Object> nextIfAvailable() throws Exception {
                    throw new IOException();
                }
            };
        }
    };

}
