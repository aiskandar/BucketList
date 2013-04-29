package com.kiddobloom.bucketlist;


public class StateMachine {

    public static final int INIT_STATE = 0;
    public static final int SKIPPED_STATE = 1;
    public static final int OPENED_STATE = 2;
    public static final int CLOSED_STATE = 3;
    
    public static final String INIT_STATE_STR = "INIT";
	public static final String SKIPPED_STATE_STR = "SKIPPED";
    public static final String OPENED_STATE_STR = "OPENED";
    public static final String CLOSED_STATE_STR = "CLOSED";

    public static final String stateStr[] = {INIT_STATE_STR, SKIPPED_STATE_STR, OPENED_STATE_STR, CLOSED_STATE_STR};

    public static final int LOGIN_SUCCESS_EVENT = 0;
    public static final int LOGOUT_SUCCESS_EVENT = 1;    
    public static final int SKIP_EVENT = 2;
    
    public static final String LOGIN_SUCCESS_EVENT_STR = "LOGIN SUCCESS EVENT";
    public static final String LOGOUT_SUCCESS_EVENT_STR = "LOGOUT SUCCESS EVENT";    
    public static final String SKIP_EVENT_STR = "SKIP EVENT";
    
    public static final String eventStr[] = {LOGIN_SUCCESS_EVENT_STR, LOGOUT_SUCCESS_EVENT_STR, SKIP_EVENT_STR};

    public static final int NONE_ERROR = 0;
    public static final int FB_GET_ME_FAILED_ERROR = 1;
    public static final int FB_GET_FRIENDS_FAILED_ERROR = 2;
    public static final int SERVER_REGISTER_ERROR = 3;
    public static final int SERVER_SYNC_ERROR = 4;

    public static final String NONE_ERROR_STR = "NO ERROR";
    public static final String FB_GET_ME_FAILED_ERROR_STR = "FACEBOOK GET ME FAILED";
    public static final String FB_GET_FRIENDS_FAILED_ERROR_STR = "FACEBOOK GET FRIENDS FAILED";
    public static final String SERVER_REGISTER_ERROR_STR = "SERVER REGISTER ERROR";
    public static final String SERVER_SYNC_ERROR_STR = "SERVER SYNC ERROR";

    public static final String errorStr[] = {NONE_ERROR_STR, FB_GET_ME_FAILED_ERROR_STR, FB_GET_FRIENDS_FAILED_ERROR_STR, SERVER_REGISTER_ERROR_STR, SERVER_SYNC_ERROR_STR}; 
}
