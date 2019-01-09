/*
 * Copyright 2015 Christophe Drion
 * 
 * @author christophe.drion@gmail.com
 * @version 1.3
 * @since 2015-02-23
 */
package com.eglantine.sql;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.postgresql.jdbc.PgArray;

import com.eglantine.reflection.Reflection;
import com.eglantine.util.MyBiMap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 *
 * @author postgresqltutorial.com
 */
public class PostgresqlBridge {

	static boolean debug = true;
	
	public static String getTable(Object o) {
		
		SQLClassAnnotation[] classAnnotations = o.getClass().getAnnotationsByType(SQLClassAnnotation.class);
		for (SQLClassAnnotation a : classAnnotations) {
			return a.table();
		}
		
		return null;
	}

	static public void getItemList(String filter,ISQLCallBack sqlCallBack) {

		String columns = null;
		Object o = sqlCallBack.getNewItem();

		String sqlTable = getTable(o);

		for(Field field : o.getClass().getDeclaredFields()){
//			Class<?> type = field.getType();
//			String name = field.getName();
			SQLFieldAnnotation sqlFieldAnnotations = field.getAnnotation(SQLFieldAnnotation.class);
			if(sqlFieldAnnotations != null) {
				String column = sqlFieldAnnotations.column();
				sqlFieldAnnotations.foreignKey();
				sqlFieldAnnotations.pkey();
				sqlFieldAnnotations.minSize();
				sqlFieldAnnotations.maxSize();

				if(columns == null)
					columns = new String(column);
				else
					columns += (","+column);
			}
		}

		if(columns == null)
			return;

		String SQL = "SELECT "+columns+" FROM "+sqlTable;

		if(filter != null) {
//			SQL += " WHERE * LIKE %"+filter+"%";
			SQL += " WHERE "+filter;
		}

		try (
				Connection conn = PostgresqlAccess.connect();
				PreparedStatement pstmt = conn.prepareStatement(SQL)) {
			
			if(debug) System.out.println(pstmt.toString());

			ResultSet rs = pstmt.executeQuery();

			while(rs.next()) {

				Object sqlItem = sqlCallBack.getNewItem();

				for(Field field : sqlItem.getClass().getDeclaredFields()){
//					Class type = field.getType();
//					String name = field.getName();
					SQLFieldAnnotation sqlFieldAnnotations = field.getAnnotation(SQLFieldAnnotation.class);

					if(sqlFieldAnnotations != null) {

						Reflection.set(sqlItem,field,rs);

					}
				}

				sqlCallBack.callBack(sqlItem);
			}
			conn.close();
		} catch (SQLException | IllegalArgumentException | ReflectiveOperationException e) {
			//            System.out.println(e.getMessage());
			int i = 0;
		}
	}
	
	private static Object _dateConvert(Object o, Field field) {
		if(o instanceof java.util.Date) { 
			if(field.getAnnotation(SQLFieldAnnotation.class).timestamp()) {
				return new java.sql.Timestamp(((java.util.Date)o).getTime());
			}
			else {
				return new java.sql.Date(((java.util.Date)o).getTime());
			}
		}
		else 
			return o;
	}

