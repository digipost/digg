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
package no.digipost.jaxb;

import no.digipost.jaxb.testdomain.MyCustomBoundType;
import no.digipost.jaxb.testdomain.MyJaxbReadEntity;
import no.digipost.jaxb.testdomain.MyJaxbWriteEntity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xmlmatchers.transform.StringResult;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import static no.digipost.DiggCollectors.allowAtMostOne;
import static no.digipost.DiggExceptions.asUnchecked;
import static no.digipost.DiggExceptions.causalChainOf;
import static no.digipost.DiggExceptions.getUnchecked;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.xmlmatchers.XmlMatchers.isEquivalentTo;
import static org.xmlmatchers.transform.XmlConverters.the;

public class SimpleXmlAdapterTest {

    private static final JAXBContext jaxbContext = getUnchecked(() -> JAXBContext.newInstance(MyJaxbReadEntity.class, MyJaxbWriteEntity.class));

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void supportsReadingAndWritingXmlWithJaxb() throws JAXBException {
        String inputXml =
                "<root>" +
                "  <a>42</a>" +
                "</root>";

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<MyJaxbReadEntity> unmarshalled = unmarshaller.unmarshal(the(inputXml), MyJaxbReadEntity.class);
        MyJaxbReadEntity root = unmarshalled.getValue();
        assertThat(root.a.value, is("42"));

        MyJaxbWriteEntity writeEntity = root.toWriteEntity();
        StringResult writtenXml = new StringResult();
        jaxbContext.createMarshaller().marshal(writeEntity, writtenXml);
        assertThat(the(inputXml), isEquivalentTo(the(writtenXml)));
    }


    @Test
    public void writingWithReadOnlyAdapterThrowsException() throws JAXBException {
        MyJaxbReadEntity entity = new MyJaxbReadEntity();
        entity.a = new MyCustomBoundType("x");
        Marshaller marshaller = jaxbContext.createMarshaller();
        try {
            marshaller.marshal(entity, new StringResult());
        } catch (JAXBException e) {
            UnsupportedOperationException unsupported = causalChainOf(e)
                    .filter(UnsupportedOperationException.class::isInstance)
                    .map(UnsupportedOperationException.class::cast)
                    .collect(allowAtMostOne()).get();
            assertThat(unsupported.getMessage(), containsString("only supports unmarshalling"));
            return;
        }
        fail("Should throw exception");
    }

    @Test
    public void readingWithWriteOnlyAdapterThrowsException() throws JAXBException {
        String inputXml =
                "<root>" +
                "  <a>42</a>" +
                "</root>";

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setEventHandler(event -> { throw asUnchecked(event.getLinkedException()); });
        try {
            unmarshaller.unmarshal(the(inputXml), MyJaxbWriteEntity.class).getValue();
        } catch (Exception e) {
            UnsupportedOperationException unsupported = causalChainOf(e)
                    .filter(UnsupportedOperationException.class::isInstance)
                    .map(UnsupportedOperationException.class::cast)
                    .collect(allowAtMostOne()).get();
            assertThat(unsupported.getMessage(), containsString("only supports marshalling"));
            return;
        }
        fail("Should throw exception");
    }
}
