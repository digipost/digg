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
package no.digipost.concurrent.executor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import static java.util.Collections.emptyList;


class ExternallyManagedExecutorService implements ExecutorService {

	private final ExecutorService executor;

	ExternallyManagedExecutorService(ExecutorService executor) {
		this.executor = executor;
	}

	@Override
    public void execute(Runnable command) {
		executor.execute(command);
    }

	@Override
    public void shutdown() {
		return;
    }

	@Override
    public List<Runnable> shutdownNow() {
		return emptyList();
    }

	@Override
    public boolean isShutdown() {
		return executor.isShutdown();
    }

	@Override
    public boolean isTerminated() {
		return executor.isTerminated();
    }

	@Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
		return executor.isTerminated();
    }

	@Override
    public <T> Future<T> submit(Callable<T> task) {
		return executor.submit(task);
    }

	@Override
    public <T> Future<T> submit(Runnable task, T result) {
		return executor.submit(task, result);
    }

	@Override
    public Future<?> submit(Runnable task) {
		return executor.submit(task);
    }

	@Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return executor.invokeAll(tasks);
    }

	@Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		return executor.invokeAll(tasks, timeout, unit);
    }

	@Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return executor.invokeAny(tasks);
    }

	@Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return executor.invokeAny(tasks, timeout, unit);
    }

}
