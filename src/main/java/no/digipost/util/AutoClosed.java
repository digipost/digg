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
package no.digipost.util;

import no.digipost.function.ThrowingConsumer;

import static no.digipost.DiggExceptions.runUnchecked;

/**
 * An adapter to enable any arbitrary object to be managed by the
 * <em>try-with-resources</em> facility of Java >= 7, i.e. it will have a
 * closing operation invoked when exiting the {@code try}-block.
 *
 * @param <T> The type of the arbitrary object which will have an operation invoked
 *            on exiting from a try-with-resources block.
 * @param <X> The type of exception which may be throwed by the {@code closeOperation}
 */
public final class AutoClosed<T, X extends Exception> implements AutoCloseable {

    private final T managedObject;
    private final ThrowingConsumer<? super T, X> closeOperation;

    public AutoClosed(T managedObject, ThrowingConsumer<? super T, X> closeOperation) {
        this.managedObject = managedObject;
        this.closeOperation = closeOperation;
    }

    /**
     * @return the object managed by try-with-resources.
     */
    public T object() {
        return managedObject;
    }

    @Override
    public void close() throws X {
        closeOperation.accept(managedObject);
        if (managedObject instanceof AutoCloseable) {
            runUnchecked(() -> ((AutoCloseable) managedObject).close());
        }
    }


}
