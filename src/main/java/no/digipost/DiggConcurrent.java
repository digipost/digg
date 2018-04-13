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
package no.digipost;

import no.digipost.concurrent.executor.ExternallyManagedExecutorService;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongFunction;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.logging.Level.FINE;

public class DiggConcurrent {

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(DiggConcurrent.class.getName());


    public static ExecutorService fixedThreadPool(int threadAmount, String name) {
        return Executors.newFixedThreadPool(threadAmount, threadNamingFactory(name));
    }

    public static ExecutorService singleThreaded(String name) {
        return Executors.newSingleThreadExecutor(threadNamingFactory(name));
    }

    public static ScheduledExecutorService scheduledSingleThreaded(String name) {
        return Executors.newSingleThreadScheduledExecutor(threadNamingFactory(name));
    }

    public static ScheduledExecutorService scheduled(int threadAmount, String name) {
        return Executors.newScheduledThreadPool(threadAmount, threadNamingFactory(name));
    }


    /**
     * Create a thread factory based on {@link Executors#defaultThreadFactory()}, but naming the
     * created threads using the given {@code threadBaseName} and an incrementing number for each thread
     * created by the factory. The threads will have names on the form {@code threadBaseName-N}.
     *
     * @param threadBaseName the base name for threads created by the factory.
     */
    public static ThreadFactory threadNamingFactory(String threadBaseName) {
        return threadNamingFactory(threadNum -> threadBaseName + "-" + threadNum);
    }

    /**
     * Create a thread factory based on {@link Executors#defaultThreadFactory()}, but naming the
     * created threads using the result of the given given {@link LongFunction long -&gt; String} function, which
     * argument is the number of the created thread.
     *
     * @param threadName function used for naming each thread created by the factory.
     */
    public static ThreadFactory threadNamingFactory(LongFunction<String> threadName) {
        return threadNamingFactory(threadName, Executors.defaultThreadFactory());
    }

    /**
     * Create a thread factory delegating to the given {@code backingFactory} for creating new threads, but naming the
     * created threads using the result of the given given {@link LongFunction long -&gt; String} function, which
     * argument is the number of the created thread.
     *
     * @param threadName function used for naming each thread created by the factory.
     * @param backingFactory the factory used for creating new threads.
     */
    public static ThreadFactory threadNamingFactory(LongFunction<String> threadName, ThreadFactory backingFactory) {
        AtomicLong threadNum = new AtomicLong(0);
        return r -> {
            Thread newThread = backingFactory.newThread(r);
            newThread.setName(threadName.apply(threadNum.incrementAndGet()));
            return newThread;
        };
    }


    /**
     * Wraps another {@link ExecutorService} as an "externally managed
     * executor service", which will discard any invocations of the
     * methods used to manage the lifecycle of the ExecutorService itself.
     * This includes the methods:
     * <ul>
     *  <li>{@link ExecutorService#shutdown()}</li>
     *  <li>{@link ExecutorService#shutdownNow()} (returns empty list)</li>
     *  <li>{@link ExecutorService#awaitTermination(long, TimeUnit)} (immediately
     *      returns with the result of {@link ExecutorService#isTerminated()})</li>
     * </ul>
     * An externally managed executor will not be attempted shut down by {@link #ensureShutdown(ExecutorService, Duration)}.
     */
    public static ExecutorService externallyManaged(ExecutorService executor) {
        return new ExternallyManagedExecutorService(executor);
    }


    /**
     * Determine if the given {@link ExecutorService} is marked as
     * {@link #externallyManaged(ExecutorService) externally managed}.
     */
    public static boolean isExternallyManaged(ExecutorService executor) {
        return executor instanceof ExternallyManagedExecutorService;
    }


    /**
     * Perform an orderly shutdown, trying to wait for any currently running tasks to finish,
     * or else forcefully shutdown the executor if the tasks are not able to finish their work
     * within the given timeout duration. The {@link #ensureShutdown(String, ExecutorService, Duration)}
     * method is preferred over this.
     *
     * @param executor the {@link ExecutorService} to shut down.
     * @param timeoutBeforeForcefulShutdown the maximum amount of time to wait for tasks to finish
     *                                      before forcefully shutting down the executor.
     */
    public static void ensureShutdown(ExecutorService executor, Duration timeoutBeforeForcefulShutdown) {
        ensureShutdown(executor.getClass().getSimpleName(), executor, timeoutBeforeForcefulShutdown);
    }


    /**
     * Perform an orderly shutdown, trying to wait for any currently running tasks to finish,
     * or else forcefully shutdown the executor if the tasks are not able to finish their work
     * within the given timeout duration.
     *
     * @param executorName a descriptive name of the executor to shut down, used for logging.
     * @param executor the {@link ExecutorService} to shut down.
     * @param timeoutBeforeForcefulShutdown the maximum amount of time to wait for tasks to finish
     *                                      before forcefully shutting down the executor.
     */
    public static void ensureShutdown(String executorName, ExecutorService executor, Duration timeoutBeforeForcefulShutdown) {
        if (isExternallyManaged(executor)) {
            LOG.info(() -> "Not shutting down " + executorName + " executor since it is an " + ExternallyManagedExecutorService.class.getSimpleName());
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeoutBeforeForcefulShutdown.toMillis(), MILLISECONDS)) {
                LOG.info(() -> executorName + " executor is forcefully shut down as waiting for orderly termination took more than " + timeoutBeforeForcefulShutdown);
                executor.shutdownNow();
            } else {
                LOG.info(() -> executorName + " executor was orderly shut down within the timeout of " + timeoutBeforeForcefulShutdown.toMillis() + " ms");
            }
        } catch (InterruptedException e) {
            String logMessageTemplate = "Interrupted when waiting for termination of %s executor. %s: %s";
            if (LOG.isLoggable(FINE)) {
                LOG.log(FINE, e, () -> String.format(logMessageTemplate, executorName, e.getClass().getSimpleName(), e.getMessage()));
            } else {
                LOG.info(() -> String.format(logMessageTemplate, executorName, e.getClass().getSimpleName(), e.getMessage()));
            }
        }
    }


    private DiggConcurrent() {
    }

}
