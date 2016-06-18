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
package no.digipost.collection;

import static no.digipost.DiggExceptions.exceptionNameAndMessage;

/**
 * This exception indicates that an element was encountered which conflicts with
 * what was expected for an operation to complete successfully.
 */
public class ConflictingElementEncountered extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConflictingElementEncountered(Object allowed, Object conflicting) {
        this(allowed, conflicting, (Throwable) null);
    }

    public ConflictingElementEncountered(Object allowed, Object conflicting, String reason) {
        this(allowed, conflicting, reason, null);
    }

    public ConflictingElementEncountered(Object allowed, Object conflicting, Throwable cause) {
        this(allowed, conflicting, (cause != null ? exceptionNameAndMessage(cause) : ""), cause);
    }

    public ConflictingElementEncountered(Object allowed, Object conflicting, String reason, Throwable cause) {
        super(conflicting + " conflicts with " + allowed + (reason != null ? " because " + reason : ""), cause);
    }

}
