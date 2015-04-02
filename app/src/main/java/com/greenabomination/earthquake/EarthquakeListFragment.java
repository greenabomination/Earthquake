package com.greenabomination.earthquake;

import android.app.ListFragment;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by kabardinov133238 on 02.04.2015.
 */
public class EarthquakeListFragment extends ListFragment {

    ArrayAdapter<Quake> aa;
    ArrayList<Quake> earthquakes = new ArrayList<Quake>();

    private static final String TAG = "EARTHQUAKE";
    private Handler handler = new Handler();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        int layoutID = android.R.layout.simple_list_item_1;
        aa = new ArrayAdapter<Quake>(getActivity(), layoutID, earthquakes);
        setListAdapter(aa);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "thread running");
                refreshquakes();
            }
        });
        t.start();
    }

    public void refreshquakes() {
        Log.d(TAG, "refreshquakes");
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
                earthquakes.clear();
                Log.d(TAG, "step1");
                NodeList nl = docelem.getElementsByTagName("entry");
                if (nl != null && nl.getLength() > 0) {
                    for (int i = 0; i < nl.getLength(); i++) {
                        Element entry = (Element) nl.item(i);
                        Element title = (Element) entry.getElementsByTagName("title").item(0);
                        Element g = (Element) entry.getElementsByTagName("georss:point").item(0);
                        Element when = (Element) entry.getElementsByTagName("updated").item(0);
                        Element link = (Element) entry.getElementsByTagName("link").item(0);

                        String details = title.getFirstChild().getNodeValue();
                        Log.d(TAG, details);
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
        earthquakes.add(_q);
        aa.notifyDataSetChanged();
    }
}
