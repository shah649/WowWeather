/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.team2.wowweather;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JCheckBox;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.team2.wowweather.classes.Constants;
import se.team2.wowweather.classes.UserInterface;

public class SplashScreen extends javax.swing.JFrame {

    /**
     * Creates new form SplashScreen.
     */
    public SplashScreen() {
        
        initComponents();
        
        this.setTitle(Constants.TITLE_SPLASH_SCREEN);
        this.setVisible(true);
        // centers application in the middle of the screen
        this.setLocationRelativeTo(null);
        // will disable window resizing
        // (otherwise weird things happen with the widgets)
        this.setResizable(false);
        
        // initializes default content
        lblTopHeader.setText(Constants.SPLASH_HEADER_TITLE);
        lblWelcomeText.setText(Constants.SPLASH_WELCOME_TEXT);
    }
    
    /**
     *  Returns value with the given key from the settings.
     */
    public static String readSettingsField(String field_name) {
        
        ArrayList<String[]> settings = readSettings();
        
        for (int i = 0; i < settings.size(); i++) {
            if (settings.get(i)[0].equals(field_name)) {
                return settings.get(i)[1];
            }
        }
        
        return "";
    }
    
    /**
     * Saves values with the given key in settings.
     */
    public static void saveToSettings(String field_key, String field_value) {
        
        ArrayList<String[]> settings = readSettings();
        
        Boolean key_found = false;
        
        for (int i = 0; i < settings.size(); i++) {
            if (settings.get(i)[0].equals(field_key)) {
                settings.set(i,
                    new String[]{field_key, field_value});
                key_found = true;
            }
        }
        
        if (!key_found) {
            settings.add(new String[]{field_key, field_value});
        }
        
        // saves new settings
        String output_obj = new JSONArray(settings).toString();
        
        try {
            try (PrintWriter writer = new PrintWriter(
                Constants.PATH_SETTINGS, "UTF-8")) {
                writer.write(output_obj);
                writer.close();
            }
        } catch (IOException e) {/**/}
    }
    
    /**
     *  Reads settings from json settings file.
     */
    public static ArrayList<String[]> readSettings() {
        
        ArrayList<String[]> settings = new ArrayList<>();
        
        File file;
        FileInputStream fis;
        byte[] data;        
        String str_settings;
        JSONArray jsonarr_settings;
        
        file = new File(Constants.PATH_SETTINGS);
        
        // if file does not exist, creates a new one
        if (!file.exists() || file.isDirectory()) {            
            try {
                PrintWriter writer = new PrintWriter(
                    Constants.PATH_SETTINGS, "UTF-8");
                writer.close();
            } catch (IOException e) {/**/}
            
            file = new File(Constants.PATH_SETTINGS);
            if (!file.exists() || file.isDirectory()) {
                return settings; // returns empty list
            }
        }

        // reads file content
        try {            
            fis = new FileInputStream(file);
            data = new byte[(int) file.length()];
            fis.read(data);
            str_settings = new String(data);
            fis.close();

            // file content is json-formatted array list
            jsonarr_settings = new JSONArray(str_settings);
            JSONArray arr;
            
            int j = 0;
            for (int i = 0; i < jsonarr_settings.length(); i++) {
                arr = jsonarr_settings.getJSONArray(i);
                String[] str = {arr.getString(0), arr.getString(1)};
                settings.add(str);
            }

        } catch (IOException | JSONException e) {
            System.err.println("Err: couldn't read settings.");
        }
        
        return settings;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlTopHeader = new javax.swing.JPanel();
        lblTopHeader = new javax.swing.JLabel();
        pnlBackground = new javax.swing.JPanel();
        imgLogo = new javax.swing.JLabel();
        lblWelcomeText = new javax.swing.JLabel();
        btnContinue = new javax.swing.JButton();
        cbDoNotShow = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));

        pnlTopHeader.setBackground(new java.awt.Color(69, 152, 216));

        lblTopHeader.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblTopHeader.setForeground(new java.awt.Color(255, 255, 255));
        lblTopHeader.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTopHeader.setText("Welcome!");
        lblTopHeader.setToolTipText("");
        lblTopHeader.setAlignmentY(0.0F);
        lblTopHeader.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout pnlTopHeaderLayout = new javax.swing.GroupLayout(pnlTopHeader);
        pnlTopHeader.setLayout(pnlTopHeaderLayout);
        pnlTopHeaderLayout.setHorizontalGroup(
            pnlTopHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 640, Short.MAX_VALUE)
            .addGroup(pnlTopHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlTopHeaderLayout.createSequentialGroup()
                    .addGap(22, 22, 22)
                    .addComponent(lblTopHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 598, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(20, Short.MAX_VALUE)))
        );
        pnlTopHeaderLayout.setVerticalGroup(
            pnlTopHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
            .addGroup(pnlTopHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlTopHeaderLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblTopHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(12, Short.MAX_VALUE)))
        );

        pnlBackground.setBackground(new java.awt.Color(255, 255, 255));
        pnlBackground.setForeground(new java.awt.Color(255, 255, 255));
        pnlBackground.setAlignmentX(0.0F);
        pnlBackground.setAlignmentY(0.0F);
        pnlBackground.setDoubleBuffered(false);

        imgLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo.png"))); // NOI18N

        lblWelcomeText.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblWelcomeText.setLabelFor(imgLogo);
        lblWelcomeText.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblWelcomeText.setAutoscrolls(true);
        lblWelcomeText.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout pnlBackgroundLayout = new javax.swing.GroupLayout(pnlBackground);
        pnlBackground.setLayout(pnlBackgroundLayout);
        pnlBackgroundLayout.setHorizontalGroup(
            pnlBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBackgroundLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(imgLogo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addComponent(lblWelcomeText, javax.swing.GroupLayout.PREFERRED_SIZE, 411, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29))
        );
        pnlBackgroundLayout.setVerticalGroup(
            pnlBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBackgroundLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(imgLogo)
                .addContainerGap(26, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBackgroundLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(lblWelcomeText, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        btnContinue.setText("Continue");
        btnContinue.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnContinueMouseClicked(evt);
            }
        });
        btnContinue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContinueActionPerformed(evt);
            }
        });

        cbDoNotShow.setText("Do not show this on future launches");
        cbDoNotShow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbDoNotShowActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBackground, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(cbDoNotShow)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnContinue, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnlTopHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(70, 70, 70)
                .addComponent(pnlBackground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnContinue, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                    .addComponent(cbDoNotShow))
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(pnlTopHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 248, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnContinueMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnContinueMouseClicked
        
        // closes Splash Screen
        this.dispose();
        
        // opens the main window
        java.awt.EventQueue.invokeLater(() -> {
            new UserInterface().setVisible(true);
        });
    }//GEN-LAST:event_btnContinueMouseClicked

    private void btnContinueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContinueActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnContinueActionPerformed

    private void cbDoNotShowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbDoNotShowActionPerformed
        // checking/unchecking checkbox, saves choice in the settings
        
        JCheckBox cbDoNotShow = (JCheckBox) evt.getSource();
        boolean selected = cbDoNotShow.getModel().isSelected();
        saveToSettings("splash_hide", String.valueOf(selected));
    }//GEN-LAST:event_cbDoNotShowActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnContinue;
    private javax.swing.JCheckBox cbDoNotShow;
    private javax.swing.JLabel imgLogo;
    private javax.swing.JLabel lblTopHeader;
    private javax.swing.JLabel lblWelcomeText;
    private javax.swing.JPanel pnlBackground;
    private javax.swing.JPanel pnlTopHeader;
    // End of variables declaration//GEN-END:variables
}
