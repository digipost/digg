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

import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Arrays.asList;

/**
 * An object which knows if a certain state has been
 * reached {@link #yet() yet}.
 *
 * <p>
 * The reference to a {@code TargetState} should
 * be named as the state it models
 * <em>when the state has been reached</em>.
 *
 * E.g:
 * <p>
 * <pre>
 * TargetState isShutdown = ...
 * ...
 * if(isShutdown.yet()) {
 *     ...
 * }
 * </pre>
 *
 * Or to run a repeating task until the target state is reached using {@link #untilThen(Runnable, Consumer)}:
 *
 * <pre>
 * TargetState isShutdown = ...
 * ...
 * isShutdown.untilThen(() -> {
 *     ...
 * }, exception -> handle(exception));
 * </pre>
 */
@FunctionalInterface
public interface TargetState {

	/**
	 * A target state which is already reached.
	 */
	static TargetState IMMEDIATELY = () -> true;

	/**
	 * A target state which will never be reached.
	 */
	static TargetState NEVER = () -> false;

	/**
	 * Tell if the target state has been reached yet. Once this method returns
	 * <code>true</code>, it will never return <code>false</code> again.
	 *
	 *
	 * @return <code>true</code> when the target state has been reached,
	 *         <code>false</code> otherwise.
	 */
	boolean yet();


	/**
	 * Control signals returned from tasks to control
	 * execution with {@link TargetState#untilThen(Supplier, Consumer)}
	 */
	enum TaskControl {
		/**
		 * The task will be tried repeated, unless {@link TargetState#yet()}
		 * returns {@code true}.
		 */
		TRY_REPEAT,

		/**
		 * The execution is immediately stopped, even if {@link TargetState#yet()}
		 * still returns {@code false}.
		 */
		EXIT
	}


	/**
	 * Run a task in a loop until the target state is reached ({@link #yet()} returns {@code true}),
	 * though the loop can be controlled to exit prematurely using {@link TaskControl#EXIT}.
	 * Any exceptions thrown by the task is caught and forwarded to the given handler, and
	 * then the loop is continued. If the {@code exceptionHandler} itself throws an exception,
	 * the loop is exited, and the exception is propagated to the caller of
	 * {@code until(..)}.
	 *
	 * @param loopingTask the task to run repeatedly.
	 * @param exceptionHandler the exception handler.
	 */
	default void untilThen(Supplier<TaskControl> loopingTask, Consumer<? super Exception> exceptionHandler) {
		while(!yet()) {
			try {
				TaskControl taskControl = loopingTask.get();
				if (taskControl == TaskControl.EXIT) return;
				else if (taskControl == TaskControl.TRY_REPEAT) continue;
				else throw new IllegalStateException(
						"'" + taskControl + "' is not a valid " + TaskControl.class.getSimpleName() + ". " +
						"The loopingTask must return either " + TaskControl.TRY_REPEAT + " or " + TaskControl.EXIT +
						" to control the looping behavior.");
			} catch (Exception e) {
				exceptionHandler.accept(e);
			}
		}
	}

	/**
	 * Run a task in a loop until the target state is reached ({@link #yet()} returns {@code true}).
	 * Any exceptions thrown by the task is caught and forwarded to the given handler, and
	 * then the loop is continued. If the {@code exceptionHandler} itself throws an exception,
	 * the loop is exited, and the exception is propagated to the caller of
	 * {@code until(..)}.
	 *
	 * @param loopingTask the task to run repeatedly.
	 * @param exceptionHandler the exception handler.
	 */
	default void untilThen(Runnable loopingTask, Consumer<? super Exception> exceptionHandler) {
		untilThen(() -> {
			loopingTask.run();
			return TaskControl.TRY_REPEAT;
		}, exceptionHandler);
	}


	/**
	 * @return a new TargetState which is reached when either this or the given state
	 *         is reached.
	 */
	default TargetState or(TargetState that) {
		return () -> this.yet() || that.yet();
	}


	/**
	 * @return a new TargetState which is reached only when all the given states
	 *         are reached.
	 */
	static TargetState all(TargetState ... states) {
		return all(asList(states));
	}


	/**
	 * @return a new TargetState which is reached only when all the given states
	 *         are reached.
	 */
	static TargetState all(Iterable<? extends TargetState> states) {
		return () -> {
			for (TargetState state : states) {
				if (!state.yet()) return false;
			}
			return true;
		};
	}
}
