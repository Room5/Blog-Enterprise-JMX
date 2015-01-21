package com.room5.server.spring_boot.jmx;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.support.MetricType;

import com.room5.utils.IntervalClock;

@ManagedResource(objectName = "com.room5.jmx:name=RollingStatistics", description = "JMX Service Rolling Statistics.")
public class RollingStatistics
{

    private AtomicLong requests = new AtomicLong(0);
    private AtomicLong success = new AtomicLong(0);
    private AtomicLong rejections = new AtomicLong(0);
    private AtomicLong filesTransferred = new AtomicLong(0);
    private AtomicLong fileTransferErrors = new AtomicLong(0);
    private DoubleAdder bytesTransferred = new DoubleAdder();

    private long interval;
    private IntervalClock clock;
    
    SummaryStatistics summaryStatistics1 = new SummaryStatistics();
    SummaryStatistics summaryStatistics2 = new SummaryStatistics();
    
    //private final Logger logger = LoggerFactory.getLogger(RollingStatistics.class);


    /*pkg*/ RollingStatistics(long interval, IntervalClock clock) {
	this.interval = interval;
	this.clock = clock;
    }
    
    /**
     * Current interval
     */
    @ManagedMetric(description = "Interval start time.", displayName = "Interval start time.", metricType = MetricType.COUNTER)
    public String getIntervalStart() {
	long start = clock.getIntervalStartMillis(interval);
	
	//format 
	SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss z");
	df.setTimeZone(TimeZone.getTimeZone("UTC"));

	return df.format(new Date(start));
    }
    
    @ManagedMetric(description = "Count of the number of requests.", displayName = "Count of the number of requests.", metricType = MetricType.COUNTER)
    public long getSubmittedRequests() {
	return rejections.longValue();
    }

    @ManagedMetric(description = "Count of the number of succesfully processed requests.", displayName = "Count of the number of succesfully processed requests.", 
	    metricType = MetricType.COUNTER)
    public long getSuccessfulRequests() {
	return success.longValue();
    }
    
    @ManagedMetric(description = "Count of the number of rejected requests.", displayName = "Count of the number of rejected requests.", metricType = MetricType.COUNTER)
    public long getRejectedRequests() {
	return rejections.longValue();
    }

    @ManagedMetric(description = "Number of entries for Summary Statistics 1 call.", displayName = "Number of entries for Summary Statistics 1  call.", 
	    	metricType = MetricType.COUNTER)
    public double getSummaryStatistics_1_Count() {
	return summaryStatistics1.getN();
    }

    @ManagedMetric(description = "Average duration for Summary Statistics 1 call.", displayName = "Average duration for Summary Statistics 1  call.", 
    	metricType = MetricType.GAUGE)
    public double getSummaryStatistics_1_Average() {
	return summaryStatistics1.getMean();
    }
	
    @ManagedMetric(description = "Standard deviation of duration for Summary Statistics 1 call.", displayName = "Standard deviation of duration for Summary Statistics 1 call.", 
    	metricType = MetricType.GAUGE)
    public double getSummaryStatistics_1_STD() {
	return summaryStatistics1.getStandardDeviation();
    }
	
    @ManagedMetric(description = "Number of entries for Summary Statistics 2 call.", displayName = "Number of entries for Summary Statistics 2  call.", 
	    	metricType = MetricType.COUNTER)
    public double getSummaryStatistics_2_Count() {
	return summaryStatistics2.getN();
    }
    
    @ManagedMetric(description = "Average duration for Summary Statistics 2 call.", displayName = "Average duration for Summary Statistics 2 call.", metricType = MetricType.GAUGE)
    public double getSummaryStatistics_2_Average() {
	return summaryStatistics2.getMean();
    }
	
    @ManagedMetric(description = "Standard deviation of duration for Summary Statistics 2 call.", displayName = "Standard deviation of duration for Summary Statistics 2 call.",
    	metricType = MetricType.GAUGE)
    public double getSummaryStatistics_2_STD() {
	return summaryStatistics2.getStandardDeviation();
    }

    public long getCurrentInterval() {
	return interval;
    }
    
    /*pkg*/ void incrementRequest() {
	requests.incrementAndGet();
    }

    /*pkg*/ void incrementSuccess() {
	success.incrementAndGet();
    }

    /*pkg*/ void incrementRejection() {
	rejections.incrementAndGet();
    }

    /*pkg*/ void addSummaryStatistics1(long duration) {
	summaryStatistics1.addValue(duration);
    }

    /*pkg*/ void addSummaryStatistics2(long duration) {
	summaryStatistics2.addValue(duration);
    }

    @ManagedMetric(description = "Count of number of file transferred.", displayName = "Count of number of file transferred.", metricType = MetricType.COUNTER)
    public long getFilesTransferred() {
        return filesTransferred.longValue();
    }

    /*pkg*/ void incrementFilesTransferred() {
        filesTransferred.incrementAndGet();
    }

    @ManagedMetric(description = "Count of number of file transfer errors.", displayName = "Count of number of file transfer errors.", metricType = MetricType.COUNTER)
    public long getFileTransferErrors() {
        return fileTransferErrors.longValue();
    }

    /*pkg*/ void incrementFileTransferErrors() {
        fileTransferErrors.incrementAndGet();
    }

    /*pkg*/ void addFileTransferBytes(long bytes) {
	bytesTransferred.add(bytes);
    }

    @ManagedMetric(description = "Total bytes transferred.", displayName = "Total bytes transferred.", metricType = MetricType.COUNTER)
    public double getBytesTransferred() {
	return bytesTransferred.sum();
    }
	
}
