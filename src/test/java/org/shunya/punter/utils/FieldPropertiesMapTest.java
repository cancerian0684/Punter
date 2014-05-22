package org.shunya.punter.utils;

import org.shunya.punter.tasks.EchoTask;
import org.shunya.punter.tasks.Tasks;

import javax.xml.bind.JAXBException;

public class FieldPropertiesMapTest {

    @org.junit.Test
    public void ShouldConvertFieldsPropertiesToXML() throws JAXBException {
        FieldPropertiesMap propertiesMap = Tasks.listInputParams(new EchoTask());
        System.out.println("propertiesMap = " + FieldPropertiesMap.convertObjectToXml(propertiesMap));
    }
}
