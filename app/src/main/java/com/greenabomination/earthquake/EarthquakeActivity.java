package com.greenabomination.earthquake;

import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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

    TabListener<EarthquakeListFragment> listTabListener;
    TabListener<EarthquakeMapFragment> mapTabListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake);
        updateFromPreferences();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        SearchView searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setSearchableInfo(searchableInfo);

        ActionBar ab = getSupportActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ab.setDisplayShowTitleEnabled(false);

        ActionBar.Tab listTab = ab.newTab();
        listTabListener = new TabListener<EarthquakeListFragment>(this, R.id.EarthquakeFragmentContainer,
                EarthquakeListFragment.class);
        listTab.setText("List").setContentDescription("List of earthquakes").setTabListener(listTabListener);
        ab.addTab(listTab);

        ActionBar.Tab mapTab = ab.newTab();
        mapTabListener = new TabListener<EarthquakeMapFragment>(this, R.id.EarthquakeFragmentContainer,
                EarthquakeMapFragment.class);
        mapTab.setText("Map").setContentDescription("Map of earthquakes").setTabListener(mapTabListener);
        ab.addTab(mapTab);


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
        startService(new Intent(this, EarthquakeUpdateService.class));


    }

    public static class TabListener<T extends android.support.v4.app.Fragment> implements ActionBar.TabListener {
        private android.support.v4.app.Fragment fragment;
        private Activity activity;
        private Class<T> fragmentClass;
        private int fragmentContainer;

        public TabListener(Activity activity, int fragmentContainer, Class<T> fragmentClass) {
            this.activity = activity;
            this.fragmentContainer = fragmentContainer;
            this.fragmentClass = fragmentClass;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {


            if (fragment == null) {
                String fragmentName = fragmentClass.getName();
                fragment = android.support.v4.app.Fragment.instantiate(activity, fragmentClass.getName());
                fragmentTransaction.add(fragmentContainer, fragment, fragmentName);
            } else {

                fragmentTransaction.attach(fragment);
            }

        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            if (fragment != null) {
                fragmentTransaction.detach(fragment);
            }
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            if (fragment != null) {
                fragmentTransaction.attach(fragment);
            }
        }
    }

}
