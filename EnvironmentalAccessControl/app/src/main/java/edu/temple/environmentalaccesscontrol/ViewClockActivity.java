package edu.temple.environmentalaccesscontrol;

import android.os.Bundle;

import edu.temple.eac.trackers.ITrackerListener;
import edu.temple.eac.utils.Constants;

/**
 *
 */
public class ViewClockActivity extends BaseTrackerActivity implements ITrackerListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tracker);

        eaController.addTrackerListener(Constants.TRACKER_TYPE.Time, this);
        eaController.initializeActivity(this, true);
        eaController.initializeTrackers();
    }

}
