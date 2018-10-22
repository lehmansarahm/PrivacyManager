package edu.temple.environmentalaccesscontrol;

import android.os.Bundle;

/**
 * Basic activity to provide a launching / help point for the Game
 * Manager Admin application.  Informs the user about the nature of
 * the application, and the functions available within.
 */
public class MainActivity extends BaseActivity {
    /**
     * Creates the activity to be displayed
     * @param savedInstanceState the instance state to create
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
