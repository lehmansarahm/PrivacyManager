package edu.temple.eac.trackers;

import android.app.Activity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import edu.temple.eac.utils.Constants;
import edu.temple.eac.utils.StorageManager;

/**
 *
 */
public class ClockTracker implements ITracker {

    public static Updater updater;
    public static String CONFIG_FILE_NAME = "clock.txt";

    // read from config
    public static int STANDARD_INTERVAL;
    public static List<String> START_TIMES = new ArrayList<>();
    public static List<String> END_TIMES = new ArrayList<>();

    /**
     *
     */
    public void initialize(Activity currentActivity) {
        init();
    }

    /**
     *
     */
    public void restart() {
        this.updater = null;
        init();
    }

    /**
     *
     * @param listener - the new listener subscribing to this tracker
     */
    public void addListener(ITrackerListener listener) {
        if (this.updater == null) this.updater = new Updater(START_TIMES, END_TIMES);
        this.updater.addListener(listener);
    }

    /**
     *
     */
    public void removeListeners() { this.updater.removeListeners(); }


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
            // set interval and start/end times from config
            STANDARD_INTERVAL = Integer.parseInt(configContents.get(0));
            for (int i = 1; i < configContents.size(); i++) {
                String[] restrictedInterval = configContents.get(i).split(",");
                START_TIMES.add(restrictedInterval[0]);
                END_TIMES.add(restrictedInterval[1]);
            }

            // periodically check to see if we are within a restricted time range
            if (this.updater == null) this.updater = new Updater(START_TIMES, END_TIMES);
            (new Timer()).schedule(this.updater, 0, STANDARD_INTERVAL);
        }
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      PRIVATE NESTED CLASS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     * Nested class to monitor the listener events
     */
    class Updater extends TimerTask {
        private List<ITrackerListener> listeners;
        private List<Map.Entry<String, String>> restrictedTimes;
        private boolean currentlyRestricted;

        /**
         *
         */
        public Updater(List<String> startTimes, List<String> endTimes) {
            listeners = new ArrayList<>();
            currentlyRestricted = false;

            restrictedTimes = new ArrayList<>();
            for (int i = 0; i < startTimes.size(); i++) {
                restrictedTimes.add(
                        new AbstractMap.SimpleEntry<>(startTimes.get(i), endTimes.get(i)));
            }
        }

        /**
         *
         * @param listener
         */
        public void addListener(ITrackerListener listener) { this.listeners.add(listener); }

        /**
         *
         */
        public void removeListeners() { this.listeners.clear(); }

        /**
         * Timed method to fire off listener update event
         */
        public void run() {
            try {
                DateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                Date currentDate = new java.util.Date();
                boolean isRestricted = false;

                // trying to get the current time is REALLY STUPID in Java and I hate it
                long currentTime = sdf.parse(sdf.format(currentDate)).getTime();
                String out = "Current date: " + currentDate.toString()
                        + "\nCurrent time:  " + currentTime;

                // Now, iterate through restricted time pairs and identify any changes
                out += "\n\nRESTRICTED TIMES:";
                long startTime = 0, endTime = 0;
                for (Map.Entry<String, String> restrictedTime : restrictedTimes) {
                    startTime = sdf.parse(restrictedTime.getKey()).getTime();
                    endTime = sdf.parse(restrictedTime.getValue()).getTime();

                    if (startTime <= currentTime && currentTime <= endTime) {
                        isRestricted = true;
                        break;
                    } else if ((currentTime < startTime || endTime < currentTime)) {
                        isRestricted = false;
                        break;
                    }
                }

                // update listeners as necessary
                out += "\nStart time: " + startTime + ", End time: " + endTime;
                if (currentlyRestricted != isRestricted) {
                    for (ITrackerListener listener : listeners) {
                        if (isRestricted) {
                            listener.onTrackerActivated(out);
                        } else {
                            listener.onTrackerDeactivated();
                        }
                    }
                    currentlyRestricted = isRestricted;
                }
            } catch (ParseException ex) {
                // continue
            }
        }
    }

}