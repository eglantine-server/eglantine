/*
 * Copyright 2015 Christophe Drion
 * 
 * @author christophe.drion@gmail.com
 * @version 1.3
 * @since 2015-02-23
 */
package com.eglantine.ui;

public interface IBasicBeanBridge {
	public boolean isSelected();
	public void setSelected(boolean selected);
	public Integer getBaseId();
	public String getRowStyle();
}
