package org.shunya.punter.annotations;

import javax.xml.bind.annotation.XmlTransient;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@XmlTransient
public @interface OutputParam{
	String bind() default "";
	String type() default "";
}