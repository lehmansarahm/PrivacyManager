package edu.temple.eac;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import java.util.HashMap;
import java.util.Map;

import edu.temple.eac.scanners.IScanner;
import edu.temple.eac.scanners.IScannerListener;
import edu.temple.eac.scanners.StepMonitor;
import edu.temple.eac.scanners.location.LocationScanner;
import edu.temple.eac.scanners.signal.SignalScanner;
import edu.temple.eac.trackers.ClockTracker;
import edu.temple.eac.trackers.ITracker;
import edu.temple.eac.trackers.ITrackerListener;
import edu.temple.eac.trackers.IntervalTracker;
import edu.temple.eac.trackers.LightTracker;
import edu.temple.eac.utils.Constants;
import edu.temple.eac.utils.LogManager;
import edu.temple.eac.utils.PropertyReader;
import edu.temple.eac.utils.StorageManager;
import edu.temple.eac.utils.ToastManager;

/**
 *
 */
public class EAController {

    protected Activity currentActivity;
    private Map<Constants.TRACKER_TYPE, ITracker> trackers;
    private Map<Constants.SCANNER_TYPE, IScanner> scanners;
    private StepMonitor stepMonitor;


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      CONSTRUCTORS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     */
    public EAController(Context context) {
        PropertyReader.context = context;
        trackers = new HashMap<>();
        scanners = new HashMap<>();
        LogManager.info("EA Controller instantiation complete...");
    }

