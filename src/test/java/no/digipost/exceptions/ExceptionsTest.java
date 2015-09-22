package no.digipost.exceptions;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.digipost.exceptions.Exceptions.causalChainOf;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ExceptionsTest {

	@Test
	public void causalChainOfNullIsEmptyStream() {
		assertThat(causalChainOf(null).collect(toList()), empty());
	}

    @Test
    @SuppressWarnings("unchecked")
	public void returnsTheCausalChainOfExceptions() {
		List<Throwable> exception = causalChainOf(new Exception(new IllegalStateException(new IOException()))).collect(toList());
		assertThat(exception, contains(instanceOf(Exception.class), instanceOf(IllegalStateException.class), instanceOf(IOException.class)));
	}
}
