package se.team2.wowweather.classes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *  Contains static hashing functions for WowWeather application.
 */
public class Hashing {
    
    /**
     *  Returns MD5 hash for the given text string.
     */
    public static String getMd5(String text) {
        
        MessageDigest md;
        
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException exception){
            // in case of unexpected error returns empty string
            System.out.println(exception.getStackTrace());
            return "";
        }
        
        md.update(text.getBytes());

        byte byte_data[] = md.digest();

        // converts bytes to hex format
        StringBuffer hexString = new StringBuffer();
        
    	for (int i = 0; i < byte_data.length; i++) {
            String hex = Integer.toHexString(0xff & byte_data[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
    	}
        
        return hexString.toString();
    }
}
