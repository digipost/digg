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

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Optional.ofNullable;


/**
 * An {@code Attribute} defines a String-based name (or key) and the <em>type</em> of a value which
 * the name maps to. This construct offers pseudo-typesafe handling where objects of arbitrary
 * types are retrieved by name and must be manually casted.
 *
 * @param <V> The type of the object this attribute is mapping.
 */
public final class Attribute<V> implements Serializable {

    public final String name;

    public Attribute(String name) {
        this.name = name;
    }

    public void setOn(Map<String, ? super V> map, V value) {
        setOn(map::put, value);
    }

    public void setOn(BiConsumer<String, ? super V> setter, V value) {
        setter.accept(name, value);
    }

    public Optional<V> getFrom(Map<String, ? super V> map) {
        return getFrom(map::get);
    }

    public Optional<V> getFrom(Function<String, ?> getter) {
        @SuppressWarnings("unchecked")
        V attributeValue = (V) getter.apply(name);
        return ofNullable(attributeValue);
    }

    public V requireFrom(Map<String, ? super V> map) {
        return requireFrom(map::get);
    }

    public V requireFrom(Function<String, ?> getter) {
        return getFrom(getter).orElseThrow(() -> new NotFound(this));
    }

    @Override
    public String toString() {
        return "attribute '" + name + "'";
    }

    public static class NotFound extends RuntimeException {
        public NotFound(Attribute<?> attribute) {
            super("The " + attribute + " was not found!");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Attribute) {
            Attribute<?> that = (Attribute<?>) obj;
            return Objects.equals(this.name, that.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }
}
