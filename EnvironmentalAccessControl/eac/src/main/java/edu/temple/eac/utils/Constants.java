package edu.temple.eac.utils;

import android.Manifest;

/**
 *
 */
public class Constants {

    public static final String SAMPLE_CONFIG_DOWNLOAD_URL =
            "https://www.dropbox.com/s/fx1zzdwqsenyyg9/eac_configs.zip?dl=1";
    public static final String SAMPLE_CONFIG_DOWNLOAD_URL2 =
            "https://www.dropbox.com/s/o2ypx9041hwmacb/eac_configs2.zip?dl=1";

    public enum APP_FOLDERS { ROOT, CONFIG, LOGS, PROPS }
    public static final String ROOT_FOLDER_NAME = "EnvAccControl";
    public static final String CONFIG_FOLDER_NAME = "configs";
    public static final String LOGS_FOLDER_NAME = "logs";
    public static final String PROPS_FOLDER_NAME = "props";
    public static final String IMAGES_FOLDER_NAME = "images";
    public static final String TEMP_FOLDER_NAME = "temp";

    public static final String[] REQUEST_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };
    public static final int REQUEST_INITIAL = 1337;

    public enum TRACKER_TYPE { Interval, Light, Screen, Time }
    public enum SCANNER_TYPE { GPS, Wifi }
    public enum SCAN_TYPE { Constant, Interval, Modified }
    public enum SCANNER_STATE { Mapping, Scanning, TargetReached, Canceled, Idle}

    public static final String DIALOG_SIGNAL_TITLE = "Loading...";
    public static final String DIALOG_SIGNAL_MESSAGE = "Retrieving signals... please wait.";
    public static final String DIALOG_SIGNAL_FOUND = "Signal found.";
    public static final String DIALOG_SIGNAL_COMPLETE = "Mapping complete.";

    public static final String DIALOG_API_TITLE = "Connecting...";
    public static final String DIALOG_API_MESSAGE = "Attempting to connect to Google API... please wait.";
    public static final String DIALOG_API_COMPLETE = "Connection acquired.";

}