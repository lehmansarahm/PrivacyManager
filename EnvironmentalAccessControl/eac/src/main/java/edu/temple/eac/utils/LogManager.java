package edu.temple.eac.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class LogManager {

    private static final String APP_TAG = "EAC";
    private static String INFO_LOG_FILE_NAME = "info_log_%s.csv";
    private static String ERROR_LOG_FILE_NAME = "error_log_%s.csv";

    private static List<String> infoMessages = new ArrayList<>();
    private static List<String> errorMessages = new ArrayList<>();

    /**
     *
     * @param message
     */
    public static void info(String message) {
        infoMessages.add(message);
        Log.i(APP_TAG, message);
    }

    /**
     *
     * @param message
     */
    public static void error(String message) {
        errorMessages.add(message);
        Log.e(APP_TAG, message);
    }

    /**
     *
     */
    public static void dumpLogs() {
        String infoLog = StorageManager.generateDatedFileName(INFO_LOG_FILE_NAME, false);
        generateLog(infoLog, infoMessages);

        String errorLog = StorageManager.generateDatedFileName(ERROR_LOG_FILE_NAME, false);
        generateLog(errorLog, errorMessages);
    }

    /**
     *
     * @param fileName
     * @param messages
     */
    private static void generateLog(String fileName, List<String> messages) {
        String out = "";
        for (String message : messages) out += message + "\n";
        Log.i(APP_TAG, "Generating log file for: " + fileName);
        StorageManager.writeToFile(Constants.APP_FOLDERS.LOGS, fileName, out, false);
    }

}