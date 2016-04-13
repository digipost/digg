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

import no.digipost.tuple.Tuple;
import no.digipost.tuple.ViewableAsTuple;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.EnumSet.copyOf;
import static java.util.EnumSet.noneOf;

/**
 * An immutable collection of {@link Attribute attributes}.
 */
public final class AttributeMap implements Serializable {

    /**
     * Switches to indicate behavior other than default.
     */
    public enum Config implements Supplier<AttributeMap.Builder> {
        ALLOW_NULL_VALUES;

        @Override
        public Builder get() {
            return AttributeMap.buildNew(this);
        }

    }

    /**
     * An empty map with no attributes.
     */
    public static final AttributeMap EMPTY = buildNew().build();


    public static <V> AttributeMap.Builder with(ViewableAsTuple<? extends SetsNamedValue<V>, V> attributeWithValue, Config ... configSwitches) {
        return buildNew(configSwitches).and(attributeWithValue);
    }

    public static <V> AttributeMap.Builder with(SetsNamedValue<V> attribute, V value, Config ... configSwitches) {
        return buildNew(configSwitches).and(attribute, value);
    }

    public static AttributeMap.Builder buildNew(Config ... configSwitches) {
        return new Builder(configSwitches);
    }

    private static final Serializable NON_EXISTING_VALUE = new Serializable() {
        private Object readResolve() {
            return NON_EXISTING_VALUE;
        }

        @Override
        public String toString() {
            return "[non-existing value]";
        };
    };


    /**
     * Builder to incrementally construct an immutable {@link AttributeMap}.
     */
    public static class Builder {

        private final ConcurrentMap<String, Object> incrementalMap = new ConcurrentHashMap<>();
        private final Set<Config> configSwitches;

        private Builder(Config ... configSwitches) {
            this.configSwitches = configSwitches.length == 0 ? noneOf(Config.class) : copyOf(asList(configSwitches));
        }


        /**
         * Add an attribute coupled with a value.
         *
         * @param attributeWithValue the attribute and the value.
         * @return the builder
         */
        public <V> Builder and(ViewableAsTuple<? extends SetsNamedValue<V>, V> attributeWithValue) {
            Tuple<? extends SetsNamedValue<V>, V> t = attributeWithValue.asTuple();
            return and(t.first(), t.second());
        }


        /**
         * Add an attribute with a value.
         *
         * @param attribute the attribute
         * @param value the value to bind to the attribute
         * @return the builder
         */
        public <V> Builder and(SetsNamedValue<V> attribute, V value) {
            if (value != null || configSwitches.contains(Config.ALLOW_NULL_VALUES)) {
                attribute.setOn((name, v) -> incrementalMap.put(name, v != null ? v : NON_EXISTING_VALUE), value);
            }
            return this;
        }

        /**
         * Add all attributes from an existing {@link AttributeMap}.
         *
         * @param otherMap contains the other attributes to add.
         * @return the builder
         */
        public Builder and(AttributeMap otherMap) {
            incrementalMap.putAll(otherMap.untypedMap);
            return this;
        }

        /**
         * Add all attributes from another {@link AttributeMap.Builder}
         *
         * @param otherBuilder contains the other attributes to add
         * @return the builder
         */
        public Builder and(AttributeMap.Builder otherBuilder) {
            incrementalMap.putAll(otherBuilder.incrementalMap);
            return this;
        }

        /**
         * @return a new immutable {@link AttributeMap} containing the attributes and
         *         values added to the builder.
         */
        public AttributeMap build() {
            return new AttributeMap(incrementalMap);
        }
    }



    private static final long serialVersionUID = 1L;

    private final Map<String, Object> untypedMap;

    private AttributeMap(Map<String, Object> untypedMap) {
        this.untypedMap = unmodifiableMap(untypedMap);
    }


    /**
     * Retrieve a required attribute value. This method throws an exception
     * if the attribute value is not present.
     *
     * @param attribute the attribute to retrieve.
     * @return the value
     * @throws GetsNamedValue.NotFound if the attribute is not present.
     */
    public <V> V get(GetsNamedValue<V> attribute) {
        V value = attribute.requireFrom(untypedMap);
        return value != NON_EXISTING_VALUE ? value : null;
    }

    /**
     * @return the number of attributes contained in this map.
     */
    public int size() {
        return untypedMap.size();
    }

    /**
     * @return {@code true} if this map contains no attributes, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return untypedMap.isEmpty();
    }

    @Override
    public String toString() {
        return "attributes: " + untypedMap.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AttributeMap) {
            AttributeMap that = (AttributeMap) obj;
            return Objects.equals(this.untypedMap, that.untypedMap);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(untypedMap);
    }
}
