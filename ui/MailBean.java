package com.eglantine.ui;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.eglantine.jsf.SessionUtils;
import com.eglantine.mail.MailTLS;
import com.eglantine.sql.users.PostgresqlUsers;

@Named("mailBean")
@ViewScoped
public class MailBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2024513517815032918L;

	private String mailText = "empty";
	private String mailSubject = "no subject";
	
  	public void sendMail(String mailto) {
  		String login = SessionUtils.getLogin();
  		String replyto = PostgresqlUsers.getUserEmail(login);
		if((mailto != null)&&(replyto != null)&&(mailSubject != null)&&(mailText != null)) {
  			MailTLS.sendEglantineMail(replyto, mailto, mailSubject, mailText);
  			
//  			System.out.println("MAIL : \n"+mailText);
	        FacesContext.getCurrentInstance().addMessage(null,
  	                new FacesMessage("eMail sent to "+mailto));
  		}
		else {
			
  	        FacesContext.getCurrentInstance().addMessage(null,
  	                new FacesMessage(FacesMessage.SEVERITY_WARN,"Error Sending Mail",
  	                		(mailto==null?"check mail address":"") +
  	                		(replyto==null?"check sender mail address":"") +
  	                		(mailSubject==null?"no mail subject":"") +
  	                		(mailText==null?"no mail content":"")
  	                		));
		}
  	}

	public String getMailText() {
		return mailText;
	}

	public void setMailText(String mailText) {
		this.mailText = mailText;
	}

	public String getMailSubject() {
		return mailSubject;
	}

	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

}
