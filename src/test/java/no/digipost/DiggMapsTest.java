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
package no.digipost;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static no.digipost.DiggCollectors.allowAtMostOne;
import static no.digipost.DiggMaps.unMap;
import static no.digipost.DiggMaps.unMapToKeys;
import static no.digipost.DiggMaps.unMapToValues;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DiggMapsTest {

    @Test
    public void unMapBySettingValueOnKey() {
        Map<AtomicReference<String>, String> map = new HashMap<>();
        map.put(new AtomicReference<>(), "x");

        assertThat(unMapToKeys(map, AtomicReference::set).collect(allowAtMostOne()).get().get(), is("x"));
    }

    @Test
    public void unMapBySettingKeyOnValue() {
        Map<String, AtomicReference<String>> map = new HashMap<>();
        map.put("x", new AtomicReference<>());

        assertThat(unMapToValues(map, AtomicReference::set).collect(allowAtMostOne()).get().get(), is("x"));
    }

    @Test
    public void unMapUsingBiFunction() {
        Map<AtomicReference<String>, String> map = new HashMap<>();
        map.put(new AtomicReference<>(), "x");

        assertThat(unMap(map, (k, v) -> k.updateAndGet(old -> v)).collect(allowAtMostOne()).get(), is("x"));
    }
}
