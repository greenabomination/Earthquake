package com.greenabomination.earthquake;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class EarthquakeActivity extends ActionBarActivity {
    private static final String TAG = "EARTHQUAKE";
    private static final int MENU_PREFERENCE = Menu.FIRST + 1;
    private static final int MENU_REFRESH = Menu.FIRST + 2;
    private static final int SHOW_PREFERENCE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake);
        Log.d(TAG, "CREATED");
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
                Intent i = new Intent(this, PreferencesActivity.class);
                startActivityForResult(i, SHOW_PREFERENCE);
                return true;
            }
            case MENU_REFRESH:
                Toast.makeText(this, "Can't refresh", Toast.LENGTH_SHORT).show();
                return true;

        }
        return false;
    }
}
