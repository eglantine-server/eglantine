package com.eglantine.jsf;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SessionUtils {

	public static HttpSession getSession() {
		return (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(false);
	}

	public static HttpServletRequest getRequest() {
		return (HttpServletRequest) FacesContext.getCurrentInstance()
				.getExternalContext().getRequest();
	}

	public static String getLogin() {
		HttpSession session = getSession();
		return session.getAttribute("login").toString();
	}

	public static void login(String login, String[] rights) {
		HttpSession session = getSession();
		session.setAttribute("login", login);
		session.setAttribute("rights", rights);
	}

	public static void logout() {
		HttpSession session = getSession();
		session.removeAttribute("login");
		session.removeAttribute("rights");
	}
}
