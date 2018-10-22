package edu.temple.eac.scanners;

import edu.temple.eac.utils.Constants;

/**
 *
 */
public interface IScanner {

    /**
     * Allows env. access controller to initialize all scanners
     * @param scanType
     */
    void initialize(Constants.SCAN_TYPE scanType);

    /**
     *
     * @return
     */
    boolean markScannerTargetReached();

    /**
     *
     * @return
     */
    String finalizeScanner();

    /**
     *
     * @return
     */
    boolean hasListeners();

    /**
     * Imports subscribing listener
     *
     * @param listener - the new listener subscribing to this scanner
     */
    void addListener(IScannerListener listener);

    /**
     *
     * @param listener
     */
    void removeListener(IScannerListener listener);

    /**
     * Clears the list of listeners for the given scanner
     */
    void removeListeners();

    /**
     *
     * @param areaName
     * @param appendToConfig
     */
    void mapArea(String areaName, boolean appendToConfig);

    /**
     *
     * @param monitor
     */
    void addStepMonitor(StepMonitor monitor);

}