package edu.temple.eac.scanners.abs;

import java.util.ArrayList;
import java.util.List;

import edu.temple.eac.utils.Constants;
import edu.temple.eac.utils.LogManager;

/**
 *
 */
public abstract class ScanRateUpdater<T> {

    private static final int INTERVAL_SCAN_RATE = 15000;    // 15sec in milliseconds
    private static final int CONSTANT_SCAN_RATE = 1;        // so we don't get a "divide by zero" error
    private Constants.SCAN_TYPE scanType;

    protected List<T> listeners = new ArrayList<>();
    private boolean currentlyScanning = false;
    private double scanRateMod = 1.0;

    /**
     *
     * @return
     */
    public int getDefaultScanRate() {
        return ((int) (CONSTANT_SCAN_RATE * scanRateMod));
    }

    /**
     *
     * @param listener
     * @param scanType
     * @return
     */

    public int getRegularUpdates(T listener, Constants.SCAN_TYPE scanType) {
        this.scanType = scanType;
        int scanInterval = (scanType == Constants.SCAN_TYPE.Interval)
                ? INTERVAL_SCAN_RATE : CONSTANT_SCAN_RATE;

        addListener(listener);
        requestListenerUpdates(listener, scanInterval);
        LogManager.info("Scanning for locations at new interval: " + scanInterval);
        return scanInterval;
    }

    /**
     *
     * @param listener
     * @param scanInterval
     * @param newScanRateMod
     * @return
     */
    public int getVariableUpdates(T listener, int scanInterval, double newScanRateMod) {
        int newScanInterval = (int) (scanInterval * newScanRateMod);
        if (!currentlyScanning || newScanRateMod != scanRateMod) {
            LogManager.info("Applying modifier: " + newScanRateMod + " to scanning interval: " + scanInterval);
            LogManager.info("Scanning for locations at new interval: " + newScanInterval);

            requestListenerUpdates(listener, newScanInterval);
            scanRateMod = newScanRateMod;
            currentlyScanning = true;
            return newScanInterval;
        } else if (newScanRateMod == scanRateMod) {
            LogManager.info("New scan rate modifier same as old... No changes made.");
            LogManager.info("Scanning for locations at interval: " + newScanInterval);
            return 0;
        } else {
            LogManager.error("Attempting to get variable updates for invalid conditions");
            return 0;
        }
    }

    /**
     *
     * @param listener
     * @return
     */
    public void removeUpdates(T listener) {
        removeListener(listener);
        removeListenerUpdates(listener);
        if (this.listeners.size() == 0) currentlyScanning = false;
    }

    /**
     *
     * @param listener
     */
    public void removeListener(T listener) {
        if (this.listeners.contains(listener)) this.listeners.remove(listener);
    }

    /**
     *
     * @param listener
     */
    public void addListener(T listener) {
        if (!this.listeners.contains(listener)) this.listeners.add(listener);
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      ABSTRACT METHODS TO BE DEFINED BY INHERITING CLASSES
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    protected abstract void requestListenerUpdates(T listener, int scanInterval);

    protected abstract void removeListenerUpdates(T listener);

}