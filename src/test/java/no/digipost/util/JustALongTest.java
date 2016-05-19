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
import org.junit.Test;

import static nl.jqno.equalsverifier.Warning.NULL_FIELDS;
import static no.digipost.util.DiggMatchers.isEffectivelySerializable;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class JustALongTest {

    @Test
    public void correctEqualsAndHashCode() {
        EqualsVerifier.forClass(JustAnId.class).withRedefinedSuperclass().withOnlyTheseFields("theLong").suppress(NULL_FIELDS).verify();
    }

    @Test
    public void toStringContainsTheStringAndClassAsDescription() {
        JustAnId justTheNameJohn = WithId.of(1337);
        assertThat(justTheNameJohn.toString(), containsString("1337"));
        assertThat(justTheNameJohn.toString(), containsString(JustAnId.class.getSimpleName()));
    }

    @Test
    public void toStringContainsTheStringAndGivenDescription() {
        JustAnId justTheSurnameSimpson = WithId.of(1, "entity id");
        assertThat(justTheSurnameSimpson.toString(), containsString("1"));
        assertThat(justTheSurnameSimpson.toString(), containsString("entity id"));
        assertThat(justTheSurnameSimpson.toString(), not(containsString(JustAnId.class.getSimpleName())));
    }

    @Test
    public void isSerializable() throws Exception {
        assertThat(WithId.of(42), isEffectivelySerializable());
    }


    interface WithId {

        static JustAnId of(long name) {
            return new JustAnId(name);
        }

        static JustAnId of(long name, String description) {
            return new JustAnId(name, description);
        }

        long getId();
    }

    static final class JustAnId extends JustALong implements WithId {

        private JustAnId(long name) {
            super(name);
        }

        private JustAnId(long name, String description) {
            super(name, description);
        }

        @Override
        public long getId() {
            return theLong;
        }

    }

}

