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

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

final class Compound {
    final String text;
    final int number;
    final BigDecimal bigNumber;
    final URI uri;
    final String anotherText;
    final UUID id;
    final List<String> strings;
    final int yetAnotherNumber;
    final URI secondUri;
    final Object arbitraryObject;

    Compound(String text, int number) {
        this(text, number, null, null, null, null, null, -1, null, null);
    }

    Compound(String text, int number, BigDecimal bigNumber) {
        this(text, number, bigNumber, null, null, null, null, -1, null, null);
    }

    Compound(String text, int number, BigDecimal bigNumber, URI uri) {
        this(text, number, bigNumber, uri, null, null, null, -1, null, null);
    }

    Compound(String text, int number, BigDecimal bigNumber, URI uri, String anotherText) {
        this(text, number, bigNumber, uri, anotherText, null, null, -1, null, null);
    }

    Compound(String text, int number, BigDecimal bigNumber, URI uri, String anotherText, UUID id) {
        this(text, number, bigNumber, uri, anotherText, id, null, -1, null, null);
    }

    Compound(String text, int number, BigDecimal bigNumber, URI uri, String anotherText, UUID id,
            List<String> strings) {
        this(text, number, bigNumber, uri, anotherText, id, strings, -1, null, null);
    }

    Compound(String text, int number, BigDecimal bigNumber, URI uri, String anotherText, UUID id,
            List<String> strings, int yetAnotherNumber) {
        this(text, number, bigNumber, uri, anotherText, id, strings, yetAnotherNumber, null, null);
    }

    Compound(String text, int number, BigDecimal bigNumber, URI uri, String anotherText, UUID id,
            List<String> strings, int yetAnotherNumber, URI secondUri) {
        this(text, number, bigNumber, uri, anotherText, id, strings, yetAnotherNumber, secondUri, null);
    }

    Compound(String text, int number, BigDecimal bigNumber, URI uri, String anotherText, UUID id,
             List<String> strings, int yetAnotherNumber, URI secondUri, Object arbitraryObject) {
        this.text = text;
        this.number = number;
        this.bigNumber = bigNumber;
        this.uri = uri;
        this.anotherText = anotherText;
        this.id = id;
        this.strings = strings;
        this.yetAnotherNumber = yetAnotherNumber;
        this.secondUri = secondUri;
        this.arbitraryObject = arbitraryObject;
    }
}
