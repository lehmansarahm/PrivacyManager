package edu.temple.eac.trackers;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

import edu.temple.eac.utils.Constants;
import edu.temple.eac.utils.StorageManager;

/**
 *
 */
public class LightTracker implements ITracker, SensorEventListener {

    private Activity currentActivity;
    private SensorManager mSensorManager;
    private Sensor mPressure;

    private List<ITrackerListener> listeners = new ArrayList<>();
    public static String CONFIG_FILE_NAME = "light.txt";

    private static int LUMEN_THRESHOLD; // read from config
    private boolean previousThresholdMet = true;

    /**
     *
     */
    public void initialize(Activity currentActivity) {
        this.currentActivity = currentActivity;
        init();
    }

    /**
     *
     */
    public void restart() {
        mSensorManager.unregisterListener(this);
        init();
    }

    /**
     *
     * @param listener - the new listener subscribing to this tracker
     */
    public void addListener(ITrackerListener listener) {
        this.listeners.add(listener);
    }

    /**
     *
     */
    public void removeListeners() {
        this.listeners.clear();
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      PRIVATE REFERENCE METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     */
    private void init() {
        // read in the interval period from the config file
        List<String> configContents =
                StorageManager.readFile(Constants.APP_FOLDERS.CONFIG, CONFIG_FILE_NAME);

        // do nothing if config file DNE or is empty
        if (configContents != null && !configContents.isEmpty()) {
            // set lumen threshold from config
            LUMEN_THRESHOLD = Integer.parseInt(configContents.get(0));

            // register listener to watch for light sensor updates
            mSensorManager = (SensorManager) currentActivity.getSystemService(Context.SENSOR_SERVICE);
            mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      SENSOR EVENT LISTENER METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float lumens = event.values[0];
        boolean currentThresholdMet = (lumens >= LUMEN_THRESHOLD);
        if (currentThresholdMet != previousThresholdMet) {
            if (currentThresholdMet) {
                // trigger permission restriction disable events
                for (ITrackerListener listener : listeners) {
                    listener.onTrackerDeactivated();
                }
            } else {
                // trigger permission restriction enable events
                for (ITrackerListener listener : listeners) {
                    listener.onTrackerActivated("Ambient light below threshold at lumen value: " + lumens);
                }
            }
            previousThresholdMet = currentThresholdMet;
        }
    }

}