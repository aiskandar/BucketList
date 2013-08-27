package com.kiddobloom.bucketlist;


public class StateMachine {

	// states
    public static final int INIT_STATE = 0;
    public static final int SKIPPED_STATE = 1;
    public static final int FB_OPENED_STATE = 2;
    public static final int FB_CLOSED_STATE = 3;
    public static final int OFFLINE_STATE = 4;
    public static final int FB_GET_ME_STATE = 5;
    public static final int FBID_REGISTER_STATE = 6;
    public static final int FB_GET_FRIENDS_STATE = 7;
    public static final int ONLINE_STATE = 8;
    public static final int FBFRIENDS_CHECK_STATE = 9;
    		
    public static final String INIT_STATE_STR = "INIT_STATE";
	public static final String SKIPPED_STATE_STR = "SKIPPED_STATE";
    public static final String FB_OPENED_STATE_STR = "FB_OPENED_STATE";
    public static final String FB_CLOSED_STATE_STR = "FB_CLOSED_STATE";
    public static final String OFFLINE_STATE_STR = "OFFLINE_STATE";
    public static final String FB_GET_ME_STATE_STR = "FB_GET_ME_STATE";
    public static final String FBID_REGISTER_STATE_STR = "FBID_REGISTER_STATE";
    public static final String FB_GET_FRIENDS_STATE_STR = "FB_GET_FRIENDS_STATE";
    public static final String ONLINE_STATE_STR = "ONLINE_STATE";
    public static final String FBFRIENDS_CHECK_STATE_STR = "FBFRIENDS_CHECK_STATE";
    
    public static final String stateStr[] = {INIT_STATE_STR, SKIPPED_STATE_STR, FB_OPENED_STATE_STR, FB_CLOSED_STATE_STR, 
    	OFFLINE_STATE_STR, FB_GET_ME_STATE_STR, FBID_REGISTER_STATE_STR, FB_GET_FRIENDS_STATE_STR, ONLINE_STATE_STR, FBFRIENDS_CHECK_STATE_STR};

    // status
    public static final int OK_STATUS = 0;
    public static final int TRANSACTING_STATUS = 1;
    public static final int RETRY_STATUS = 2;
    public static final int ERROR_STATUS = 3;

    public static final String OK_STATUS_STR = "OK";
    public static final String TRANSACTING_STATUS_STR = "TRANSACTING";
    public static final String RETRY_STATUS_STR = "RETRY";
    public static final String ERROR_STATUS_STR = "ERROR";
    
    public static final String statusStr[] = {OK_STATUS_STR, TRANSACTING_STATUS_STR, RETRY_STATUS_STR, ERROR_STATUS_STR};
    
    // error cases
    public static final int NO_ERROR = 0;
    public static final int FB_GET_ME_FAILED_ERROR = 1;
    public static final int FB_GET_FRIENDS_FAILED_ERROR = 2;
    public static final int FBID_SERVER_REGISTER_ERROR = 3;
    public static final int SERVER_SYNC_ERROR = 4;
    public static final int NETWORK_DISCONNECT_ERROR = 5;
    public static final int FBFRIENDS_CHECK_ERROR = 6;

    public static final String NO_ERROR_STR = "NO ERROR";
    public static final String FB_GET_ME_FAILED_ERROR_STR = "FACEBOOK GET ME FAILED";
    public static final String FB_GET_FRIENDS_FAILED_ERROR_STR = "FACEBOOK GET FRIENDS FAILED";
    public static final String FBID_SERVER_REGISTER_ERROR_STR = "FBID SERVER REGISTER ERROR";
    public static final String SERVER_SYNC_ERROR_STR = "SERVER SYNC ERROR";
    public static final String NETWORK_DISCONNECT_ERROR_STR = "NETWORK DISCONNECT ERROR";
    public static final String FBFRIENDS_CHECK_ERROR_STR = "FBFRIENDS CHECK ERROR";

    public static final String errorStr[] = {NO_ERROR_STR, FB_GET_ME_FAILED_ERROR_STR, FB_GET_FRIENDS_FAILED_ERROR_STR, 
    					FBID_SERVER_REGISTER_ERROR_STR, SERVER_SYNC_ERROR_STR, NETWORK_DISCONNECT_ERROR_STR, FBFRIENDS_CHECK_ERROR_STR}; 

    // events
    public static final int LOGIN_SUCCESS_EVENT = 0;
    public static final int LOGOUT_SUCCESS_EVENT = 1;    
    public static final int SKIP_EVENT = 2;
    
    public static final String LOGIN_SUCCESS_EVENT_STR = "LOGIN SUCCESS EVENT";
    public static final String LOGOUT_SUCCESS_EVENT_STR = "LOGOUT SUCCESS EVENT";    
    public static final String SKIP_EVENT_STR = "SKIP EVENT";
    
    public static final String eventStr[] = {LOGIN_SUCCESS_EVENT_STR, LOGOUT_SUCCESS_EVENT_STR, SKIP_EVENT_STR};
}
