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
package no.digipost.function;

import no.digipost.DiggExceptions;

import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface ThrowingRunnable<X extends Throwable> {

    void run() throws X;

    default Runnable asUnchecked() {
        return ifExceptionThrow(DiggExceptions::asUnchecked);
    }

    default Runnable ifExceptionThrow(Function<? super Exception, ? extends RuntimeException> exceptionMapper) {
        return ifException(e -> { throw exceptionMapper.apply(e); });
    }

    default Runnable ifException(Consumer<Exception> exceptionHandler) {
        return () -> {
            try {
                ThrowingRunnable.this.run();
            } catch (Exception e) {
                exceptionHandler.accept(e);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw DiggExceptions.asUnchecked(e);
            }
        };
    }

}
