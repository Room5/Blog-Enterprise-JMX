package com.room5.canonical;


public class ExceptionMessage extends Exception {
    
    public static final int AUTH_RESULT_CODE_SUCCESS = 0;

    public static final int AUTH_RESULT_CODE_JSON_PARSE_ERROR = 1500;
    public static final int AUTH_RESULT_CODE_GENERIC_ERROR2 = 2000;
    public static final int AUTH_RESULT_CODE_GENERIC_ERROR3 = 3000;
    public static final int AUTH_RESULT_CODE_GENERIC_ERROR4 = 4000;


    private static final long serialVersionUID = -5227398972519430795L;

    

    private String command;
    private int result;
    private String reason;
    
    public ExceptionMessage(String reason) {
    	super(reason);
    	this.reason = reason;
    }   
    
    public void setCommand(String command){
    	this.command = command;
    }
    
    public String getCommand() {
    	return this.command;
    }
    
    public void setResult(int result) {
    	this.result = result;
    }
    
    public int getResult() {
    	return this.result;
    }
    
    public void setReason(String reason){
    	this.reason = reason;
    }
    
    public String getReason() {
    	return this.reason;
    }
    
    @Override
    public String toString() {
    	String Json = "{\"command\":\"" + command +"\",\"result\":" + result +",\"reason\":\"" + reason + "\"}";
    	return Json;
    }
}
