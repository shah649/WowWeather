package se.team2.wowweather.interfaces;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *  Interface for Network Requests in WowWeather application.
 */
public interface NetworkRequestInterface {    
    
    /**
     *  Called when the response from the server is received.
     */
    public void onRequestCompleted(int task_id, JSONObject obj);
    
    /**
     *  Called when the request for the server fails.
     */
    public void onRequestFailed(int task_id, String error);
}
