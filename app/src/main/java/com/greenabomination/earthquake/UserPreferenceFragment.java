package com.greenabomination.earthquake;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

/**
 * Created by kabardinov133238 on 03.04.2015.
 */
public class UserPreferenceFragment extends PreferenceFragment {
    private static final String TAG = "EARTHQUAKE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "2");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }



}
