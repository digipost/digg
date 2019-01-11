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

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class SignalMediatorTest {

    @Test
    public void singleSignalIsMediated() {
        SignalMediator signalMediator = new SignalMediator();

        signalMediator.signal();
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> signalMediator.signalWaiter.doWait());
    }

    @Test
    public void multipleSignalsAreTreatedAsOneUntilTaken() throws Exception {
        SignalMediator signalMediator = new SignalMediator();

        signalMediator.signal();
        signalMediator.signal();
        signalMediator.signal();
        signalMediator.signal();
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> signalMediator.signalWaiter.doWait());

        CompletableFuture<?> waitForever = CompletableFuture.runAsync(() -> signalMediator.signalWaiter.doWait());
        assertThrows(TimeoutException.class, () -> waitForever.get(700, MILLISECONDS));
    }
}
