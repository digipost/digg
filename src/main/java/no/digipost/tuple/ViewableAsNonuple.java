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
 * A type which may be viewed as a composite of nine values, i.e. a {@link Nonuple}.
 *
 * @param <T1> The type of the first value.
 * @param <T2> The type of the second value.
 * @param <T3> The type of the third value.
 * @param <T4> The type of the fourth value.
 * @param <T5> The type of the fifth value.
 * @param <T6> The type of the sixth value.
 * @param <T7> The type of the seventh value.
 * @param <T8> The type of the eight value.
 * @param <T9> The type of the ninth value.
 */
@FunctionalInterface
public interface ViewableAsNonuple<T1, T2, T3, T4, T5, T6, T7, T8, T9> {

    /**
     * @return The {@link Nonuple} view of this object.
     */
    Nonuple<T1, T2, T3, T4, T5, T6, T7, T8, T9> asNonuple();

}
