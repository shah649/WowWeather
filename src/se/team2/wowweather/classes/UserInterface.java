/**
 * UserInterface of WowWeather application.
 *
 * @author Team 2 of Applied Software Project Management, 2016.
 */
package se.team2.wowweather.classes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.team2.wowweather.AboutScreen;
import se.team2.wowweather.SplashScreen;
import se.team2.wowweather.interfaces.NetworkRequestInterface;

public class UserInterface extends javax.swing.JFrame
    implements NetworkRequestInterface {
    
    // IDs of different requests to recognize them
    // when the response is received
    private static final int TASK_DOWNLOAD_CITY_DATA = 1;
    
    // for how many days the data needs to be fetched
    private static final int MAX_SHOWN_DAYS = 5;
    
    private static final int SEARCH_HISTORY_SIZE = 5;    
    private static final int FAVOURITE_SIZE = 5;
    
    // panels
    JPanel pnlTopBar;
    JPanel pnlLeftBar;
    JPanel pnlRightSide;
    JPanel pnlInfoText;
    JPanel pnlContent;
    JPanel pnlSearchHistory;
    JPanel pnlFavourites;
    
    // top panel
    JTextField txtLocation;
    JLabel btnSearch;
    
    // info panel
    JLabel lblInfoText;
    
    // content panel
    JLabel lblContentText;
    JPanel pnlReportDays;
    JLabel lblLocationTitle;    
    JLabel imgFavorite;
    JPanel[] pnlsDays;
    
    // right side panel
    JLabel lblSearchHistoryTitle;
    JLabel lblSearchHistory;
    JLabel lblFavoritesTitle;
    JLabel lblFavorites;
    JLabel[] lblsHistory = null;
    JLabel[] lblsFavourites = null;
    
    // panel colors
    Color clrRightPanel = new Color(97, 128, 211);    
    Color clrTopPanel = new Color(21, 30, 54);
    Color clrBox = new Color(105, 136, 220);
    Color clrContentPanel = new Color(203, 215, 247);
    Color clrReportDaysRow = new Color(177, 192, 232);
    Color clrRightColTitle = new Color(66, 96, 175);
    Color clrDaysLeftLine = new Color(154, 172, 220);
 
    // size for different panels/other widgets
    int top_bar_height = 50;
    int info_text_height = 35;
    int right_side_width = 250;
    int left_bar_width = 50;
    int location_input_width = 250;
    int shist_location_height = 24;
    
    Boolean location_loaded = false;
    int temp_location_id = 0;
    String temp_location_title = "";
    Boolean temp_starred = false;
    
    String unit_type = "metric";

    /**
     * Creates new form UserInterface.
     */
    public UserInterface() {
        
        initComponents();
        
        // centers application in the middle of the screen
        this.setLocationRelativeTo(null);
        this.setSize(new java.awt.Dimension(1100, 500));
        this.setResizable(false);
        this.setTitle(Constants.TITLE_MAIN_SCREEN);
        
        initPanels();
        initLeftMenu();
        initContentPanel();
        
        // will close window when Escape key is pressed
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
        getRootPane().getActionMap().put("Cancel", new AbstractAction() { 
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        // if search history is not empty, automatically downloads and
        // displays data for the most recent search
        ArrayList<String> locations = getSearchHistory();
        if (!locations.isEmpty()) {
            String location = locations.get(locations.size() - 1);
            // uses input field to submit the same location
            txtLocation.setText(location);
            findLocation();
        } else {
            System.err.println("Err: search history is empty.");
        }
    }
    
    /**
     *  Called upon completion of Network request.
     * 
     *  @param task_id
     *  @param response
     */
    @Override
    public void onRequestCompleted(int task_id, JSONObject response) {
        
        setInfoText(""); // erases network status messages
        
        if (task_id == TASK_DOWNLOAD_CITY_DATA) {
            location_loaded = true;
            setResponseContent(response);
            txtLocation.setText(""); // erases location
            // need to allow searching for new locations again
            setSearchingEnabled(true);
        }
    }
    
    /**
     *  Called upon failure of Network request.
     * 
     *  @param task_id
     *  @param error_msg
     */
    @Override
    public void onRequestFailed(int task_id, String error_msg) {
        
        setSearchingEnabled(true);
        setInfoText(error_msg);
        txtLocation.selectAll();
        
        // if no location has been loaded and displayed on the screen, yet,
        // hides icons for favoriting
        refreshFavoriteStar();
    }
    
    /**
     *  Shows the given text in the info message field.
     */
    private void setInfoText(String text) {
        lblInfoText.setText(text);
    }
    
    /**
     *  Parses the given weather report object and
     *  displays it in the content panel.
     */
    private void setResponseContent(JSONObject response) {
        
        // removes all existing content from gridlayout cells
        pnlReportDays.removeAll();
        pnlReportDays.repaint();
        
        // inits the panel for each day
        if (pnlsDays == null) {
            pnlsDays = new JPanel[MAX_SHOWN_DAYS];
            int left; // first day should have left border, others - not
            for (int i = 0; i < MAX_SHOWN_DAYS; i++) {
                left = (i == 0) ? 5 : 0;
                pnlsDays[i] = new JPanel();
                pnlsDays[i].setLayout(new BoxLayout(
                    pnlsDays[i], BoxLayout.Y_AXIS));
//                pnlsDays[i].setBackground(clrReportDaysRow);
//                pnlsDays[i].setBorder(new CompoundBorder(
//                    BorderFactory.createMatteBorder(
//                    0, left, 0, 5, clrContentPanel),
//                    BorderFactory.createMatteBorder(
//                    1, 1, 1, 1, clrRightPanel)
//                ));
                pnlsDays[i].setBorder(
                    BorderFactory.createMatteBorder(
                    0, left, 0, 5, clrContentPanel)
                );
            }
        }
        
        // variables for daily data         
        JSONObject city;     
        int city_id;
        String city_name;
        String country_code;
        
        JSONArray lstAllDays;
        JSONObject single_day;
        JSONObject main;
        double temp_highest;
        double temp_lowest;
        String day_description;
        String icon_name;
        String date_txt;
        String curr_date_txt = "";
        int day_counter = 0;
        
        // parses JSON response for weather report data
        try {
            
            city = response.getJSONObject("city");
            city_id = city.getInt("id");
            city_name = city.getString("name");
            country_code = city.getString("country");            
            lstAllDays = response.getJSONArray("list");
            
            int hour_cnt = 1;
            
            for (int i = 0; i < lstAllDays.length(); i++) {
                
                single_day = lstAllDays.getJSONObject(i);
                
                // will continue if it's the same day we have seen already
                date_txt = single_day.getString("dt_txt").substring(0, 10);
                if (!date_txt.equals(curr_date_txt)) {
                    hour_cnt = 1;
                }
                if ((hour_cnt < 4 || hour_cnt > 4) &&
                        date_txt.equals(curr_date_txt)) {
                    hour_cnt++;
                    continue;
                }
                curr_date_txt = single_day.getString("dt_txt").substring(0, 10);
                
                // parses daily data
                main = single_day.getJSONObject("main");
                temp_lowest = main.getDouble("temp_min");
                temp_highest = main.getDouble("temp_max");
                day_description = single_day.getJSONArray("weather").
                    getJSONObject(0).getString("description");
                icon_name = single_day.getJSONArray("weather").
                    getJSONObject(0).getString("icon");
                
                // formats date so that we could get the name of day in English          
                LocalDateTime datetime = LocalDateTime.parse(
                    single_day.getString("dt_txt"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                Date date = Date.from(
                    datetime.atZone(ZoneId.systemDefault()).toInstant());                
                SimpleDateFormat sdf = new SimpleDateFormat(
                    "EEE, d", Locale.ENGLISH);

                // removes old content from the panel
                pnlsDays[day_counter].removeAll();
                pnlsDays[day_counter].repaint();
                
                // label for day title
                JLabel lblDayTitle = new JLabel();
                lblDayTitle.setFont(new java.awt.Font("Segoe UI", 0, 14));
                lblDayTitle.setBackground(clrBox);
                lblDayTitle.setForeground(Color.white);
                lblDayTitle.setOpaque(true);
                lblDayTitle.setText(sdf.format(date));
                lblDayTitle.setBorder(
                    BorderFactory.createMatteBorder(
                        5, 20, 5, 20, clrBox)
                );
                pnlsDays[day_counter].add(lblDayTitle);
                
                // weather image for the day
                try {                    
                    // downloads image for the day and adds to panel for the day
                    URL url = new URL("http://openweathermap.org/img/w/" +
                        icon_name + ".png");                    
                    BufferedImage image = ImageIO.read(url);
                    JLabel lblDayImage = new JLabel(new ImageIcon(image));
                    lblDayImage.setBorder(new EmptyBorder(0, 80, 0, 10));
                    pnlsDays[day_counter].add(lblDayImage);
                } catch (MalformedURLException e) {/**/
                } catch (IOException e) {/**/}
                
                // label for day temp
                JLabel lblTemperature = new JLabel();
                lblTemperature.setFont(new java.awt.Font("Segoe UI", 0, 12));
//                lblTemperature.setBackground(Color.red);
//                lblTemperature.setOpaque(true);
                lblTemperature.setText(
                    "<html><body><div style=\"margin-left:20px\">" +
                    "<span style=\"font-size:20px\">" +
                    Math.round(temp_highest) + "\u00b0</span> " +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "<span style=\"font-size:12px\">" +
                    Math.round(temp_lowest) + "\u00b0</span>" +
                    "</div></body></html>");
                pnlsDays[day_counter].add(lblTemperature);
                
                // weather textual description
                JLabel lblDayDescription = new JLabel();
                lblDayDescription.setFont(new java.awt.Font("Segoe UI", 0, 12));
                lblDayDescription.setForeground(Color.DARK_GRAY);
                lblDayDescription.setText(
                    capitalizeFirstLetter(day_description));
                lblDayDescription.setBorder(new EmptyBorder(7, 10, 13, 10));
                pnlsDays[day_counter].add(lblDayDescription);                                
                
                // adds day panel to gridlayout
                pnlReportDays.add(pnlsDays[day_counter]);
                
                // we need data only for 1 week
                if (++day_counter >= MAX_SHOWN_DAYS) break;
            }
            
        } catch (JSONException e) {
            setInfoText("Couldn't parse response. Response has different format than expected.");
            return;
        }
        
        lblLocationTitle.setText(city_name + ", " + country_code);
        
        // temporary saves found location
        temp_location_id = city_id;
        temp_location_title = city_name.trim().toLowerCase();

        // sets correct star icon for favoriting
        refreshFavoriteStar();
    }
    
    /**
     *  Saves the given location to search history.
     */
    private void saveToSearchHistory(String location) {
        
        ArrayList<String> locations = getSearchHistory();
        
        // removes existing duplicates for this location
        for (int i = 0; i < locations.size(); i++) {
            if (locations.get(i).equals(location)) {
                locations.remove(i--);
            }
        }
        
        // adds this location as the latest entry
        locations.add(location);
        
        // if now more items than needed, remove the oldest one
        if (locations.size() > SEARCH_HISTORY_SIZE) {
            locations.remove(0);
        }
        
        // search history is saved as json array string
        String output_obj = new JSONArray(locations).toString();
        
        try {
            try (PrintWriter writer = new PrintWriter(
                Constants.PATH_SEARCH_HISTORY, "UTF-8")) {
                writer.write(output_obj);
                writer.close();
            }
        } catch (IOException e) {
            setInfoText("Couldn't save location to search history.");
        }
    }    
    
    /**
     *  Returns search history from storage file.
     */
    private ArrayList<String> getSearchHistory() {
        
        ArrayList<String> locations = new ArrayList<>();
        
        File file;
        FileInputStream fis;
        byte[] data;        
        String str_history;
        JSONArray jsonarr_locations;
        
        file = new File(Constants.PATH_SEARCH_HISTORY);
        
        // if file does not exist, creates a new one
        if (!file.exists() || file.isDirectory()) {            
            try {
                PrintWriter writer = new PrintWriter(
                    Constants.PATH_SEARCH_HISTORY, "UTF-8");
                writer.close();
            } catch (IOException e) {/**/}
            
            file = new File(Constants.PATH_SEARCH_HISTORY);
            if (!file.exists() || file.isDirectory()) {
//                setInfoText("Error while reading search history.");
                return locations; // returns empty list
            }
        }

        // reads file content
        try {            
            fis = new FileInputStream(file);
            data = new byte[(int) file.length()];
            fis.read(data);
            str_history = new String(data);
            fis.close();

            // file content is json-formatted array list
            jsonarr_locations = new JSONArray(str_history);

            // reads only latest SEARCH_HISTORY_SIZE items
            int j = 0;
            for (int i = jsonarr_locations.length() - 1; i >= 0; i--) {
                locations.add(jsonarr_locations.getString(i));
                if (++j == SEARCH_HISTORY_SIZE) break;
            }
            // since we read backwards, need to reverse the list
            Collections.reverse(locations);

        } catch (IOException | JSONException e) {
            System.err.println("Err: couldn't read search history.");
        }
        
        return locations;
    }    
    
    /**
     *  Reloads the list of recently searched locations on the right side panel.
     */
    private void refreshSearchHistory(){

        ArrayList<String> locations = getSearchHistory();

        // if user hasn't searched for anything, yet,
        // display informational message instead of empty list
        if (locations.isEmpty()) {  
           
            String txt = "<i>No locations have been searched for, yet.</i>";
            txt = "<html><body><div style=\"height:80px\">" + txt +
                  "</div></body></html>"; 
            pnlSearchHistory.setVisible(false);           
            lblSearchHistory.setText(txt);
            lblSearchHistory.setVisible(true);
            return;
        }
        
        // otherwise display list
        lblSearchHistory.setVisible(false);
        pnlSearchHistory.setVisible(true);
        
        // removes all previous labels if they exist
        pnlSearchHistory.removeAll();
        pnlSearchHistory.repaint();
        
        // label action listeners
        MouseAdapter ma = new MouseAdapter() {
            // onclick - downloads and displays location
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                String location = ((JLabel)evt.getComponent()).getText();
                // uses input field to submit the same location
                txtLocation.setText(location);
                findLocation();
            }
            // when the user hovers over the label, change text color
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                ((JLabel)evt.getComponent()).setForeground(
                    new Color(171, 194, 255));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                ((JLabel)evt.getComponent()).setForeground(Color.white);
            }
        };
        
        // inits the list of JLabels
        if (lblsHistory == null) {
            lblsHistory = new JLabel[SEARCH_HISTORY_SIZE];
            
            for (int i = 0; i < SEARCH_HISTORY_SIZE; i++) {  
                
                lblsHistory[i] = new JLabel();
                lblsHistory[i].setFont(new java.awt.Font("Segoe UI", 0, 12));
                lblsHistory[i].setForeground(Color.white);
                lblsHistory[i].setCursor(new Cursor(Cursor.HAND_CURSOR));            
                lblsHistory[i].addMouseListener(ma);
                lblsHistory[i].setMaximumSize(
                    new Dimension(right_side_width, shist_location_height));
                lblsHistory[i].setPreferredSize(
                    new Dimension(right_side_width, shist_location_height));
                
                // first label should have top border
                int bdr_top = (i == 0) ? 1 : 0;
                lblsHistory[i].setBorder(new CompoundBorder(
                    BorderFactory.createMatteBorder(
                        bdr_top, 0, 1, 0, new Color(87, 118, 201)),
                    new EmptyBorder(2, 20, 2, 20)
                ));
            }
        }
        
        // we need to display history in reverse order
        Collections.reverse(locations);
        
        // for each location in the history, updates label content
        for (int i = 0; i < locations.size(); i++) {   
            lblsHistory[i].setText(locations.get(i));
            // all labels have been removed previously
            pnlSearchHistory.add(lblsHistory[i]);
        }
    }
    
    /**
     *  Reloads the list of recently searched locations on the right side panel.
     */
    private void refreshFavoritesList(){

        ArrayList<String[]> favorites = getFavourites();

        // if user hasn't favorited anything, yet,
        // display informational message instead of empty list
        if (favorites.isEmpty()) {           
            String txt = "<html><body><div style=\"height:100px\">" +
                "<i>No location has been favorited, yet.</i>" +
                "</div></body></html>"; 
            pnlFavourites.setVisible(false);           
            lblFavorites.setText(txt);
            lblFavorites.setVisible(true);
            return;
        }
        
        // otherwise display list
        lblFavorites.setVisible(false);
        pnlFavourites.setVisible(true);
        
        // removes all previous labels if they exist
        pnlFavourites.removeAll();
        pnlFavourites.repaint();
        
        // label action listeners
        MouseAdapter ma = new MouseAdapter() {
            // onclick - downloads and displays location
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                String location = ((JLabel)evt.getComponent()).getText();
                // uses input field to submit the same location
                txtLocation.setText(location);
                findLocation();
            }
            // when the user hovers over the label, change text color
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                ((JLabel)evt.getComponent()).setForeground(
                    new Color(171, 194, 255));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                ((JLabel)evt.getComponent()).setForeground(Color.white);
            }
        };

        // inits the list of JLabels
        if (lblsFavourites == null) {
            lblsFavourites = new JLabel[FAVOURITE_SIZE];
            
            for (int i = 0; i < FAVOURITE_SIZE; i++) {  
                
                lblsFavourites[i] = new JLabel();
                lblsFavourites[i].setFont(new java.awt.Font("Segoe UI", 0, 12));
                lblsFavourites[i].setForeground(Color.white);
                lblsFavourites[i].setCursor(new Cursor(Cursor.HAND_CURSOR));            
                lblsFavourites[i].addMouseListener(ma);
                lblsFavourites[i].setMaximumSize(
                    new Dimension(right_side_width, shist_location_height));
                lblsFavourites[i].setPreferredSize(
                    new Dimension(right_side_width, shist_location_height));
                
                // first label should have top border
                int bdr_top = (i == 0) ? 1 : 0;
                lblsFavourites[i].setBorder(new CompoundBorder(
                    BorderFactory.createMatteBorder(
                        bdr_top, 0, 1, 0, new Color(87, 118, 201)),
                    new EmptyBorder(2, 20, 2, 20)
                ));
            }
        }
        
        // for each location in the list, updates label content
        for (int i = 0; i < favorites.size(); i++) {   
            lblsFavourites[i].setText(CapsFirst(favorites.get(i)[1]));
            // all labels have been removed previously
            pnlFavourites.add(lblsFavourites[i]);
        }
    }   
    
    /**
     *  Capitalizes the first letter of each word.
     */
    private String CapsFirst(String str) {
        String[] words = str.split(" ");
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            ret.append(Character.toUpperCase(words[i].charAt(0)));
            ret.append(words[i].substring(1));
            if (i < words.length - 1) {
                ret.append(' ');
            }
        }
        return ret.toString();
    }
    
    /**
     * Refreshes star icon next to location title.
     */
    private void refreshFavoriteStar() {
        refreshFavoritesList();
        
        // no data loaded, yet; no need to show the star
        if (!location_loaded) {
            imgFavorite.setVisible(false);
            return;
        }
        
        String img_title = "star_empty";
        
        ArrayList<String[]> favorites = getFavourites();
        
        // checks if location is there
        for (int i = 0; i < favorites.size(); i++) {
            // [0] location id as string; [1] location title
            if (Integer.parseInt(favorites.get(i)[0]) == temp_location_id) {
                img_title = "star_full";
                break;
            }
        }
        
        // remembers if this location has been favorited
        temp_starred = (img_title.equals("star_full"));
        
        // if location not found and max number of favorites reached,
        // star will not be available
        if (img_title.equals("star_empty") &&
                favorites.size() >= FAVOURITE_SIZE) {
            imgFavorite.setVisible(false);
            return;
        }
        
        imgFavorite.setIcon(new ImageIcon(
            getClass().getResource("/images/" + img_title + ".png")));
        imgFavorite.setVisible(true);
    }
    
    /**
     *  Returns search history from storage file.
     */
    private ArrayList<String[]> getFavourites() {
        
        ArrayList<String[]> favorites = new ArrayList<>();
        
        File file;
        FileInputStream fis;
        byte[] data;        
        String str_favorites;
        JSONArray jsonarr_favorites;
        
        file = new File(Constants.PATH_FAVOURITE);
        
        // if file does not exist, creates a new one
        if (!file.exists() || file.isDirectory()) {            
            try {
                PrintWriter writer = new PrintWriter(
                    Constants.PATH_FAVOURITE, "UTF-8");
                writer.close();
            } catch (IOException e) {/**/}
            
            file = new File(Constants.PATH_FAVOURITE);
            if (!file.exists() || file.isDirectory()) {
//                setInfoText("Error while reading the list of favorites.");
                return favorites; // returns empty list
            }
        }

        // reads file content
        try {            
            fis = new FileInputStream(file);
            data = new byte[(int) file.length()];
            fis.read(data);
            str_favorites = new String(data);
            fis.close();

            // file content is json-formatted array list
            jsonarr_favorites = new JSONArray(str_favorites);

            // reads only FAVORITES_SIZE items
            for (int i = 0; i < jsonarr_favorites.length(); i++) {
                if (i == FAVOURITE_SIZE) break;
                String[] str = new String[2];
                str[0] = jsonarr_favorites.getJSONArray(i).getString(0); // id
                str[1] = jsonarr_favorites.getJSONArray(i).getString(1); // title
                favorites.add(str);
            }

        } catch (IOException | JSONException e) {
            System.err.println("Err: couldn't read the list of favorite locations.");
        }
        
        return favorites;
    }
    
    /**
     *  Saves the given location to the list of favorites.
     */
    private Boolean saveToFavorites(int location_id, String location_title) {
        
        ArrayList<String[]> favorites = getFavourites();
        
        // only FAVORITES_SIZE items allowed
        if (favorites.size() >= FAVOURITE_SIZE) return false;
        
        // checks if location isn't already there
        for (int i = 0; i < favorites.size(); i++) {
            // [0] location id as string; [1] location title
            if (Integer.parseInt(favorites.get(i)[0]) == location_id) {
                return false;
            }
        }
        
        String[] str = new String[2];
        str[0] = String.valueOf(location_id); // int to String conversion
        str[1] = location_title;
        favorites.add(str);
        
        // list of favorites is saved as json array string
        String output_obj = new JSONArray(favorites).toString();
        
        try {
            try (PrintWriter writer = new PrintWriter(
                Constants.PATH_FAVOURITE, "UTF-8")) {
                writer.write(output_obj);
                writer.close();
            }
        } catch (IOException e) {
            setInfoText("Couldn't save location to favorites.");
            return false;
        }
        
        return true;
    }
    
    /**
     *  Removes the given location from the list of favorites.
     */
    private void deleteFromFavorites(int location_id) {
        
        ArrayList<String[]> favorites = getFavourites();
        
        // looks for the given location
        for (int i = 0; i < favorites.size(); i++) {
            // [0] location id as string; [1] location title
            if (Integer.parseInt(favorites.get(i)[0]) == location_id) {
                favorites.remove(i);
                break;
            }
        }
        
        // saves the new list
        String output_obj = new JSONArray(favorites).toString();
        
        try {
            try (PrintWriter writer = new PrintWriter(
                Constants.PATH_FAVOURITE, "UTF-8")) {
                writer.write(output_obj);
            }
        } catch (IOException e) {/**/}
    }
    
    /**
     *  Enables/disables input field and button for location search.
     */
    private void setSearchingEnabled(Boolean flag) {
        txtLocation.setEnabled(flag);
        btnSearch.setEnabled(flag);
    }
    
    /**
     *  Upon entering a location in the input field
     *  will search for the location in the saved text file.
     */
    private void findLocation() {
        
        setInfoText("Searching for the location...");
        
        JSONObject obj;
        int field_id = 0; // 0 means not found
        String field_name = "";
        
        // location from the input field
        String location = txtLocation.getText().trim().toLowerCase();
        if (location.equals("")) {
            setInfoText("Please, enter location name.");
            return;
        }
        
        // will disable input field while searching/downloading
        setSearchingEnabled(false);
            
        try {
            // reads line by line
            try (BufferedReader br = new BufferedReader(
                new FileReader(Constants.PATH_LOCATION_FILE))) {
                // reads line by line
                for (String line; (line = br.readLine()) != null; ) {
                    // each line is a JSON-formatted string
                    obj = new JSONObject(line);
                    // compares "name" field with the needed location string
                    if (obj.getString("name").toLowerCase().equals(location)) {
                        field_id = obj.getInt("_id");
                        field_name = obj.getString("name");
                        // we can use this id to download data from webpage
                        break;
                    }
                }
            }
        } catch (IOException e) {
            setInfoText("Couldn't search for the location. (" + e.getMessage() + ")");
            setSearchingEnabled(true);
            return;
        } catch (JSONException e) {
            setInfoText("Error while searching for the location. (" + e.getMessage() + ")");
            setSearchingEnabled(true);
            return;
        }
        
        if (field_id == 0) {
            setInfoText("Location not found. Try searching differently.");
            setSearchingEnabled(true);
            return;
        }
        
        // saves location to search history
        saveToSearchHistory(field_name);
        refreshSearchHistory();
        
        setInfoText("Location \"" + field_name + "\" found with an ID " + field_id + ". Downloading data...");
        Network.get(this, "/forecast?units="+ unit_type +"&id=" + field_id, TASK_DOWNLOAD_CITY_DATA);
    }
    
    /**
     *  Sets needed window structure widgets programmatically.
     */
    private void initPanels() {
        
        int wnd_width = this.getWidth();
        int wnd_height = this.getHeight();
        
        // top menu bar
        pnlTopBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlTopBar.setLocation(0, 0);
        pnlTopBar.setSize(wnd_width, top_bar_height);
        pnlTopBar.setBackground(clrTopPanel);
        
        // left side menu bar
        pnlLeftBar = new JPanel();
        pnlLeftBar.setLocation(0, top_bar_height);
        pnlLeftBar.setSize(left_bar_width, wnd_height - top_bar_height);
        pnlLeftBar.setBackground(new Color(43, 43, 43));
        
        // right side extra info column
        pnlRightSide = new JPanel();
        pnlRightSide.setLayout(new BoxLayout(pnlRightSide, BoxLayout.Y_AXIS));
        pnlRightSide.setLocation(wnd_width - right_side_width, top_bar_height);
        pnlRightSide.setSize(right_side_width, wnd_height - top_bar_height);
        pnlRightSide.setBackground(clrRightPanel);
        
        // panel for info text above content panel
        pnlInfoText = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlInfoText.setLocation(left_bar_width, top_bar_height);
        pnlInfoText.setSize(wnd_width - left_bar_width - right_side_width,
            info_text_height);
        pnlInfoText.setBackground(Color.white);
        pnlInfoText.setBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.lightGray));
        
        // main content panel in the middle of the window
        pnlContent = new JPanel(new FlowLayout(FlowLayout.CENTER));
