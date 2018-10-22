package edu.temple.eac.scanners.location;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Date;

import edu.temple.eac.scanners.IScanner;
import edu.temple.eac.scanners.IScannerListener;
import edu.temple.eac.scanners.StepMonitor;
import edu.temple.eac.scanners.location.gps.GPSScanner;
import edu.temple.eac.utils.Constants;
import edu.temple.eac.utils.DialogManager;
import edu.temple.eac.utils.LogManager;
import edu.temple.eac.utils.StorageManager;

// TODO - swap for testing
// public class LocationScanner extends ApiScanner implements IScanner {

public class LocationScanner extends GPSScanner implements IScanner {

    /**
     *
     * @param currentActivity
     */
    public LocationScanner(Activity currentActivity) {
        super.initialize(currentActivity);
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      SCANNING TRACKER METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     * @param areaName
     */
    public void mapArea(String areaName, boolean appendToConfig) {
        // Prepare the loading view
        DialogManager.show(currentActivity,
                Constants.DIALOG_SIGNAL_TITLE, Constants.DIALOG_SIGNAL_MESSAGE);

        // Set the mapping properties
        state = Constants.SCANNER_STATE.Mapping;
        this.areaName = areaName;
        this.appendToConfig = appendToConfig;

        // Fire off the signal updates, then log completion of map activity
        super.sru.getRegularUpdates(this, Constants.SCAN_TYPE.Constant);
        LogManager.info("Map area activity initialized");
    }

    /**
     *
     * @param monitor
     */
    public void addStepMonitor(StepMonitor monitor) {
        scm.addStepMonitor(currentActivity, monitor);
    }

    /**
     *
     * @return
     */
    public boolean markScannerTargetReached() {
        // user has manually indicated that they have reached their target destination
        state = Constants.SCANNER_STATE.TargetReached;
        LogManager.info("User stopped scanning");
        stopScan = new Date();

        if (matchScan != null) {
            // if a match has already been found, go ahead and write the log results now
            writeResultsToLog();
            return true;
        } else {
            // else, keep scanning until we find a match or user cancels
            return false;
        }
    }

    /**
     *
     * @return
     */
    public String finalizeScanner() {
        // user force cancelled operations
        state = Constants.SCANNER_STATE.Canceled;
        return ("User has manually finalized scanning... Session complete");
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      BASE TRACKER METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     * Look for changes in GPS as compared to previously measured locations
     */
    public void initialize(Constants.SCAN_TYPE scanType) {
        LogManager.info("Initializing GPS for scanning method: " + scanType);
        state = Constants.SCANNER_STATE.Scanning;
        scanResults = new ArrayList<>();

        // Show dialog asking user to wait while we obtain location signal
        DialogManager.show(currentActivity, Constants.DIALOG_SIGNAL_TITLE,
                Constants.DIALOG_SIGNAL_MESSAGE);

        // read in and store the values from the config file
        scm.readConfigs(sc);

        // Get a constant update the first time around so we can establish starting location
        // If using variable scanning, will kick in on next location update
        super.scanType = scanType;
        int constScanInterval = super.sru.getRegularUpdates(this, scanType);
        sc.setScanInterval((scanType == Constants.SCAN_TYPE.Modified)
                ? sc.getDefaultScanInterval() : constScanInterval);

        // Update the log file name for this scanner
        LOG_FILE_NAME = (scanType + "_" + LOG_FILE_NAME);
        LOG_FILE_NAME = StorageManager.generateDatedFileName(LOG_FILE_NAME, true);
    }

    /**
     *
     * @return
     */
    public boolean hasListeners() {
        return (!this.listeners.isEmpty());
    }

    /**
     *
     * @param listener - the new listener subscribing to this tracker
     */
    public void addListener(IScannerListener listener) {
        listeners.add(listener);
    }

    /**
     *
     * @param listener
     */
    public void removeListener(IScannerListener listener) {
        if (this.listeners.contains(listener)) this.listeners.remove(listener);
    }

    /**
     *
     */
    public void removeListeners() {
        listeners.clear();
    }

}