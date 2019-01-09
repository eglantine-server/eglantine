/*
 * Copyright 2015 Christophe Drion
 * 
 * @author christophe.drion@gmail.com
 * @version 1.3
 * @since 2015-02-23
 */
package com.eglantine.jsf;

import java.lang.reflect.Field;

import com.eglantine.reflection.Reflection;
import com.eglantine.sql.SQLFieldAnnotation;

public class JSFBridge {

	public static boolean isPassword(Object o,String property) {
		
		Field field;
		try {
			field = o.getClass().getDeclaredField(property);
		} catch (NoSuchFieldException | SecurityException e) {
			return false;
		}
		if(field == null)
			return false;
		JSFFieldAnnotation jsfFieldAnnotations = field.getAnnotation(JSFFieldAnnotation.class);
		if(jsfFieldAnnotations == null)
			return false;
		return jsfFieldAnnotations.password();
	}

	public static boolean isSelectOneMenu(Object o,String property) {
		
		Field field;
		try {
			field = o.getClass().getDeclaredField(property);
		} catch (NoSuchFieldException | SecurityException e) {
			return false;
		}
		if(field == null)
			return false;
		JSFFieldAnnotation jsfFieldAnnotations = field.getAnnotation(JSFFieldAnnotation.class);
		if(jsfFieldAnnotations == null)
			return false;
		return jsfFieldAnnotations.selectOneMenu();
	}

	public static boolean isDate(Object o, String property) {
		Field field;
		try {
			field = o.getClass().getDeclaredField(property);
		} catch (NoSuchFieldException | SecurityException e) {
			return false;
		}
		if(field == null)
			return false;
		return(field.getType().isInstance(new java.sql.Date(0))||
				field.getType().isInstance(new java.util.Date()));
	}
	
	public static boolean isBoolean(Object o, String property) {
		Field field;
		try {
			field = o.getClass().getDeclaredField(property);
		} catch (NoSuchFieldException | SecurityException e) {
			return false;
		}
		if(field == null)
			return false;
		return(field.getType().isInstance(new Boolean(true)));
	}
	
	public static boolean isIcon(Object o, String property) {
		Field field;
		try {
			field = o.getClass().getDeclaredField(property);
		} catch (NoSuchFieldException | SecurityException e) {
			return false;
		}
		if(field == null)
			return false;
		JSFFieldAnnotation jsfFieldAnnotations = field.getAnnotation(JSFFieldAnnotation.class);
		if(jsfFieldAnnotations == null)
			return false;
		return jsfFieldAnnotations.icon().length() > 0;
	}

	public static boolean isHRef(Object o,String prop) {
		try {
			Field field = o.getClass().getDeclaredField(prop);
			JSFFieldAnnotation jsfFieldAnnotation  = field.getAnnotation(JSFFieldAnnotation.class);
			if(!jsfFieldAnnotation.href().isEmpty())
				return true;
			return false;
		} catch (NoSuchFieldException | SecurityException e) {
			return false;
		}
	}
}
