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
