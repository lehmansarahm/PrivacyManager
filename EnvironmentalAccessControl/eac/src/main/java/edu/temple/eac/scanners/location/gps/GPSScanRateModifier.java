package edu.temple.eac.scanners.location.gps;

import android.app.Activity;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;

import edu.temple.eac.scanners.abs.ScanRateUpdater;
import edu.temple.eac.utils.LogManager;

/**
 *
 */
public class GPSScanRateModifier extends ScanRateUpdater<LocationListener> {

    private LocationManager lm;

    /**
     *
     * @param currentActivity
     */
    public GPSScanRateModifier(Activity currentActivity) {
        lm = (LocationManager) currentActivity.getSystemService(Context.LOCATION_SERVICE);
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      SCAN RATE UPDATER METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    protected void requestListenerUpdates(LocationListener listener, int interval) {
        (new LocationUpdateTask(lm, listener, interval)).execute();
    }

    protected void removeListenerUpdates(LocationListener listener) {
        lm.removeUpdates(listener);
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      PRIVATE REFERENCE CLASS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    private class LocationUpdateTask extends AsyncTask<Void, Void, Void>
    {
        private LocationManager lm;
        private LocationListener listener;
        private int requestInterval;

        public LocationUpdateTask (LocationManager lm, LocationListener listener, int interval) {
            this.lm = lm;
            this.requestInterval = interval;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            lm.removeUpdates(listener);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                LogManager.info("Previous location updates removed for listener: "
                        + listener.getClass().toString());
                LogManager.info("Requesting new location updates at interval: "
                        + requestInterval + "\n\n");
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, requestInterval, 0, listener);
            } catch (SecurityException ex) {
                LogManager.error("Could not acquire location updates for listener: "
                        + listener.getClass().toString());
                LogManager.error("Error: " + ex.getMessage());
            }
        }
    }

}