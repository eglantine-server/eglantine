/*
 * Copyright 2015 Christophe Drion
 * 
 * @author christophe.drion@gmail.com
 * @version 1.3
 * @since 2015-02-23
 */
package com.eglantine.sql;

public class TableKeyValue {
	private String table;
	private String keycolumn;
	private Object value;
	public TableKeyValue(String table, String keycolumn, Object value) {
		this.table=table;
		this.keycolumn=keycolumn;
		this.value=value;
	}
	public String getTable() {
		return table;
	}
	public String getKeycolumn() {
		return keycolumn;
	}
	public Object getValue() {
		return value;
	}
}
