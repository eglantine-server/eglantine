/*
 * Copyright 2015 Christophe Drion
 * 
 * @author christophe.drion@gmail.com
 * @version 1.3
 * @since 2015-02-23
 */


package com.eglantine.ui;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.annotation.ManagedProperty;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;

import org.primefaces.event.RowEditEvent;

import com.eglantine.jsf.JSFBridge;
import com.eglantine.jsf.JSFClassAnnotation;
import com.eglantine.jsf.JSFFieldAnnotation;
import com.eglantine.reflection.Reflection;
import com.eglantine.sql.ISQLCallBack;
import com.eglantine.sql.PostgresqlBridge;
import com.eglantine.sql.SQLClassAnnotation;
import com.eglantine.sql.SQLFieldAnnotation;
import com.eglantine.sql.TableKeyValue;
import com.eglantine.ui.BasicBeanBridge.ItemList;
import com.eglantine.util.MyBiMap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public abstract class ABasicBeanBridge<T extends IBasicBeanBridge> implements Serializable {

	private ItemList itemList =null;
	private T editItem = getNewT();
	private T updateItem = getNewT();

	private HashMap<String,String> uiNameMap = null;
	private HashMap<String,String> uiIconMap = null;
	private HashMap<String,String> updatesMap = null;
	private HashMap<Integer,String[]> viewIdProps = null;

	protected abstract T getNewT();

	@SuppressWarnings("serial")
	class ItemList extends ArrayList<T> implements ISQLCallBack {

		@Override
		public void callBack(Object item) {
			this.add((T)item);
		}

		@Override
		public Object getNewItem() {
			return getNewT();
		}
	}

	T readItem(String column,String id) {

		String filter = null;
		
		filter = column+"="+id;
		itemList = new ItemList();
		PostgresqlBridge.getItemList(filter,itemList);

		return itemList.size() == 1 ? itemList.get(0) : null;
	}

	@PostConstruct
	public void init() {

		Map<String,String> requestParams = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String id = requestParams.get("id");	
		String column = requestParams.get("column");
		String table =requestParams.get("table");

		String otable = PostgresqlBridge.getTable(getNewT());

		if(!table.equals(otable))
			return;

		editItem = readItem(column,id);
	}

	public String reloadItem() {
		TableKeyValue kv = PostgresqlBridge.getTableKeyValue(editItem);
		
		editItem = readItem(kv.getKeycolumn(),kv.getValue().toString());
		
		FacesMessage msg = new FacesMessage(getLabel()+" Reloaded", editItem.getBaseId().toString());
		FacesContext.getCurrentInstance().addMessage(null, msg);

		return "";
	}
	public String updateItem() {
		String result = PostgresqlBridge.updateItem(editItem);
		if(result == null) {
			//				Reflection.copy(editItem,updatedItem);
			FacesMessage msg = new FacesMessage(getLabel()+" Updated", editItem.getBaseId().toString());
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		else {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN,"Error Updating "+editItem.getBaseId().toString(),result));
		}
		return "";
	}

	public String getLabel() {
		JSFClassAnnotation[] classAnnotations = editItem.getClass().getAnnotationsByType(JSFClassAnnotation.class);
		for (JSFClassAnnotation a : classAnnotations) {
			return a.label();
		}

		return null;
	}


	public String getLabel(Object o) {
		JSFClassAnnotation[] classAnnotations = o.getClass().getAnnotationsByType(JSFClassAnnotation.class);
		for (JSFClassAnnotation a : classAnnotations) {
			return a.label();
		}

		return null;
	}

	private String getTablePage(Object o) {
		JSFClassAnnotation[] classAnnotations = o.getClass().getAnnotationsByType(JSFClassAnnotation.class);
		for (JSFClassAnnotation a : classAnnotations) {
			return a.tablepage();
		}

		return null;
	}

	private String getPage(Object o) {
		JSFClassAnnotation[] classAnnotations = o.getClass().getAnnotationsByType(JSFClassAnnotation.class);
		for (JSFClassAnnotation a : classAnnotations) {
			return a.page();
		}

		return null;
	}

	public String deleteItem() {

		PostgresqlBridge.deleteItem(editItem);

		editItem = null;

		return "";

	}

	public T getEditItem() {
		return editItem;
	}

	public String[] jsfProps(int viewId) {
		
		if(viewIdProps == null)
			viewIdProps = new HashMap<Integer,String[]>();
			
		if(viewIdProps.get(viewId) == null) {
			ArrayList<String> plist = new ArrayList<String>();
			for(Field field : getNewT().getClass().getDeclaredFields()){
				Class<?> type = field.getType();
				String name = field.getName();
				JSFFieldAnnotation jsfFieldAnnotations = field.getAnnotation(JSFFieldAnnotation.class);
				if(jsfFieldAnnotations != null) {
					if ((viewId != 0)&&(jsfFieldAnnotations.views().length>0)) {
						for (int i=0; i < jsfFieldAnnotations.views().length;i++) {
							if(jsfFieldAnnotations.views()[i]==viewId) {
								plist.add(name);
								break;
							}
						}
					}
					else {
						plist.add(name);
					}
				}
			}
			String[] jsfProps = plist.toArray(new String[plist.size()]);
			viewIdProps.put(viewId, jsfProps);
		}
		return viewIdProps.get(viewId);
	}

	public String uiName(String prop) {
		if(uiNameMap == null) {
			uiNameMap = new HashMap<String,String>();
			for(Field field : getNewT().getClass().getDeclaredFields()){
				Class<?> type = field.getType();
				String name = field.getName();
				JSFFieldAnnotation jsfFieldAnnotations = field.getAnnotation(JSFFieldAnnotation.class);
				if(jsfFieldAnnotations != null) {
					if(jsfFieldAnnotations.label()!=null && !jsfFieldAnnotations.label().isEmpty()) {
						uiNameMap.put(name, jsfFieldAnnotations.label());
					}
				}
			}
		}
		return uiNameMap.get(prop);
	}

	public String uiIcon(String prop) {
		if(uiIconMap == null) {
			uiIconMap = new HashMap<String,String>();
			for(Field field : getNewT().getClass().getDeclaredFields()){
				Class<?> type = field.getType();
				String name = field.getName();
				JSFFieldAnnotation jsfFieldAnnotations = field.getAnnotation(JSFFieldAnnotation.class);
				if(jsfFieldAnnotations != null) {
					if(jsfFieldAnnotations.icon()!=null && !jsfFieldAnnotations.icon().isEmpty()) {
						uiIconMap.put(name, jsfFieldAnnotations.icon());
					}
				}
			}
		}
		return uiIconMap.get(prop);
	}

	public boolean isPassword(String prop) {
		return JSFBridge.isPassword(getNewT(),prop);
	}

	public boolean isSelectOneMenu(String prop) {
		return JSFBridge.isSelectOneMenu(getNewT(),prop);
	}

	public boolean isDate(String prop) {
		return JSFBridge.isDate(getNewT(),prop);
	}

	public boolean isXmlDoc(String prop) {
		Field field;
		try {
			field = getNewT().getClass().getDeclaredField(prop);
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return false;
		}
		if (field==null)
			return false;
		JSFFieldAnnotation jsfFieldAnnotations = field.getAnnotation(JSFFieldAnnotation.class);
		if(jsfFieldAnnotations != null) {
			return jsfFieldAnnotations.xmldoc();
		}
		return false;
	}

	Boolean canSend = null;
	Field mailToField = null;
	public boolean canSendMailTo() {
		if(canSend == null) {
			canSend = false;
			for(Field field : getNewT().getClass().getDeclaredFields()){
				Class<?> type = field.getType();
				String name = field.getName();
				JSFFieldAnnotation jsfFieldAnnotations = field.getAnnotation(JSFFieldAnnotation.class);
				if(jsfFieldAnnotations != null) {
					if((jsfFieldAnnotations.sendMailTo()&&(type.isInstance(new String())))) {
						canSend=true;
						mailToField = field;
					}
				}
			}
		}
		return canSend;
	}

	public String getMailTo() {
		if(canSendMailTo()&&(getEditItem()!=null)) {
			try {
				return (String)Reflection.get(getEditItem(),mailToField);
			} catch (IllegalArgumentException | IllegalAccessException e) {
			}
		}
		return null;
	}

