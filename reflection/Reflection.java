/*
 * Copyright 2015 Christophe Drion
 * 
 * @author christophe.drion@gmail.com
 * @version 1.3
 * @since 2015-02-23
 */

package com.eglantine.reflection;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.jdbc.PgArray;

import com.eglantine.sql.SQLFieldAnnotation;

public class Reflection {

	public static void set(Object object,Field field,ResultSet rs) throws IllegalArgumentException, IllegalAccessException, SQLException {
		//TODO should use setter instead of direct access
		SQLFieldAnnotation fieldAnnotations = field.getAnnotation(SQLFieldAnnotation.class);
		if(fieldAnnotations != null) {
			boolean accessibleField = field.isAccessible();
			field.setAccessible(true);
			Object rso = rs.getObject(fieldAnnotations.column());
			if (rso instanceof PgArray) {
				Object rso_b = ((PgArray)rso).getArray();
				field.set(object, rso_b);
			}
			else {
				field.set(object, rso);
			}
			field.setAccessible(accessibleField);
		}
	}
	
	public static void set(Object object,Field field,Object value) throws IllegalArgumentException, IllegalAccessException {
		boolean accessibleField = field.isAccessible();
		field.setAccessible(true);
		field.set(object,value);
		field.setAccessible(accessibleField);
	}
	
	public static void set(Object object,String prop,Object value) throws IllegalArgumentException, IllegalAccessException {
		for(Field field : object.getClass().getDeclaredFields()){
			if(field.getName().compareTo(prop) == 0) {
				boolean accessibleField = field.isAccessible();
				field.setAccessible(true);
				field.set(object,value);
				field.setAccessible(accessibleField);
			}
		}
	}

	public static Object get(Object o, Field field) throws IllegalArgumentException, IllegalAccessException {
		//TODO should use getter instead of direct access
		Object result = null;

		boolean accessibleField = field.isAccessible();
		field.setAccessible(true);
		result = field.get(o);
		field.setAccessible(accessibleField);

		return result;
	}

	public static Object get(Object o, String prop) throws IllegalArgumentException, IllegalAccessException  {
		for(Field field : o.getClass().getDeclaredFields()){
			if(field.getName().compareTo(prop) == 0) {
				return get(o,field);
			}
		}
		return null;
	}

	public static void copy(Object source, Object destination) throws IllegalArgumentException, IllegalAccessException {
		for(Field field : source.getClass().getDeclaredFields()){
			set(destination,field,get(source,field));
		}
	}
}
