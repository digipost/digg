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
package no.digipost;

import no.digipost.util.AtMostOne;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static no.digipost.DiggMaps.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DiggMapsTest {

    @Test
    public void unMapBySettingValueOnKey() {
        Map<AtomicReference<String>, String> map = new HashMap<>();
        map.put(new AtomicReference<>(), "x");

        assertThat(AtMostOne.from(unMapToKeys(map, AtomicReference::set)).get().get().get(), is("x"));
    }


    @Test
    public void unMapBySettingKeyOnValue() {
        Map<String, AtomicReference<String>> map = new HashMap<>();
        map.put("x", new AtomicReference<>());

        assertThat(AtMostOne.from(unMapToValues(map, AtomicReference::set)).get().get().get(), is("x"));
    }

    @Test
    public void unMapUsingBiFunction() {
        Map<AtomicReference<String>, String> map = new HashMap<>();
        map.put(new AtomicReference<>(), "x");

        assertThat(AtMostOne.from(unMap(map, (k, v) -> k.updateAndGet(old -> v))).get().get(), is("x"));
    }
}
