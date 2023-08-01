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
package no.digipost.io;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.io.Serializable;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.digipost.DiggExceptions.applyUnchecked;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.reflections.scanners.Scanners.SubTypes;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

public class SerializableClassesTest {

    @Test
    public void serializableClassesMustDefineSerialVersionUID() {

        List<Class<? extends Object>> serializables = new Reflections(new ConfigurationBuilder().setScanners(SubTypes).forPackages("no.digipost"))
            .getAll(SubTypes).stream()
            .filter(typeName -> typeName.startsWith("no.digipost"))
            .map(typeName -> applyUnchecked(Class::forName, typeName))
            .filter(c -> !c.getName().contains("Test"))
            .filter(c -> !Enum.class.isAssignableFrom(c))
            .filter(Serializable.class::isAssignableFrom)
            .collect(toList());

        assertAll(serializables.stream().map(TypeInspection::new).map(serializableType ->
            () -> assertThat(serializableType + " defines serialVersionUID", serializableType, where(t -> t.definesFieldNamed("serialVersionUID")))));

    }

    static final class TypeInspection {
        final Class<?> cls;

        TypeInspection(Class<?> cls) {
            this.cls = cls;
        }

        boolean definesFieldNamed(String name) {
            try {
                cls.getDeclaredField(name);
                return true;
            } catch (NoSuchFieldException e) {
                return false;
            }
        }

        @Override
        public String toString() {
            return "inspection of " + cls.getName();
        }
    }
}
