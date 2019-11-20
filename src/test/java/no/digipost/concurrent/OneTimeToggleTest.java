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

import no.digipost.DiggConcurrent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static co.unruly.matchers.Java8Matchers.where;
import static co.unruly.matchers.Java8Matchers.whereNot;
import static java.util.stream.Stream.generate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OneTimeToggleTest {

    private final OneTimeToggle done = new OneTimeToggle();

    @Test
    public void toggleIsAllowedSeveralTimes() {
        assertThat(done, whereNot(OneTimeToggle::yet));
        done.now();
        assertThat(done, where(OneTimeToggle::yet));
        done.now();
        assertThat(done, where(OneTimeToggle::yet));
    }

    @Test
    public void toggleOnceOrThrowException() {
        assertThat(done, whereNot(OneTimeToggle::yet));
        done.nowOrIfAlreadyThenThrow(IllegalStateException::new);
        assertThat(done, where(OneTimeToggle::yet));

        assertThrows(IllegalStateException.class, () -> done.nowOrIfAlreadyThenThrow(IllegalStateException::new));
    }

    @Test
    void toggleAndExecuteAtMostOnce() {
        AtomicInteger counter = new AtomicInteger(0);
        done.nowAndUnlessAlreadyToggled(() -> { counter.incrementAndGet(); });
        done.nowAndUnlessAlreadyToggled(() -> { counter.incrementAndGet(); });
        assertThat(done, where(OneTimeToggle::yet));
        assertThat(counter, where(Number::intValue, is(1)));

    }


    private static final ExecutorService executor = Executors.newWorkStealingPool(20);

    @RepeatedTest(200)
    @Timeout(10)
    void threadSafeToggleAndExecuteAtMostOnce() {
        OneTimeToggle done = new OneTimeToggle();
        AtomicInteger counter = new AtomicInteger(0);

        @SuppressWarnings("rawtypes")
        CompletableFuture[] allToggles = generate(() -> (Runnable) () -> done.nowAndUnlessAlreadyToggled(counter::incrementAndGet))
            .parallel()
            .limit(1000)
            .map(toggler -> CompletableFuture.runAsync(toggler, executor))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(allToggles).join();

        assertThat(done, where(OneTimeToggle::yet));
        assertThat(counter, where(Number::intValue, is(1)));
    }

    @AfterAll
    static void shutdownExecutor() {
        DiggConcurrent.ensureShutdown(executor, Duration.ofSeconds(3));
    }




}
