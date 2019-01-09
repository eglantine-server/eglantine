/*
 * Copyright 2015 Christophe Drion
 * 
 * @author christophe.drion@gmail.com
 * @version 1.3
 * @since 2015-02-23
 */

package com.eglantine.sql;

import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SQLFieldAnnotation {
	String column();
	boolean pkey() default false;
	boolean auto() default false;
	boolean unique() default false;
	boolean timestamp() default false;
	int minSize() default -1;
	int maxSize() default -1;
	String[] foreignKey() default {};
	String[] foreignKeySet() default {};
	String enumerationtype() default "";
}
