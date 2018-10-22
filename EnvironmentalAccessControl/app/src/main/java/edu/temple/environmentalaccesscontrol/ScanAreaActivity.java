package edu.temple.environmentalaccesscontrol;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import edu.temple.eac.scanners.IScannerListener;
import edu.temple.eac.utils.Constants;
import edu.temple.eac.utils.DialogManager;
import edu.temple.eac.utils.LogManager;

import static edu.temple.environmentalaccesscontrol.R.id.constant;
import static edu.temple.environmentalaccesscontrol.R.id.gps;
import static edu.temple.environmentalaccesscontrol.R.id.interval;
import static edu.temple.environmentalaccesscontrol.R.id.modified;
import static edu.temple.environmentalaccesscontrol.R.id.output;
import static edu.temple.environmentalaccesscontrol.R.id.wifi;

public class ScanAreaActivity extends BaseActivity implements IScannerListener {

    private boolean targetReached = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_area);
        eaController.initializeActivity(this, true);

        final RadioButton gpsRadio = (RadioButton) findViewById(R.id.gps);
        final RadioButton wifiRadio = (RadioButton) findViewById(R.id.wifi);

        final Button scanButton = (Button) findViewById(R.id.scanBtn);
        final Button stopButton = (Button) findViewById(R.id.stopBtn);

        final TextView outputTV = (TextView) findViewById(output);

        scanButton.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param v
             */
            public void onClick(View v) {
                // Reinit scanner listeners - GPS
                if (gpsRadio.isChecked()) {
                    LogManager.info("Attempting to init GPS scanner");
                    eaController.addScannerListener(Constants.SCANNER_TYPE.GPS, ScanAreaActivity.this);
                } else {
                    eaController.removeScannerListener(Constants.SCANNER_TYPE.GPS, ScanAreaActivity.this);
                }

                // Reinit scanner listeners - Wifi
                if (wifiRadio.isChecked()) {
                    LogManager.info("Attempting to init Wifi scanner");
                    eaController.addScannerListener(Constants.SCANNER_TYPE.Wifi, ScanAreaActivity.this);
                } else {
                    eaController.removeScannerListener(Constants.SCANNER_TYPE.Wifi, ScanAreaActivity.this);
                }

                // initialize step counter
                LogManager.info("Attempting to init step monitor");
                int userHeightInches =
                        Integer.parseInt(((EditText) findViewById(R.id.userHeightInches)).getText().toString());
                eaController.initializeStepMonitor(userHeightInches);

                // Initialize scanners with proper scanning mode
                Constants.SCAN_TYPE scanType =
                        ((RadioButton)findViewById(R.id.constant)).isChecked()
                                ? Constants.SCAN_TYPE.Constant :
                                (((RadioButton)findViewById(interval)).isChecked()
                                        ? Constants.SCAN_TYPE.Interval : Constants.SCAN_TYPE.Modified);
                eaController.initializeScanners(scanType);

                // Reset GUI
                outputTV.setText("");
                scanButton.setVisibility(View.INVISIBLE);
                stopButton.setVisibility(View.VISIBLE);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param v
             */
            public void onClick(View v) {
                String output = "Scan complete";
                if (targetReached) {
                    // Show "wait" dialog and wrap up scanning activities
                    DialogManager.show(ScanAreaActivity.this, "Please Wait", "Finalizing scan results...");
                    eaController.finalizeScanners();

                    // Reset GUI and remove "wait" dialog
                    outputTV.setText(output);
                    scanButton.setVisibility(View.VISIBLE);
                    stopButton.setVisibility(View.INVISIBLE);
                    DialogManager.hide(ScanAreaActivity.this, null);
                    targetReached = false;
                } else {
                    boolean finalized = eaController.markScannerTargetReached();
                    if (finalized) {
                        outputTV.setText(output);
                        scanButton.setVisibility(View.VISIBLE);
                        stopButton.setVisibility(View.INVISIBLE);
                    } else {
                        outputTV.setText("Target reached... awaiting finalization.");
                        targetReached = true;
                    }
                }
            }
        });
    }

    /**
     *
     * @param view
     */
    public void onSignalTypeRadioButtonClicked(View view) {
        switch(view.getId()) {
            case gps:
                if (((RadioButton) view).isChecked())
                    ((RadioButton)findViewById(wifi)).setChecked(false);
                break;
            case wifi:
                if (((RadioButton) view).isChecked())
                    ((RadioButton)findViewById(gps)).setChecked(false);
                break;
        }
    }

    /**
     *
     * @param view
     */
    public void onScanTypeRadioButtonClicked(View view) {
        switch(view.getId()) {
            case constant:
                if (((RadioButton) view).isChecked()) {
                    ((RadioButton)findViewById(interval)).setChecked(false);
                    ((RadioButton) findViewById(modified)).setChecked(false);
                }
                break;
            case interval:
                if (((RadioButton) view).isChecked()) {
                    ((RadioButton)findViewById(constant)).setChecked(false);
                    ((RadioButton) findViewById(modified)).setChecked(false);
                }
                break;
            case modified:
                if (((RadioButton) view).isChecked()) {
                    ((RadioButton) findViewById(constant)).setChecked(false);
                    ((RadioButton)findViewById(interval)).setChecked(false);
                }
                break;
        }
    }

    public void onSignalAcquired(String message) {

    }

    /**
     *
     * @param message optional feedback message
     */
    public void onRestrictedAreaEntered(final String message) {
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
    public void onRestrictedAreaDeparted() {
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