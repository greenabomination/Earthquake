package com.greenabomination.earthquake;

import android.preference.PreferenceActivity;
import android.util.Log;

import java.util.List;

/**
 * Created by green on 02.04.15.
 */
public class FragmentPreferences extends PreferenceActivity {
    private static final String TAG = "EARTHQUAKE";
    public static final String PREF_MIN_MAG = "PREF_MIN_MAG";
    public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";
    public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";


    @Override
    public void onBuildHeaders(List<Header> target) {
        Log.d(TAG, "1");
        isValidFragment("com.greenabomination.earthquake.UserPreferenceFragment");
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return UserPreferenceFragment.class.getName().equals(fragmentName);
    }
}
