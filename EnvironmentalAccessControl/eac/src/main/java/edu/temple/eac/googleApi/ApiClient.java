package edu.temple.eac.googleApi;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import edu.temple.eac.utils.Constants;
import edu.temple.eac.utils.DialogManager;
import edu.temple.eac.utils.LogManager;

public class ApiClient implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient gac;
    private IApiListener listener;
    private Activity currentActivity;

    public ApiClient(Activity currentActivity, Api api) {
        this.currentActivity = currentActivity;
        gac = new GoogleApiClient.Builder(currentActivity)
                .addApi(api)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      GOOGLE API CLIENT METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        String message = "Google services API is now live";
        DialogManager.hide(currentActivity, message);
        listener.onConnectionAcquired(message);
        LogManager.info(message);
    }

    @Override
    public void onConnectionSuspended(int connection) {
        String message = "Google services API is no longer active";
        DialogManager.hide(currentActivity, message);
        listener.onConnectionLost(message);
        LogManager.info(message);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        String message = "Unable to connect to Google services API";
        DialogManager.hide(currentActivity, message);
        listener.onConnectionLost(message);
        LogManager.info(message);
    }

    public void connect(IApiListener listener) {
        DialogManager.show(currentActivity,
                Constants.DIALOG_API_TITLE, Constants.DIALOG_API_MESSAGE);
        this.listener = listener;
        gac.connect();
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      LOCATION SPECIFIC METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    public void requestUpdates(LocationRequest request, LocationListener listener) {
        (new LocationUpdateTask(gac, request, listener)).execute();
    }

    public void removeUpdates(LocationListener listener) {
        LocationServices.FusedLocationApi.removeLocationUpdates(gac, listener);
    }

    public static LocationRequest generateLocationRequest(int interval) {
        LocationRequest gmsRequest = new LocationRequest();
        gmsRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        gmsRequest.setMaxWaitTime((long)(interval * 1.5));
        gmsRequest.setFastestInterval(interval);
        gmsRequest.setInterval(interval);
        return gmsRequest;
    }

    private class LocationUpdateTask extends AsyncTask<Void, Void, Void>
    {
        private GoogleApiClient gac;
        private LocationRequest request;
        private LocationListener listener;


        public LocationUpdateTask (GoogleApiClient gac, LocationRequest request,
                                   LocationListener listener) {
            this.gac = gac;
            this.request = request;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            LocationServices.FusedLocationApi.removeLocationUpdates(gac, listener);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                LogManager.info("Previous location updates removed for listener: " + listener.getClass().toString());
                LogManager.info("Requesting new location updates at interval: " + request.getInterval());
                LocationServices.FusedLocationApi.requestLocationUpdates(gac, request, listener);
            } catch (SecurityException ex) {
                LogManager.error("Could not request location updates: " + ex.getMessage());
            }
        }
    }

}