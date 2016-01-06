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
package no.digipost.tuple;

/**
 * A type which may be viewed as a composite of two values, i.e. a {@link Tuple}.
 *
 * @param <T1> The type of the first value.
 * @param <T2> The type of the second value.
 */
@FunctionalInterface
public interface ViewableAsTuple<T1, T2> {

    /**
     * @return The {@link Tuple} view of this object.
     */
    Tuple<T1, T2> asTuple();

}
