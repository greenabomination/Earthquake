package com.greenabomination.earthquake;

import android.location.Location;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kabardinov133238 on 02.04.2015.
 */
public class Quake {
    private Date date;
    private String details;
    private Location location;
    private double magnitude;
    private String link;

    public Quake(Date _d, String _det, Location _loc, double _mag, String _link) {
        date = _d;
        details = _det;
        location = _loc;
        magnitude = _mag;
        link = _link;
    }

    public Date getDate() {
        return date;
    }

    public String getDetails() {
        return details;
    }

    public Location getLocation() {
        return location;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
        String dateString = sdf.format(date);
        return dateString + ": " + magnitude + " " + details;
    }
}
