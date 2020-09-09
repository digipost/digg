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

import no.digipost.collection.EnforceAtMostOneElementCollector;
import no.digipost.concurrent.OneTimeAssignment;
import org.junit.jupiter.api.Test;

import java.util.function.BinaryOperator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EnforceAtMostOneElementCollectorTest {

    private final BinaryOperator<OneTimeAssignment<String>> combiner = new EnforceAtMostOneElementCollector<String>(ViewableAsOptional.TooManyElements::new).combiner();

    @Test
    public void combinerThrowsException() {
        assertThat(combiner.apply(OneTimeAssignment.newInstance(), OneTimeAssignment.newInstance()).get(), nullValue());

        OneTimeAssignment<String> v1 = OneTimeAssignment.newInstance();
        v1.set("1");
        assertThat(combiner.apply(v1, OneTimeAssignment.newInstance()).get(), is("1"));
        assertThat(combiner.apply(OneTimeAssignment.newInstance(), v1).get(), is("1"));

        OneTimeAssignment<String> v2 = OneTimeAssignment.newInstance();
        v2.set("2");
        assertThrows(ViewableAsOptional.TooManyElements.class, ()-> combiner.apply(v1, v2));
    }
}
