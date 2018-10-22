package edu.temple.eac.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {

    public static Context context;

    /**
     *
     * @param propName
     * @param defaultValue
     * @return
     */
    public static String getProperty(String propName, String defaultValue) {
        try {
            InputStream is = context.getAssets().open("eac.properties");
            Properties props = new Properties();
            props.load(is);
            return props.getProperty(propName);
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            return defaultValue;
        }
    }

}