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
package no.digipost.time;

import java.io.Serializable;
import java.time.*;
import java.util.concurrent.atomic.AtomicReference;

public class ControllableClock extends Clock implements Serializable {

	private final AtomicReference<Instant> now;
	private final ZoneId zone;

	public ControllableClock(Instant fixedNow) {
		this(fixedNow, ZoneId.systemDefault());
	}

	public ControllableClock(LocalDateTime fixedNow) {
		this(fixedNow.atZone(ZoneId.systemDefault()));
	}

	public ControllableClock(ZonedDateTime fixedNow) {
		this(fixedNow.toInstant(), fixedNow.getZone());
	}

	public ControllableClock(Instant fixedNow, ZoneId zone) {
		this.now = new AtomicReference<>(fixedNow);
		this.zone = zone;
	}

	@Override
	public ZoneId getZone() {
		return zone;
	}

	@Override
	public ControllableClock withZone(ZoneId zone) {
		return new ControllableClock(now.get(), zone);
	}

	@Override
	public Instant instant() {
		return now.get();
	}

	public void set(LocalDateTime dateTime) {
		set(dateTime.atZone(zone).toInstant());
	}

	public void set(Instant instant) {
		now.set(instant);
	}

	public void timePasses(Duration duration) {
		now.getAndUpdate(previous -> previous.plus(duration));
	}

}
