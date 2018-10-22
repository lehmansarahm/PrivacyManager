package edu.temple.eac.trackers;

import android.app.Activity;

/**
 *
 */
public interface ITracker {

    /**
     * Allows env. access controller to initialize all trackers
     */
    void initialize(Activity currentActivity);

    /**
     *
     */
    void restart();

    /**
     * Imports subscribing listener
     *
     * @param listener - the new listener subscribing to this tracker
     */
    void addListener(ITrackerListener listener);

    /**
     * Clears the list of listeners for the given tracker
     */
    void removeListeners();

}