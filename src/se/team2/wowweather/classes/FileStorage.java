package se.team2.wowweather.classes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 *  Files' manipulation class for WowWeather application.
 */
public class FileStorage {
    
    /**
     *  Combines filename with the given path and returns absolute path.
     */
    public static String getFullPath(String filename, String path) {
        File f = new File(path + filename);
        return f.getAbsolutePath();
    }
    
    /**
     *  Returns TRUE if the given file is found, otherwise FALSE.
     */
    public static Boolean fileExists(String filename, String path) {
        
        File f = new File(path + filename);
        
        if (f.exists() && !f.isDirectory()) {
            
            // if looking into cache dir, checks file age
            if (path.equals(Constants.PATH_NETWORK_RESPONSES)) {
                System.out.println("FileStorage: checking cache file \"" + filename + "\" age...");
                
                long diff = new Date().getTime() - f.lastModified();

                // if file older than 3 hours, ignores it
                if (diff > 180 * 60 * 1000) {
                    System.out.println("FileStorage: file not recent, ignoring it.");
                    return false;
                } else {
                    System.out.println("FileStorage: file is recent, reusing it.");
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     *  Reads the given file and returns its content as String.
     */
    public static String readFile(String filename) {
        return readFile(filename, Constants.PATH_STORAGE);
    }
    public static String readFile(String filename, String path) {
        
        String file_content;
        String full_path = getFullPath(filename, path);
        
        try {
            
            BufferedReader br = new BufferedReader(new FileReader(full_path));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            
            file_content = sb.toString();
            
            System.out.println("FileStorage: returning content of \"" + full_path + "\"");
            
        } catch (Exception e) {
            file_content = "";
            System.out.println("FileStorage: " + e.getMessage());
        }
        
        return file_content;
    }
    
    /**
     *  Writes String content to the given file, overwriting it.
     */
    public static void writeToFile(String content, String filename) {
        writeToFile(content, filename, Constants.PATH_STORAGE);
    }
    public static void writeToFile(String content, String filename, String path) {

        BufferedWriter writer = null;
        
        try {            
            writer = new BufferedWriter(new FileWriter(path + filename));
            writer.write(content);
            System.out.println("FileStorage: content saved in \"" + filename + "\".");
            
        } catch (Exception e) {
            System.out.println("FileStorage: " + e.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                    writer = null;
                }
            } catch (IOException e) {
                System.out.println("FileStorage: " + e.getMessage());
            }
        }
    }
}
