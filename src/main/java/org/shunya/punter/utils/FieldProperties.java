package org.shunya.punter.utils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "name",
        "value",
        "description",
        "required"
})
public class FieldProperties implements Serializable {
    private String name;
    private String value;
    private String description;
    private boolean required;

    public FieldProperties() {
        //required by JAXB
    }

    public FieldProperties(String name, String value,String description, boolean required) {
        this.name = name;
        this.value = value;
        this.description = description;
        this.required = required;
    }

    public String getValue() {
        if (value == null)
            value = "";
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public String toString() {
        return value;
    }
}