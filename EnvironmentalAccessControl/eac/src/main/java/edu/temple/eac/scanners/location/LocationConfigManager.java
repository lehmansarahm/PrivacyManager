package edu.temple.eac.scanners.location;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.temple.eac.scanners.ScannerConfig;
import edu.temple.eac.scanners.StepMonitor;
import edu.temple.eac.scanners.abs.ScannerConfigManager;
import edu.temple.eac.utils.Constants;
import edu.temple.eac.utils.LogManager;
import edu.temple.eac.utils.PropertyReader;
import edu.temple.eac.utils.StorageManager;

/**
 *
 */
public class LocationConfigManager extends ScannerConfigManager<List<Double>, List<Double>> {

    private static final String CONFIG_FILE = "config_file.gps";
    private static final String CONFIG_FILE_DEFAULT = "gps-scanner.txt";
    public static String CONFIG_FILE_NAME = PropertyReader.getProperty(CONFIG_FILE, CONFIG_FILE_DEFAULT);

    private static final String LOCATION_FILE = "location_file.gps";
    private static final String LOCATION_FILE_DEFAULT = "gps-locations.txt";
    public static String LOCATION_FILE_NAME = PropertyReader.getProperty(LOCATION_FILE, LOCATION_FILE_DEFAULT);

    private static final String DISTANCE_THRESHOLD = "gps_config.distance_threshold";
    private static final String DISTANCE_THRESHOLD_DEFAULT = "DistanceThreshold";
    private static String DISTANCE_THRESHOLD_HEADER = PropertyReader.getProperty(DISTANCE_THRESHOLD, DISTANCE_THRESHOLD_DEFAULT);

    private static final String SPEED_THRESHOLD = "gps_config.speed_threshold";
    private static final String SPEED_THRESHOLD_DEFAULT = "SpeedThreshold";
    private static String SPEED_THRESHOLD_HEADER = PropertyReader.getProperty(SPEED_THRESHOLD, SPEED_THRESHOLD_DEFAULT);

    private static final String DEFAULT_SCAN_INTERVAL = "gps_config.default_scan_interval";
    private static final String DEFAULT_SCAN_INTERVAL_DEFAULT = "DefaultScanInterval";
    private static String SCAN_INTERVAL_HEADER = PropertyReader.getProperty(DEFAULT_SCAN_INTERVAL, DEFAULT_SCAN_INTERVAL_DEFAULT);

    private static final String GR_CLOSE = "gps_config.growth_rate_close";
    private static final String GR_CLOSE_DEFAULT = "GrowthRateClose";
    private static String GROWTH_RATE_CLOSE_HEADER = PropertyReader.getProperty(GR_CLOSE, GR_CLOSE_DEFAULT);

    private static final String GR_FAST = "gps_config.growth_rate_fast";
    private static final String GR_FAST_DEFAULT = "GrowthRateFast";
    private static String GROWTH_RATE_FAST_HEADER = PropertyReader.getProperty(GR_FAST, GR_FAST_DEFAULT);

    private static final String GR_SLOW = "gps_config.growth_rate_slow";
    private static final String GR_SLOW_DEFAULT = "GrowthRateSlow";
    private static String GROWTH_RATE_SLOW_HEADER = PropertyReader.getProperty(GR_SLOW, GR_SLOW_DEFAULT);

    private static final String LOCATION = "gps_config.location";
    private static final String LOCATION_DEFAULT = "Location";
    private static String LOCATION_HEADER = PropertyReader.getProperty(LOCATION, LOCATION_DEFAULT);

    private static final String LATITUDE = "gps_config.latitude";
    private static final String LATITUDE_DEFAULT = "Latitude";
    private static String LATITUDE_HEADER = PropertyReader.getProperty(LATITUDE, LATITUDE_DEFAULT);

    private static final String LONGITUDE = "gps_config.longitude";
    private static final String LONGITUDE_DEFAULT = "Longitude";
    private static String LONGITUDE_HEADER = PropertyReader.getProperty(LONGITUDE, LONGITUDE_DEFAULT);

    private static List<LocationScan> scans = new ArrayList<>();
    private static double[] lastCoords;
    private static Date lastScan;
    private StepMonitor stepMonitor;

