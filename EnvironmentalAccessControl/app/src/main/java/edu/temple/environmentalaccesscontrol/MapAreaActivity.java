package edu.temple.environmentalaccesscontrol;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import edu.temple.eac.utils.Constants;

import static edu.temple.environmentalaccesscontrol.R.id.gps;
import static edu.temple.environmentalaccesscontrol.R.id.wifi;

/**
 *
 */
public class MapAreaActivity extends BaseActivity {

    /**
     *
     * @param savedInstanceState the instance state to create
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_area);
        eaController.initializeActivity(this, true);

        final RadioButton gpsButton = (RadioButton) findViewById(R.id.gps);
        Button mapBtn = (Button) findViewById(R.id.map_btn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Constants.SCANNER_TYPE mapType = (gpsButton.isChecked()
                        ? Constants.SCANNER_TYPE.GPS
                        : Constants.SCANNER_TYPE.Wifi);

                String mapName = ((EditText) findViewById(R.id.mapname_et)).getText().toString();
                boolean appendToConfig = ((CheckBox) findViewById(R.id.append)).isChecked();
                eaController.mapArea(mapType, mapName, appendToConfig);
            }
        });

        Button configBtn = (Button) findViewById(R.id.writeDefaultConfigs);
        configBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                eaController.writeDefaultConfigs();
            }
         });
    }

    /**
     *
     * @param view
     */
    public void onRadioButtonClicked(View view) {
        switch(view.getId()) {
            case gps:
                if (((RadioButton) view).isChecked())
                    ((RadioButton)findViewById(R.id.wifi)).setChecked(false);
                    break;
            case wifi:
                if (((RadioButton) view).isChecked())
                    ((RadioButton)findViewById(R.id.gps)).setChecked(false);
                    break;
        }
    }

}