//	HashMap<String,BiMap<String,Integer>> foreignMaps = new HashMap<String,BiMap<String,Integer>>();
//	HashMap<String,BiMap<String, String>> enumerationTypeMap = new HashMap<String,BiMap<String, String>>();

	MyBiMap<String,MyBiMap<String,Integer>> foreignMaps = new MyBiMap<String,MyBiMap<String,Integer>>();
	MyBiMap<String,MyBiMap<String, String>> enumerationTypeMap = new MyBiMap<String,MyBiMap<String, String>>();

	public MyBiMap<String, Integer> getForeignList(Object item, String prop) {
		MyBiMap<String,Integer> ret = foreignMaps.get(prop);
		if(ret != null) {
			return ret;
		}

		//TODO use .getClass().getDeclaredField(prop); instead of looping getDeclaredFields()
		Field field;
		try {
			field = getNewT().getClass().getDeclaredField(prop);
			/*		for(Field field : getNewT().getClass().getDeclaredFields()){
			Class<?> type = field.getType();
			String name = field.getName();
			if(name.equals(prop)) {
			 */				SQLFieldAnnotation sqlFieldAnnotations = field.getAnnotation(SQLFieldAnnotation.class);
			 if(sqlFieldAnnotations != null) {
				 String[] foreignkey = sqlFieldAnnotations.foreignKey();
				 String[] foreignkeySet = sqlFieldAnnotations.foreignKeySet();
				 if(foreignkey.length > 2) {
					 String[] foreigncols = new String[foreignkey.length - 2];
					 for (int i = 2; i < foreignkey.length;i++) {
						 foreigncols[i-2] = foreignkey[i];
					 }
					 ret = PostgresqlBridge.getForeignItemList(null,foreignkey[0],foreignkey[1],foreigncols);
					 foreignMaps.put(prop,ret);
					 return ret;
				 }
				 else if(foreignkeySet.length > 2) {
					 String[] foreigncols = new String[foreignkeySet.length - 2];
					 for (int i = 2; i < foreignkeySet.length;i++) {
						 foreigncols[i-2] = foreignkeySet[i];
					 }
					 String columnName = (String)Reflection.get(item,foreignkeySet[0]);
					 if((ret=foreignMaps.get(prop+":::"+columnName))==null) { 
						 ret = PostgresqlBridge.getForeignItemList(null,columnName,foreignkeySet[1],foreigncols);
						 foreignMaps.put(prop+":::"+columnName,ret);
					 }
					 return ret;
				 }
			 }
			 /*			}
		}
			  */
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public MyBiMap<String, String> getEnumList(String prop) {
		MyBiMap<String, String> ret = enumerationTypeMap.get(prop);
		if(ret != null) {
			return ret;
		}
//		ret = HashBiMap.create();
		ret = new MyBiMap<String,String>();
		try {
			Field field = getNewT().getClass().getDeclaredField(prop);
			SQLFieldAnnotation sqlFieldAnnotations = field.getAnnotation(SQLFieldAnnotation.class);
			if(sqlFieldAnnotations!= null) {
				String enum_type = sqlFieldAnnotations.enumerationtype();
				if(enum_type.length()>0) {
					String[] enumList = PostgresqlBridge.getEnumeration(enum_type);
					if(enumList != null) {
						for(int i = 0; i < enumList.length;i++) {
							ret.put(enumList[i], enumList[i]);
						}
						enumerationTypeMap.put(prop, ret);
						return ret;
					}
				}
			}

		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}


	public Object getList(Object item,String prop) {
		Object foreignList = getForeignList(item,prop);
		if(foreignList != null)
			return foreignList;
		Object enumList = getEnumList(prop);
		if(enumList != null)
			return enumList;
		return null;
	}

	public String getForeignName(Object item,String prop) {

		MyBiMap<String, String> enumList = enumerationTypeMap.get(prop);
		MyBiMap<String,Integer> foreignList = foreignMaps.get(prop);

		if((foreignList == null)&&(enumList==null)) {
			enumList = getEnumList(prop);
			if(enumList==null)
				foreignList = getForeignList(item,prop);
		}
		if((foreignList == null)&&(enumList == null))
			return null;
		if(foreignList != null) {
			try {
				Object i = Reflection.get(item, prop);
				if(i instanceof Integer) {
//					return foreignList.inverse().get(i);
					return foreignList.getInverse((Integer)i);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				return null;
			}
		}
		else if(enumList != null) {
			try {
				Object i = Reflection.get(item, prop);
				if(i instanceof String) {
					return (String)i;
				}
			}
			catch (IllegalArgumentException | IllegalAccessException e) {
				return null;
			}
		}
		return null;
	}

	public void editSet(String prop,Object value) {
		try {
			Reflection.set(editItem,prop,value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onPropertyChange(String prop) {
		int i = 0;
	}

	public String getDependantPropertyIds(String prop) {
		if(updatesMap == null) {
			updatesMap = new HashMap<String,String>();
			for(Field field : getNewT().getClass().getDeclaredFields()){
				String name = field.getName();
				JSFFieldAnnotation jsfFieldAnnotations = field.getAnnotation(JSFFieldAnnotation.class);
				if(jsfFieldAnnotations != null) {
					if(jsfFieldAnnotations.update()!=null && !jsfFieldAnnotations.update().isEmpty()) {
						updatesMap.put(name, jsfFieldAnnotations.update());
					}
				}
			}
		}
		return updatesMap.get(prop);
	}

	public void onPropertyChange(Object item,String prop) {
		int i = 0;
	}

	public String getComponentPropId(Object item,String prop) {
		int i = 0;
		return item+"::prop";
	}

	public boolean isHRef(String prop) {
		return JSFBridge.isHRef(getNewT(), prop);
	}

	public boolean isIcon(String prop) {
		return JSFBridge.isIcon(getNewT(), prop);
	}

	public String goTo(Object item,String prop) {
		int i = 0;

		try {
			Field field = item.getClass().getDeclaredField(prop);
			Object fieldObject = Reflection.get(item, prop);
			JSFFieldAnnotation jsfFieldAnnotations = field.getAnnotation(JSFFieldAnnotation.class);
			SQLFieldAnnotation sqlFieldAnnotations  = field.getAnnotation(SQLFieldAnnotation.class);
			if(jsfFieldAnnotations.href().isEmpty())
				return null;
			else if(jsfFieldAnnotations.href().equals("@")) {
				Object o = Reflection.get(item, prop);
				if(o==null)
					return null;
				else 
					return o.toString();
			}
			String table;
			String column;
			String value;
			String name;
			if(sqlFieldAnnotations.foreignKey().length>=2) {
				table = sqlFieldAnnotations.foreignKey()[0];
				column = sqlFieldAnnotations.foreignKey()[1];
				value = fieldObject.toString();
				name = getForeignName(item,prop);
			}
			else {
				TableKeyValue tableKeyValue = PostgresqlBridge.getTableKeyValue(item);
				table = tableKeyValue.getTable();
				column = tableKeyValue.getKeycolumn();
				value = tableKeyValue.getValue().toString();
				name = Reflection.get(item, prop).toString();
			}
			String res =  jsfFieldAnnotations.href()+"?faces-redirect=true"+
					"&id="+value+
					"&table="+table+
					"&column="+column+
					"&name="+name;
			return res;
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

}
