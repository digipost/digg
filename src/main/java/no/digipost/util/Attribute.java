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

import no.digipost.tuple.Tuple;

import java.io.Serializable;
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
public final class Attribute<V> implements GetsNamedValue<V>, SetsNamedValue<V>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The name of this attribute.
     */
    public final String name;

    public Attribute(String name) {
        this.name = name;
    }

    /**
     * Create a new attribute of the <em>same type</em> with another name.
     *
     * @param anotherName the name for the new attribute
     * @return the new attribute instance. If the given name is equal to this attribute's name, the same instance is returned.
     *
     * @see #name
     */
    public Attribute<V> withName(String anotherName) {
        return !Objects.equals(name, anotherName) ? new Attribute<>(anotherName) : this;
    }

    /**
     * Bundle an attribute with an associated value in a {@link Tuple}.
     *
     * @param value the value to associate with the attribute
     * @return a tuple containing the attribute and the value.
     */
    public Tuple<Attribute<V>, V> withValue(V value) {
        return Tuple.of(this, value);
    }

    @Override
    public void setOn(BiConsumer<String, ? super V> setter, V value) {
        setter.accept(name, value);
    }

    @Override
    public Optional<V> getFrom(Function<String, ?> getter) {
        @SuppressWarnings("unchecked")
        V attributeValue = (V) getter.apply(name);
        return ofNullable(attributeValue);
    }

    @Override
    public String toString() {
        return "attribute '" + name + "'";
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
