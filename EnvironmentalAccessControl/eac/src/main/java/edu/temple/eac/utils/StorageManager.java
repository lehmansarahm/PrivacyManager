package edu.temple.eac.utils;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.temple.eac.scanners.location.LocationConfigManager;
import edu.temple.eac.scanners.signal.SignalConfigManager;
import edu.temple.eac.trackers.ClockTracker;
import edu.temple.eac.trackers.IntervalTracker;
import edu.temple.eac.trackers.LightTracker;

/**
 *
 */
public class StorageManager {

    private static DateFormat SHORT_DATE = new SimpleDateFormat("yyyyMMdd");
    private static DateFormat LONG_DATE = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
    private static DateFormat EXACT_TIME = new SimpleDateFormat("HH:mm:ss.SSS");

    private static File ROOT_FOLDER;
    private static File CONFIG_FOLDER;
    private static File LOGS_FOLDER;
    private static File PROPS_FOLDER;


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      CONFIG, PROPS I/O METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     */
    public static void writeSampleConfigs() {
        writeToFile(Constants.APP_FOLDERS.CONFIG, LocationConfigManager.CONFIG_FILE_NAME,
                "DistanceThreshold:100\nSpeedThreshold:4\n" +
                        "DefaultScanInterval:5000\nGrowthRateClose:2\n" +
                        "GrowthRateFast:3\nGrowthRateSlow:1.5\n", true);
        writeToFile(Constants.APP_FOLDERS.CONFIG, LocationConfigManager.LOCATION_FILE_NAME,
                        // "Location:Home,Latitude:40.0355027,Longitude:-75.2209001\n" +
                        "Location:HermAndMitch,Latitude:40.03582609,Longitude:-75.22041383\n", true);

        writeToFile(Constants.APP_FOLDERS.CONFIG, SignalConfigManager.CONFIG_FILE_NAME,
                "DistanceThreshold:100\nSpeedThreshold:4\n" +
                        "DefaultScanInterval:5000\nGrowthRateClose:2\n" +
                        "GrowthRateFast:3\nGrowthRateSlow:1.5\n", true);
        writeToFile(Constants.APP_FOLDERS.CONFIG, SignalConfigManager.LOCATION_FILE_NAME,
                "Location:SchoolDesk\n" +
                "BSSID:18:64:72:4f:5f:12,Level:-40,Frequency:5220\n"+
                "BSSID:18:64:72:4f:5f:10,Level:-40,Frequency:5220\n"+
                "BSSID:18:64:72:4f:5f:11,Level:-40,Frequency:5220\n"+
                "BSSID:18:64:72:4f:5f:02,Level:-50,Frequency:2437\n"+
                "BSSID:18:64:72:4f:5f:00,Level:-50,Frequency:2437\n"+
                "BSSID:18:64:72:4f:6e:32,Level:-56,Frequency:5180\n"+
                "BSSID:18:64:72:4f:6e:30,Level:-57,Frequency:5180\n"+
                "BSSID:18:64:72:4f:6e:31,Level:-57,Frequency:5180\n"+
                "BSSID:c4:3d:c7:4f:a0:28,Level:-58,Frequency:2437\n"+
                "BSSID:18:64:72:4f:5f:d0,Level:-60,Frequency:5765\n", true);

        writeToFile(Constants.APP_FOLDERS.CONFIG, LightTracker.CONFIG_FILE_NAME, "50", true);
        writeToFile(Constants.APP_FOLDERS.CONFIG, IntervalTracker.CONFIG_FILE_NAME, "5000", true);
        writeToFile(Constants.APP_FOLDERS.CONFIG, ClockTracker.CONFIG_FILE_NAME,
                "5000\n13:00:00,15:00:00\n16:00:00,17:00:00", true);
    }

