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
package no.digipost.concurrent;

import org.junit.jupiter.api.Test;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.api.Subject2;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import static co.unruly.matchers.Java8Matchers.where;
import static co.unruly.matchers.Java8Matchers.whereNot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.fail;


public class OneTimeAssignmentTest implements WithQuickTheories {

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
        assertThrows(OneTimeAssignment.AlreadyAssigned.class, () -> x.set("x"));
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
        assertThrows(OneTimeAssignment.AlreadyAssigned.class, () -> x.set(null));
    }

    @Test
    public void assignDefaultValue() {

        Subject2<Object, OneTimeAssignment<Object>> initializedWithDefaultValue = qt()
            .forAll(arbitrary().pick(new Object(), "some string", null))
            .asWithPrecursor(OneTimeAssignment::defaultTo);

        initializedWithDefaultValue
            .check((defaultValue, assignment) -> assignment.get() == defaultValue);
        initializedWithDefaultValue
            .checkAssert((defaultValue, assignment) -> assertThat(assignment, where(OneTimeAssignment::toOptional, is(Optional.ofNullable(defaultValue)))));

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
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> expectedFails.await());
        assertThat(x.get(), is("x"));
    }

    @Test
    public void askIfAValueHasBeenSet() {
        OneTimeAssignment<String> assignment = OneTimeAssignment.newInstance();
        OneTimeAssignment<String> assignmentWithDefault = OneTimeAssignment.defaultTo("x");


        assertThat(assignment, whereNot(OneTimeAssignment::isSet));
        assertThat(assignment, whereNot(OneTimeAssignment::isSet));
        assertThat(assignmentWithDefault, whereNot(OneTimeAssignment::isSet));
        assertThat(assignmentWithDefault, whereNot(OneTimeAssignment::isSet));

        assignment.set("x");
        assertThat(assignment, where(OneTimeAssignment::isSet));

        assignmentWithDefault.get();
        assertThat(assignmentWithDefault, where(OneTimeAssignment::isSet));
    }
}
