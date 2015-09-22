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

import java.time.Duration;

/**
 * Instances of this class has no other purpose than to hold execution.
 * The use case is typically to
 * implement a periodically running process.
 * <p>
 * It will throw Waiter.WasInterrupted if
 * the current thread is {@link Thread#interrupt() interrupted} while
 * {@link #doWait()} is blocking.
 */
public abstract class Waiter {


	/**
	 * Invoking this method will do nothing but block for
	 * any duration decided by the instance it is
	 * invoked on.
	 *
	 * @throws WasInterrupted if the current thread is
	 *         {@link Thread#interrupt() interrupted} while the
	 *         method is blocking.
	 */
    public abstract void doWait();

    Waiter() {
    }




    public static Waiter wait(Duration duration) {
    	return wait(duration, Waiter.class.getSimpleName());
    }

    public static Waiter wait(Duration duration, String waiterName) {
    	return new Waiter() {
			@Override
			public void doWait() {
				try {
		    		Thread.sleep(duration.toMillis());
		    	} catch (InterruptedException e) {
		    		throw new WasInterrupted(waiterName + " was interrupted.", e);
		    	}
			}
		};
    }


    public static class WasInterrupted extends RuntimeException {
    	protected WasInterrupted(String message, InterruptedException e) {
    		super(message + " " + e.getClass().getSimpleName() + " '" + e.getMessage() + "'", e);
    	}
    }

}
