package edu.temple.environmentalaccesscontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import edu.temple.eac.EAController;

/**
 * Base, view-less activity which provides common menu functionality
 * for all application views.
 */
public class BaseActivity extends AppCompatActivity {
    protected static EAController eaController;

    /**
     * Creates the activity to be displayed
     * @param savedInstanceState the instance state to create
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eaController = new EAController(getBaseContext());
    }

    /**
     * Creates an options menu to be displayed
     * @param menu the menu to display
     * @return whether the menu was created successfully
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Responds to the selection of a menu option item.
     * @param item the item selected
     * @return whether the item was properly handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent = null;
        int id = item.getItemId();
        switch (id) {
            case R.id.map:
                myIntent = new Intent(this, MapAreaActivity.class);
                break;
            case R.id.scan:
                myIntent = new Intent(this, ScanAreaActivity.class);
                break;
            case R.id.clock:
                myIntent = new Intent(this, ViewClockActivity.class);
                break;
            case R.id.interval:
                myIntent = new Intent(this, ViewIntervalActivity.class);
                break;
            case R.id.light:
                myIntent = new Intent(this, ViewLightActivity.class);
                break;
            case R.id.help:
                myIntent = new Intent(this, MainActivity.class);
                break;
        }

        if (myIntent != null) {
            startActivityForResult(myIntent, 0);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}