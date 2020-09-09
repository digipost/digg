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
package no.digipost.util;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface GetsNamedValue<V> {

    Optional<V> getFrom(Function<String, ?> getter);

    default Optional<V> getFrom(Map<String, ? super V> map) {
        return getFrom(map::get);
    }

    default V requireFrom(Map<String, ? super V> map) {
        return requireFrom(map::get);
    }

    default V requireFrom(Function<String, ?> getter) {
        return getFrom(getter).orElseThrow(() -> new NotFound(this));
    }


    public static class NotFound extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public NotFound(GetsNamedValue<?> valueGetter) {
            super(valueGetter + " could not resolve to a value.");
        }
    }

}
