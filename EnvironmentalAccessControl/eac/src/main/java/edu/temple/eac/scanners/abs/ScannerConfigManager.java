package edu.temple.eac.scanners.abs;

import android.app.Activity;

import edu.temple.eac.scanners.ScannerConfig;
import edu.temple.eac.scanners.StepMonitor;

/**
 *
 */
public abstract class ScannerConfigManager<T,U> {

    /**
     *
     */
    public abstract void readConfigs(ScannerConfig config);

    /**
     *
     * @param areaName
     * @param appendToConfig
     * @param scans
     */
    public abstract void writeLocationConfig(String areaName, boolean appendToConfig, T scans);

    /**
     *
     * @param scans
     * @return
     */
    public abstract boolean isMatch(T scans);

    /**
     *
     * @param currentActivity
     * @param monitor
     */
    public abstract void addStepMonitor(Activity currentActivity, StepMonitor monitor);

    /**
     *
     * @param scans
     * @return
     */
    public abstract double getDistanceFromClosestSource(T scans);

    /**
     *
     * @param inputs
     * @return
     */
    public abstract double getWalkingSpeed(double speedThreshold, U inputs);

}