package com.shunya.punter.utils;

import com.shunya.punter.tasks.EchoTask;
import com.shunya.punter.tasks.Tasks;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

@XmlRootElement()
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "properties"
})
public class FieldPropertiesMap {
    Map<String, FieldProperties> properties;

    public FieldPropertiesMap() {
        // For XML Initialization
    }

    public FieldPropertiesMap(Map<String, FieldProperties> fieldPropertiesMap) {
        this.properties = fieldPropertiesMap;
    }

    public FieldProperties get(String name) {
        return properties.get(name);
    }

    public static String convertObjectToXml(FieldPropertiesMap fieldPropertiesMap) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(FieldPropertiesMap.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter sw = new StringWriter(1000);
        marshaller.marshal(fieldPropertiesMap, sw);
        return sw.toString();
    }

    public static FieldPropertiesMap convertXmlToObject(String xml) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(FieldPropertiesMap.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        FieldPropertiesMap unmarshal = (FieldPropertiesMap) unmarshaller.unmarshal(new StringReader(xml));
        return unmarshal;
    }

    public Set<String> keySet() {
        return properties.keySet();
    }
}
