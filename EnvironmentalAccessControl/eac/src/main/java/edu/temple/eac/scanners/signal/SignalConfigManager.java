package edu.temple.eac.scanners.signal;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.List;

import edu.temple.eac.scanners.ScannerConfig;
import edu.temple.eac.scanners.StepMonitor;
import edu.temple.eac.scanners.abs.ScannerConfigManager;
import edu.temple.eac.utils.Constants;
import edu.temple.eac.utils.LogManager;
import edu.temple.eac.utils.StorageManager;

/**
 *
 */

public class SignalConfigManager extends ScannerConfigManager<List<ScanResult>,Object> {

    public static final String CONFIG_FILE_NAME = "wifi-scanner.txt";
    public static final String LOCATION_FILE_NAME = "wifi-locations.txt";

    private static final String LOCATION_HEADER = "Location:";
    private static final String BSSID_HEADER = "BSSID:";
    private static final String LEVEL_HEADER = "Level:";
    private static final String FREQUENCY_HEADER = "Frequency:";

    private String areaName;
    private List<Signal> signalScans;

    private StepMonitor monitor;

    /**
     *
     */
    @Override
    public void readConfigs(ScannerConfig config) {
        List<String> configContents = StorageManager.readFile(Constants.APP_FOLDERS.CONFIG, LOCATION_FILE_NAME);
        String area = "";
        List<Signal> scans = new ArrayList<>();

        for (String contentsLine : configContents) {
            String[] contents = contentsLine.split(":");
            String header = contents[0] + ":";

            switch (header) {
                case LOCATION_HEADER:
                    area = contents[1];
                    break;
                case BSSID_HEADER:
                    if (contents.length == 6) {
                        int level = Integer.parseInt(contents[3]);
                        int frequency = Integer.parseInt(contents[5]);
                        scans.add(new Signal(contents[1], level, frequency));
                    }
                    break;
                default:
                    break;
            }
        }

        if (!area.equals("") && scans.size() > 0) {
            this.areaName = area;
            this.signalScans = scans;
        }
    }

    /**
     *
     * @param areaName
     * @param appendToConfig
     * @param scans
     */
    @Override
    public void writeLocationConfig(String areaName, boolean appendToConfig, List<ScanResult> scans) {
        LogManager.info("Ready to write config for area: " + areaName +
                " with " + scans.size() + " scans");
        if (!areaName.equals("") && scans.size() > 0) {
            this.areaName = areaName;
            this.signalScans = new ArrayList<>();

            String content = LOCATION_HEADER + areaName + "\n";
            for (ScanResult scan : scans) {
                this.signalScans.add(new Signal(scan.BSSID, scan.level, scan.frequency));
                content += BSSID_HEADER + scan.BSSID + ","
                        + LEVEL_HEADER + scan.level + ","
                        + FREQUENCY_HEADER + scan.frequency + "\n";
            }

            boolean overwrite = !appendToConfig;
            StorageManager.writeToFile(Constants.APP_FOLDERS.CONFIG, LOCATION_FILE_NAME, content, overwrite);
        }
    }

    /**
     *
     * @param scans
     * @return
     */
    @Override
    public boolean isMatch(List<ScanResult> scans) {
        // TODO - implement
        return false;
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
        Sensor stepCounter = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        System.out.println("Attempting to register listener: " + monitor + " to sensor: " + stepCounter);
        sm.registerListener(monitor, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
        this.monitor = monitor;
    }

    /**
     *
     * @param scans
     * @return
     */
    @Override
    public double getDistanceFromClosestSource(List<ScanResult> scans) {
        // TODO - test and optimize !!
        double averageDistance = 0.0;
        int matchCounter = 0;

        for (ScanResult scan : scans) {
            for (Signal signal : this.signalScans) {
                if (scan.BSSID.equals(signal.apName)) {
                    Signal newSignal = new Signal(scan.BSSID, scan.level, scan.frequency);
                    averageDistance += newSignal.calculateDistance();
                    matchCounter++;
                }
            }
        }

        averageDistance = (averageDistance / (double) matchCounter);
        return averageDistance;
    }

    /**
     *
     * @return
     */
    @Override
    public double getWalkingSpeed(double speedThreshold, Object inputs) {
        // TODO - test and optimize !!
        double walkingSpeed = (this.monitor != null) ? this.monitor.getWalkingSpeed() : (speedThreshold + 1);
        return walkingSpeed;
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      Private internal class to represent a given signal scan
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    class Signal {
        String apName;
        int level;
        int frequency;

        /**
         *
         * @param apName
         * @param level
         * @param frequency
         */
        public Signal(String apName, int level, int frequency) {
            this.apName = apName;
            this.level = level;
            this.frequency = frequency;
        }

        /**
         * Modified from Free-Space Path Loss:
         * https://en.wikipedia.org/wiki/Free-space_path_loss#Free-space_path_loss_in_decibels
         * http://stackoverflow.com/questions/11217674/how-to-calculate-distance-from-wifi-router-using-signal-strength
         *
         * @return
         */
        public double calculateDistance() {
            double signalLevelInDb = (double) level;
            double freqInMHz = (double) frequency;
            double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
            return Math.pow(10.0, exp);
        }
    }

}