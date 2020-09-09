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

import no.digipost.function.SerializableFunction;

import java.io.Serializable;
import java.util.Objects;

/**
 * Base class for creating simple "typed primitives" value classes. Typically
 * for when you do not want to pass around simple Strings for values that has
 * certain (business-) semantics in your application, even though a String does adequately
 * express the value. By extending this class you do not have to implement the
 * equals and hashcode. You would <em>not</em> refer to this class other than with
 * an {@code extends JustA<SomeType>} decalaration in your class definition.
 *
 * <p>A common pattern for using this class would be:</p>
 *
 * <pre>
 * interface WithName {
 *
 *   static JustAName of(String name) {
 *     return new JustAName(name);
 *   }
 *
 *   final class JustAName extends JustA&lt;String&gt; implements WithName {
 *
 *      private JustAName(String name) {
 *        super(name);
 *      }
 *
 *      public String getName() {
 *        return theValue;
 *      }
 *    }
 *
 *
 *    String getName();
 * }</pre>
 *
 * <p>
 * This yields certain benefits, as you now have an interface ({@code WithName}) to use with your
 * more complex domain types, which among other stuff, having a name. And you have a neat way to pass
 * typed simple values using {@code WithName.of("John Doe")}. This especially enhances the readability of
 * method invocations with multiple arguments, as you must explicitly state the semantics of each argument.
 * Say a query of some sort: <pre>{@code db.findPerson(WithName.of("John Doe"), WithPhonenumber.of("555-98437"))}</pre>
 * If the method parameters for some reason are refactored to switch places, the code invoking the method will not compile
 * anymore, as the arguments are not given in the correct order even though they are really just Strings.
 *
 *
 * @param <T> The type of wrapped value. Should be an immutable value-type.
 */
public abstract class JustA<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final T theValue;

    private final String description;
    private final SerializableFunction<? super T, String> toString;


    protected JustA(T theValue) {
        this(theValue, Object::toString);
    }

    protected JustA(T theValue, String description) {
        this(theValue, description, Object::toString);
    }

    protected JustA(T theValue, SerializableFunction<? super T, String>  valueToString) {
        this(theValue, null, valueToString);
    }

    protected JustA(T theValue, String description, SerializableFunction<? super T, String> valueToString) {
        this.theValue = theValue;
        this.description = description != null ? description : getClass().getSimpleName();
        this.toString = valueToString;
    }


    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof JustA && getClass().isInstance(obj)) {
            JustA<?> that = (JustA<?>) obj;
            return Objects.equals(this.theValue, that.theValue);
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(theValue);
    }

    @Override
    public String toString() {
        return description + " '" + toString.apply(theValue) + "'";
    }
}
