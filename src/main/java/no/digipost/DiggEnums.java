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

/**
 * Utilities for working with Java {@code enum}s.
 */
public final class DiggEnums {


    /**
     * Resolve {@code enum} constants from a string of comma separated names. The names must be exactly equal
     * to the constant names of the given {@code enum} type, but may include any whitespace before and/or after
     * each comma.
     *
     * @param enumNames the string containing the comma separated {@code enum} names.
     * @param enumType the type of the {@code enum}.
     * @return a stream of the resolved {@code enum} constants.
     */
    public static <E extends Enum<E>> Stream<E> fromCommaSeparatedNames(String enumNames, Class<E> enumType) {
        return fromCommaSeparated(enumNames, Function.<String>identity(), enumType);
    }


    /**
     * Resolve {@code enum} constants from a string of comma separated identifiers.
     * The string may include any whitespace before and/or after each comma.
     * The identifiers must be converted to {@code enum} names using a given {@link Function}.
     *
     * @param enumsString the string containing the comma separated {@code enum} identifiers.
     * @param toEnumName the function to convert from the identifiers to {@code enum} names.
     * @param enumType the type of the {@code enum}.
     * @return a stream of the resolved {@code enum} constants.
     */
    public static <E extends Enum<E>> Stream<E> fromCommaSeparated(String enumsString, Function<String, String> toEnumName, Class<E> enumType) {
        return fromEnumsString(enumsString, "\\s*,\\s*", toEnumName.<E>andThen(e -> Enum.valueOf(enumType, e)));
    }


    /**
     * Resolve {@code enum} constants from a string containing identifiers separated by a delimiter.
     * The identifiers must be converted to {@code enum} constants using a given {@link Function}.
     *
     * @param enumsString the string containing the {@code enum} identifiers.
     * @param delimRegex the regular expression which is applied to the {code enumString} to split it into
     *                   separate identifiers.
     * @param convertToEnum the function to convert from the identifiers to {@code enum} constants from the identifiers.
     * @return a stream of the resolved {@code enum} constants.
     */
    public static <E extends Enum<E>> Stream<E> fromEnumsString(String enumsString, String delimRegex, Function<String, E> convertToEnum) {
        return fromEnumsString(enumsString, delimRegex, e -> true, convertToEnum);
    }


    /**
     * Resolve {@code enum} constants from a string containing identifiers separated by a delimiter.
     * The identifiers must be converted to {@code enum} constants using a given {@link Function}. In
     * addition, this method offers a filtering mechanism to only include identifiers which satisfy a {@link Predicate}.
     *
     * @param enumsString the string containing the {@code enum} identifiers.
     * @param delimRegex the regular expression which is applied to the {code enumString} to split it into
     *                   separate identifiers.
     * @param included predicate which is used as a filter on each identifier. Identifiers not satisfying this predicate
     *                 will not be attempted to resolve as an {@code enum} constant.
     * @param convertToEnum the function to convert from the identifiers to {@code enum} constants from the identifiers.
     * @return a stream of the resolved {@code enum} constants.
     */
    public static <E extends Enum<E>> Stream<E> fromEnumsString(String enumsString, String delimRegex, Predicate<String> included, Function<String, E> convertToEnum) {
        String trimmed = enumsString != null ? enumsString.trim() : "";
        return trimmed.isEmpty() ? Stream.empty() : stream(trimmed.split(delimRegex)).filter(included).map(convertToEnum);
    }


    /**
     * Join several {@code enum} constants to a comma separated string of their {@link Enum#name() names}.
     *
     * @param enums the {@code enum}s
     * @return the comma separated {@code enum} names.
     */
    @SafeVarargs
    public static <E extends Enum<E>> String toCommaSeparatedNames(E ... enums) {
        return toCommaSeparatedNames(asList(enums));
    }


    /**
     * Join several {@code enum} constants to a comma separated string of their {@link Enum#name() names}.
     *
     * @param enums the {@code enum}s
     * @return the comma separated {@code enum} names.
     */
    public static <E extends Enum<E>> String toCommaSeparatedNames(Collection<E> enums) {
        return toNames(",", enums);
    }


    /**
     * Join several {@code enum} constants to a string of their {@link Enum#name() names},
     * separated by the given {@code delim}iter.
     *
     * @param delim the {@code delim}iter.
     * @param enums the {@code enum}s.
     * @return the {@code enum} names, separated by the {@code delim}iter.
     */
    @SafeVarargs
    public static <E extends Enum<E>> String toNames(String delim, E ... enums) {
        return toNames(delim, asList(enums));
    }


    /**
     * Join several {@code enum} constants to a string of their {@link Enum#name() names},
     * separated by the given {@code delim}iter.
     *
     * @param delim the {@code delim}iter.
     * @param enums the {@code enum}s.
     * @return the {@code enum} names, separated by the {@code delim}iter.
     */
    public static <E extends Enum<E>> String toNames(String delim, Collection<E> enums) {
        return toStringOf(Enum::name, delim, enums);
    }


    /**
     * Join several {@code enum} constants to a string where each constant is converted to
     * a string, and separated by the given {@code delim}iter.
     *
     * @param enumAsString the function which converts each {@code enum} constant to a string.
     * @param delim the {@code delim}iter.
     * @param enums the {@code enum}s.
     * @return the joined string
     */
    @SafeVarargs
    public static <E extends Enum<E>> String toStringOf(Function<? super E, String> enumAsString, String delim, E ... enums) {
        return toStringOf(enumAsString, delim, asList(enums));
    }


    /**
     * Join several {@code enum} constants to a string where each constant is converted to
     * a string, and separated by the given {@code delim}iter.
     *
     * @param enumAsString the function which converts each {@code enum} constant to a string.
     * @param delim the {@code delim}iter.
     * @param enums the {@code enum}s.
     * @return the joined string.
     */
    public static <E extends Enum<E>> String toStringOf(Function<? super E, String> enumAsString, String delim, Collection<E> enums) {
        return toStringOf(enumAsString, joining(delim), enums);
    }


    /**
     * Join several {@code enum} constants to a string where each constant is converted to
     * a string, and joined to one string using a given {@link Collector}.
     *
     * @param enumAsString the function which converts each {@code enum} constant to a string.
     * @param joiner the {@code Collector} to join the strings.
     * @param enums the {@code enum}s.
     * @return the joined string.
     */
    @SafeVarargs
    public static <E extends Enum<E>> String toStringOf(Function<? super E, String> enumAsString, Collector<? super String, ?, String> joiner, E ... enums) {
        return toStringOf(enumAsString, joiner, asList(enums));
    }


    /**
     * Join several {@code enum} constants to a string where each constant is converted to
     * a string, and joined to one string using a given {@link Collector}.
     *
     * @param enumAsString the function which converts each {@code enum} constant to a string.
     * @param joiner the {@code Collector} to join the strings.
     * @param enums the {@code enum}s.
     * @return the joined string.
     */
    public static <E extends Enum<E>> String toStringOf(Function<? super E, String> enumAsString, Collector<? super String, ?, String> joiner, Collection<E> enums) {
        return enums.stream().map(enumAsString).collect(joiner);
    }




    private DiggEnums() {}
}
