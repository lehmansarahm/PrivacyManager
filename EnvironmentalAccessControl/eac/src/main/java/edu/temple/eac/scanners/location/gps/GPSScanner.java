package edu.temple.eac.scanners.location.gps;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import java.util.Date;
import java.util.List;

import edu.temple.eac.scanners.ScannerBase;
import edu.temple.eac.utils.Constants;

/**
 *
 */
public class GPSScanner extends ScannerBase implements LocationListener {

    protected GPSScanRateModifier sru;

    protected void initialize(Activity currentActivity) {
        sru = new GPSScanRateModifier(currentActivity);
        super.initialize(currentActivity);
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      LOCATION LISTENER METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------

    /**
     *
     * @param location
     */
    public void onLocationChanged(Location location) {
        // get ready to process the new scan
        Date newScan = new Date();
        processNewScan(newScan);

        // don't start tracking scan data until we get our GPS fix
        processGPSFix(newScan);

        // process the new location coordinates according to the current scanner state
        List<Double> coords = processCoordinates(location);
        switch (state) {
            case Canceled:
                // write what we have to the log, then remove listener
                finalizeScanSession();
                sru.removeUpdates(this);
                break;
            case Mapping:
                // process new location, then remove listener
                processMappedLocation(coords);
                sru.removeUpdates(this);
                break;
            case Scanning: // NO BREAK ... continue to next case
            case TargetReached:
                //  short circuit logic to filter out extra scans before the desired interval
                if (lastScan == null || (newScan.getTime() - lastScan.getTime()) >= scanInterval) {
                    boolean isModifiedScan = (scanType == Constants.SCAN_TYPE.Modified);
                    double newScanRateMod = isModifiedScan ? getUpdatedScanRateMod(coords) : 1.0;
                    processScanResult(newScan, coords, newScanRateMod);
                    processScanMatch(newScan, coords);

                    // check to see if target has been reached and a match has been found
                    if (state == Constants.SCANNER_STATE.TargetReached && matchScan != null) {
                        // if yes, we've reached an end condition
                        finalizeScanSession();
                        sru.removeUpdates(this);
                        state = Constants.SCANNER_STATE.Idle;
                    } else if (isModifiedScan) {
                        // else, keep scanning at modified rate until scanner is disabled
                        scanInterval = sru.getVariableUpdates(this, sc.getDefaultScanInterval(),
                                newScanRateMod);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     *
     * @param provider
     * @param status
     * @param extras
     */
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    /**
     *
     * @param provider
     */
    public void onProviderEnabled(String provider) {}

    /**
     *
     * @param provider
     */
    public void onProviderDisabled(String provider) {}

}