    /**
     *
     * @param configURL
     */
    public static void downloadConfigs(String configURL) {
        try {
            // Attempt to download config in 60 seconds
            // Halt other functions until complete
            DownloadConfigTask task = new DownloadConfigTask();
            task.execute(configURL).get(60, TimeUnit.SECONDS);
        } catch (Exception ex) {
            LogManager.error("Could not download config.  Exception: " + ex.getMessage());
        }
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      GENERAL PURPOSE FILE I/O METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     * @param folder
     * @param filename
     * @return
     */
    public static List<String> readFile(Constants.APP_FOLDERS folder, String filename) {
        List<String> contents = new ArrayList<>();
        File parentDir = initializeStorage(folder);
        File file = new File(parentDir, filename);

        if (file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    contents.add(line);
                }
                br.close();
            } catch (IOException e) {
                LogManager.info("Could not read file: " + e.toString());
                LogManager.error("File read failed: " + e.toString());
            }
        }

        return contents;
    }

    /**
     *
     * @param folder
     * @param filename
     * @param content
     * @param overwrite
     * @return
     */
    public static boolean writeToFile(Constants.APP_FOLDERS folder, String filename,
                                      String content, boolean overwrite) {
        LogManager.info("Ready to write to file: " + filename
                + " with content:\n" + content);
        LogManager.info("Previous file contents will be overwritten: " + overwrite);
        boolean success = true;

        try
        {
            File parentDir = initializeStorage(folder);
            File file = new File(parentDir, filename);
            if (!file.exists()) file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file, !overwrite);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            if (overwrite) osw.write(content);
            else osw.append(content);

            osw.close();
            fos.flush();
            fos.close();
        }
        catch (IOException e)
        {
            LogManager.error("File write failed: " + e.toString());
            success = false;
        }

        return success;
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      GENERAL PURPOSE INFO METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     * @param date
     * @return
     */
    public static String getShortFormattedDate(Date date) {
        return SHORT_DATE.format(date);
    }

    /**
     *
     * @param date
     * @return
     */
    public static String getLongFormattedDate(Date date) {
        return LONG_DATE.format(date);
    }

    /**
     *
     * @param fileNameTemplate
     * @param includeTime
     * @return
     */
    public static String generateDatedFileName(String fileNameTemplate, boolean includeTime) {
        Date currentDate = new Date();
        String date = getShortFormattedDate(currentDate);
        if (includeTime) date += "_" + getCurrentTimestamp();

        String newFileName = String.format(fileNameTemplate, date);
        return newFileName;
    }

    /**
     *
     * @return
     */
    public static String getCurrentTimestamp() {
        Date currentTime = new Date();
        String currentTimestamp = EXACT_TIME.format(currentTime);
        return currentTimestamp;
    }

    /**
     *
     * @return
     */
    public static String formatTimestamp(Date currentTime) {
        String currentTimestamp = EXACT_TIME.format(currentTime);
        return currentTimestamp;
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      PRIVATE REFERENCE METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     * @param folder
     * @return
     */
    private static File initializeStorage(Constants.APP_FOLDERS folder) {
        ROOT_FOLDER = new File(Environment.getExternalStorageDirectory(), Constants.ROOT_FOLDER_NAME);
        if (!ROOT_FOLDER.exists()) { ROOT_FOLDER.mkdirs(); }

        CONFIG_FOLDER = new File(ROOT_FOLDER, Constants.CONFIG_FOLDER_NAME);
        if (!CONFIG_FOLDER.exists()) { CONFIG_FOLDER.mkdirs(); }

        LOGS_FOLDER = new File(ROOT_FOLDER, Constants.LOGS_FOLDER_NAME);
        if (!LOGS_FOLDER.exists()) { LOGS_FOLDER.mkdirs(); }

        PROPS_FOLDER = new File(ROOT_FOLDER, Constants.PROPS_FOLDER_NAME);
        if (!PROPS_FOLDER.exists()) { PROPS_FOLDER.mkdirs(); }

        switch (folder) {
            case ROOT:
                return ROOT_FOLDER;
            case CONFIG:
                return CONFIG_FOLDER;
            case LOGS:
                return LOGS_FOLDER;
            case PROPS:
                return PROPS_FOLDER;
            default:
                return null;
        }
    }

}