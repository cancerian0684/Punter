package com.sapient.punter.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.xml.bind.annotation.XmlTransient;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@XmlTransient
public @interface InputParam{
	public enum Required{TRUE,FALSE};
	String bind() default "";
	String type() default "";
	String description() default "";
	boolean required() default false;
}