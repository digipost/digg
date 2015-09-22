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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class SignalMediatorTest {

    @Rule
    public final Timeout timeout = new Timeout(2, SECONDS);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void singleSignalIsMediated() {
        SignalMediator signalMediator = new SignalMediator();

        signalMediator.signal();
        signalMediator.signalWaiter.doWait();
    }

    @Test
    public void multipleSignalsAreTreatedAsOneUntilTaken() throws Exception {
        SignalMediator signalMediator = new SignalMediator();

        signalMediator.signal();
        signalMediator.signal();
        signalMediator.signal();
        signalMediator.signal();
        signalMediator.signalWaiter.doWait();

        CompletableFuture<Void> waitForever = CompletableFuture.runAsync(() -> signalMediator.signalWaiter.doWait());
        expectedException.expect(TimeoutException.class);
        waitForever.get(700, MILLISECONDS);
    }
}
