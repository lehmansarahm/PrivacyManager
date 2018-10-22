package edu.temple.eac.scanners;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.Date;

import edu.temple.eac.utils.LogManager;

/**
 *
 */
public class StepMonitor implements SensorEventListener {

    private double strideLength = 0.762; // meters
    private Date lastScan;
    private double walkingSpeed;

    /**
     *
     * @param userHeightMeters
     */
    public StepMonitor(double userHeightMeters) {
        strideLength = 0.45 * userHeightMeters;
        lastScan = new Date();
    }

    /**
     *
     * @param sensor
     * @param accuracy
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do something
    }

    /**
     *
     * @param event
     */
    public void onSensorChanged(SensorEvent event) {
        // we took a new step!  recalculate walking speed
        Date currentScan = new Date();
        double elapsedTimeInSec = (currentScan.getTime() - lastScan.getTime()) / 1000.0;

        walkingSpeed = (strideLength / elapsedTimeInSec);
        lastScan = currentScan;
    }

    /**
     *
     * @return
     */
    public double getWalkingSpeed() {
        LogManager.info("Using Step Monitor walking speed");
        if (!isUserMoving()) walkingSpeed = 0;
        return this.walkingSpeed;
    }

    /**
     *
     * @param heightInInches
     * @return
     */
    public static double convertInchesToMeters(int heightInInches) {
        double heightInMeters = heightInInches * 0.0254d;
        return heightInMeters;
    }

    /**
     *
     * @return
     */
    private boolean isUserMoving() {
        // if it's been more than 2sec since we got our last step event, we have paused
        boolean isUserMoving = (((new Date().getTime() - lastScan.getTime()) / 1000) <= 2);
        return isUserMoving;
    }

}