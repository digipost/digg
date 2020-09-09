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
package no.digipost.concurrent.executor;

import no.digipost.DiggConcurrent;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.LongFunction;

/**
 * @deprecated Use {@link DiggConcurrent} instead. This will eventually be removed.
 */
@Deprecated
public final class DefaultExecutors {

    /**
     * @see DiggConcurrent#fixedThreadPool(int, String)
     */
    public static ExecutorService fixedThreadPool(int threadAmount, String name) {
        return DiggConcurrent.fixedThreadPool(threadAmount, name);
    }

    /**
     * @see DiggConcurrent#singleThreaded(String)
     */
    public static ExecutorService singleThreaded(String name) {
        return DiggConcurrent.singleThreaded(name);
    }

    /**
     * @see DiggConcurrent#scheduledSingleThreaded(String)
     */
    public static ScheduledExecutorService scheduledSingleThreaded(String name) {
        return DiggConcurrent.scheduledSingleThreaded(name);
    }

    /**
     * @see DiggConcurrent#scheduled(int, String)
     */
    public static ScheduledExecutorService scheduled(int threadAmount, String name) {
        return DiggConcurrent.scheduled(threadAmount, name);
    }


    /**
     * @see DiggConcurrent#threadNamingFactory(String)
     */
    public static ThreadFactory threadNamingFactory(String threadBaseName) {
        return DiggConcurrent.threadNamingFactory(threadBaseName);
    }

    /**
     * @see DiggConcurrent#threadNamingFactory(LongFunction)
     */
    public static ThreadFactory threadNamingFactory(LongFunction<String> threadName) {
        return DiggConcurrent.threadNamingFactory(threadName);
    }

    /**
     * @see DiggConcurrent#threadNamingFactory(LongFunction, ThreadFactory)
     */
    public static ThreadFactory threadNamingFactory(LongFunction<String> threadName, ThreadFactory backingFactory) {
        return DiggConcurrent.threadNamingFactory(threadName, backingFactory);
    }


    /**
     * @see DiggConcurrent#externallyManaged(ExecutorService)
     */
    public static ExecutorService externallyManaged(ExecutorService executor) {
        return DiggConcurrent.externallyManaged(executor);
    }


    /**
     * @see DiggConcurrent#isExternallyManaged(ExecutorService)
     */
    public static boolean isExternallyManaged(ExecutorService executor) {
        return DiggConcurrent.isExternallyManaged(executor);
    }


    /**
     * @see DiggConcurrent#ensureShutdown(ExecutorService, Duration)
     */
    public static void ensureShutdown(ExecutorService executor, Duration timeoutBeforeForcefulShutdown) {
        DiggConcurrent.ensureShutdown(executor, timeoutBeforeForcefulShutdown);
    }


    /**
     * @see DiggConcurrent#ensureShutdown(String, ExecutorService, Duration)
     */
    public static void ensureShutdown(String executorName, ExecutorService executor, Duration timeoutBeforeForcefulShutdown) {
        DiggConcurrent.ensureShutdown(executorName, executor, timeoutBeforeForcefulShutdown);
    }

    private DefaultExecutors() {}
}
