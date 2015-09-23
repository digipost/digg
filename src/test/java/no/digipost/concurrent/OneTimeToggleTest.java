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
package no.digipost.concurrent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class OneTimeToggleTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final OneTimeToggle done = new OneTimeToggle();

    @Test
    public void toggleIsAllowedSeveralTimes() {
        assertThat(done.yet(), is(false));
        done.now();
        assertThat(done.yet(), is(true));
        done.now();
        assertThat(done.yet(), is(true));
    }

    @Test
    public void toggleOnceOrThrowException() {
        assertThat(done.yet(), is(false));
        done.nowOrIfAlreadyThenThrow(IllegalStateException::new);
        assertThat(done.yet(), is(true));

        expectedException.expect(IllegalStateException.class);
        done.nowOrIfAlreadyThenThrow(IllegalStateException::new);
    }
}
