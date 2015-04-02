package com.greenabomination.earthquake;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;


public class EarthquakeActivity extends ActionBarActivity {
    private static final String TAG = "EARTHQUAKE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake);
        Log.d(TAG, "CREATED");
    }

}
