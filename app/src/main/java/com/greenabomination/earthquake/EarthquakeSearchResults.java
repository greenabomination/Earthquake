package com.greenabomination.earthquake;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

/**
 * Created by kabardinov133238 on 07.04.2015.
 */
public class EarthquakeSearchResults extends ListActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter adapter;
    private static String QUERY_EXTRA_KEY = "QUERY_EXTRA_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null,
                new String[]{EarthquakeProvider.KEY_SUMMARY}, new int[]{android.R.id.text1}, 0);
        setListAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);
        parseIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseIntent(getIntent());
    }

    private void parseIntent(Intent intent) {
        if (intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            Bundle args = new Bundle();
            args.putString(QUERY_EXTRA_KEY, searchQuery);
            getLoaderManager().restartLoader(0, args, this);
        }
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String query = "0";

        if (bundle != null) {
            query = bundle.getString(QUERY_EXTRA_KEY);
        }
        String[] projection = {EarthquakeProvider.KEY_ID, EarthquakeProvider.KEY_SUMMARY};
        String where = EarthquakeProvider.KEY_SUMMARY + " LIKE \"%" + query + "%\"";
        String[] whereArgs = null;
        String sortOrder = EarthquakeProvider.KEY_SUMMARY + " COLLATE LOCALIZED ASC";

        return new CursorLoader(this, EarthquakeProvider.CONTENT_URI, projection, where, whereArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        adapter.swapCursor(null);

    }


}
