package edu.temple.eac.scanners.location.api;

import android.app.Activity;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import edu.temple.eac.googleApi.ApiClient;
import edu.temple.eac.scanners.abs.ScanRateUpdater;

/**
 *
 */
public class ApiScanRateModifier extends ScanRateUpdater<LocationListener> {

    private ApiClient apiClient;

    /**
     *
     * @param currentActivity
     */
    public ApiScanRateModifier(Activity currentActivity) {
        apiClient = new ApiClient(currentActivity, LocationServices.API);
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      SCAN RATE UPDATER METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    protected void requestListenerUpdates(LocationListener listener, int interval) {
        LocationRequest request = ApiClient.generateLocationRequest(interval);
        apiClient.requestUpdates(request, listener);
    }

    protected void removeListenerUpdates(LocationListener listener) {
        apiClient.removeUpdates(listener);
    }

}