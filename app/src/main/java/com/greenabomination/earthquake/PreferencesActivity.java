package com.greenabomination.earthquake;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

/**
 * Created by green on 02.04.15.
 */
public class PreferencesActivity extends Activity {

    CheckBox autoUpdate;
    Spinner updateFreqSpinner, magnitudeSpinner;

    public static final String USER_PREFERENCE = "USER_PREFERENCE";
    public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
    public static final String PREF_MIN_MAG_INDEX = "PREF_MIN_MAG_INDEX";
    public static final String PREF_UPDATE_FREQ_INDEX = "PREF_UPDATE_FREQ_INDEX";

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);

        autoUpdate = (CheckBox) findViewById(R.id.checkbox_auto_update);
        updateFreqSpinner = (Spinner) findViewById(R.id.spinner_update_freq);
        magnitudeSpinner = (Spinner) findViewById(R.id.spinner_quake_mag);

        populateSpinner();

        Context context = getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        updateUIfromPreference();

        Button button_ok = (Button) findViewById(R.id.button_ok);
        button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesActivity.this.setResult(RESULT_OK);
                finish();
            }
        });

    }

    private void updateUIfromPreference() {
        boolean autoUpdateCheked = sharedPreferences.getBoolean(PREF_AUTO_UPDATE, false);
        int updateFreqIndex = sharedPreferences.getInt(PREF_UPDATE_FREQ_INDEX, 2);
        int minmagIndex = sharedPreferences.getInt(PREF_MIN_MAG_INDEX, 0);
        updateFreqSpinner.setSelection(updateFreqIndex);
        magnitudeSpinner.setSelection(minmagIndex);
        autoUpdate.setChecked(autoUpdateCheked);
    }

    void populateSpinner() {

        ArrayAdapter<CharSequence> fAdapter;
        fAdapter = ArrayAdapter.createFromResource(this, R.array.update_freq_options,
                android.R.layout.simple_spinner_item);
        int spinnerDdItem = android.R.layout.simple_spinner_dropdown_item;
        fAdapter.setDropDownViewResource(spinnerDdItem);
        updateFreqSpinner.setAdapter(fAdapter);

        ArrayAdapter<CharSequence> mAdapter;
        mAdapter = ArrayAdapter.createFromResource(this, R.array.magnitude_options,
                android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(spinnerDdItem);
        magnitudeSpinner.setAdapter(mAdapter);
    }

    private void savePreference() {
        int updateIndex = updateFreqSpinner.getSelectedItemPosition();
        int minmagIndex = magnitudeSpinner.getSelectedItemPosition();
        boolean autoUpdateCheck = autoUpdate.isChecked();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_AUTO_UPDATE, autoUpdateCheck);
        editor.putInt(PREF_AUTO_UPDATE, updateIndex);
        editor.putInt(PREF_MIN_MAG_INDEX, minmagIndex);
        editor.commit();
    }
}
