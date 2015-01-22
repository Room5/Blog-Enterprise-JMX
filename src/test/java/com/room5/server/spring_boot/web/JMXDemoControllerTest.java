package com.room5.server.spring_boot.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.room5.server.spring_boot.JMXServiceServer;
import com.room5.server.spring_boot.jmx.JMXServerStatistics;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = JMXServiceServer.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
@DirtiesContext
public class JMXDemoControllerTest
{

    @Value("${local.server.port}")
    private int port;

    private static final int REQUESTS = 10;
    
    @Autowired
    JMXServerStatistics jmxServerStatistics;
    
    @Test
    public void testEndPoint1() throws Exception {
	HttpHeaders headers = new HttpHeaders();
	HttpEntity<String> request = new HttpEntity<String>("", headers);

	TestRestTemplate template = new TestRestTemplate();
	ResponseEntity<String> entity;
	for (int counter = 0 ; counter < REQUESTS ; counter++) {
	    entity = template.postForEntity("http://localhost:" + this.port + "/demo/endpoint1", request, String.class);
	    
	    assertEquals("Create failed: " + entity.getBody(), HttpStatus.CREATED, entity.getStatusCode());
	    
	    System.out.println("Response body: " + entity.getBody());
	    
	    headers = entity.getHeaders();
	    for (String header : headers.keySet()) {
		if (header.equals("AuthenticationExceptionMessage")) {
		    System.err.println("Header name: " + header + ", content = " + headers.getFirst(header));
		} else {
		    System.out.println("Header name: " + header + ", content = " + headers.getFirst(header));
		}
	    }
	}
	
	System.err.println("Current Requests: " + jmxServerStatistics.getCurrentRequests());
	System.err.println("Total Requests: " + jmxServerStatistics.getRequests());
	System.err.println("Successful Requests: " + jmxServerStatistics.getSuccess());

	//get statistics
	assertEquals("invalid request count", REQUESTS, jmxServerStatistics.getRequests());
	assertEquals("invalid failure count", 0, jmxServerStatistics.getRejections());
	assertEquals("invalid current count", 0, jmxServerStatistics.getCurrentRequests());
	
	//handle interval rollover
	int requests = 0;
	System.err.println("Endpoint 1 statistics (" + jmxServerStatistics.getStatistics().length + "): ");
	for (int counter = 0 ; counter < jmxServerStatistics.getStatistics().length ; counter++) {
	    requests += jmxServerStatistics.getStatistics()[counter].getSummaryStatistics_1_Count();
	    System.err.println("\tInterval [" + counter + "] statistics: \n\t\tCount: " + jmxServerStatistics.getStatistics()[counter].getSummaryStatistics_1_Count() 
		    + "\n\t\tAverage: " + jmxServerStatistics.getStatistics()[counter].getSummaryStatistics_1_Average()  
		    + "\n\t\tSTD: " + jmxServerStatistics.getStatistics()[counter].getSummaryStatistics_1_STD());
	    
	}

	assertEquals("invalid statistical request count", REQUESTS, requests);
	
	entity = template.getForEntity("http://localhost:9191/jolokia/list/com.room5.jmx", String.class);
	System.err.println("Custom MBean List: " + entity.getBody());

	template = new TestRestTemplate();
	entity = template.getForEntity("http://localhost:9191/jolokia/read/com.room5.jmx:name=JMXServerStatistics/Uptime", String.class);
	System.err.println("Uptime: " + entity.getBody());

	template = new TestRestTemplate();
	entity = template.getForEntity("http://localhost:9191/jolokia/read/com.room5.jmx:name=JMXServerStatistics/Requests", String.class);
	System.err.println("Requests: " + entity.getBody());
	assertTrue("jmx requests not matching", entity.getBody().contains("\"value\":" + REQUESTS + ",") );
	
	template = new TestRestTemplate();
	entity = template.getForEntity("http://localhost:9191/jolokia/read/com.room5.jmx:name=JMXServerStatistics/Statistics", String.class);
	System.err.println("Statistics: " + entity.getBody());
    }

    @Test
    public void testEndPoint2() throws Exception {
	//TODO
    }

}
