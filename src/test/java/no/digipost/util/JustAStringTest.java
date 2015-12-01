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

import nl.jqno.equalsverifier.EqualsVerifier;
import no.digipost.util.WithName.JustAName;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static nl.jqno.equalsverifier.Warning.NULL_FIELDS;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JustAStringTest {

    @Test
    public void correctEqualsAndHashCode() {
        EqualsVerifier.forClass(JustAName.class).withRedefinedSuperclass().suppress(NULL_FIELDS).verify();
    }

    @Test
    public void toStringContainsTheStringAndClassAsDescription() {
        JustAName justTheNameJohn = WithName.of("John");
        assertThat(justTheNameJohn.toString(), containsString("John"));
        assertThat(justTheNameJohn.toString(), containsString(JustAName.class.getSimpleName()));
    }

    @Test
    public void toStringContainsTheStringAndGivenDescription() {
        JustAName justTheSurnameSimpson = WithName.of("Simpson", "surname");
        assertThat(justTheSurnameSimpson.toString(), containsString("Simpson"));
        assertThat(justTheSurnameSimpson.toString(), containsString("surname"));
        assertThat(justTheSurnameSimpson.toString(), not(containsString(JustAName.class.getSimpleName())));
    }

    @Test
    public void isSerializable() throws Exception {
        JustAName original = WithName.of("Mary");
        ByteArrayOutputStream writtenBytes = new ByteArrayOutputStream();
        try (
                ByteArrayOutputStream bytes = writtenBytes;
                ObjectOutputStream serializer = new ObjectOutputStream(bytes)) {

            serializer.writeObject(original);
        }

        try (ObjectInputStream unserializer = new ObjectInputStream(new ByteArrayInputStream(writtenBytes.toByteArray()))) {
            JustAName unserialized = (JustAName) unserializer.readObject();
            assertThat(unserialized, equalTo(original));
        }
    }

}

interface WithName {

    static JustAName of(String name) {
        return new JustAName(name);
    }

    static JustAName of(String name, String description) {
        return new JustAName(name, description);
    }

    final class JustAName extends JustA<String> implements WithName {

        private JustAName(String name) {
            super(name);
        }

        private JustAName(String name, String description) {
            super(name, description);
        }

        @Override
        public String getName() {
            return theValue;
        }

    }

    String getName();
}
