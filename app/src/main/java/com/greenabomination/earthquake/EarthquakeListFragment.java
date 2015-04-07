package com.greenabomination.earthquake;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //      Log.d(TAG, "thread running");
                refreshquakes();
            }
        });
        t.start();
    }

    public void refreshquakes() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                getLoaderManager().restartLoader(0, null, EarthquakeListFragment.this);
            }
        });
        URL url;
        try {
            String quakeFeed = getString(R.string.quake_feed);
            url = new URL(quakeFeed);
            URLConnection connection;
            connection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
            int resourceCode = httpURLConnection.getResponseCode();

            if (resourceCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpURLConnection.getInputStream();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = dbf.newDocumentBuilder();

                Document dom = documentBuilder.parse(inputStream);
                Element docelem = dom.getDocumentElement();
                //  Log.d(TAG, "step1");
                NodeList nl = docelem.getElementsByTagName("entry");
                if (nl != null && nl.getLength() > 0) {
                    for (int i = 0; i < nl.getLength(); i++) {
                        Element entry = (Element) nl.item(i);
                        Element title = (Element) entry.getElementsByTagName("title").item(0);
                        Element g = (Element) entry.getElementsByTagName("georss:point").item(0);
                        Element when = (Element) entry.getElementsByTagName("updated").item(0);
                        Element link = (Element) entry.getElementsByTagName("link").item(0);

                        String details = title.getFirstChild().getNodeValue();
                        String hostname = "http://earthquake.usgs.gov";
                        String linkString = hostname + link.getAttribute("href");
                        String point = g.getFirstChild().getNodeValue();
                        String dt = when.getFirstChild().getNodeValue();

                        SimpleDateFormat simpleDateFormat =
                                new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.sss'Z'");

                        Date qdate = new GregorianCalendar(0, 0, 0).getTime();
                        try {
                            qdate = simpleDateFormat.parse(dt);
                        } catch (ParseException e) {
                            Log.d(TAG, "Date parsing exception." + e);
                            e.printStackTrace();
                        }
                        String[] location = point.split(" ");
                        Location l = new Location("dummyGPS");
                        l.setLatitude(Double.parseDouble(location[0]));
                        l.setLongitude(Double.parseDouble(location[1]));

                        String magnitudeString = details.split(" ")[1];
                        int end = magnitudeString.length() - 1;
                        double magnitude = Double.parseDouble(magnitudeString.substring(0, end));
                        // Log.d(TAG, details);
                        details = details.split(" - ")[1].trim();

                        final Quake quake = new Quake(qdate, details, l, magnitude, linkString);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                addNewQuake(quake);
                            }
                        });
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } finally {

        }
    }

    private void addNewQuake(Quake _q) {
        ContentResolver cr = getActivity().getContentResolver();
        String w = EarthquakeProvider.KEY_DATE + "=" + _q.getDate().getTime();
        Cursor query = cr.query(EarthquakeProvider.CONTENT_URI, null, w, null, null);

        if (query.getCount() == 0) {
            ContentValues cv = new ContentValues();
            cv.put(EarthquakeProvider.KEY_DATE, _q.getDate().getTime());
            cv.put(EarthquakeProvider.KEY_DETAILS, _q.getDetails());
            cv.put(EarthquakeProvider.KEY_SUMMARY, _q.toString());
            double lat = _q.getLocation().getLatitude();
            double lng = _q.getLocation().getLongitude();
            cv.put(EarthquakeProvider.KEY_LOCATION_LAT, lat);
            cv.put(EarthquakeProvider.KEY_LOCATION_LNG, lng);
            cv.put(EarthquakeProvider.KEY_LINK, _q.getLink());
            cv.put(EarthquakeProvider.KEY_MAGNITUDE, _q.getMagnitude());
            cr.insert(EarthquakeProvider.CONTENT_URI, cv);
        }
        query.close();
        /*

        EarthquakeActivity earthquakeActivity = (EarthquakeActivity) getActivity();
        if (_q.getMagnitude() > earthquakeActivity.minimumMagnitude) {
            Log.d(TAG, _q.toString());
            earthquakes.add(_q);
            aa.notifyDataSetChanged();
    }*/
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
