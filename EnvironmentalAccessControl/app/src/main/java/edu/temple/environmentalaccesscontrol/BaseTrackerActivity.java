package edu.temple.environmentalaccesscontrol;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.temple.eac.trackers.ITrackerListener;
import edu.temple.eac.utils.Constants;

/**
 *
 */
public class BaseTrackerActivity extends BaseActivity implements ITrackerListener {

    private static boolean useDefaultConfig = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tracker);

        final Button downloadConfigsBtn = (Button) findViewById(R.id.downloadConfigs);
        downloadConfigsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (useDefaultConfig) {
                    eaController.updateConfigs(Constants.SAMPLE_CONFIG_DOWNLOAD_URL);
                } else {
                    eaController.updateConfigs(Constants.SAMPLE_CONFIG_DOWNLOAD_URL2);
                }
                useDefaultConfig = !useDefaultConfig;
            }
        });
    }

    /**
     *
     * @param message optional feedback message
     */
    public void onTrackerActivated(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView status = (TextView) findViewById(R.id.status);
                status.setText("Access Restricted");

                TextView messageTV = (TextView) findViewById(R.id.message);
                messageTV.setText(message);
            }
        });
    }

    /**
     *
     */
    public void onTrackerDeactivated() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView status = (TextView) findViewById(R.id.status);
                status.setText("Access Granted");

                TextView messageTV = (TextView) findViewById(R.id.message);
                messageTV.setText("");
            }
        });
    }

}
