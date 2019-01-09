/*
 * Copyright 2015 Christophe Drion
 * 
 * @author christophe.drion@gmail.com
 * @version 1.3
 * @since 2015-02-23
 */
package com.eglantine.jsf;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface JSFFieldAnnotation {
	boolean password() default false; // if to be hidden 
	String label() default ""; // UI invite
	String icon() default ""; 
	String href() default "";
	boolean sendMailTo() default false;
	boolean selectOneMenu() default false;
	String[] uimap() default {}; // to map an enumeration to display strings
	String[] hrefmap() default {}; // to map an enumeration to display strings
	String update() default "";
	int[] views() default {}; // to filter the display according to the context (view)
	boolean xmldoc() default false;
}
