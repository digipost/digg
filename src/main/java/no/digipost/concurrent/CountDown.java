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

import java.util.concurrent.atomic.AtomicLong;

/**
 * A count down implementation of {@link TargetState},
 * which will count the invocations of {@link #yet()} and
 * return {@code true} <em>after</em> a given amount of invocations.
 * <p>
 * The implementation is thread-safe in the sense that it is guaranteed
 * that {@link #yet()} will return {@code false} an <em>exact</em> amount
 * of times, regardless of different threads invoking {@code yet()},
 * before switching to only return {@code true}.
 * <p>
 * This can often serve for testing purposes where you would
 * like a controlled amount of queries to a {@code TargetState},
 * before it changes.
 */
public final class CountDown implements TargetState {

    private final AtomicLong count;

    /**
     * Create a new count down from the given start {@code count}.
     *
     * @param count the number of invocations where {@link #yet()} will
     *              indicate the count down is not finished yet, i.e.
     *              the number of times {@code yet()} will return {@code false},
     *              and switches to only return {@code true}.
     */
    public CountDown(long count) {
        if (count < 0) {
            throw new IllegalArgumentException("negative count: " + count);
        }
        this.count = new AtomicLong(count);
    }

    @Override
    public boolean yet() {
        return count.getAndUpdate(i -> i > 0 ? i - 1 : i) == 0;
    }

    @Override
    public String toString() {
        return "count down currently at " + count.get();
    }

}
