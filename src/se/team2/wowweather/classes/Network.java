package se.team2.wowweather.classes;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import se.team2.wowweather.interfaces.NetworkRequestInterface;

/**
 *  Network functions for WowWeather application.  
 */
public class Network {
    
    /**
     *  HTTP GET request.
     */
    public static void get(final NetworkRequestInterface source,
        String address, final int task_id) {

        // address is added before the given URI
        address = Constants.BASE_ADDRESS + address;
        
        // weather API app id is added to the URL
        address = appendAppId(address);
        
        // if cached content already found in file storage,
        // returns cached content without making any request
        JSONObject cached_response = getCachedContent(source, address);
        if (cached_response != null) {
            source.onRequestCompleted(task_id, cached_response);
            return;
        }
        
        System.out.println("Network: requested address: " + address);

        HttpURLConnection connection = null;

        try {
            
          // creates new connection
          URL url = new URL(address);
          connection = (HttpURLConnection) url.openConnection();
          connection.setRequestMethod("GET");
          connection.setRequestProperty("Content-Type", 
              "application/x-www-form-urlencoded");
          connection.setRequestProperty("Content-Language", "en-US");  
          connection.setUseCaches(false);
          connection.setDoOutput(true);

          // sends request
          DataOutputStream wr = new DataOutputStream (
              connection.getOutputStream());
          wr.close();

          // gets response  
          InputStream is = connection.getInputStream();
          BufferedReader rd = new BufferedReader(new InputStreamReader(is));
          StringBuilder response = new StringBuilder();
          String line;
          
          while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
          }
          rd.close();
          
          // caches received response in local file storage
          String filename = Hashing.getMd5(address) +
              Constants.CACHE_FILE_EXT;
          FileStorage.writeToFile(response.toString(), filename,
              Constants.PATH_NETWORK_RESPONSES);
          
          // converts string response to json for easier parsing
          JSONObject obj = new JSONObject(response.toString());

          // sends received response back to the caller class
          source.onRequestCompleted(task_id, obj);
          
        } catch (Exception e) {
            
            // something went wrong...
            // informs caller class
            Boolean err_returned = false;
            try {
//                System.out.println("HTTP code: " + connection.getResponseCode());
                if (connection.getResponseCode() == 405) {
                    err_returned = true;
                    source.onRequestFailed(task_id,  "Request failed - searching too soon. Please, wait a moment and try again.");
                }
            } catch (IOException exc) {/**/}
            
            if (!err_returned) {
                source.onRequestFailed(task_id, "Error while downloading the latest weather report.");
            }
          
        } finally {
          if (connection != null) {
            connection.disconnect();
          }
        }
    }
    
    /**
     *  Returns the given URL with OpenWeather APP ID added at the end.
     */
    private static String appendAppId(String url) {
        
        // checks if "?" symbols is found in the string
        if (url.indexOf("?") == -1) { // not found
            return url + "?APPID=" + Constants.APP_ID;
        } else { // is found
            return url + "&APPID=" + Constants.APP_ID;
        }
    }
    
    /**
     *  Returns cached content for the given address or NULL.
     */
    private static JSONObject getCachedContent(
        final NetworkRequestInterface source,
        String address) {
        
        // creates filename to search for based on the given address
        String filename = Hashing.getMd5(address) +
                          Constants.CACHE_FILE_EXT; 
        
        if (FileStorage.fileExists(filename, Constants.PATH_NETWORK_RESPONSES)) {
            
            System.out.println("Network: cached file \"" + filename + "\" found.");
            String content = FileStorage.readFile(filename,
                Constants.PATH_NETWORK_RESPONSES);
            
            // constructs JSON object from the content in the file
            JSONObject obj;
            try {
                obj = new JSONObject(content);
                return obj;
            } catch (JSONException e) {
                System.out.println("Network: " + e.getMessage());
            }
            
        } else {
            System.out.println("Network: cached file \"" + filename + "\" NOT found.");
        }

        return null;
    }
}
