package com.room5.server.spring_boot.web;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.HandlerMapping;

import com.fasterxml.jackson.core.JsonParseException;
import com.room5.canonical.ExceptionMessage;

@ControllerAdvice
public class ErrorHandleController
{

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandleController.class);

    @RequestMapping(produces = { "application/vnd.captech-v1.0+json", "application/vnd.captech-v2.0+json" })
    @ExceptionHandler({ MissingServletRequestParameterException.class, UnsatisfiedServletRequestParameterException.class,
	HttpRequestMethodNotSupportedException.class,
	ServletRequestBindingException.class
    })
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleRequestException(Exception ex, HttpServletRequest request) {
	System.err.println("!!!!!!!!!HERE : IoTaErrorHandleController: " + ex + " !!!!!!!!");
	String errMsg = "Request Error " + ex.getMessage();
	logger.error(errMsg, ex);
	ExceptionMessage authMessage = new ExceptionMessage(errMsg);
	authMessage.setCommand((String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
	authMessage.setResult(ExceptionMessage.AUTH_RESULT_CODE_GENERIC_ERROR2);
	return authMessage.toString();
    }

    @RequestMapping(produces = { "application/vnd.captech-v1.0+json", "application/vnd.captech-v2.0+json" })
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(value = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public @ResponseBody String handleUnsupportedMediaTypeException(HttpMediaTypeNotSupportedException ex,
	    HttpServletRequest request) {
	System.err.println("!!!!!!!!!HERE : IoTaErrorHandleController !!!!!!!!");
	String errMsg = "Unsupported Media Type" + ex.getLocalizedMessage() + ex.getSupportedMediaTypes();
	logger.error(errMsg, ex);
	ExceptionMessage authMessage = new ExceptionMessage(errMsg);
	authMessage.setCommand((String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
	authMessage.setResult(ExceptionMessage.AUTH_RESULT_CODE_GENERIC_ERROR3);
	return authMessage.toString();
    }

    @RequestMapping(produces = { "application/vnd.captech-v1.0+json", "application/vnd.captech-v2.0+json" })
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody String handleUncaughtException(Exception ex, HttpServletRequest request) {
	System.err.println("!!!!!!!!!HERE : IoTaErrorHandleController !!!!!!!!");
	logger.error("Uncaught exception", ex);
	String errMsg = "Unmanaged Error: ";
	if (ex.getCause() != null) {
	    errMsg += ex.getCause().getMessage();
	} else {
	    errMsg += ex.getMessage();
	}
	ExceptionMessage authMessage = new ExceptionMessage(errMsg);
	authMessage.setCommand((String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
	authMessage.setResult(ExceptionMessage.AUTH_RESULT_CODE_GENERIC_ERROR4);
	return authMessage.toString();
    }

    @RequestMapping(produces = { "application/vnd.captech-v1.0+json", "application/vnd.captech-v2.0+json" })
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleJSONParseError(HttpMessageNotReadableException ex, HttpServletRequest request) {
	//System.err.println("!!!!!!!!!HERE : IoTaErrorHandleController : handleJSONParseError !!!!!!!!");
	if ((ex.getCause() != null) && (ex.getCause() instanceof JsonParseException)) {
	    logger.error("JSON Parse Error", ex);
	    
	    //delete any non-printables
	    String error = ex.getCause().getMessage().replaceAll("[^\\p{Print}]", "");
	    
	    //get line/column
	    if (error.contains("[Source:")) {
		error = error.substring(0, error.indexOf("[Source:"));
		String errorMsg = ex.getCause().getMessage(); 
		if (errorMsg.lastIndexOf("line:") > -1) {
		    error += errorMsg.substring(errorMsg.lastIndexOf("line:"), errorMsg.length()-1); 
		}
	    }
	    error = "Unable to parse incoming JSON message: " + error;

	    ExceptionMessage authMessage = new ExceptionMessage(error);
	    authMessage.setCommand((String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
	    authMessage.setResult(ExceptionMessage.AUTH_RESULT_CODE_JSON_PARSE_ERROR);
	    return authMessage.toString();
	}
	
	return handleUncaughtException(ex, request);
    }

}
