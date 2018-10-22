package edu.temple.eac.scanners.location.api;

import android.app.Activity;
import android.location.Location;

import com.google.android.gms.location.LocationListener;

import java.util.Date;
import java.util.List;

import edu.temple.eac.googleApi.IApiListener;
import edu.temple.eac.scanners.ScannerBase;
import edu.temple.eac.utils.Constants;
import edu.temple.eac.utils.DialogManager;
import edu.temple.eac.utils.LogManager;
import edu.temple.eac.utils.ToastManager;

/**
 *
 */
public class ApiScanner extends ScannerBase implements IApiListener, LocationListener {

    protected ApiScanRateModifier sru;

    protected void initialize(Activity currentActivity) {
        sru = new ApiScanRateModifier(currentActivity);
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
            case Mapping:
                // process new location, then remove listener
                processMappedLocation(coords); // NO BREAK... continue to next case
            case Canceled:
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
                        state = Constants.SCANNER_STATE.Idle;
                        sru.removeUpdates(this);
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


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      API LISTENER METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    public void onConnectionAcquired(String message) {
        // we have received connection to the Google API
        // make sure user waits until we have our GPS fix
        DialogManager.show(currentActivity,
                Constants.DIALOG_SIGNAL_TITLE, Constants.DIALOG_SIGNAL_MESSAGE);
        scanInterval = sru.getRegularUpdates(this, scanType);

    }

    public void onConnectionLost(String message) {
        // any other problem ... either it failed to connect or still hasn't connected
        ToastManager.showShortToast(currentActivity, "Failed to connect to Google Locations API");
        LogManager.error("Failed to connect to Google Locations API.  Error message: " + message);
    }

}