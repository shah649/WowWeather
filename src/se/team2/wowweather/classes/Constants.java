package se.team2.wowweather.classes;

/**
 *  Contains constants that are used in WowWeather application.
 */
public class Constants {
    
    public static final String TITLE_SPLASH_SCREEN = "Applied Software Project Management - Team 2 - 2016/2017";
    public static final String TITLE_MAIN_SCREEN = "WowWeather - Latest weather reports - Team 2";
    public static final String TITLE_ABOUT_SCREEN = "About WowWeather";
    
    // OpenWeather API specific constants
    public static final String APP_ID = "f8d9c9eb88b38963faf5a1875a147a86";
    
    // URLs
    // http://api.openweathermap.org/data/2.5/forecast/city?id=2701713&APPID=f8d9c9eb88b38963faf5a1875a147a86
    public static final String BASE_ADDRESS = "http://api.openweathermap.org/data/2.5";
    
    // relative paths
    public static final String PATH_STORAGE = "storage\\";
    public static final String PATH_NETWORK_RESPONSES = "storage\\cached_network_responses\\";
    public static final String PATH_LOCATION_FILE = "storage\\city.list.json";
    public static final String PATH_SEARCH_HISTORY = "storage\\search.history.json";
    public static final String PATH_FAVOURITE = "storage\\favourite.json";
    public static final String PATH_SETTINGS = "storage\\settings.json";
    
    // other
    public static final String CACHE_FILE_EXT = ".json";
    
    public static final String SPLASH_HEADER_TITLE = "Welcome to WowWeather application!";
    public static final String SPLASH_WELCOME_TEXT = "<html>WowWeather software is a free Windows Desktop application " +
        "that shows<br>the latest weather report for any location in the world.<br><br>" +
        "Features include:<br>" +
        "&nbsp;&nbsp;- location search<br>" +
        "&nbsp;&nbsp;- 5-day weather report<br>" +
        "&nbsp;&nbsp;- search history<br>" +
        "&nbsp;&nbsp;- location favorites<br>" +
        "&nbsp;&nbsp;- personalized settings";
}
