package com.room5.server.spring_boot.jmx;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.support.MetricType;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.room5.utils.IntervalClock;

/*
 * Required annotation to access MBeans during JUnit run
 */
@EnableMBeanExport(registration = RegistrationPolicy.REPLACE_EXISTING)

@Component
@ManagedResource(objectName = "com.room5.jmx:name=JMXServerStatistics", description = "JMX Service Statistics.")
public class JMXServerStatistics
{

    private final Logger logger = LoggerFactory.getLogger(JMXServerStatistics.class);

    private long startTime = System.currentTimeMillis();
    private AtomicLong totalRequests = new AtomicLong(0);
    private AtomicLong successfulRequests = new AtomicLong(0);
    private AtomicLong failedRequests = new AtomicLong(0);
    private AtomicLong rejectedRequests = new AtomicLong(0);
    private AtomicInteger currentRequests = new AtomicInteger(0);

    private AtomicLong transfersInProgress = new AtomicLong(0);
    private AtomicLong filesTransferred = new AtomicLong(0);
    private AtomicLong fileTransferErrors = new AtomicLong(0);
    private AtomicLong filesNotFound = new AtomicLong(0);

    @Value("${application.version}")
    private String buildVersion;
    @Value("${statistics.interval.count:6}")
    private int statCount;
    @Value("${statistics.interval.duration.minutes:10}")
    private int statIntervalDuration;
    @Value("${statistics.log.marker:STATISTICS}")
    private String loggingMarker;
    
    private CircularFifoQueue<RollingStatistics> rollingStats;
    private IntervalClock intervalClock;
    private ObjectMapper mapper = new ObjectMapper();

    private Object semaphore = new Object();

    @PostConstruct
    private void init() {
	//initialize stats rolling queue
	rollingStats = new CircularFifoQueue<RollingStatistics>(statCount);
	
	//convert minute intervals to seconds
	intervalClock = new IntervalClock(statIntervalDuration * 60);
    }
    
    /**
     * Current uptime:  http://localhost:9191/jolokia/read/com.room5.jmx:name=JMXServerStatistics/Uptime
     */
    @ManagedMetric(description = "Current system uptime.", displayName = "Current system uptime.", metricType = MetricType.COUNTER, currencyTimeLimit=10)
    public String getUptime() {
	long upTime = System.currentTimeMillis() - startTime;
	
	long days = TimeUnit.MILLISECONDS.toDays(upTime);
	long hoursRaw = TimeUnit.MILLISECONDS.toHours(upTime);
	long minutesRaw = TimeUnit.MILLISECONDS.toMinutes(upTime);
	
	//convert to string
	return String.format("%d days, %d hrs, %d min, %d sec",
	    days,
	    hoursRaw - TimeUnit.DAYS.toHours(days),
	    minutesRaw - TimeUnit.HOURS.toMinutes(hoursRaw),
	    TimeUnit.MILLISECONDS.toSeconds(upTime) - TimeUnit.MINUTES.toSeconds(minutesRaw)
	);
    }

    @ManagedMetric(description = "Server version information.", displayName = "Server Version.")
    public String getServerVersion() {
	return buildVersion;
    }
    
    @ManagedMetric(description = "Current interval start time.", displayName = "Current interval start time.", metricType = MetricType.COUNTER, currencyTimeLimit=120)
    public String getIntervalStart() {
	long start = intervalClock.getIntervalStartMillis(intervalClock.getCurrentInterval());
	
	//format 
	SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss z");
	df.setTimeZone(TimeZone.getTimeZone("UTC"));

	return df.format(new Date(start));
    }

    @ManagedMetric(description = "Current interval duration setting.", displayName = "Current interval duration setting.", unit="minutes")
    public int getIntervalDuration() {
	return statIntervalDuration;
    }

    @ManagedMetric(description = "Count of submitted requests.", displayName = "Count of submitted requests.", metricType = MetricType.COUNTER)
    public long getRequests() {
        return totalRequests.longValue();
    }

    public void incrementRequests() {
        totalRequests.incrementAndGet();
        
        //update rolling statistics
        getCurrentRollingStatistic().incrementRequest();
    }

    @ManagedMetric(description = "Count of successful requests.", displayName = "Count of successful requests.", metricType = MetricType.COUNTER)
    public long getSuccess() {
        return successfulRequests.longValue();
    }

    public void incrementSuccess() {
        successfulRequests.incrementAndGet();
    
        //update rolling statistics
        getCurrentRollingStatistic().incrementSuccess();
    }

    @ManagedMetric(description = "Count of failed requests.", displayName = "Count of failed requests.", metricType = MetricType.COUNTER)
    public long getFailures() {
        return failedRequests.longValue();
    }

    public void incrementFailures() {
        failedRequests.incrementAndGet();
    }

