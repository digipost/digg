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
package no.digipost;

public final class DiggCompare {

    public static <T extends Comparable<T>> T min(T t1, T t2) {
        return t1.compareTo(t2) > 0 ? t2 : t1;
    }

    public static <T extends Comparable<T>> T max(T t1, T t2) {
        return t2.compareTo(t1) > 0 ? t2 : t1;
    }

    private DiggCompare() {}
}
