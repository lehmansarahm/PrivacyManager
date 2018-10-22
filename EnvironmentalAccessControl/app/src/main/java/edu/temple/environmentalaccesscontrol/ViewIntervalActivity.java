package edu.temple.environmentalaccesscontrol;

import android.os.Bundle;

import edu.temple.eac.utils.Constants;

/**
 *
 */
public class ViewIntervalActivity extends BaseTrackerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eaController.addTrackerListener(Constants.TRACKER_TYPE.Interval, this);
        eaController.initializeActivity(this, true);
        eaController.initializeTrackers();
    }

}