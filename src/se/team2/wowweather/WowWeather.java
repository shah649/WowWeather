/**
 * WowWeather application downloads and shows the latest
 * weather report for any location of your choice.
 *
 * @author Team 2 of Applied Software Project Management, 2016.
 */
package se.team2.wowweather;

import java.util.ArrayList;
import static se.team2.wowweather.SplashScreen.readSettings;
import se.team2.wowweather.classes.Constants;
import se.team2.wowweather.classes.UserInterface;

/**
 * Main class for WowWeather GUI.
 */
public class WowWeather {

    /**
     * Application's entry point function.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // checks if the user hasn't selected that he does not want
        // to see Splash Screen again
        Boolean hide_splash = 
            (SplashScreen.readSettingsField("splash_hide").equals("true"));
        
        if (!hide_splash) {
            // opens SplashScreen before the main window
            new SplashScreen();
        } else {
            // opens the main GUI for WowWeather application
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new UserInterface().setVisible(true);
                }
            });
        }
    }    
}
