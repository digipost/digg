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

import java.util.function.Consumer;

/**
 * An adapter to enable any arbitrary object to be managed by the
 * <em>try-with-resources</em> facility of Java &gt;= 7, i.e. it will have a
 * closing operation invoked when exiting the {@code try}-block.
 *
 * @param <T> The type of the arbitrary object which will have an operation invoked
 *            on exiting from a try-with-resources block.
 */
public final class AutoClosed<T> extends AutoCloseableAdapter<T, RuntimeException> implements AutoCloseable {

    public AutoClosed(T managedObject, Consumer<? super T> closeOperation) {
        super(managedObject, closeOperation::accept);
    }

}
