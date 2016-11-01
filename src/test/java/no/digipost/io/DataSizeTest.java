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
package no.digipost.io;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.ValuesOf;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static no.digipost.io.DataSize.bytes;
import static no.digipost.io.DataSize.kB;
import static no.digipost.io.DataSizeUnit.BYTES;
import static no.digipost.io.DataSizeUnit.GIGABYTES;
import static no.digipost.io.DataSizeUnit.MEGABYTES;
import static no.digipost.io.DataSizeUnit.kB;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(JUnitQuickcheck.class)
public class DataSizeTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void correctEqualsAndHashCode() {
        EqualsVerifier.forClass(DataSize.class).verify();
    }

    @Test
    public void convertFromDifferentUnits() {
        assertThat(kB(1), is(bytes(1_024)));
        assertThat(DataSize.MB(2), both(is(kB(2_048))).and(is(bytes(2_097_152))));
    }

    @Property
    public void convertToUnit(@InRange(minInt = 0, maxInt=10240) int amount, @ValuesOf DataSizeUnit unit) {
        DataSize size = DataSize.of(amount, unit);
        assertThat(size.get(unit), is((double) amount));
    }

    @Property
    public void sorting(Set<@InRange(minInt = 0, maxInt = 10240) Integer> ints, @ValuesOf DataSizeUnit unit) {
        SortedSet<DataSize> sortedSizes = ints.stream().map(i -> DataSize.of(i, unit)).collect(toCollection(TreeSet::new));
        Iterator<Integer> sortedByteSizes = new TreeSet<>(ints).iterator();
        for (DataSize size : sortedSizes) {
            assertThat(size.get(unit), is((double) sortedByteSizes.next()));
        }
    }

    @Property
    public void zeroIsZero(@ValuesOf DataSizeUnit unit) {
        assertThat(DataSize.of(0, unit), sameInstance(DataSize.ZERO));
    }

    @Test
    public void maxSizeIsASingleton() {
        long maxBytes = Long.MAX_VALUE;
        assertThat(DataSize.of(maxBytes, BYTES), sameInstance(DataSize.MAX));
    }

    @Test
    public void negativeSizeIsNotAllowed() {
        expectedException.expect(IllegalArgumentException.class);
        DataSize.bytes(-1);
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
    public void theUnitsIncreaseByFactorsOf1024() {
        Optional<DataSizeUnit> biggestUnit = Stream.of(DataSizeUnit.values()).reduce((smallerUnit, biggerUnit) -> {
                assertThat(smallerUnit.asBytes(1024), is(biggerUnit.asBytes(1)));
                return biggerUnit;
            });
        assertThat(biggestUnit.get(), is(GIGABYTES));
    }

}