    /**
     *
     */
    @Override
    public void readConfigs(ScannerConfig sc) {
        List<String> configContents =
                StorageManager.readFile(Constants.APP_FOLDERS.CONFIG, CONFIG_FILE_NAME);

        for (String config : configContents) {
            String[] contents = config.split(":");
            String header = contents[0];

            // have to use if-else block since header string is not constant
            if (header.equals(DISTANCE_THRESHOLD_HEADER)) {
                double value = Double.parseDouble(contents[1]);
                sc.setDistanceThreshold(value);
            } else if (header.equals(SPEED_THRESHOLD_HEADER)) {
                double value = Double.parseDouble(contents[1]);
                sc.setSpeedThreshold(value);
            } else if (header.equals(SCAN_INTERVAL_HEADER)) {
                int value = Integer.parseInt(contents[1]);
                sc.setDefaultScanInterval(value);
            } else if (header.equals(GROWTH_RATE_CLOSE_HEADER)) {
                double value = Double.parseDouble(contents[1]);
                sc.setGrowthRateClose(value);
            } else if (header.equals(GROWTH_RATE_FAST_HEADER)) {
                double value = Double.parseDouble(contents[1]);
                sc.setGrowthRateFast(value);
            } else if (header.equals(GROWTH_RATE_SLOW_HEADER)) {
                double value = Double.parseDouble(contents[1]);
                sc.setGrowthRateSlow(value);
            }
        }

        List<String> locationContents =
                StorageManager.readFile(Constants.APP_FOLDERS.CONFIG, LOCATION_FILE_NAME);
        for (String location: locationContents) {
            String area = "";
            double lat = 0.0, lon = 0.0;

            String[] locationLine = location.split(",");
            for (String contentsLine : locationLine) {
                // Parse out the stuff we need
                String[] contents = contentsLine.split(":");
                String header = contents[0];

                // have to use if-else block since header string is not constant
                if (header.equals(LOCATION_HEADER)) area = contents[1];
                else if (header.equals(LATITUDE_HEADER)) lat = Double.parseDouble(contents[1]);
                else if (header.equals(LONGITUDE_HEADER)) lon = Double.parseDouble(contents[1]);
            }

            if (!area.equals("") && lat != 0.0 && lon != 0.0) addScan(area, lat, lon);
        }
    }

    /**
     *
     * @param areaName
     * @param appendToConfig
     * @param coords
     */
    @Override
    public void writeLocationConfig(String areaName, boolean appendToConfig, List<Double> coords) {
        double lat = coords.get(0);
        double lon = coords.get(1);

        addScan(areaName, lat, lon, true);
        String content = LOCATION_HEADER + ":" + areaName + ","
                + LATITUDE_HEADER + ":" + lat + ","
                + LONGITUDE_HEADER + ":" + lon + "\n";

        boolean overwrite = !appendToConfig;
        StorageManager.writeToFile(Constants.APP_FOLDERS.CONFIG, LOCATION_FILE_NAME, content, overwrite);
    }

    /**
     *
     * @param coords
     * @return
     */
    @Override
    public boolean isMatch(List<Double> coords) {
        boolean matchFound = false;
        for (LocationScan scan : scans) {
            if (scan.isMatch(coords.get(0), coords.get(1))) {
                matchFound = true;
                break;
            }
        }
        return matchFound;
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      SCAN RATE MODIFIER METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     * @param monitor
     */
    public void addStepMonitor(Activity currentActivity, StepMonitor monitor) {
        SensorManager sm = (SensorManager) currentActivity.getSystemService(Context.SENSOR_SERVICE);
        if (sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            Sensor stepCounter = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            sm.registerListener(monitor, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
            stepMonitor = monitor;
        } else LogManager.error("Could not connect to step counter sensor");
    }

    /**
     *
     * @param coords
     * @return
     */
    @Override
    public double getDistanceFromClosestSource(List<Double> coords) {
        double lat = coords.get(0);
        double lon = coords.get(1);
        LogManager.info("Searching " + scans.size() +
                " scans for a match with current position: " + lat + ", " + lon);

        double distanceToClosestSource = Float.MAX_VALUE;
        for (LocationScan scan : scans) {
            LogManager.info("Comparing current position against scan: " + scan);
            double distanceToSource = scan.getDistance(lat, lon);
            if (distanceToSource < distanceToClosestSource)
                distanceToClosestSource = distanceToSource;
        }
        return distanceToClosestSource;
    }

    /**
     *
     * @return
     */
    @Override
    public double getWalkingSpeed(double speedThreshold, List<Double> coords) {
        double walkingSpeed;
        //if (stepMonitor != null) {
        //    LogManager.error("Retrieving walking speed from step stepMonitor");
        //    walkingSpeed = stepMonitor.getWalkingSpeed();
        //} else {
            LogManager.info("Calculating walking speed from last GPS coordinates");

            double lat = coords.get(0);
            double lon = coords.get(1);
            Date currentScan = new Date();

            if (lastScan != null && lastCoords != null) {
                double distanceFromLastPoint = LocationScan.getDistance(lastCoords[0], lastCoords[1], lat, lon);
                double elapsedTimeInSec = (currentScan.getTime() - lastScan.getTime()) / 1000.0;
                walkingSpeed = (distanceFromLastPoint / elapsedTimeInSec);
            } else {
                walkingSpeed = speedThreshold + 1;
            }

            lastScan = currentScan;
            lastCoords = new double[]{lat, lon};
        //}
        return walkingSpeed;
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      PRIVATE REFERENCE METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     * @param areaName
     * @param lat
     * @param lon
     */
    private static void addScan(String areaName, double lat, double lon) {
        addScan(areaName, lat, lon, false);
    }

    /**
     *
     * @param areaName
     * @param lat
     * @param lon
     * @param reset
     */
    private static void addScan(String areaName, double lat, double lon, boolean reset) {
        if (reset) scans = new ArrayList<>();
        scans.add(new LocationScan(areaName,lat,lon));
    }

}