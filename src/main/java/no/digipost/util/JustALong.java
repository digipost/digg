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

import no.digipost.function.SerializableFunction;

import java.io.Serializable;
import java.util.Objects;

/**
 * Base class for creating simple "typed {@code long}" value classes, for cases
 * when you do not want to pass around simple {@code long}s for numbers that has
 * certain (business-) semantics in your application, even though a {@code long}
 * does adequately express the value. By extending this class you do not have to
 * implement the equals and hashcode. You would <em>not</em> refer to this class
 * other than with an {@code extends JustALong} decalaration in your class definition.
 *
 * <p><em>This is a special case implementation of {@link JustA} to avoid the boxing cost
 * for primitive {@code long}s.</em> The typical use for this are for <em>id</em>s, and classes
 * where instances are uniquely distinguished by their {@code long} ids, and equals/hashcode
 * will exercise correct behavior when <em>only</em> operating on the id. Any additional
 * fields in an extending class should usually be declared {@code final}.</p>
 *
 * <p>A common pattern for using this class would be:</p>
 *
 * <pre>
 * interface WithId {
 *
 *   static JustAnId of(long id) {
 *     return new JustAnId(id);
 *   }
 *
 *   final class JustAnId extends JustALong implements WithId {
 *
 *      private JustAnId(String id) {
 *        super(id);
 *      }
 *
 *      public long getId() {
 *        return theValue;
 *      }
 *    }
 *
 *
 *    long getId();
 * }</pre>
 *
 * @see JustA {@code JustA<SomeType>} is the generic version for any (reference-) type.
 *
 * @param <T> The type of wrapped value. Should be an immutable value-type.
 */
public abstract class JustALong implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final long theLong;

    private final String description;
    private final SerializableFunction<? super Long, String> toString;


    protected JustALong(long theValue) {
        this(theValue, Object::toString);
    }

    protected JustALong(long theValue, String description) {
        this(theValue, description, Object::toString);
    }

    protected JustALong(long theValue, SerializableFunction<? super Long, String>  valueToString) {
        this(theValue, null, valueToString);
    }

    protected JustALong(long theValue, String description, SerializableFunction<? super Long, String> valueToString) {
        this.theLong = theValue;
        this.description = description != null ? description : getClass().getSimpleName();
        this.toString = valueToString;
    }


    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof JustALong && getClass().isInstance(obj)) {
            JustALong that = (JustALong) obj;
            return Objects.equals(this.theLong, that.theLong);
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(theLong);
    }

    @Override
    public String toString() {
        return description + " '" + toString.apply(theLong) + "'";
    }
}
