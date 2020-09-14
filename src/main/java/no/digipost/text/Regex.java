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
package no.digipost.text;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class Regex {

    /**
     * Extract the groups from text which matches the given pattern.
     *
     * @param text the text to extract from
     * @param pattern The pattern which contains groups to extract.
     *
     * @return The extracted groups. If the pattern does not match or does not define
     *         any groups, the empty {@link Stream#empty() stream} is returned.
     */
    public static Stream<String> extractGroups(CharSequence text, Pattern pattern) {
        return Optional.of(text).map(pattern::matcher).map(Regex::groups).orElseGet(Stream::empty);
    }


    /**
     * Extract the groups from a {@code Matcher}, if a match is {@link Matcher#find() found}.
     *
     * @param matcher the {@code Matcher}.
     *
     * @return The extracted groups, or the empty {@link Stream#empty() stream} if no match is
     *         found or no groups are defined.
     */
    public static Stream<String> groups(Matcher matcher) {
        Stream.Builder<String> streams = Stream.builder();
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                streams.add(matcher.group(i));
            }
        }
        return streams.build();
    }


    private Regex() {}
}
