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
package no.digipost.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import java.util.function.Function;

/**
 * Provides a way create {@link XmlAdapter} subclasses with a bit less boilerplate code,
 * using Java 8 lambas instead of implementing the {@link XmlAdapter#marshal(Object)} and
 * {@link XmlAdapter}{@link #unmarshal(Object)} methods.
 *
 * @param <BoundType>
 *      The type that JAXB doesn't know how to handle. An adapter is written
 *      to allow this type to be used as an in-memory representation through
 *      the <tt>ValueType</tt>.
 * @param <ValueType>
 *      The type that JAXB knows how to handle out of the box.
 */
public abstract class SimpleXmlAdapter<ValueType, BoundType> extends XmlAdapter<ValueType, BoundType> {

    public interface Unmarshal<ValueType, BoundType> extends Function<ValueType, BoundType> {}
    public interface Marshal<BoundType, ValueType> extends Function<BoundType, ValueType> {}



    private final Function<ValueType, BoundType> unmarshal;
    private final Function<BoundType, ValueType> marshal;


    /**
     * Create an adapter which only supports marshalling, i.e. for converting Java objects to XML.
     * If the adapter is used for unmarshalling it will throw an exception.
     *
     * @param marshal this function defines how to convert from your custom {@code BoundType} to the
     *                {@code ValueType} which JAXB knows how to handle.
     */
    protected SimpleXmlAdapter(Marshal<BoundType, ValueType> marshal) {
        this(value -> { throw new UnsupportedOperationException("This adapter only supports marshalling, and is not able to convert the value " + value + " retrieved from XML!"); },
             marshal);
    }


    /**
     * Create an adapter which only supports unmarshalling, i.e. for converting XML into Java objects.
     * If the adapter is used for marshalling it will throw an exception.
     *
     * @param unmarshal this function defines how to convert from the {@code ValueType}
     *                  which JAXB knows how to handle into your custom {@code BoundType}.
     */
    protected SimpleXmlAdapter(Unmarshal<ValueType, BoundType> unmarshal) {
        this(unmarshal,
             bound -> { throw new UnsupportedOperationException("This adapter only supports unmarshalling, and is not able to produce a value from " + bound + " which JAXB will be able to marshal to XML!"); });
    }


    /**
     * Create an adapter for converting between a {@code ValueType} natively supported by JAXB
     * and a custom {@code BoundType}.
     *
     * @param unmarshal this function defines how to convert from the {@code ValueType}
     *                  which JAXB knows how to handle into your custom {@code BoundType}.
     * @param marshal this function defines how to convert from your custom {@code BoundType} to the
     *                {@code ValueType} which JAXB knows how to handle.
     */
    protected SimpleXmlAdapter(Unmarshal<ValueType, BoundType> unmarshal, Marshal<BoundType, ValueType> marshal) {
        this.unmarshal = unmarshal;
        this.marshal = marshal;
    }


    @Override
    public final BoundType unmarshal(ValueType v) {
        return unmarshal.apply(v);
    }

    @Override
    public final ValueType marshal(BoundType v) {
        return marshal.apply(v);
    }
}