//        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));
        pnlContent.setLocation(left_bar_width, top_bar_height + info_text_height);
        pnlContent.setSize(wnd_width - left_bar_width - right_side_width,
            wnd_height - top_bar_height - info_text_height);
        pnlContent.setPreferredSize(new Dimension(
            wnd_width - left_bar_width - right_side_width,
            wnd_height - top_bar_height - info_text_height));
        pnlContent.setBackground(clrContentPanel);
        
        initSmallerComponents();
        
        // adds all panels to the main frame
        this.add(pnlTopBar);
        this.add(pnlLeftBar);
        this.add(pnlRightSide);
        this.add(pnlInfoText);
        this.add(pnlContent);
    }

    private void initSmallerComponents() {
        
        // input field for location search on top bar
        txtLocation = new JTextField();
        txtLocation.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtLocation.setBackground(new Color(209, 209, 209));
        txtLocation.setColumns(25);
        txtLocation.setMaximumSize(new Dimension(70, 35));
        txtLocation.setPreferredSize(new Dimension(70, 35));
        txtLocation.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(
                5, 0, 0, 0, clrTopPanel),
            new EmptyBorder(0, 10, 0, 10)
        ));
        pnlTopBar.add(txtLocation);

        // when pressing enter, will execute location search
        // (same as button click)
        txtLocation.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findLocation();
            }
        });
        
        // search button on top bar
        btnSearch = new JLabel();
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.setIcon(new javax.swing.ImageIcon(
            getClass().getResource("/images/magnifying_glass_white.png")));
        btnSearch.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(
                5, 0, 0, 0, clrTopPanel),
            new EmptyBorder(0, 5, 0, 20)
        ));
        // when clicked, will execute location search
        btnSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                findLocation();
            }
        });
        pnlTopBar.add(btnSearch);
        
        // label for info text above main content panel
        lblInfoText = new JLabel();
        lblInfoText.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblInfoText.setForeground(Color.black);
        lblInfoText.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(
                3, 0, 0, 0, Color.white),
            new EmptyBorder(0, 5, 0, 20)
        ));
        pnlInfoText.add(lblInfoText);

        // title label for search history
        lblSearchHistoryTitle = new JLabel();
        lblSearchHistoryTitle.setText("Search history");
        lblSearchHistoryTitle.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblSearchHistoryTitle.setOpaque(true);
        lblSearchHistoryTitle.setForeground(Color.white);
        lblSearchHistoryTitle.setBackground(new Color(66, 96, 175));
        lblSearchHistoryTitle.setMaximumSize(new Dimension(200, info_text_height + 30));
        lblSearchHistoryTitle.setPreferredSize(new Dimension(200, info_text_height + 30));
        lblSearchHistoryTitle.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(
                info_text_height - 1, 0, 0, 0, clrRightPanel),
            new EmptyBorder(10, 10, 10, 0)
        ));
        pnlRightSide.add(lblSearchHistoryTitle);
        
        // label for if the list of recent searches is empty
        lblSearchHistory = new JLabel();
        lblSearchHistory.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblSearchHistory.setForeground(Color.black);
        lblSearchHistory.setBackground(clrBox);
        lblSearchHistory.setOpaque(true);
        lblSearchHistory.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(
                10, 0, 10, 0, clrRightPanel),
            new EmptyBorder(10, 10, 10, 0)
        ));        
        lblSearchHistory.setVisible(false);
        pnlRightSide.add(lblSearchHistory);
        
        // sub-panel for the list of location jlabels
        pnlSearchHistory = new JPanel();
        pnlSearchHistory.setLayout(new BoxLayout(pnlSearchHistory, BoxLayout.Y_AXIS));
        pnlSearchHistory.setMaximumSize(new Dimension(right_side_width,
            10 + shist_location_height * SEARCH_HISTORY_SIZE));
        pnlSearchHistory.setBackground(clrBox);
        pnlSearchHistory.setBorder(
            BorderFactory.createMatteBorder(
                10, 0, 0, 0, clrRightPanel));
        pnlRightSide.add(pnlSearchHistory);

        // title label for favorite locations list
        lblFavoritesTitle = new JLabel();
        lblFavoritesTitle.setText("Favorite locations");
        lblFavoritesTitle.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblFavoritesTitle.setOpaque(true);
        lblFavoritesTitle.setForeground(Color.white);
        lblFavoritesTitle.setBackground(new Color(66, 96, 175));
        lblFavoritesTitle.setMaximumSize(new Dimension(200, 40));
        lblFavoritesTitle.setPreferredSize(new Dimension(200, 40));
        lblFavoritesTitle.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(
                10, 0, 0, 0, clrRightPanel),
            new EmptyBorder(0, 10, 0, 10)
        ));
        pnlRightSide.add(lblFavoritesTitle);
        
        // sub-panel for the list of favorite locations
        pnlFavourites = new JPanel();
        pnlFavourites.setLayout(new BoxLayout(pnlFavourites, BoxLayout.Y_AXIS));
        pnlFavourites.setMaximumSize(new Dimension(right_side_width,
            10 + shist_location_height * FAVOURITE_SIZE));
        pnlFavourites.setBackground(clrBox);
        pnlFavourites.setBorder(
            BorderFactory.createMatteBorder(
                10, 0, 0, 0, clrRightPanel));
        pnlRightSide.add(pnlFavourites);
        
        // label for if the list of favorites is empty
        lblFavorites = new JLabel();
        lblFavorites.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblFavorites.setForeground(Color.black);
        lblFavorites.setBackground(clrBox);
        lblFavorites.setOpaque(true);
        lblFavorites.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(
                10, 0, 10, 0, clrRightPanel),
            new EmptyBorder(10, 10, 10, 0)
        ));
        lblFavorites.setVisible(false);
        pnlRightSide.add(lblFavorites);
        
        refreshSearchHistory();
        refreshFavoritesList();
    }
    
    /**
     * Sets up the content panel layout and widgets.
     */
    private void initContentPanel() {        
        
        int width = this.getWidth() - left_bar_width - right_side_width;
        
        // panel in which location title and star icon is put inside
        JPanel pnlLocationTitle = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlLocationTitle.setPreferredSize(new Dimension(width, 40));
        pnlLocationTitle.setMaximumSize(new Dimension(width, 40));
        pnlLocationTitle.setMinimumSize(new Dimension(width, 40));
        pnlLocationTitle.setBackground(clrContentPanel);
        pnlLocationTitle.setBorder(new EmptyBorder(0, 16, 10, 20));
        
        // title of the searched location as the first thing in the panel
        lblLocationTitle = new JLabel();
        lblLocationTitle.setFont(new java.awt.Font("Segoe UI", 0, 20));
        lblLocationTitle.setBackground(clrContentPanel);
        lblLocationTitle.setOpaque(true);
        lblLocationTitle.setForeground(Color.black);
        pnlLocationTitle.add(lblLocationTitle);
        
        // star icon for location favoriting
        imgFavorite = new JLabel();
        imgFavorite.addMouseListener(new java.awt.event.MouseAdapter() {
            // onclick - saves location to favorites
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (temp_starred) { // remove from favorites
                    deleteFromFavorites(temp_location_id);
                } else { // add to favorites
                    saveToFavorites(temp_location_id, temp_location_title);
                }
                refreshFavoriteStar();
            }
        });
        imgFavorite.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnlLocationTitle.add(imgFavorite);
        pnlContent.add(pnlLocationTitle);
        refreshFavoriteStar();
        
        // empty, transparent rectangle between title and daily report
        pnlContent.add(Box.createRigidArea(new Dimension(width, 70)));

        // GridLayout for list of x days
        pnlReportDays = new JPanel(new GridLayout(0, MAX_SHOWN_DAYS));
