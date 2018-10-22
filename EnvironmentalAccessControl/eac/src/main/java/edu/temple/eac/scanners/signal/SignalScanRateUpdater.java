package edu.temple.eac.scanners.signal;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.temple.eac.scanners.abs.ScanRateUpdater;

/**
 *
 */
public class SignalScanRateUpdater extends ScanRateUpdater<ISignalUpdateListener> {

    private WifiManager wm;

    private Timer regularTimer;
    private Updater regularUpdater;
    private boolean regularUpdateScheduled = false;

    private Timer variableTimer;
    private Updater variableUpdater;
    private boolean variableUpdateScheduled = false;

    public SignalScanRateUpdater(Activity currentActivity) {
        wm = (WifiManager) currentActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        regularTimer = new Timer();
        regularUpdater = new Updater(wm);

        variableTimer = new Timer();
        variableUpdater = new Updater(wm);
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      SCAN RATE UPDATER METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    protected void requestListenerUpdates(ISignalUpdateListener listener, int scanInterval) {
        if (!regularUpdateScheduled) {
            regularTimer.schedule(regularUpdater, 0, scanInterval);
            regularUpdateScheduled = true;
        }
    }

    protected void removeListenerUpdates(ISignalUpdateListener listener) {
        regularUpdater.removeListener(listener);
        variableUpdater.removeListener(listener);

        if (!variableUpdater.hasListeners()) variableUpdateScheduled = false;
        else {
            int scanRate = getDefaultScanRate();
            variableTimer.schedule(variableUpdater, 0, scanRate);
        }
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      PRIVATE NESTED CLASS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    class Updater extends TimerTask {
        private List<ISignalUpdateListener> listeners;
        private WifiManager wm;

        public Updater(WifiManager wm) {
            listeners = new ArrayList<>();
            this.wm = wm;
        }

        public boolean hasListeners() {
            return (!this.listeners.isEmpty());
        }

        public void addListener(ISignalUpdateListener listener) { this.listeners.add(listener); }

        public void removeListener(ISignalUpdateListener listener) {
            if (this.listeners.contains(listener)) this.listeners.remove(listener);
        }

        public void removeListeners() {
            this.listeners.clear();
        }

        public void run() {
            wm.startScan();
            List<ScanResult> scanResults = wm.getScanResults();
            for (ISignalUpdateListener listener : listeners) {
                listener.getUpdateResults(scanResults);
            }
        }
    }

}