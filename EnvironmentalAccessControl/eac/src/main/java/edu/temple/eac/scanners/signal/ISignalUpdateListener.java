package edu.temple.eac.scanners.signal;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 *
 */
public interface ISignalUpdateListener {

    public void getUpdateResults(List<ScanResult> scanResults);

}