package edu.temple.eac.scanners;

public class ScannerConfig {

    private double distanceThreshold;
    private double speedThreshold;

    private int defaultScanInterval;
    private int scanInterval;

    private double growthRateClose;
    private double growthRateFast;
    private double growthRateSlow;

    /**
     *
     * @param dt
     */
    public void setDistanceThreshold(double dt) {
        distanceThreshold = dt;
    }

    /**
     *
     * @return
     */
    public double getDistanceThreshold() {
        return distanceThreshold;
    }

    /**
     *
     * @param st
     */
    public void setSpeedThreshold(double st) {
        speedThreshold = st;
    }

    /**
     *
     * @return
     */
    public double getSpeedThreshold() {
        return speedThreshold;
    }

    /**
     *
     * @param dsi
     */
    public void setDefaultScanInterval(int dsi) {
        defaultScanInterval = dsi;
    }

    /**
     *
     * @return
     */
    public int getDefaultScanInterval() {
        return defaultScanInterval;
    }

    /**
     *
     * @param si
     */
    public void setScanInterval(int si) {
        scanInterval = si;
    }

    /**
     *
     * @return
     */
    public int getScanInterval() {
        return scanInterval;
    }

    /**
     *
     * @param grc
     */
    public void setGrowthRateClose(double grc) {
        growthRateClose = grc;
    }

    /**
     *
     * @return
     */
    public double getGrowthRateClose() {
        return growthRateClose;
    }

    /**
     *
     * @param grf
     */
    public void setGrowthRateFast(double grf) {
        growthRateFast = grf;
    }

    /**
     *
     * @return
     */
    public double getGrowthRateFast() {
        return growthRateFast;
    }

    /**
     *
     * @param grs
     */
    public void setGrowthRateSlow(double grs) {
        growthRateSlow = grs;
    }

    /**
     *
     * @return
     */
    public double getGrowthRateSlow() {
        return growthRateSlow;
    }

}