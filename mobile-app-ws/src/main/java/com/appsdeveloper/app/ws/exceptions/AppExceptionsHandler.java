package com.appsdeveloper.app.ws.exceptions;

import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.appsdeveloper.app.ws.ui.model.response.ErrorMessage;

@ControllerAdvice
public class AppExceptionsHandler {
	
	@ExceptionHandler(value= {UserServiceException.class})
	public ResponseEntity<Object> handleUserServiceException(UserServiceException ex, WebRequest reqest) {
		
		ErrorMessage eMsg = new ErrorMessage(new Date(), ex.getMessage());
		return new ResponseEntity<>(eMsg, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR) ;
	}
	
	@ExceptionHandler(value= {Exception.class})
	public ResponseEntity<Object> handleOtherExceptions(Exception ex, WebRequest reqest) {
		
		ErrorMessage eMsg = new ErrorMessage(new Date(), ex.getMessage());
		return new ResponseEntity<>(eMsg, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR) ;
	}
}