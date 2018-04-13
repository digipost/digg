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

abstract class AutoCloseableAdapter<T, X extends Exception> {
    private final T managedObject;
    private final ThrowingConsumer<? super T, X> closeOperation;

    protected AutoCloseableAdapter(T managedObject, ThrowingConsumer<? super T, X> closeOperation) {
        this.managedObject = managedObject;
        this.closeOperation = closeOperation;
    }

    /**
     * @return the object managed by try-with-resources.
     */
    public T object() {
        return managedObject;
    }

    /**
     * @see AutoCloseable#close()
     */
    public void close() throws X {
        closeOperation.accept(managedObject);
        if (managedObject instanceof AutoCloseable) {
            runUnchecked(() -> ((AutoCloseable) managedObject).close());
        }
    }

}
