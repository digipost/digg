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
package no.digipost.io;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

public class SerializableClassesTest {

    @Test
    public void serializableClassesMustDefineSerialVersionUID() throws IOException {

        List<Class<?>> serializableClassesWithoutSerialVersionUID = ClassPath
            .from(SerializableClassesTest.class.getClassLoader())
            .getTopLevelClassesRecursive("no.digipost")
            .stream().map(ClassInfo::load)
            .flatMap(c -> Stream.concat(Stream.of(c), Stream.of(c.getDeclaredClasses())))
            .filter(c -> !c.getName().contains("Test"))
            .filter(c -> !Enum.class.isAssignableFrom(c))
            .filter(Serializable.class::isAssignableFrom)
            .filter(c -> {
                try {
                    c.getDeclaredField("serialVersionUID");
                    return false;
                } catch (NoSuchFieldException e) {
                    return true;
                }
            }).collect(toList());

        assertThat(serializableClassesWithoutSerialVersionUID, empty());
    }
}
