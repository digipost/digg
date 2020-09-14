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
package no.digipost.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A signal mediator is a many-to-one carrier of a simple "no-content"
 * signal that one other part is interested in occurring. Any threads may provide as many
 * {@link #signal() signals} to the mediator they like, the first occurring signal will enable
 * <em>one</em> unblocking invocation of the {@link #signalWaiter}.{@link Waiter#doWait() doWait()},
 * and any more signaling will be silently discarded until the "consumption" of the signal
 * is done. When a call to {@link Waiter#doWait() doWait()} has completed, the
 * mediator will accept a new signal again.
 */
public final class SignalMediator {

    public final Waiter signalWaiter;


    private enum Signal { $ }
    private final BlockingQueue<Signal> signalHolder = new LinkedBlockingQueue<>(1);



    public SignalMediator() {
        this(SignalMediator.class.getSimpleName());
    }

    public SignalMediator(String name) {
        signalWaiter = new Waiter() {
            @Override
            public void doWait() {
                try {
                    signalHolder.take();
                } catch (InterruptedException e) {
                    throw new WasInterrupted(name + " was interrupted", e);
                }
            }
        };
    }

    public void signal() {
        signalHolder.offer(Signal.$);
    }

}
