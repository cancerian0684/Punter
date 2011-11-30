package com.shunya.punter.utils;

import com.shunya.punter.tasks.EchoTask;
import com.shunya.punter.tasks.Tasks;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

public class FieldPropertiesMapTest {

    @org.junit.Test
    public void ShouldConvertFieldsPropertiesToXML() throws JAXBException {
        FieldPropertiesMap propertiesMap = Tasks.listInputParams(new EchoTask());
        System.out.println("propertiesMap = " + FieldPropertiesMap.convertObjectToXml(propertiesMap));
    }
}