    /**
     *
     */
    public EAController(Context context, String configURL) {
        PropertyReader.context = context;
        trackers = new HashMap<>();
        scanners = new HashMap<>();

        StorageManager.downloadConfigs(configURL);
        LogManager.info("EA Controller instantiation complete... using app configs from URL: " + configURL);
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      CONFIG METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     */
    public void writeDefaultConfigs() {
        StorageManager.writeSampleConfigs();
        LogManager.info("Writing default configuration details to file.");
        if (currentActivity != null)
            ToastManager.showShortToast(currentActivity, "Config file operation complete");
    }

    /**
     *
     * @param configURL
     */
    public void updateConfigs(String configURL) {
        StorageManager.downloadConfigs(configURL);
        LogManager.info("Config download complete.  Restarting trackers");

        for (Constants.TRACKER_TYPE type : Constants.TRACKER_TYPE.values()) {
            if (trackers.containsKey(type)) {
                trackers.get(type).restart();
            }
        }
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      INITIALIZERS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     * @param activity
     */
    public void initializeActivity(Activity activity) { initializeActivity(activity, false); }

    /**
     *
     * @param activity
     * @param forcePermissionsRequest
     */
    public void initializeActivity(Activity activity, boolean forcePermissionsRequest) {
        this.currentActivity = activity;

        if (forcePermissionsRequest) {
            ActivityCompat.requestPermissions(currentActivity,
                    Constants.REQUEST_PERMISSIONS, Constants.REQUEST_INITIAL);
        }

        int wifiPermissionStatus =
                currentActivity.checkCallingOrSelfPermission(Manifest.permission.CHANGE_WIFI_STATE);
        int extStoragePermissionStatus =
                currentActivity.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        try {
            if (wifiPermissionStatus == PackageManager.PERMISSION_GRANTED
                    && extStoragePermissionStatus == PackageManager.PERMISSION_GRANTED) {
                // TODO - write configuration file I/O logic
            }
            else {
                ToastManager.showLongToast(currentActivity, "Insufficient wifi or external storage "
                        + "access.  Is the AR manifest properly formatted?");
            }
        } catch (Exception ex) {
            ToastManager.showLongToast(currentActivity, ex.getMessage());
        }

        LogManager.info("Activity initialized");
    }

    /**
     *
     */
    public void initializeTrackers() {
        for (Constants.TRACKER_TYPE type : Constants.TRACKER_TYPE.values()) {
            if (trackers.containsKey(type)) {
                trackers.get(type).initialize(this.currentActivity);
            }
        }
    }

    /**
     *
     * @param scanType
     */
    public void initializeScanners(Constants.SCAN_TYPE scanType) {
        for (Constants.SCANNER_TYPE type : Constants.SCANNER_TYPE.values()) {
            if (scanners.containsKey(type)) {
                IScanner scanner = scanners.get(type);
                if (scanner.hasListeners()) scanner.initialize(scanType);
            }
        }
    }

    /**
     *
     * @param userHeightInches
     */
    public void initializeStepMonitor(int userHeightInches) {
        double userHeightMeters = StepMonitor.convertInchesToMeters(userHeightInches);
        stepMonitor = new StepMonitor(userHeightMeters);
        for (Constants.SCANNER_TYPE type : Constants.SCANNER_TYPE.values()) {
            if (scanners.containsKey(type))
                scanners.get(type).addStepMonitor(stepMonitor);
        }
    }

    /**
     *
     * @return
     */
    public boolean markScannerTargetReached() {
        boolean finalized = true;
        for (Constants.SCANNER_TYPE type : Constants.SCANNER_TYPE.values()) {
            if (scanners.containsKey(type)) {
                boolean scanFinalized = scanners.get(type).markScannerTargetReached();
                if (!scanFinalized) finalized = false;
            }
        }
        return finalized;
    }

    /**
     *
     * @return
     */
    public String finalizeScanners() {
        String out = "";
        for (Constants.SCANNER_TYPE type : Constants.SCANNER_TYPE.values()) {
            if (scanners.containsKey(type)) {
                out += scanners.get(type).finalizeScanner() + "\n";
            }
        }
        return out;
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      PUBLIC UTILITY METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     * @param type
     * @param listener
     */
    public void addTrackerListener(Constants.TRACKER_TYPE type, ITrackerListener listener) {
        ITracker tracker = getTracker(type);
        tracker.addListener(listener);
    }

    /**
     *
     * @param type
     */
    public void clearTrackerListeners(Constants.TRACKER_TYPE type) {
        if (trackers.containsKey(type)) {
            trackers.get(type).removeListeners();
        }
    }

    /**
     *
     */
    public void clearTrackerListeners() {
        for (Constants.TRACKER_TYPE type : Constants.TRACKER_TYPE.values()) {
            clearTrackerListeners(type);
        }
    }


    /**
     *
     * @param type
     * @param listener
     */
    public void addScannerListener(Constants.SCANNER_TYPE type, IScannerListener listener) {
        IScanner scanner = getScanner(type);
        scanner.addListener(listener);
        LogManager.info("Listener added for scanner: " + type);
    }

    /**
     *
     * @param type
     * @param listener
     */
    public void removeScannerListener(Constants.SCANNER_TYPE type, IScannerListener listener) {
        if (scanners.containsKey(type)) {
            scanners.get(type).removeListener(listener);
        }
        LogManager.info("Listener removed for scanner: " + type);
    }

    /**
     *
     * @param type
     */
    public void clearScannerListeners(Constants.SCANNER_TYPE type) {
        if (scanners.containsKey(type)) {
            scanners.get(type).removeListeners();
        }
        LogManager.info("Listeners cleared for scanner: " + type);
    }

    /**
     *
     */
    public void clearScannerListeners() {
        for (Constants.SCANNER_TYPE type : Constants.SCANNER_TYPE.values()) {
            clearScannerListeners(type);
        }
    }

    /**
     *
     * @param areaName
     */
    public void mapArea(Constants.SCANNER_TYPE type, String areaName, boolean appendToConfig) {
        LogManager.info("Mapping area: " + areaName + " with scan type: " + type);
        LogManager.info("Appending area map to config: " + appendToConfig);

        IScanner scanner = getScanner(type);
        scanner.mapArea(areaName, appendToConfig);
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      PRIVATE REFERENCE METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     * @param type
     * @return
     */
    private ITracker getTracker(Constants.TRACKER_TYPE type) {
        if (!trackers.containsKey(type)) {
            ITracker tracker;
            switch (type) {
                case Interval:
                    tracker = new IntervalTracker();
                    break;
                case Light:
                    tracker = new LightTracker();
                    break;
                case Screen:
                    tracker = new LightTracker();
                    break;
                case Time:
                    tracker = new ClockTracker();
                    break;
                default:
                    tracker = null;
                    break;
            }

            trackers.put(type, tracker);
        }
        return trackers.get(type);
    }

    /**
     *
     * @param type
     * @return
     */
    private IScanner getScanner(Constants.SCANNER_TYPE type) {
        if (!scanners.containsKey(type)) {
            IScanner scanner;
            switch (type) {
                case GPS:
                    scanner = new LocationScanner(currentActivity);
                    break;
                case Wifi:
                    scanner = new SignalScanner(currentActivity);
                    break;
                default:
                    scanner = null;
                    break;
            }

            scanners.put(type, scanner);
        }
        return scanners.get(type);
    }

}