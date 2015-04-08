package com.greenabomination.earthquake;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;


public class EarthquakeActivity extends ActionBarActivity {
    private static final String TAG = "EARTHQUAKE";
    private static final int MENU_PREFERENCE = Menu.FIRST + 1;
    private static final int MENU_REFRESH = Menu.FIRST + 2;
    private static final int SHOW_PREFERENCE = 1;
    public int minimumMagnitude = 0;
    public boolean autoUpdateFlag = false;
    public int updateFreq = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake);
        updateFromPreferences();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        SearchView searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setSearchableInfo(searchableInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_PREFERENCE, Menu.NONE, R.string.menu_preferences);
        menu.add(0, MENU_REFRESH, Menu.NONE, R.string.menu_update);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case MENU_PREFERENCE: {
                Intent i = new Intent(this, FragmentPreferences.class);
                startActivityForResult(i, SHOW_PREFERENCE);
                return true;
            }
            case MENU_REFRESH:
                Toast.makeText(this, "Can't refresh", Toast.LENGTH_SHORT).show();
                return true;

        }
        return false;
    }

    private void updateFromPreferences() {
        Context context = getApplicationContext();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        minimumMagnitude = Integer.parseInt(sp.getString(FragmentPreferences.PREF_MIN_MAG, "3"));
        updateFreq = Integer.parseInt(sp.getString(FragmentPreferences.PREF_UPDATE_FREQ, "0"));
        autoUpdateFlag = sp.getBoolean(FragmentPreferences.PREF_AUTO_UPDATE, false);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOW_PREFERENCE)

            updateFromPreferences();
        android.app.FragmentManager fm = getFragmentManager();
        final EarthquakeListFragment earthquakeListFragment
                = (EarthquakeListFragment) fm.findFragmentById(R.id.EarthquakeListFragment);

                earthquakeListFragment.refreshquakes();


    }
}
