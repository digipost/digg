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
package no.digipost.function;


/**
 * Represents a function that accepts a object-valued and an
 * {@code int}-valued argument. This is the {@code (reference, int)}
 * specialization of {@link java.util.function.BiFunction} from the JDK.
 *
 * @param <T> the type of the object argument to the operation
 * @param <R> the type of the result of the function
 *
 * @see java.util.function.BiFunction
 */
public interface ObjIntFunction<T, R> {

    R apply(T t, int i);

}
