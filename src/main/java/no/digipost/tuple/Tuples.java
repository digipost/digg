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
package no.digipost.tuple;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Utilies for working with tuples.
 */
public final class Tuples {

    /**
     * Convert a Java Collections {@link Map} to a Stream of {@link Tuple tuples}.
     *
     * @param <T1> The key type of the Map, becoming the type of the
     *             {@link Tuple#first() first} tuple value.
     * @param <T2> The value type of the Map, becoming the type of the
     *             {@link Tuple#second() second} tuple value.
     * @param map The map to convert.
     *
     * @return The resulting Stream of tuples.
     */
    public static final <T1, T2> Stream<Tuple<T1, T2>> ofMap(Map<T1, T2> map) {
        return map.entrySet().stream().map(Tuple::ofMapEntry);
    }

    private Tuples() {
    }
}
