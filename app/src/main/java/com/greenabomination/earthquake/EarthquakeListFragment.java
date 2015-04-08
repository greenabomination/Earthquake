package com.greenabomination.earthquake;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

/**
 * Created by green on 02.04.2015.
 */
public class EarthquakeListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    SimpleCursorAdapter adapter;

    private static final String TAG = "EARTHQUAKE";
    private Handler handler = new Handler();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null,
                new String[]{EarthquakeProvider.KEY_SUMMARY}, new int[]{android.R.id.text1}, 0);
        setListAdapter(adapter);
        getLoaderManager().initLoader(0, null, this);
        refreshquakes();
    }

    public void refreshquakes() {
        getLoaderManager().restartLoader(0, null, EarthquakeListFragment.this);
Log.d(TAG,"pre-service" );
        getActivity().startService(new Intent(getActivity(), EarthquakeUpdateService.class));
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]
                {EarthquakeProvider.KEY_ID,
                        EarthquakeProvider.KEY_SUMMARY
                };
        EarthquakeActivity earthquakeActivity = (EarthquakeActivity) getActivity();
        String where = EarthquakeProvider.KEY_MAGNITUDE + " > " +
                earthquakeActivity.minimumMagnitude;
        CursorLoader loader = new CursorLoader(getActivity(), EarthquakeProvider.CONTENT_URI,
                projection, where, null, null);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);

    }
}
