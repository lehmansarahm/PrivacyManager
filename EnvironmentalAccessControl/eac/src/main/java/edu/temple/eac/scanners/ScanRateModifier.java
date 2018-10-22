package edu.temple.eac.scanners;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.temple.eac.scanners.location.LocationScan;
import edu.temple.eac.utils.Constants;
import edu.temple.eac.utils.LogManager;
import edu.temple.eac.utils.StorageManager;

/**
 *
 */
public class ScanRateModifier {

    /**
     *
     * @param distance
     * @param distanceThreshold
     * @param speed
     * @param speedThreshold
     * @param growthRateClose
     * @param growthRateFast
     * @param growthRateSlow
     * @return
     */
    public static double generate(double distance, double distanceThreshold,
                                  double speed, double speedThreshold,
                                  double growthRateClose, double growthRateFast, double growthRateSlow) {
        LogManager.info("Generating new scan rate modifier for distance: " + distance + " and speed: " + speed);
        LogManager.info("Current thresholds for distance: " + distanceThreshold + " and speed: " + speedThreshold);

        double distanceMod, speedMod, scanRateMod;
        if (distance <= distanceThreshold)
            distanceMod = Math.pow((distance/distanceThreshold), growthRateClose);
        else distanceMod = Math.floor(distance/distanceThreshold);
        LogManager.info("Calculated distance modifier: " + distanceMod);

        if (speed == 0.0) speedMod = 1;
        else if (speed >= speedThreshold)
            speedMod = 1.0 / Math.pow((speed/speedThreshold), growthRateFast);
        else speedMod = 1.0 / Math.pow((speed/speedThreshold), growthRateSlow);
        LogManager.info("Calculated speed modifier: " + speedMod);

        if (distance <= distanceThreshold)
            scanRateMod = Math.min(distanceMod, speedMod);
        else scanRateMod = Math.max(distanceMod, speedMod);
        return scanRateMod;
    }

    /**
     *
     * @param firstDate
     * @param lastDate
     * @return
     */
    public static double getElapsedTime(Date firstDate, Date lastDate) {
        double elapsedTimeInSec = ((double)Math.abs(lastDate.getTime() - firstDate.getTime()) / 1000.0d);
        return elapsedTimeInSec;
    }

    /**
     *
     * @param scans
     * @param scanTimes
     * @param logFileName
     */
    public static void writeToLogCSV(List<String> scans, List<Date> scanTimes, String logFileName) {
        if (scanTimes.size() == 4) {
            // Extract necessary scan times
            Date firstScan = scanTimes.get(0);
            Date lastScan = scanTimes.get(1);
            Date stopScan = scanTimes.get(2);
            Date matchScan = scanTimes.get(3);

            // Copy the scan results so we can modify them if necessary
            List<String> scanResults = new ArrayList<>(scans);

            int numberOfScans = scanResults.size();
            double totalElapsedTimeInSec = getElapsedTime(firstScan, lastScan);
            double avgScanRate = ((double) numberOfScans / totalElapsedTimeInSec);

            String content = "Total scans collected," + numberOfScans;
            content += "\nTotal time elapsed (seconds)," + totalElapsedTimeInSec;
            content += "\nAverage scan rate," + avgScanRate + "\n";

            content += "\nUser stopped scanning at," + StorageManager.getLongFormattedDate(stopScan);
            if (matchScan != null) {
                double finalizationTimeDiffInSec = getElapsedTime(stopScan, matchScan);
                content += "\nLocation match found at time," + StorageManager.getLongFormattedDate(matchScan);
                content += "\nFinalization time difference (seconds)," + finalizationTimeDiffInSec;
            }
            content += "\n\n";

            content += getScanLogHeader();
            for (String result : scanResults) {
                content += result + "\n";
            }

            // Now we write our scan session data to the log file...
            LogManager.info("Writing to log file: " + logFileName);
            StorageManager.writeToFile(Constants.APP_FOLDERS.LOGS, logFileName, content, false);
        } else LogManager.error("Insufficient scan dates for log: " + logFileName);
    }

    /**
     *
     * @param scanResult
     * @param timeSinceLastScan
     * @param scanInterval
     * @param newScanRateMod
     * @param distance
     * @return
     */
    public static String getScanLogEntry(LocationScan scanResult, int timeSinceLastScan,
                                         int scanInterval, double newScanRateMod,
                                         double distance) {
        int newScanRate = (int) (scanInterval * newScanRateMod);
        double projScansPerSec = (1000.0 / ((double) newScanRate));
        double projScanInterval = (1000.0 / projScansPerSec);
        double scansPerSec = (timeSinceLastScan != 0) ? (1000.0 / ((double) timeSinceLastScan)) : 0;

        String scanResultOutput = scanResult.toString() // timestamp, lat, long
                + "," + distance                        // distance to closest target (feet)
                + "," + projScanInterval                // projected scan interval
                + "," + projScansPerSec                 // projected scans per second
                + "," + timeSinceLastScan               // actual scan interval
                + "," + scansPerSec;                    // actual scans per second
        return scanResultOutput;
    }

    /**
     *
     * @return
     */
    public static String getScanLogHeader() {
        return
                "Timestamp,Latitude,Longitude,Distance from Closest Target,"
                + "Projected Scan Interval (ms),Projected Scans Per Second,"
                + "Actual Scan Interval (ms),Actual Scans Per Second\n";
    }

}