    @ManagedMetric(description = "Count of requests rejected for invalid license status.", displayName = "Count of requests rejected for invalid license status.", metricType = MetricType.COUNTER)
    public long getRejections() {
        return rejectedRequests.longValue();
    }

    public void incrementRejections() {
        rejectedRequests.incrementAndGet();

        //update rolling statistics
        getCurrentRollingStatistic().incrementRejection();
    }

    @ManagedMetric(description = "Count of currently processing requests.", displayName = "Count of currently processing requests.", metricType = MetricType.GAUGE)
    public long getCurrentRequests() {
        return currentRequests.longValue();
    }

    public void incrementCurrentRequests() {
        currentRequests.incrementAndGet();
    }

    public void decrementCurrentRequests() {
        currentRequests.decrementAndGet();
    }
    
    public void addSummaryStatistics1(long duration) {
	getCurrentRollingStatistic().addSummaryStatistics1(duration);
    }

    public void addSummaryStatistics2(long duration) {
	getCurrentRollingStatistic().addSummaryStatistics2(duration);
    }
        
    @ManagedMetric(description = "Count of current file transfers.", displayName = "Count of current file transfers.", metricType = MetricType.GAUGE)
    public long getTransfersInProgress() {
        return transfersInProgress.longValue();
    }

    public void incrementTransfersInProgress() {
        transfersInProgress.incrementAndGet();
    }

    public void decrementTransfersInProgress() {
        transfersInProgress.decrementAndGet();
    }

    @ManagedMetric(description = "Count of number of file transferred.", displayName = "Count of number of file transferred.", metricType = MetricType.COUNTER)
    public long getFilesTransferred() {
        return filesTransferred.longValue();
    }

    public void incrementFilesTransferred() {
        filesTransferred.incrementAndGet();
        
        //update rolling stats
        getCurrentRollingStatistic().incrementFilesTransferred();
    }

    @ManagedMetric(description = "Count of number of file transfer errors.", displayName = "Count of number of file transfer errors.", metricType = MetricType.COUNTER)
    public long getFileTransferErrors() {
        return fileTransferErrors.longValue();
    }

    public void incrementFileTransferErrors() {
        fileTransferErrors.incrementAndGet();

        //update rolling stats
        getCurrentRollingStatistic().incrementFileTransferErrors();
    }

    @ManagedMetric(description = "Count of requests for unknown files.", displayName = "Count of requests for unknown files.", metricType = MetricType.COUNTER)
    public long getFilesNotFound() {
        return filesNotFound.longValue();
    }

    public void incrementFilesNotFound() {
        filesNotFound.incrementAndGet();
    }

    public void addFileBytesTransfered(long size) {
	getCurrentRollingStatistic().addFileTransferBytes(size);
    }
    
    @ManagedMetric(description = "Update Manager Statistics.", displayName = "Update Manager Statistics.", category="performance")
    public RollingStatistics[] getStatistics() {
	return rollingStats.toArray(new RollingStatistics[rollingStats.size()]);
    }
    
    public void logStatistics(RollingStatistics stat) {
	if ((loggingMarker != null) && (loggingMarker.length()>0)) {
	    //log only if specified
	    try {
		logger.warn(loggingMarker + ": " + mapper.writeValueAsString(stat));
	    } catch (JsonProcessingException e) {
		logger.error("Unable to generate JSON response.", e);
	    }
	}
    }

    private RollingStatistics getCurrentRollingStatistic() {
	//determine current interval
	long currentInterval = intervalClock.getCurrentInterval();
	
	//get most recent from queue tail
	RollingStatistics currentStats = null;
	if (!rollingStats.isEmpty()) {
	    currentStats = rollingStats.get(rollingStats.size() - 1);
	}

	//validate
	if ( (currentStats == null) 
		//if not matching current interval 
		|| (currentStats.getCurrentInterval() != currentInterval) 
		){

	    //sync block on creation only
	    synchronized (semaphore) {
		//test again
		if (!rollingStats.isEmpty()) {
		    currentStats = rollingStats.get(rollingStats.size() - 1);
		}

		//make sure it wasn't created while awaiting block
		if ( (currentStats == null) 
			//if not matching current interval 
			|| (currentStats.getCurrentInterval() != currentInterval) 
			){

		    //create new entry
		    currentStats = new RollingStatistics( intervalClock.getCurrentInterval(), intervalClock);
		    
		    //log if rollover will occur
		    if (rollingStats.isFull()) {
			logStatistics(rollingStats.peek());
		    }
		
		    //add to queue
		    rollingStats.add(currentStats);
		}
	    }
	    logger.debug("Adding new rollings statistics entry for interval " + currentStats.getCurrentInterval());
	}
	
	return currentStats; 
    }
}
