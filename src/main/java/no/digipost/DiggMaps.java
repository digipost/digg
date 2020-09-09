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
package no.digipost;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public final class DiggMaps {

    public static <K, V> Stream<V> unMapToValues(Map<K, V> map, BiConsumer<? super V, ? super K> consumer) {
        return unMap(map, entry -> {
            consumer.accept(entry.getValue(), entry.getKey());
            return entry.getValue();
        });
    }

    public static <K, V> Stream<K> unMapToKeys(Map<K, V> map, BiConsumer<? super K, ? super V> consumer) {
        return unMap(map, entry -> {
            consumer.accept(entry.getKey(), entry.getValue());
            return entry.getKey();
        });
    }

    public static <K, V, R> Stream<R> unMap(Map<K, V> map, BiFunction<K, V, R> unmapper) {
        return map.entrySet().stream().map(entry -> unmapper.apply(entry.getKey(), entry.getValue()));
    }

    public static <K, V, R> Stream<R> unMap(Map<K, V> map, Function<Entry<K, V>, R> unmapper) {
        return map.entrySet().stream().map(unmapper);
    }

    private DiggMaps() {}
}
