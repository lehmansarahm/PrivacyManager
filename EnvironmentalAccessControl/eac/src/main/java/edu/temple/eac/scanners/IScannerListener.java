package edu.temple.eac.scanners;

/**
 * Intended to monitor environmental aspects that are location / mobility dependent
 */
public interface IScannerListener {

    void onSignalAcquired(String message);

    /**
     * When area restrictions are imposed, this event will fire off.  Returns optional message with
     * feedback info regarding event that triggered the scanner.
     *
     * @param message optional feedback message
     */
    void onRestrictedAreaEntered(String message);

    /**
     * When area restrictions are lifted and user access control is released, this event will fire
     * off.  No additional feedback is provided.
     */
    void onRestrictedAreaDeparted();

}