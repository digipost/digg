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

import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;
import static uk.co.probablyfine.matchers.Java8Matchers.whereNot;

class CountDownTest {

    @Test
    void reachesTargetStateAfterNthQuery() {
        CountDown countDown = new CountDown(2);
        assertThat(countDown, whereNot(TargetState::yet));
        assertThat(countDown, whereNot(TargetState::yet));
        assertThat(countDown, where(TargetState::yet));
    }

    @Test
    void aZeroCountDownIsImmediatelyDone() {
        CountDown countDown = new CountDown(0);
        assertThat(countDown, where(TargetState::yet));
    }

    @Test
    void doesNotAllowNegativeCount() {
        assertThrows(IllegalArgumentException.class, () -> new CountDown(-1));
    }

    @Test
    void countDownGuaranteesAnExactAmountCrossThreads() {
        long count = 50_000;
        CountDown countDown = new CountDown(count);
        long invocationsBeforeZero = IntStream.generate(() -> countDown.yet() ? 0 : 1)
            .parallel()
            .limit(count * 20)
            .filter(i -> i == 1)
            .count();
        assertThat(invocationsBeforeZero, is(count));
    }

}
