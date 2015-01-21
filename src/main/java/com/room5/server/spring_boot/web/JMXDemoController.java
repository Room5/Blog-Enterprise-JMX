package com.room5.server.spring_boot.web;

import java.io.Serializable;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;

import com.room5.canonical.ExceptionMessage;
import com.room5.server.spring_boot.jmx.JMXServerStatistics;


@Controller
public class JMXDemoController
{

    @Autowired
    private JMXServerStatistics jmxStatistics;
    
    private Random rand = new Random(System.currentTimeMillis());

    
    private static final Logger logger = LoggerFactory.getLogger(JMXDemoController.class);

    
    @RequestMapping(value = "/demo/endpoint1", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<? extends Serializable> endPoint1(HttpServletRequest request)
	throws AuthenticationException {
	logger.info("Controller called on end point 1.");
	
	/*
	 * JMX calls replaced in interceptor
	 * 
	 */
	
	//increment JMX request counter
	//jmxStatistics.incrementRequests();

	//increment JMX pending requests
	//jmxStatistics.incrementCurrentRequests();
	
	//try {
	    
	    //begin processing
	    long timestamp = System.currentTimeMillis();
	    try {
		//sleep for a random time 
		Thread.sleep(rand.nextInt(2000) * 10);
	    } catch (Throwable e) {
		//capture response time statistics
		jmxStatistics.addSummaryStatistics1(System.currentTimeMillis() - timestamp);
		//increment JMX failed requests
		jmxStatistics.incrementFailures();

		String errMsg = "Error processing request";
		logger.error(errMsg, e);
		ExceptionMessage exceptionMsg = new ExceptionMessage("Internal server exception.");
		exceptionMsg.setCommand((String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
		exceptionMsg.setResult(ExceptionMessage.AUTH_RESULT_CODE_GENERIC_ERROR2);
		return new ResponseEntity<String>(exceptionMsg.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }

	    //capture response time statistics
	    jmxStatistics.addSummaryStatistics1(System.currentTimeMillis() - timestamp);
	    
	//} finally {
	    /*
	     * JMX call replaced in interceptor
	     * 
	     */
	    
	    //decrement JMX pending requests
	    //jmxStatistics.decrementCurrentRequests();
	//}

	//increment JMX successful requests
	jmxStatistics.incrementSuccess();

	//return successfully, 201
	return new ResponseEntity<String>("", HttpStatus.CREATED);
    }

    @RequestMapping(value = "/demo/endpoint2", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<? extends Serializable> endPoint2(HttpServletRequest request)
	throws AuthenticationException {
	logger.info("Controller called on end point 2.");
	
	/*
	 * JMX calls replaced in interceptor
	 * 
	 */
	
	//increment JMX request counter
	//jmxStatistics.incrementRequests();

	//increment JMX pending requests
	//jmxStatistics.incrementCurrentRequests();
	
	//try {
	    
	    //begin processing
	    long timestamp = System.currentTimeMillis();
	    try {
		//sleep for a random time 
		Thread.sleep(rand.nextInt(500) * 10);
	    } catch (Throwable e) {
		//capture response time statistics
		jmxStatistics.addSummaryStatistics2(System.currentTimeMillis() - timestamp);
		//increment JMX failed requests
		jmxStatistics.incrementFailures();

		String errMsg = "Error processing request";
		logger.error(errMsg, e);
		ExceptionMessage exceptionMsg = new ExceptionMessage("Internal server exception.");
		exceptionMsg.setCommand((String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
		exceptionMsg.setResult(ExceptionMessage.AUTH_RESULT_CODE_GENERIC_ERROR2);
		return new ResponseEntity<String>(exceptionMsg.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }

	    //capture response time statistics
	    jmxStatistics.addSummaryStatistics2(System.currentTimeMillis() - timestamp);
	    
	//} finally {
	    /*
	     * JMX call replaced in interceptor
	     * 
	     */
	    
	    //decrement JMX pending requests
	    //jmxStatistics.decrementCurrentRequests();
	//}

	//increment JMX successful requests
	jmxStatistics.incrementSuccess();

	//return successfully, 201
	return new ResponseEntity<String>("", HttpStatus.CREATED);
    }

}
