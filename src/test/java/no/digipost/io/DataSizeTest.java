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
package no.digipost.io;

import nl.jqno.equalsverifier.EqualsVerifier;
import no.digipost.tuple.Tuple;
import org.junit.jupiter.api.Test;
import org.quicktheories.core.Gen;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.LongFunction;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static no.digipost.io.DataSize.ZERO;
import static no.digipost.io.DataSize.bytes;
import static no.digipost.io.DataSize.kB;
import static no.digipost.io.DataSizeUnit.B;
import static no.digipost.io.DataSizeUnit.BYTES;
import static no.digipost.io.DataSizeUnit.GB;
import static no.digipost.io.DataSizeUnit.GIGABYTES;
import static no.digipost.io.DataSizeUnit.KILOBYTES;
import static no.digipost.io.DataSizeUnit.MB;
import static no.digipost.io.DataSizeUnit.MEGABYTES;
import static no.digipost.io.DataSizeUnit.kB;
import static no.digipost.tuple.Tuple.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.arbitrary;
import static org.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.generators.SourceDSL.lists;

public class DataSizeTest {

    @Test
    public void correctEqualsAndHashCode() {
        EqualsVerifier.forClass(DataSize.class).verify();
    }

    @Test
    public void convertFromDifferentUnits() {
        assertThat(kB(1), is(bytes(1_024)));
        assertThat(DataSize.MB(2), both(is(kB(2_048))).and(is(bytes(2_097_152))));
    }

    @Test
    public void factoryMethodsCorrectlyCorrespondsToUnits() {
        Stream.<Tuple<LongFunction<DataSize>, DataSizeUnit>>of(
                of(DataSize::bytes, B),
                of(DataSize::bytes, BYTES),
                of(DataSize::kB, kB),
                of(DataSize::kB, KILOBYTES),
                of(DataSize::MB, MB),
                of(DataSize::MB, MEGABYTES),
                of(DataSize::GB, GB),
                of(DataSize::GB, GIGABYTES))
            .map(t -> t.mapFirst(factory -> factory.apply(1)))
            .forEach(t -> assertThat(t.first() + " is the unit size of " + t.second(), t.first().toBytes(), is(t.second().asBytes(1))));
    }


    private final Gen<Integer> amounts = integers().between(0, 10240);
    private final Gen<DataSizeUnit> units = arbitrary().enumValues(DataSizeUnit.class);
    private final Gen<DataSize> dataSizes = amounts.flatMap(size -> units.map(unit -> DataSize.of(size, unit)));


    @Test
    public void convertToUnit() {
        qt()
            .forAll(amounts, units)
            .asWithPrecursor(DataSize::of)
            .check((amount, unit, dataSize) -> dataSize.get(unit) == amount);
    }

    @Test
    public void zeroSizeIsAlwaysTheZeroInstance() {
        qt()
            .forAll(units).as(unit -> DataSize.of(0, unit))
            .check(size -> size == DataSize.ZERO);
    }

    @Test
    public void datasizeHasNaturalOrder() {
        Gen<Set<Integer>> setsOfUnorderedAmounts = lists().of(amounts).ofSizeBetween(0, 100).map(l -> new HashSet<>(l));

        qt()
            .forAll(setsOfUnorderedAmounts, units)
            .check((unorderedAmounts, unit) -> {
                SortedSet<DataSize> sortedSizes = unorderedAmounts.stream().map(i -> DataSize.of(i, unit)).collect(toCollection(TreeSet::new));
                Iterator<Integer> sortedByteSizes = new TreeSet<>(unorderedAmounts).iterator();
                return sortedSizes.stream().sequential().allMatch(size -> size.get(unit) == sortedByteSizes.next());
            });
    }


    @Test
    public void maxSizeIsASingleton() {
        long maxBytes = Long.MAX_VALUE;
        assertThat(DataSize.of(maxBytes, BYTES), sameInstance(DataSize.MAX));
    }

    @Test
    public void negativeSizeIsNotAllowed() {
        assertThrows(IllegalArgumentException.class, () -> DataSize.bytes(-1));
    }

    @Test
    public void toStringIsInBytes() {
        assertThat(DataSize.of(1234, MEGABYTES).toString(), containsString(BYTES.toString()));
    }

    @Test
    public void accessByteAmount() {
        assertThat(DataSize.of(2, kB).toBytes(), is(2048L));
    }

    @Test
    public void theUnitsIncreasedByFactorsOf1024() {
        Optional<DataSizeUnit> biggestUnit = Stream.of(DataSizeUnit.values()).reduce((smallerUnit, biggerUnit) -> {
                assertThat(smallerUnit.asBytes(1024), is(biggerUnit.asBytes(1)));
                return biggerUnit;
            });
        assertThat(biggestUnit.get(), is(GIGABYTES));
    }

    @Test
    public void addingAndSubtractingTheSameSizeYieldsTheOriginal() {
        qt()
            .forAll(dataSizes).check(size -> size.plus(size).minus(size).equals(size));
    }

    @Test
    public void equalSizesAreAlwaysTheSameOrAnything() {
        qt()
            .forAll(dataSizes)
            .check(size ->
                size.isSameOrLessThan(size) &&
                size.isSameOrMoreThan(size));
    }


    @Test
    public void arithemtic() {
        qt()
            .forAll(dataSizes, dataSizes.assuming(size -> !DataSize.ZERO.equals(size)))
            .check((original, delta) -> {
                DataSize newSize = original.plus(delta);
                return !newSize.equals(original) &&
                        newSize.isMoreThan(original) &&
                        newSize.isSameOrMoreThan(original) &&
                        original.isLessThan(newSize) &&
                        original.isSameOrLessThan(newSize) &&
                       !newSize.isSameOrLessThan(original) &&
                       !original.isSameOrMoreThan(newSize);
            });
    }

    @Test
    public void addZeroYieldsSameInstance() {
        qt()
            .forAll(dataSizes).check(size -> size.plus(ZERO) == size);
    }

    @Test
    public void subtractZeroYieldsSameInstance() {
        qt()
            .forAll(dataSizes).check(size -> size.minus(ZERO) == size);

    }

}
