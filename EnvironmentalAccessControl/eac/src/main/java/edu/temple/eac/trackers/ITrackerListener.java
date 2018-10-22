package edu.temple.eac.trackers;

/**
 * Event listener to allow user classes to subscribe to NON-MOBILITY-DEPENDENT
 * tracker-triggered events
 */
public interface ITrackerListener {

    /**
     * When tracker is activated, this event will fire off.  Returns optional message with feedback
     * info regarding event that triggered the tracker.
     *
     * @param message optional feedback message
     */
    void onTrackerActivated(String message);

    /**
     * When tracker is deactivated and user access control is released, this event will fire off.
     * No additional feedback is provided.
     */
    void onTrackerDeactivated();

}