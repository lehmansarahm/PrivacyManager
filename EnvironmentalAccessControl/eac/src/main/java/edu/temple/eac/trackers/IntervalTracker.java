package edu.temple.eac.trackers;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.temple.eac.utils.Constants;
import edu.temple.eac.utils.StorageManager;

/**
 *
 */
public class IntervalTracker implements ITracker {

    private List<ITrackerListener> listeners = new ArrayList<>();
    private static Activity currentActivity;
    public static Updater updater;

    public static String CONFIG_FILE_NAME = "interval.txt";
    public static int STANDARD_INTERVAL; // read from config

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
        Log.e("INFO", "Interval tracker restarting");
        this.updater.removeListeners();
        this.updater = new Updater();
        for (ITrackerListener listener : listeners) {
            this.updater.addListener(listener);
        }
        init();
    }

    /**
     *
     * @param listener - the new listener subscribing to this tracker
     */
    public void addListener(ITrackerListener listener) {
        if (this.updater == null) this.updater = new Updater();
        this.updater.addListener(listener);
        this.listeners.add(listener);
    }

    /**
     *
     */
    public void removeListeners() {
        this.updater.removeListeners();
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
            // set interval from config
            STANDARD_INTERVAL = Integer.parseInt(configContents.get(0));
            Log.e("INFO", "Starting interval tracker with period: " + STANDARD_INTERVAL);

            // periodically check to see if we are within a restricted time range
            if (this.updater == null) this.updater = new Updater();
            (new Timer()).schedule(this.updater, 0, STANDARD_INTERVAL);
        }
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      PRIVATE NESTED CLASS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     * Nested class to toggle the listener events
     */
    class Updater extends TimerTask {
        private List<ITrackerListener> listeners;
        private boolean trackerActivated;

        /**
         *
         */
        public Updater() {
            this.listeners = new ArrayList<>();
            this.trackerActivated = false;
        }

        /**
         *
         * @param listener
         */
        public void addListener(ITrackerListener listener) { this.listeners.add(listener); }

        /**
         *
         */
        public void removeListeners() {
            this.listeners.clear();
        }

        /**
         * Timed method to fire off listener update event
         */
        public void run() {
            if (trackerActivated) {
                for (ITrackerListener listener : listeners) {
                    listener.onTrackerDeactivated();
                }
            } else {
                for (ITrackerListener listener : listeners) {
                    String message = "Tracker activated due to regular interval: "
                            + STANDARD_INTERVAL + "ms";
                    listener.onTrackerActivated(message);
                }
            }
            this.trackerActivated = !this.trackerActivated;
        }
    }

}