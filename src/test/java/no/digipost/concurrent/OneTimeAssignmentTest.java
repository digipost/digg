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
package no.digipost.concurrent;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(JUnitQuickcheck.class)
public class OneTimeAssignmentTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final Timeout timeout = new Timeout(2, SECONDS);

    @Test
    public void assignAndRetrieveAValue() {
        OneTimeAssignment<String> x = OneTimeAssignment.newInstance();
        assertThat(x.get(), nullValue());
        assertThat(x.get(), nullValue());
        x.set("x");
        assertThat(x.get(), is("x"));
        assertThat(x.get(), is("x"));
    }

    @Test
    public void useDefaultValueIfNoValueIsAssigned() {
        OneTimeAssignment<String> y = OneTimeAssignment.defaultTo("y");
        assertThat(y.get(), is("y"));
    }

    @Test
    public void settingAValueDiscardsDefaultValue() {
        OneTimeAssignment<String> x = OneTimeAssignment.defaultTo("y");
        x.set("x");
        assertThat(x.get(), is("x"));
    }

    @Test
    public void setValueTwiceThrowsException() {
        OneTimeAssignment<String> x = OneTimeAssignment.newInstance();
        x.set("x");
        expectedException.expect(OneTimeAssignment.AlreadyAssigned.class);
        x.set("x");
    }

    @Test
    public void keepsTheFirstAssignedValueOnErroneousDoubleAssignment() {
        OneTimeAssignment<String> x = OneTimeAssignment.defaultTo("y");
        x.get();
        try {
            x.set("x");
            fail("should throw exception");
        } catch (OneTimeAssignment.AlreadyAssigned e) {
            assertThat(x.get(), is("y"));
        }
    }

    @Test
    public void setNullTwiceThrowsException() {
        OneTimeAssignment<String> x = OneTimeAssignment.newInstance();
        x.set(null);
        expectedException.expect(OneTimeAssignment.AlreadyAssigned.class);
        x.set(null);
    }

    @Property
    public void assignWhenConvertToOptionalForOneTimeAssignmentWithDefaultValue(Object defaultValue) {
        OneTimeAssignment<?> assignment = OneTimeAssignment.defaultTo(defaultValue);
        Optional<?> value = assignment.toOptional();
        assertThat(assignment.get(), is(defaultValue));
        assertThat(value, is(ofNullable(defaultValue)));
    }

    @Test
    public void onlyPossibleToConcurrentlyAssignOnce() throws InterruptedException {
        int concurrentAssignments = 500;
        CountDownLatch expectedFails = new CountDownLatch(concurrentAssignments - 1);
        OneTimeAssignment<String> x = OneTimeAssignment.defaultTo("y");
        Stream<Runnable> assignments = Stream.iterate(() -> {
            try {
                x.set("x");
                return;
            } catch (OneTimeAssignment.AlreadyAssigned e) {
                expectedFails.countDown();
            }
        }, (Runnable r) -> r);

        assignments.limit(concurrentAssignments).parallel().forEach(CompletableFuture::runAsync);
        expectedFails.await();
        assertThat(x.get(), is("x"));
    }

    @Test
    public void askIfAValueHasBeenSet() {
        OneTimeAssignment<String> assignment = OneTimeAssignment.newInstance();
        OneTimeAssignment<String> assignmentWithDefault = OneTimeAssignment.defaultTo("x");


        assertFalse(assignment.isSet());
        assertFalse(assignment.isSet());
        assertFalse(assignmentWithDefault.isSet());
        assertFalse(assignmentWithDefault.isSet());

        assignment.set("x");
        assertTrue(assignment.isSet());

        assignmentWithDefault.get();
        assertTrue(assignmentWithDefault.isSet());
    }
}
