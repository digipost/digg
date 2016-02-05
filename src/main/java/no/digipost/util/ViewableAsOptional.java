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
package no.digipost.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;

@FunctionalInterface
public interface ViewableAsOptional<V> {

    /**
     * An object which was attempted to be viewed as a {@link java.util.Optional}
     * representation of itself contained more than one element, and is thus
     * not applicable for such conversion.
     */
    class TooManyElements extends RuntimeException {

        public TooManyElements(List<? extends Object> objects) {
            this(objects, (Throwable) null);
        }

        public TooManyElements(List<? extends Object> objects, Throwable cause) {
            this(objects, Object::toString, cause);
        }

        public <T extends Object> TooManyElements(List<T> objects, Function<? super T, ? extends CharSequence> toString) {
            this(objects, toString, null);
        }

        public <T extends Object> TooManyElements(List<T> objects, Function<? super T, ? extends CharSequence> toString, Throwable cause) {
            this("Expected at most one element, but there were excess ones. All elements: [" + objects.stream().map(toString).collect(joining(", ")) + "]", cause);
        }

        public TooManyElements(Object allowed, Object firstExcess) {
            this(allowed, firstExcess, null);
        }

        public TooManyElements(Object allowed, Object firstExcess, Throwable cause) {
            this("Expected only the element '" + allowed + "', but there were at least one excess one: '" + firstExcess + "'", cause);
        }

        public TooManyElements() {
            this("Exepcted at most one element, but there were excess ones", null);
        }

        public TooManyElements(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Get an {@link java.util.Optional} representation of this object. If it contains
     * more elements than one, and thus cannot be viewed as an {@code Optional} without
     * loosing any information, a {@link TooManyElements} is thrown.
     *
     * @return the {@link java.util.Optional} representation of this object.
     * @throws TooManyElements if this object contains more than one element, and thus
     *                         is not applicable for lossless conversion to an Optional.
     */
    Optional<V> toOptional() throws TooManyElements;
}
