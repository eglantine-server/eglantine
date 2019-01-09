/*
 * Copyright 2015 Christophe Drion
 * 
 * @author christophe.drion@gmail.com
 * @version 1.3
 * @since 2015-02-23
 */
package com.eglantine.ui;

public interface ISecBeanBridge extends IBasicBeanBridge {
	public Integer getUserId();
	public void setUserId(Integer userId);
	public Integer getGroupId();
	public void setGroupId(Integer userId);
	public void setPermissions(Short permissions);
	public short getPermissions();
}
