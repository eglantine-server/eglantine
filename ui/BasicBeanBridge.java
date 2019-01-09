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
import com.eglantine.util.MyBiMap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public abstract class BasicBeanBridge<T extends IBasicBeanBridge> implements Serializable {

	class Context {
		String id = null;
		String column = null;
		String table = null;
		String name = null;
	};
	
	Context context = new Context();

	private ItemList itemList =null;
	private T selectedItem;
	private T newItem = null;
	private T editItem = getNewT();
	
	@Inject
	@ManagedProperty("#{exFilter}")
	private ExFilter exFilter;

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

	T newItem() {

		T lnew = getNewT();
		
		if(context.id==null || context.column == null || context.table == null)
			return lnew;
		
		for(Field field: lnew.getClass().getDeclaredFields()) {
			SQLFieldAnnotation sqlFieldAnnotation = field.getAnnotation(SQLFieldAnnotation.class);
			if((sqlFieldAnnotation != null) && (sqlFieldAnnotation.foreignKey().length >= 2)) {
				if(sqlFieldAnnotation.foreignKey()[0].equals(context.table)&&
					sqlFieldAnnotation.foreignKey()[1].equals(context.column)) {
					try {
						if(field.getType() == Integer.class) {
							Reflection.set(lnew, field, new Integer(context.id));
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else if((sqlFieldAnnotation != null) && (sqlFieldAnnotation.foreignKeySet().length >= 2)) {
				try {
					Field fieldSet = lnew.getClass().getDeclaredField(sqlFieldAnnotation.foreignKeySet()[0]);
					JSFFieldAnnotation jsfFieldAnnotationSet = fieldSet.getAnnotation(JSFFieldAnnotation.class);
					for(int ii = 0; ii < jsfFieldAnnotationSet.uimap().length;ii+=2) {
						if(jsfFieldAnnotationSet.uimap()[ii].equals(context.table)) {
							Reflection.set(lnew, fieldSet, context.table);
							Reflection.set(lnew, field, new Integer(context.id));
							break;
						}
					};
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
					
				}
			}
		}
		return lnew;
	}

	@PostConstruct
	public void init() {

		String filter = null;
		
		context = new Context();

		Map<String,String> requestParams = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		context.id = requestParams.get("id");	
		context.column = requestParams.get("column");
		context.table =requestParams.get("table");
		context.name = requestParams.get("name");
		
		newItem = newItem();

		if((context.table!=null)&&(context.column!=null)&&(context.id!=null)) {
			for(Field field : getNewT().getClass().getDeclaredFields()){
				Class<?> type = field.getType();
				String name = field.getName();
				SQLFieldAnnotation sqlFieldAnnotation = field.getAnnotation(SQLFieldAnnotation.class);
				if(sqlFieldAnnotation != null) {
					if(sqlFieldAnnotation.foreignKey().length >= 2) {
						if (sqlFieldAnnotation.foreignKey()[0].equals(context.table)&&
								sqlFieldAnnotation.foreignKey()[1].equals(context.column)) {

							filter = filter == null ? "":filter+" OR ";
							filter += sqlFieldAnnotation.column()+"='"+context.id+"'";
						}
					}
					try {
						if(sqlFieldAnnotation.foreignKeySet().length >= 2) {
							Field f1 = getNewT().getClass().getDeclaredField(sqlFieldAnnotation.foreignKeySet()[0]);
							SQLFieldAnnotation sqlFieldAnnotationSel = f1.getAnnotation(SQLFieldAnnotation.class);
							if((sqlFieldAnnotationSel != null)&&(sqlFieldAnnotationSel.column()!=null)) {

								filter = filter == null ? "":filter+" OR ";
								filter += "("+sqlFieldAnnotationSel.column()+"='"+context.table+"'";

								filter = filter == null ? "":filter+" AND ";
								filter += sqlFieldAnnotation.column()+"='"+context.id+"')";
							}
						}
					} catch (IllegalArgumentException | NoSuchFieldException | SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		itemList = new ItemList();
		PostgresqlBridge.getItemList(filter,itemList);
	}

	/* initialize need to be called as follows
	<h:head>
		<f:metadata>
        	<f:viewParam name="table" value="#{itemsBean.exFilter.table}"/>
        	<f:viewParam name="column" value="#{itemsBean.exFilter.column}"/>
        	<f:viewParam name="id" value="#{itemsBean.exFilter.id}"/>
	    	<f:viewAction action="#{itemsBean.initialize}"/>
		</f:metadata>
	</h:head>
	 */
	public String initialize() {
		return null;
	}

	public void updateList() {
		init();
	}

	public ItemList getList() {
		return itemList;
	}

	public String getLabel() {
		JSFClassAnnotation[] classAnnotations = newItem.getClass().getAnnotationsByType(JSFClassAnnotation.class);
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

		String id = selectedItem.getBaseId().toString();

		itemList.remove(selectedItem);

		PostgresqlBridge.deleteItem(selectedItem);

		selectedItem = null;

		FacesMessage msg = new FacesMessage("Deleted ", id);
		FacesContext.getCurrentInstance().addMessage(null, msg);
		
		return "";

	}

	public T getSelectedItem() {
		return selectedItem;
	}

	public T getNewItem() {
		return newItem;
	}

	public T getEditItem() {
		return editItem;
	}

	public void setSelectedItem(T selectedItem) {
		this.selectedItem = selectedItem;
	} 

	public void onRowEditInit(RowEditEvent event) {
		T updatedItem =  (T) event.getObject();
		try {
			Reflection.copy(updatedItem,editItem);
			/*			event.getComponent();
			PrimeFaces pf = PrimeFaces.current();
			for (UIComponent child : event.getComponent().getChildren()) {
				pf.ajax().update(child.getClientId());
			}
			 */		} catch (IllegalArgumentException | IllegalAccessException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
			 }
		int i = 0;
	}

	public void onRowEdit(RowEditEvent event) {
		try {
			T updatedItem =  (T) event.getObject();
			String result = PostgresqlBridge.updateItem(updatedItem);
			if(result == null) {
				//				Reflection.copy(editItem,updatedItem);
				FacesMessage msg = new FacesMessage(getLabel()+" Edited", updatedItem.getBaseId().toString());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN,"Error Updating "+getLabel(),result));
			}
		}
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onRowCancel(RowEditEvent event) {
		try {
			T updatedItem =  (T) event.getObject();
			Reflection.copy(editItem,updatedItem);
			FacesMessage msg = new FacesMessage("Edit Cancelled", ((T) event.getObject()).getBaseId().toString());
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addNewItem() {
		String result;

		result = PostgresqlBridge.createItem(newItem);

		if(result != null) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN,"Error Creating "+getLabel(),result));
		}
		else {
			itemList.add(newItem);
			newItem = newItem();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("New "+getLabel()+" Created"));
		}
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
	
	public boolean isBoolean(String prop) {
		return JSFBridge.isBoolean(getNewT(),prop);
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
		if(canSendMailTo()&&(getSelectedItem()!=null)) {
			try {
				return (String)Reflection.get(getSelectedItem(),mailToField);
			} catch (IllegalArgumentException | IllegalAccessException e) {
			}
		}
		return null;
	}

	HashMap<String,MyBiMap<String,Integer>> foreignMaps = new HashMap<String,MyBiMap<String,Integer>>();
	HashMap<String,MyBiMap<String, String>> enumerationTypeMap = new HashMap<String,MyBiMap<String, String>>();

	private MyBiMap<String, Integer> getForeignList(Object item, String prop) {
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

	private MyBiMap<String, String> getEnumList(String prop) {
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

	// to handle datatable filtered rows
	private List<T> filtered;
	public List<T> getFiltered() { return filtered; }
	public void setFiltered(List<T> filtered) { this.filtered = filtered; }	

	//TODO enhance the custom sorter function
	public int sort(Object value1, Object value2) {

		if((value1 instanceof Date)&&(value2 instanceof Date)) {
			return ((Date)value1).compareTo((Date)value2);
		}
		else if((value1 instanceof Integer)&&(value2 instanceof Integer)) {
			return ((Integer)value1).compareTo((Integer)value2);
		}
		else if((value1 instanceof String)&&(value2 instanceof String)) {
			return ((String)value1).compareTo((String)value2);
		}

		return 0;
	}

	//TODO enhance the custom filter function
	public boolean filter(Object value, Object filter, Locale locale) {
		try {
			boolean greaterThan = false;
			boolean lessThan = false;
			boolean equal = false;
			if(!(filter instanceof String)) {
				return false;
			}
			String filterS = (String)filter;
			if(filterS.length()>0) {
				if(filterS.charAt(0)=='<') {
					lessThan = true;
					filterS = filterS.substring(1, filterS.length());
				}
				else if(filterS.charAt(0)=='>') {
					greaterThan = true;
					filterS = filterS.substring(1, filterS.length());
				}
			}
			else
				return true;

			if(value instanceof Date) {
				DateFormat format = new SimpleDateFormat("d/MM/yyyy", Locale.ENGLISH);
				Date date = format.parse((String)filterS);
				if(greaterThan)  
					return date.before((Date)value);
				else if(lessThan) 
					return date.after((Date)value);
				else
					return date.compareTo((Date)value) == 0;
			}
			else if(value instanceof Integer) {
				Integer integer = Integer.valueOf(filterS);
				if(greaterThan)  
					return (Integer)value > integer;
					else if(lessThan) 
						return (Integer)value < integer;
					else if(equal)
						return (Integer)value == integer;
					else
						return value.toString().toLowerCase().contains(filterS.toLowerCase());
			}
			else if(value instanceof String) {
				if(greaterThan)  
					return (((String) value).compareTo(filterS)>0);
				else if(lessThan)  
					return (((String) value).compareTo(filterS)<0);
				else if(equal)  
					return (((String) value).compareTo(filterS)==0);
				else
					return ((String)value).toLowerCase().contains(filterS.toLowerCase());
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			return true;
		}
		return true;
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
			if(jsfFieldAnnotations.href().equals("@")) {
				Object o = Reflection.get(item, prop);
				if(o==null)
					return null;
				else 
					return o.toString();
			}
			String table = null;
			String column = null;
			String value = null;
			String name = null;
			String href = null;
			if(sqlFieldAnnotations.foreignKey().length>=2) {
				table = sqlFieldAnnotations.foreignKey()[0];
				column = sqlFieldAnnotations.foreignKey()[1];
				value = fieldObject.toString();
				name = getForeignName(item,prop);
				href = jsfFieldAnnotations.href();
			}
			else if(sqlFieldAnnotations.foreignKeySet().length>=2) {
				Field fieldSel = item.getClass().getDeclaredField(sqlFieldAnnotations.foreignKeySet()[0]);
				Object fieldSelValue = Reflection.get(item, fieldSel);
				if(fieldSelValue != null && fieldSelValue instanceof String)
					table = (String)fieldSelValue;
				JSFFieldAnnotation jsfFieldSelAnnotation = fieldSel.getAnnotation(JSFFieldAnnotation.class);
				column = sqlFieldAnnotations.foreignKeySet()[1];
				value = fieldObject.toString();
				name = getForeignName(item,prop);
				if(jsfFieldAnnotations.href().equals("#") && (table != null)) {
					for(int ii = 0; ii < jsfFieldSelAnnotation.hrefmap().length; ii+=2) {
						if(jsfFieldSelAnnotation.hrefmap()[ii].equals(table)) {
							href = jsfFieldSelAnnotation.hrefmap()[ii+1];
							break;
						}
					}
				}
				else if(!jsfFieldAnnotations.href().equals("#")) {
					href = jsfFieldAnnotations.href();
				}
			}
			else {
				TableKeyValue tableKeyValue = PostgresqlBridge.getTableKeyValue(item);
				table = tableKeyValue.getTable();
				column = tableKeyValue.getKeycolumn();
				value = tableKeyValue.getValue().toString();
				name = Reflection.get(item, prop).toString();
				href = jsfFieldAnnotations.href();
			}
			if (href!=null && table != null) {
				String res =  href+"?faces-redirect=true"+
						"&id="+value+
						"&table="+table+
						"&column="+column+
						"&name="+name;
				return res;
			}
			return null;
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String goToAction() {
		int i = 0;
		return "";
	}

	public void goToListener(ActionEvent event) {
		System.out.println("testButtonActionListener invoked");
	}

	public ExFilter getExFilter() {
		return exFilter;
	}

	public void setExFilter(ExFilter exFilter) {
		this.exFilter = exFilter;
	}

}