//        pnlReportDays.setBackground(clrReportDaysRow);
        pnlReportDays.setBackground(clrDaysLeftLine);
        pnlReportDays.setPreferredSize(new Dimension(width, 150));
        pnlReportDays.setMaximumSize(new Dimension(width, 150));        
        pnlReportDays.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 20, 0, 20, clrContentPanel),
            new EmptyBorder(0, 20, 0, 20)
        ));
        pnlContent.add(pnlReportDays);
    }
    
    /**
     *  Creates menu bar on the left side of the window.
     */
    private void initLeftMenu() {
        
        JLabel lblAbout = new javax.swing.JLabel();
        
        // outer panel for "About" menu item
        JPanel pnlAbout = new JPanel();
        pnlAbout.setSize(left_bar_width, 150);
        pnlAbout.setBackground(new Color(43, 43, 43));
        pnlAbout.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlAbout.setBackground(new Color(56, 56, 56));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnlAbout.setBackground(new Color(43, 43, 43));
            }
        });
        pnlAbout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // "About" menu item
        lblAbout.setBackground(new Color(56, 56, 56));
        lblAbout.setIcon(new javax.swing.ImageIcon(
            getClass().getResource("/images/question.png")));
        lblAbout.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlAbout.setBackground(new Color(56, 56, 56));
            }
            AboutScreen ascr = null;
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // opens AboutScreen
                if (ascr == null) ascr = new AboutScreen();
                if (!ascr.isVisible()) {
                    ascr.setTitle(Constants.TITLE_ABOUT_SCREEN);
                    ascr.setLocationRelativeTo(null);
                    ascr.setVisible(true);
                }
            }
        });        
        
        // adds menu items to the menu bar
        pnlAbout.add(lblAbout);
        pnlLeftBar.add(pnlAbout);
        
        JLabel lblFahrenheit = new javax.swing.JLabel();
        JLabel lblCelsius = new javax.swing.JLabel();
        
        // outer panel for "Celsius" menu item
        JPanel pnlCelsius = new JPanel();
        pnlCelsius.setSize(left_bar_width, 150);
        pnlCelsius.setBackground(new Color(43, 43, 43));
        pnlCelsius.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlCelsius.setBackground(new Color(56, 56, 56));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnlCelsius.setBackground(new Color(43, 43, 43));
            }
        });
        pnlCelsius.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        String saved_unit = SplashScreen.readSettingsField("temp_unit");
        if (saved_unit.isEmpty()) {
            saved_unit = "metric";
        } else if (!saved_unit.equals("metric") &&
                !saved_unit.equals("imperial")) {
            saved_unit = "metric"; // default one
        }
        unit_type = saved_unit;
        
        // "Celsius" menu item
        lblCelsius.setBackground(new Color(56, 56, 56));
        String c_selected = (saved_unit.equals("metric")) ? "-selected" : "";
        lblCelsius.setIcon(new javax.swing.ImageIcon(
            getClass().getResource("/images/celsius" + c_selected + ".jpg")));
        lblCelsius.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlCelsius.setBackground(new Color(56, 56, 56));
            }
            AboutScreen ascr = null;
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                unit_type = "metric";
                SplashScreen.saveToSettings("temp_unit", unit_type);
                lblCelsius.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/celsius-selected.jpg")));
                lblFahrenheit.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/fahrenheit.jpg")));
                
                // redownloads the latest report
                ArrayList<String> locations = getSearchHistory();
                Collections.reverse(locations);
                if (locations.size() > 1) {
                    txtLocation.setText(locations.get(0));
                    findLocation();    
                }
            }
        });        
        
        // adds menu items to the menu bar
        pnlCelsius.add(lblCelsius);
        pnlLeftBar.add(pnlCelsius);
      
        // outer panel for "Fahrenheit" menu item
        JPanel pnlFahrenheit = new JPanel();
        pnlFahrenheit.setSize(left_bar_width, 150);
        pnlFahrenheit.setBackground(new Color(43, 43, 43));
        pnlFahrenheit.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlFahrenheit.setBackground(new Color(56, 56, 56));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnlFahrenheit.setBackground(new Color(43, 43, 43));
            }
        });
        pnlFahrenheit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // "Fahrenheit" menu item
        lblFahrenheit.setBackground(new Color(56, 56, 56));
        String f_selected = (saved_unit.equals("imperial")) ? "-selected" : "";
        lblFahrenheit.setIcon(new javax.swing.ImageIcon(
            getClass().getResource("/images/fahrenheit" + f_selected + ".jpg")));
        lblFahrenheit.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlFahrenheit.setBackground(new Color(56, 56, 56));
            }
            AboutScreen ascr = null;
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // opens AboutScreen
                unit_type = "imperial";
                SplashScreen.saveToSettings("temp_unit", unit_type);
                lblFahrenheit.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/fahrenheit-selected.jpg")));
                lblCelsius.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/celsius.jpg")));
                
                // redownloads the latest report
                ArrayList<String> locations = getSearchHistory();
                Collections.reverse(locations);
                if (locations.size() > 1) {
                    txtLocation.setText(locations.get(0));
                    findLocation();    
                }
            }
        });        
        
        // adds menu items to the menu bar
//        pnlLeftBar.setLayout(new FlowLayout(FlowLayout.CENTER));
//        pnlLeftBar.add(Box.createRigidArea(new Dimension(7, 7)));
        pnlFahrenheit.add(lblFahrenheit);
        pnlLeftBar.add(pnlFahrenheit);
    }
    
    public String capitalizeFirstLetter(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    /**
     * This method is called from within the constructor to initialise the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1100, 500));
        setResizable(false);
        setSize(new java.awt.Dimension(1100, 500));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1061, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 487, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private AboutScreen aboutScreen;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
