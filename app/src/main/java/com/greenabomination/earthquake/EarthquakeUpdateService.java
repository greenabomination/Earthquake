package com.greenabomination.earthquake;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

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
 * Created by green on 08.04.15.
 */
public class EarthquakeUpdateService extends IntentService {

    public static String TAG = "EARTHQUAKE_UPDATE_SERVICE";
    private AlarmManager am;
    private PendingIntent pi;

    public EarthquakeUpdateService() {
        super("EarthquakeUpdateService");
    }

    public EarthquakeUpdateService(String name) {
        super(name);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int updateFreq = Integer.parseInt(sp.getString(FragmentPreferences.PREF_UPDATE_FREQ, "60"));
        boolean autoUpdateChecked = sp.getBoolean(FragmentPreferences.PREF_AUTO_UPDATE, false);
        if (autoUpdateChecked) {
            int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
            long timeToRefresh = SystemClock.elapsedRealtime() + updateFreq * 60 * 1000;
            am.setInexactRepeating(alarmType, timeToRefresh, updateFreq * 60 * 1000, pi);
        } else {
            am.cancel(pi);

            refreshEarthquakes();

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        String ALARM_ACTION = EarthquakeAlarmReceiver.ACTION_REFRESH_EARTHQUAKE_ALARM;

        Intent intentToFire = new Intent(ALARM_ACTION);
        pi = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
    }


    private void addNewQuake(Quake _q) {
        ContentResolver cr = getContentResolver();
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

    }

    public void refreshEarthquakes() {

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

                        addNewQuake(quake);

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
}
