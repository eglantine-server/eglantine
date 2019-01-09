package com.eglantine.ui;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;

import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Size;

import com.eglantine.jsf.SessionUtils;
import com.eglantine.sql.users.PostgresqlUsers;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -550213087284998631L;
	
	
	@Size(min = 2, max = 50)
    private String username;
    @Size(min = 2, max = 50)
    private String password;
    
    private Object captcha;

    private String rights[];


    public String getusername ()
    {
        return username;
    }


    public void setUsername (final String name)
    {
        this.username = name;
    }

    public String getLoggedName () {
    	if(rights == null)
    		return null;
    	else
    		return username;
	}

	public void setLoggedName () {
		
	}
	
	public boolean getLogged() {
		return rights != null;
	}

    public String getPassword ()
    {
        return password;
    }


    public void setPassword (final String password)
    {
        this.password = password;
    }
    
    public String[] getRights() {
    	if(rights == null) {
    		return new String[0];
    	}
    	else {
    		return rights;
    	}
    }

  //validate login
  	public String validateUsernamePassword() {
  		rights = PostgresqlUsers.getUserRights(username, password);
  		if (rights != null) {
  			SessionUtils.login(username, rights);
  			return "login";
  		} else {
  			SessionUtils.logout();
  			FacesContext.getCurrentInstance().addMessage(
  					null,
  					new FacesMessage(FacesMessage.SEVERITY_WARN,
  							"Incorrect Username and Password",
  							"Please enter correct username and Password"));
  			return "failed";
  		}
 	}

  	//logout event, invalidate session
  	public String logout() {
  	
  		rights = null;
  		password = null;

  		HttpSession session = SessionUtils.getSession();
		session.setAttribute("userName", null);
		session.setAttribute("userRights", null);
  		session.invalidate();
  		return "logout";
  	}


	public Object getCaptcha() {
		return captcha;
	}


	public void setCaptcha(Object captcha) {
		this.captcha = captcha;
	}
}
