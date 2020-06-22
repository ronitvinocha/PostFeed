package com.example.postfeed;


public interface RestAPIResponse {
    public enum CallApiResponse {
        SUCCESS,
        TIMEOUT,
        ERROR
    }
    public void postRestAPICall(String response, int callerid, CallApiResponse callApiResponse);
}