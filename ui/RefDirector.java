package com.eglantine.ui;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.inject.Named;

@Named("refDirector")
@SessionScoped
public class RefDirector implements Serializable {
	
	public void goTo(ActionEvent e) {
		int i = 0;
	}

}
