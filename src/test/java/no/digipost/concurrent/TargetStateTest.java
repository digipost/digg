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
import org.junit.rules.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static no.digipost.concurrent.TargetState.TaskControl.EXIT;
import static no.digipost.concurrent.TargetState.TaskControl.TRY_REPEAT;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TargetStateTest {

	@Rule
	public final Timeout timeout = new Timeout(1, TimeUnit.SECONDS);

	@Rule
	public final ExpectedException expectedException = ExpectedException.none();

	@Test
	public void loopUntilTargetStateIsReached() {
		CountDownLatch threeTimes = new CountDownLatch(3);
		TargetState isDone = () -> threeTimes.getCount() == 0;
		isDone.untilThen(threeTimes::countDown, t -> {});
		assertThat(isDone.yet(), is(true));
	}

	@Test
	public void loopUntilTaskSignalsEXIT() {
		AtomicInteger taskIterations = new AtomicInteger(0);
		TargetState isDone = () -> false;
		isDone.untilThen(() -> taskIterations.incrementAndGet() == 3 ? EXIT : TRY_REPEAT, exception -> {});
		assertThat(taskIterations.get(), is(3));
		assertThat(isDone.yet(), is(false));
	}

	@Test
	public void returningNullFromTaskIsInvalid() {
		TargetState isDone = () -> false;
		expectedException.expect(IllegalStateException.class);
		isDone.untilThen(() -> null, exception -> { throw (RuntimeException) exception; });
	}
}
