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

import no.digipost.DiggExceptions;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.io.*;

import static org.hamcrest.Matchers.equalTo;

public final class DiggMatchers {

    public static Matcher<Serializable> isEffectivelySerializable() {
        return new TypeSafeDiagnosingMatcher<Serializable>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("can be serialized");
            }

            @Override
            protected boolean matchesSafely(Serializable original, Description mismatchDescription) {
                try {
                    ByteArrayOutputStream writtenBytes = new ByteArrayOutputStream();
                    try (
                         ByteArrayOutputStream bytes = writtenBytes;
                         ObjectOutputStream serializer = new ObjectOutputStream(bytes)) {

                        serializer.writeObject(original);
                    } catch (NotSerializableException notSerializable) {
                        mismatchDescription.appendText(original.getClass().getName()).appendText(", unable to serialize: ").appendText(notSerializable.getMessage());
                        return false;
                    }

                    try (ObjectInputStream unserializer = new ObjectInputStream(new ByteArrayInputStream(writtenBytes.toByteArray()))) {
                        Object unserialized = unserializer.readObject();
                        Matcher<Serializable> matcher = equalTo(original);
                        if (!matcher.matches(unserialized)) {
                            matcher.describeMismatch(unserialized, mismatchDescription);
                            return false;
                        } else {
                            return true;
                        }
                    }
                } catch (ClassNotFoundException | IOException e) {
                    throw DiggExceptions.asUnchecked(e);
                }

            }

        };
    }


    private DiggMatchers() {}
}