	public static String updateItem(Object sqlObject) {


		String SQL = "UPDATE "+getTable(sqlObject)+" SET ";
		
		String columns = null;
		String pkeyColumn = null;
		Object pkeyValue = null;
		ArrayList<Object> valueList = new ArrayList<Object>(); 

		for(Field field : sqlObject.getClass().getDeclaredFields()){
//			Class<?> type = field.getType();
//			String name = field.getName();
			SQLFieldAnnotation sqlFieldAnnotations = field.getAnnotation(SQLFieldAnnotation.class);
			if(sqlFieldAnnotations != null) {
				String column = sqlFieldAnnotations.column();
				sqlFieldAnnotations.foreignKey();
				if(sqlFieldAnnotations.pkey() == true) {
					pkeyColumn = column;
					try {

						pkeyValue = Reflection.get(sqlObject,field);

					} catch (IllegalArgumentException | IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				sqlFieldAnnotations.minSize();
				sqlFieldAnnotations.maxSize();

				if((pkeyColumn==null)||(!column.equals(pkeyColumn))) {
					String enumType = sqlFieldAnnotations.enumerationtype();
					String exton = " = ? ";
					if(enumType.length()>0) {
						exton = " =CAST( ? AS "+enumType+")";
					}
					if(columns == null)
						columns = new String(column)+exton;
					else
						columns += (","+column+exton);

					try {
						Object o = Reflection.get(sqlObject,field);
						o = _dateConvert(o,field);
						valueList.add(o);
					} catch (IllegalArgumentException | IllegalAccessException e) {

						e.printStackTrace();
					}
				}
			}
		}

		if((columns == null)||(pkeyColumn == null)||(pkeyValue == null))
			return null;

		SQL += columns+" WHERE "+pkeyColumn+" = ?";
		try (
				Connection conn = PostgresqlAccess.connect();
				PreparedStatement pstmt = conn.prepareStatement(SQL)) {

			int i = 1;
			for (Object o : valueList) {
/*				if(o instanceof java.util.Date) {
					o = new java.sql.Date(((java.util.Date)o).getTime());
				}
*/				pstmt.setObject(i++, o);
			}

			pstmt.setObject(i, pkeyValue);

			if(debug) System.out.println(pstmt.toString());

			pstmt.executeUpdate();

			return null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();

			return e.getMessage();
		}
	}

	public static String createItem(Object sqlObject) {

		String table = getTable(sqlObject);

		String columns = null;
		String values = null;
		SQLFieldAnnotation returningFieldAnnotations = null;
		Field returningField = null;

		ArrayList<Object> valueList = new ArrayList<Object>();

		try {
			for(Field field : sqlObject.getClass().getDeclaredFields()){
//				Class<?> type = field.getType();
//				String name = field.getName();
				SQLFieldAnnotation sqlFieldAnnotations = field.getAnnotation(SQLFieldAnnotation.class);
				if(sqlFieldAnnotations != null) {
					String column = sqlFieldAnnotations.column();

					sqlFieldAnnotations.foreignKey();
					sqlFieldAnnotations.pkey();
					sqlFieldAnnotations.minSize();
					sqlFieldAnnotations.maxSize();

					if(sqlFieldAnnotations.auto()) {
						returningField = field;
						returningFieldAnnotations = sqlFieldAnnotations;
					}
					else {
						Object o = Reflection.get(sqlObject,field);
						o = _dateConvert(o,field);
						valueList.add(o);
						if(columns == null) {
							columns = new String(column);
							String enumType = sqlFieldAnnotations.enumerationtype();
							if(enumType.length()>0) {
								values = " CAST( ? AS "+enumType+")";
							}
							else {
								values = new String("?");
							}
						}
						else {
							columns += (","+column);
							String enumType = sqlFieldAnnotations.enumerationtype();
							if(enumType.length()>0) {
								values += ", CAST( ? AS "+enumType+")";
							}
							else {
								values += ",?";
							}
						}
					}
				}
			}
		}
		catch (IllegalArgumentException | IllegalAccessException e) {
			return e.getMessage();
		}

		String SQL = "INSERT INTO "+table+" ("+columns+") VALUES("+values+")" + (returningFieldAnnotations != null ? (" RETURNING "+returningFieldAnnotations.column()) : "");

		try (
				Connection conn = PostgresqlAccess.connect();
				PreparedStatement pstmt = conn.prepareStatement(SQL)) {

			int i = 1;
			for (Object o : valueList) {
/*				if(o instanceof java.util.Date) {
					o = new java.sql.Date(((java.util.Date)o).getTime());
				}
*/				pstmt.setObject(i++, o);
			}

			if(debug) System.out.println(pstmt.toString());

			ResultSet rs = pstmt.executeQuery();

			while(rs.next() && (returningFieldAnnotations != null)) {

				Reflection.set(sqlObject,returningField,rs);

			}
			pstmt.closeOnCompletion();
			conn.close();
			return null;
		} catch (SQLException | IllegalArgumentException | IllegalAccessException e) {
			return e.getMessage();
		}

	}
	
	public static TableKeyValue getTableKeyValue(Object sqlObject) {
		String pkeyColumn = null;
		Object pkeyValue = null;
		String table = getTable(sqlObject);

		for(Field field : sqlObject.getClass().getDeclaredFields()){
//			Class<?> type = field.getType();
//			String name = field.getName();
			SQLFieldAnnotation sqlFields = field.getAnnotation(SQLFieldAnnotation.class);
			if(sqlFields != null) {
				String column = sqlFields.column();
				sqlFields.foreignKey();
				if(sqlFields.pkey() == true) {
					pkeyColumn = column;
					try {

						pkeyValue = Reflection.get(sqlObject,field);

					} catch (IllegalArgumentException | IllegalAccessException e) {
						return null;
					}
				}
			}
		}
		return new TableKeyValue(table,pkeyColumn,pkeyValue);
	}

	public static String deleteItem(Object sqlObject) {

		TableKeyValue tableKeyValue = getTableKeyValue(sqlObject);

		if((tableKeyValue.getKeycolumn() == null) || (tableKeyValue.getValue() == null)) {
			return "no primary key is defined";
		}
		String SQL = "DELETE FROM "+tableKeyValue.getTable()+" WHERE "+tableKeyValue.getKeycolumn()+" = ?";

		try (
				Connection conn = PostgresqlAccess.connect();
				PreparedStatement pstmt = conn.prepareStatement(SQL)) {

			pstmt.setObject(1,tableKeyValue.getValue());
			if(debug) System.out.println(pstmt.toString());
			pstmt.executeUpdate();
			pstmt.closeOnCompletion();
			conn.close();
			return null;
		} catch (SQLException e) {
			return e.getMessage();
		}
	}

	static public MyBiMap<String, Integer> getForeignItemList(String filter,String sqlTable,String idColumn, String[] presentationColumns) {

//		HashBiMap<String,Integer> map = HashBiMap.create();
		MyBiMap<String,Integer> map = new MyBiMap<String,Integer>();
		
		String pcolumns = null;
		
		for(String pcolumn : presentationColumns) {
			if(pcolumns == null)
				pcolumns = new String(pcolumn);
			else
				pcolumns += ","+pcolumn;
		}

		String SQL = "SELECT "+idColumn+","+pcolumns+" FROM "+sqlTable+ " ORDER BY "+pcolumns+" ASC";

		if(filter != null) {
			SQL += " WHERE * LIKE %"+filter+"%";
		}

		try (
				Connection conn = PostgresqlAccess.connect();
				PreparedStatement pstmt = conn.prepareStatement(SQL)) {

			if(debug) System.out.println(pstmt.toString());

			ResultSet rs = pstmt.executeQuery();

			while(rs.next()) {
				String rcolumn = null;
				for(String pcolumn : presentationColumns) {
					String lcol = rs.getString(pcolumn);
					if(lcol == null)
						continue;
					if(rcolumn == null)
						rcolumn = new String(lcol);
					else
						rcolumn += ','+lcol;
				}
				map.put(rcolumn,rs.getInt(idColumn));
			}
			conn.close();
		} catch (SQLException e) {
			//            System.out.println(e.getMessage());
			int i = 0;
		}
		
		return map;
	}
	
	static public String[] getEnumeration(String type) {
		String SQL = "SELECT enum_range(NULL::"+type+")";
		
		try (
				Connection conn = PostgresqlAccess.connect();
				PreparedStatement pstmt = conn.prepareStatement(SQL)) {

			if(debug) System.out.println(pstmt.toString());

			ResultSet rs = pstmt.executeQuery();

			while(rs.next()) {
				Object o = rs.getObject(1);
				if (o instanceof PgArray) {
					o = ((PgArray)o).getArray();
					if (o instanceof String[]) 
						return (String[])o;
				}
				int z = 0;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}