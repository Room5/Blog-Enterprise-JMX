package com.room5.server.spring_boot;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.room5.server.spring_boot.jmx.JMXServerStatistics;
import com.room5.server.spring_boot.jmx.RollingStatistics;

public class AbstractServer implements DisposableBean
{
    
    @Autowired
    private JMXServerStatistics jmxStatistics;

    @Override
    public void destroy() throws Exception {
	//dump the rolling metric array to the logs
	for (RollingStatistics stats : jmxStatistics.getStatistics() ) {
	    jmxStatistics.logStatistics(stats);
	}
	
	// TODO add shutdown processing here
	//System.err.println("*********************** Shutting down *****************");
    }

}
