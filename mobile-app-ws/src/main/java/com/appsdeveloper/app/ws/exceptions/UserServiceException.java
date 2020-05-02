package com.appsdeveloper.app.ws.exceptions;

public class UserServiceException extends RuntimeException{
	
	private static final long serialVersionUID = 2695789672412745259L;
	
	public UserServiceException(String message) {
		super(message);
	}
}
