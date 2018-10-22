package edu.temple.eac.scanners.signal;

import android.app.Activity;
import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import edu.temple.eac.scanners.IScanner;
import edu.temple.eac.scanners.IScannerListener;
import edu.temple.eac.scanners.ScanRateModifier;
import edu.temple.eac.scanners.ScannerConfig;
import edu.temple.eac.scanners.StepMonitor;
import edu.temple.eac.scanners.abs.ScanRateUpdater;
import edu.temple.eac.scanners.abs.ScannerConfigManager;
import edu.temple.eac.utils.Constants;
import edu.temple.eac.utils.DialogManager;
import edu.temple.eac.utils.LogManager;
import edu.temple.eac.utils.StorageManager;
import edu.temple.eac.utils.ToastManager;

/**
 *
 */
public class SignalScanner implements IScanner, ISignalUpdateListener {

    private ScannerConfigManager scm;
    private ScanRateUpdater sru;
    private ScannerConfig sc;

    private Activity currentActivity;
    private List<IScannerListener> listeners;

    private String LOG_FILE_NAME = "wifi_log_%s.txt";
    private int SCAN_LIST_LIMIT = 10;
    private String areaName;

    private boolean mappingArea;
    private boolean scanningArea;
    private boolean appendToConfig;
    private boolean userStoppedScanning = false;

    private Constants.SCAN_TYPE scanType = Constants.SCAN_TYPE.Constant;

    private List<String> scanResults;
    private Date firstScan, lastScan, stopScan, matchScan;

    /**
     *
     * @param currentActivity
     */
    public SignalScanner(Activity currentActivity) {
        scm = new SignalConfigManager();
        sru = new SignalScanRateUpdater(currentActivity);
        sc = new ScannerConfig();

        this.currentActivity = currentActivity;
        listeners = new ArrayList<>();
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      SCANNING TRACKER METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     * @param areaName
     * @param appendToConfig
     */
    public void mapArea(String areaName, boolean appendToConfig) throws SecurityException {
        // Prepare the loading view
        DialogManager.show(currentActivity,
                Constants.DIALOG_SIGNAL_TITLE, Constants.DIALOG_SIGNAL_MESSAGE);
        this.areaName = areaName;
        this.appendToConfig = appendToConfig;

        // Fire off the signal updates
        mappingArea = true;
        sru.getRegularUpdates(this, scanType);

        // Log completion of map activity
        LogManager.info("Map area activity initialized");
    }

    /**
     *
     * @param monitor
     */
    public void addStepMonitor(StepMonitor monitor) {
        scm.addStepMonitor(this.currentActivity, monitor);
    }

    /**
     *
     * @return
     */
    public boolean markScannerTargetReached() {
        this.userStoppedScanning = true;
        this.stopScan = new Date();

        if (matchScan != null) {
            // match already found ... finalize now
            finalizeScanner();
            return true;
        } else {
            // else, keep scanning until match found
            return false;
        }
    }

    /**
     *
     * @return
     */
    public String finalizeScanner() {
        writeResultsToLog();
        return ("Scan complete");
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      BASE TRACKER METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     */
    public void initialize(Constants.SCAN_TYPE scanType) {
        // look for changes in GPS as compared to previously measured locations
        scanningArea = true;
        scanResults = new ArrayList<>();

        // read in and store the values from the config file
        scm.readConfigs(sc);

        // Get a constant update the first time around so we can establish starting location
        // If using variable scanning, will kick in on next location update
        this.scanType = scanType;
        sru.getRegularUpdates(this, scanType);

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
        this.listeners.add(listener);
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
        this.listeners.clear();
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      SIGNAL UPDATE LISTENER METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     * @param scanResults
     */
    public void getUpdateResults(List<ScanResult> scanResults) {
        scanResults = getStrongestSignals(scanResults, SCAN_LIST_LIMIT);
        if (mappingArea) {
            // process new scan set, write the result to the config, and remove loading screen
            scm.writeLocationConfig(areaName, appendToConfig, scanResults);
            DialogManager.hide(currentActivity, Constants.DIALOG_SIGNAL_COMPLETE);

            // We only need one reading, so cancel further updates
            sru.removeUpdates(this);

            // Log activity completion
            LogManager.info("Area mapping complete");
        } else if (scanningArea || (userStoppedScanning && matchScan == null)) {
            // we're scanning for continual updates...
            String result = StorageManager.getCurrentTimestamp() + " - "
                    + scanResults.size() + " Wifi networks scanned.\n";
            this.scanResults.add(result);

            lastScan = new Date();
            if (firstScan == null) firstScan = new Date();
            if (scm.isMatch(scanResults)) {
                matchScan = new Date();
                ToastManager.showShortToast(currentActivity, "Location match found at time: " + matchScan);
            }

            if (userStoppedScanning && matchScan != null) {
                // we've reached an end condition
                writeResultsToLog();
            } else if (scanType == Constants.SCAN_TYPE.Modified) {
                // keep scanning at modified rate until scanner is disabled
                double speedThreshold = sc.getSpeedThreshold();
                double newScanRateMod = ScanRateModifier.generate(scm.getDistanceFromClosestSource(scanResults),
                        sc.getDistanceThreshold(), scm.getWalkingSpeed(speedThreshold,new ArrayList<Double>()),
                        speedThreshold, sc.getGrowthRateClose(), sc.getGrowthRateFast(), sc.getGrowthRateSlow());
                sru.getVariableUpdates(this, sc.getDefaultScanInterval(), newScanRateMod);
            }
        }
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      PRIVATE REFERENCE METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     * @param scanResults
     * @return
     */
    private List<ScanResult> getStrongestSignals(List<ScanResult> scanResults, int limit) {
        // sort scan results array in descending order
        Collections.sort(scanResults, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult scanResult, ScanResult t1) {
                // return 0 if equal
                // return neg if scanResult level > t1 level
                // return pos if scanResult level < t1 level
                return (t1.level - scanResult.level);
            }
        });

        if (scanResults.size() > limit) {
            List<ScanResult> sortedSignals = new ArrayList<>();
            for (int i = 0; i < limit; i++) {
                sortedSignals.add(scanResults.get(i));
            }

            return sortedSignals;
        } else return scanResults;
    }

    /**
     *
     */
    private void writeResultsToLog() {
        List<Date> scanTimes = new ArrayList<>();
        scanTimes.add(firstScan);
        scanTimes.add(lastScan);
        scanTimes.add(stopScan);
        scanTimes.add(matchScan);

        System.out.println("Finalizing scanner.  Writing " + scanResults.size() + " results to log");
        ScanRateModifier.writeToLogCSV(scanResults, scanTimes, LOG_FILE_NAME);
        sru.removeUpdates(this);
        scanningArea = false;

        ToastManager.showShortToast(currentActivity, "Wifi Scan Complete");
    }

}