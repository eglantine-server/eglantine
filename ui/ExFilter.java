package com.eglantine.ui;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

@ViewScoped
@Named(value="exFilter")
public class ExFilter implements Serializable {
	
	private static final long serialVersionUID = -6476295152548780143L;
	
	private String table = null;
	private String column = null;
	private String id = null;
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
