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

import no.digipost.collection.SimpleIterator;
import no.digipost.concurrent.OneTimeToggle;

import java.util.Iterator;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Zip {

    public static Entries entriesIn(ZipInputStream zip) {
        return new Entries(zip);
    }

    public static final class Entries implements Iterable<ZipEntry> {

        private final ZipInputStream zip;
        private final OneTimeToggle iteratorRequested = new OneTimeToggle();

        private Entries(ZipInputStream zip) {
            this.zip = zip;
        }

        @Override
        public Iterator<ZipEntry> iterator() {
            iteratorRequested.nowOrIfAlreadyThenThrow(() -> new IllegalStateException("Iterator can only be requested once!"));
            return new SimpleIterator<ZipEntry>() {
                @Override
                protected Optional<ZipEntry> nextIfAvailable() throws Exception {
                    return Optional.ofNullable(zip.getNextEntry());
                }
            };
        }

    }

    private Zip() {}
}
