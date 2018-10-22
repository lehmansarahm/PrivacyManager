package edu.temple.eac.scanners;

import android.app.Activity;
import android.location.Location;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.temple.eac.scanners.abs.ScannerConfigManager;
import edu.temple.eac.scanners.location.LocationConfigManager;
import edu.temple.eac.scanners.location.LocationScan;
import edu.temple.eac.utils.Constants;
import edu.temple.eac.utils.DialogManager;
import edu.temple.eac.utils.LogManager;
import edu.temple.eac.utils.ToastManager;

public class ScannerBase {

    protected ScannerConfigManager scm;
    protected ScannerConfig sc;

    protected Activity currentActivity;
    protected List<IScannerListener> listeners;

    protected static String LOG_FILE_NAME = "gps_log_%s.csv";
    protected String areaName;

    protected Constants.SCANNER_STATE state = Constants.SCANNER_STATE.Idle;
    protected boolean appendToConfig = false;
    protected boolean initialFixAcquired = false;

    protected Constants.SCAN_TYPE scanType = Constants.SCAN_TYPE.Constant;

    protected List<String> scanResults;
    protected Date firstScan, lastScan, stopScan, matchScan;
    protected int scanInterval = 0;

    protected void initialize(Activity currentActivity) {
        this.currentActivity = currentActivity;
        scm = new LocationConfigManager();
        sc = new ScannerConfig();
        listeners = new ArrayList<>();
    }

    protected void processNewScan(Date newScan) {
        String newScanMessage = "Location change info for scan type: "
                + scanType + ",Received at: " + newScan;
        if (lastScan != null)
            newScanMessage += ",Ms since last scan: " + (newScan.getTime() - lastScan.getTime());
        LogManager.info(newScanMessage);
    }

    protected void processGPSFix(Date newScan) {
        if (!initialFixAcquired) {
            initialFixAcquired = !initialFixAcquired;
            firstScan = newScan;

            // inform user operation has completed
            String message = (state == Constants.SCANNER_STATE.Mapping)
                    ? Constants.DIALOG_SIGNAL_COMPLETE : Constants.DIALOG_SIGNAL_FOUND;
            DialogManager.hide(currentActivity, message);
        }
    }

    protected List<Double> processCoordinates(Location location) {
        List<Double> coords = new ArrayList<>();
        coords.add(location.getLatitude());
        coords.add(location.getLongitude());
        return coords;
    }

    protected void processMappedLocation(List<Double> coords) {
        scm.writeLocationConfig(areaName, appendToConfig, coords);
        state = Constants.SCANNER_STATE.Idle;
    }

    protected void processScanResult(Date newScan, List<Double> coords, double newScanRateMod) {
        LocationScan scanResult = new LocationScan(newScan, coords.get(0), coords.get(1));
        LogManager.info("Adding new scan results to collection: " + scanResult);

        int timeSinceLastScan = (lastScan != null) ? (int)(newScan.getTime() - lastScan.getTime()) : 0;
        String scanResultOutput = ScanRateModifier.getScanLogEntry(scanResult,
                timeSinceLastScan, sc.getScanInterval(),
                newScanRateMod, scm.getDistanceFromClosestSource(coords));
        scanResults.add(scanResultOutput);
        lastScan = newScan;
    }

    protected void processScanMatch(Date newScan, List<Double> coords) {
        if (matchScan == null && scm.isMatch(coords)) {
            matchScan = newScan;
            LogManager.info("Location match found at time: " + matchScan);
            ToastManager.showShortToast(currentActivity, "Location match found at time: " + matchScan);
        }
    }

    protected void finalizeScanSession() {
        LogManager.info("Finalizing scanning session");
        writeResultsToLog();
    }

    protected double getUpdatedScanRateMod(List<Double> coords) {
        double speedThreshold = sc.getSpeedThreshold();
        double newScanRateMod =
                ScanRateModifier.generate(scm.getDistanceFromClosestSource(coords),
                        sc.getDistanceThreshold(), scm.getWalkingSpeed(speedThreshold, coords),
                        speedThreshold, sc.getGrowthRateClose(), sc.getGrowthRateFast(), sc.getGrowthRateSlow());
        return newScanRateMod;
    }

    protected void writeResultsToLog() {
        List<Date> scanTimes = new ArrayList<>();
        scanTimes.add(firstScan);
        scanTimes.add(lastScan);
        scanTimes.add(stopScan);
        scanTimes.add(matchScan);

        LogManager.info("Finalizing scanner.  Writing " + scanResults.size() + " results to log");
        ScanRateModifier.writeToLogCSV(scanResults, scanTimes, LOG_FILE_NAME);
        LogManager.dumpLogs();

        ToastManager.showShortToast(currentActivity, "GPS Scan Complete");
        state = Constants.SCANNER_STATE.Idle;
    }

}