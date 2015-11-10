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

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public final class Enums {

    public static <E extends Enum<E>> Stream<E> fromCommaSeparatedNames(String enumNames, Class<E> enumType) {
        return fromCommaSeparated(enumNames, Function.<String>identity(), enumType);
    }

    public static <E extends Enum<E>> Stream<E> fromCommaSeparated(String enumsString, Function<String, String> toEnumName, Class<E> enumType) {
        return fromEnumsString(enumsString, "\\s*,\\s*", toEnumName.<E>andThen(e -> Enum.valueOf(enumType, e)));
    }

    public static <E extends Enum<E>> Stream<E> fromEnumsString(String enumsString, String delimRegex, Function<String, E> convertToEnum) {
        return fromEnumsString(enumsString, delimRegex, e -> true, convertToEnum);
    }

    public static <E extends Enum<E>> Stream<E> fromEnumsString(String enumsString, String delimRegex, Predicate<String> included, Function<String, E> convertToEnum) {
        String trimmed = enumsString != null ? enumsString.trim() : "";
        return trimmed.isEmpty() ? Stream.empty() : stream(trimmed.split(delimRegex)).filter(included).map(convertToEnum);
    }

    @SafeVarargs
    public static <E extends Enum<E>> String toCommaSeparatedNames(E ... enums) {
        return toCommaSeparatedNames(asList(enums));
    }

    public static <E extends Enum<E>> String toCommaSeparatedNames(Collection<E> enums) {
        return toNames(enums, ",");
    }

    public static <E extends Enum<E>> String toNames(Collection<E> enums, String delim) {
        return toEnumsString(enums, e -> e.name(), delim);
    }

    public static <E extends Enum<E>> String toEnumsString(Collection<E> enums, Function<? super E, String> toString, String delim) {
        return toEnumsString(enums, toString, joining(","));
    }

    public static <E extends Enum<E>> String toEnumsString(Collection<E> enums, Function<? super E, String> toString, Collector<? super String, ?, String> collector) {
        return enums.stream().map(toString).collect(collector);
    }




    private Enums() {}
}
