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
package no.digipost.exceptions;

import java.util.function.Function;

public final class Exceptions {

	public static String exceptionNameAndMessage(Throwable t) {
		return t.getClass().getSimpleName() + ": '" + t.getMessage() + "'";
	}

	public static RuntimeException asUnchecked(Exception e) {
		return asUnchecked(e, Exceptions::exceptionNameAndMessage);
	}

	public static RuntimeException asUnchecked(Exception e, Function<Exception, String> message) {
		return e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(message.apply(e), e);
	}

	private Exceptions() {}
}
