/*
 * Copyright 2015 Christophe Drion
 * 
 * @author christophe.drion@gmail.com
 * @version 1.3
 * @since 2015-02-23
 */
package com.eglantine.sql;

public interface ISQLCallBack {
	public void callBack(Object item);
	abstract Object getNewItem();
}
