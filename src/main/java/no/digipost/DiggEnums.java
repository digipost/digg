/*
 * Copyright (C) Posten Bring AS
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

import java.util.BitSet;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
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
     * Select enum constants by mapping <em>indexes</em> of elements in a boolean array which are {@code true} to <em>ordinals</em>
     * of the enum type.
     * <p>
     * It is valid that the enum contains either more or less constants than the given boolean array,
     * and the resolving will discard any exhaustive elements in either cases. E.g. specifying two booleans
     * will at most consider two first constants of a given enum type, as well as if the enum only has one
     * constant, only the first boolean will be processed.
     *
     * @param includedOrdinals the boolean array where the <em>indexes</em> of {@code true} elements are mapped
     *                         to any existing ordinals of the given {@code enumType}
     * @param enumType the enum type to resolve constants from
     *
     * @return the resolved {@code enum} constants
     */
    public static <E extends Enum<E>> Stream<E> selectByIndexAsOrdinals(boolean[] includedOrdinals, Class<E> enumType) {
        BitSet mask = new BitSet(includedOrdinals.length);
        for (int i = 0; i < includedOrdinals.length; i++) {
            mask.set(i, includedOrdinals[i]);
        }
        return selectByBitmask(mask, enumType);
    }


    /**
     * Select enum constants by overlaying a bitmask ({@code long}) and selecting the enum <em>by ordinals</em>
     * which align with the indices of the set bits, mapping the first index to the first enum constant and so
     * forth. A bitmask of {@code 10110001} (177) will select the first, fifth, sixth, and eight enum constant.
     * <p>
     * It is valid both for the enum to contain more constants than the length of the mask,
     * or less constants than the highest index of a <em>set</em> bit in the given mask,
     * and the resolving will discard any exhaustive elements in either cases. E.g. a set bit at
     * index 2 will be ignored for enums with only one constant.
     *
     * @param mask the bit mask used for selecting enum constants, the method may mutate this mask
     * @param enumType the enum type to resolve constants from
     *
     * @return the resolved {@code enum} constants
     */
    public static <E extends Enum<E>> Stream<E> selectByBitmask(BitSet mask, Class<E> enumType) {
        if (mask.cardinality() == 0) {
            return Stream.empty();
        }
        E[] candidates = requireNonNull(enumType.getEnumConstants(), enumType + " is not an enum");
        if (candidates.length == 0) {
            return Stream.empty();
        }
        return mask.get(0, candidates.length).stream().mapToObj(selectedOrdinal -> candidates[selectedOrdinal]);
    }


    /**
     * Join several {@code enum} constants to a comma separated string of their {@link Enum#name() names}.
     *
     * @param enums the {@code enum}s
     * @return the comma separated {@code enum} names.
     */
    @SafeVarargs
    @SuppressWarnings({"varargs"})
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
    @SuppressWarnings({"varargs"})
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
    @SuppressWarnings({"varargs"})
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
    @SuppressWarnings({"varargs"})
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
