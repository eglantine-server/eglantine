/*
 * Copyright 2015 Christophe Drion
 * 
 * @author christophe.drion@gmail.com
 * @version 1.3
 * @since 2015-02-23
 */

package com.eglantine.ui;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;

@Named("navHistory")
@SessionScoped
public class NavHistory implements Serializable {

	private static final long serialVersionUID = -4597101760860397146L;

	static class NavItem {
		String label;
		String url;
		NavItem(String label, String url) {
			this.label = label;
			this.url = url;
		}
	}

	private MenuModel menuModel;
	
	@PostConstruct
	public void init() {

		menuModel = new DefaultMenuModel();

	}

	public MenuModel getMenuModel() {
		return menuModel;
	}

	public void setMenuModel(MenuModel menuModel) {
		this.menuModel = menuModel;
	}

	public void setHome() {

//		String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
//		String uri = origRequest.getRequestURI();

		HttpServletRequest origRequest = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();

		String url = origRequest.getRequestURL().toString();
		// Create index menuItem
		DefaultMenuItem item = new DefaultMenuItem();
		item.setValue("Home");
		item.setUrl(url);

		// Add menuItems
		this.menuModel = new DefaultMenuModel();
		this.menuModel.addElement(item);

	}
	
	public void push(String label) {
		
		if(label == null)
			label = "?";
		
		HttpServletRequest origRequest = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();

		Map<String,String> paramMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		
		DefaultMenuItem item = new DefaultMenuItem();
		
		String params = null;
		for (Map.Entry<String, String> entry : paramMap.entrySet()) {
			params = (params == null) ? "" : params+"&";
			try {
				params+= URLEncoder.encode(entry.getKey(),"UTF-8")+"="+URLEncoder.encode(entry.getValue(),"UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}		
		item.setValue(label);
//		item.setCommand("#{navHistory.goTo('"+this.menuModel.getElements().size()+"')}");
//		item.setParam("myhref",origRequest.getRequestURL().toString() );
		item.setProcess("@this");
		item.setUrl(origRequest.getRequestURL().toString()+(params != null ? "?"+params:""));
		item.setCommand("alert('coucou')");
//		item.setOnclick("#{navHistory.goTox}");
//        item.setImmediate(true);
		
		int size = menuModel.getElements().size();
		int lsize = -1;
		for (int i = 0; i < size; i++) {
			DefaultMenuItem litem = (DefaultMenuItem)menuModel.getElements().get(i);
			if (litem.getUrl().equals(item.getUrl())) {
				lsize = i;
			}
		}
		for (int i = size - 1; (lsize>=0)&&(i >= lsize); i--) {
			menuModel.getElements().remove(i);
		}
		this.menuModel.addElement(item);
	}
	
	public void goTo(String sindex) {
		
		Integer iindex = new Integer(sindex);
		List<MenuElement> elements = menuModel.getElements();
		
		DefaultMenuItem item = (DefaultMenuItem) elements.get(iindex);
		
//		return "" ;//.get("myhref");
		
	}

	public String goTox() {
		
		List<MenuElement> elements = menuModel.getElements();
		
		DefaultMenuItem item = (DefaultMenuItem) elements.get(0);
		
		return item.getUrl();
		
	}

}
