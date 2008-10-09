package org.opensha.util.http;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.opensha.gui.UserAuthDialog;


public class HTTPAuthenticator extends Authenticator {

    public PasswordAuthentication getPasswordAuthentication () {
    	UserAuthDialog auth = new UserAuthDialog(null, false);
    	auth.setVisible(true);
        return new PasswordAuthentication (auth.getUsername(), auth.getPassword());
    }
}
