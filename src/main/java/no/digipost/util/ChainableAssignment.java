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

/**
 * An {@link Assignment} with the added ability to chain several assignments
 * with {@link #is(Object)}.
 *
 * @param <C> The "chain" type, which is returned from each call to {@link #is(Object)}
 *            to allow chained invocation.
 */
public final class ChainableAssignment<V, C> implements Assignment<V> {

    private Assignment<V> assignment;
    private C chainReturnObject;

    public ChainableAssignment(Assignment<V> assignment, C chainReturnObject) {
        this.assignment = assignment;
        this.chainReturnObject = chainReturnObject;
    }

    /**
     * Assign the given value and return object to allow chained invocation.
     *
     * @param value the value to assign
     *
     * @return the object to allow further chained invocation
     */
    public C is(V value) {
        set(value);
        return chainReturnObject;
    }

    @Override
    public V get() {
        return assignment.get();
    }

    @Override
    public void set(V value) {
        assignment.set(value);
    }

}
