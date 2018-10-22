package edu.temple.eac.scanners.location;

import java.util.Date;

import edu.temple.eac.utils.StorageManager;

/**
 *
 */
public class LocationScan {

    private String areaName;
    private double latitude;
    private double longitude;

    private Date scanTime;

    /**
     *
     * @param area
     * @param lat
     * @param lon
     */
    public LocationScan(String area, double lat, double lon) {
        areaName = area;
        latitude = lat;
        longitude = lon;
    }

    /**
     *
     * @param st
     * @param lat
     * @param lon
     */
    public LocationScan(Date st, double lat, double lon) {
        scanTime = st;
        latitude = lat;
        longitude = lon;
    }

    /**
     *
     * @return
     */
    public String toString() {
        String out = "";
        if (scanTime != null) out += "\'" + StorageManager.formatTimestamp(scanTime) + ",";
        out += latitude + "," + longitude;
        return out;
    }

    /**
     *
     * @param lat
     * @param lon
     * @return
     */
    public boolean isMatch(double lat, double lon) {
        // it's a match if the distance between the two points is less than 15ft
        return (getDistance(lat, lon) <= 15.0);
    }

    /**
     *
     * @param lat
     * @param lon
     * @return
     */
    public double getDistance(double lat, double lon) {
        return getDistance(this.latitude, this.longitude, lat, lon);
    }

    /**
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 3958.756;    // Radius of the earth in mi
        double dLat = convertDegreesToRadians(lat2-lat1);
        double dLon = convertDegreesToRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(convertDegreesToRadians(lat1)) *
            Math.cos(convertDegreesToRadians(lat2)) *
            Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;       // Distance in mi
        return (d * 5280.0);    // Distance in ft
    }

    /**
     *
     * @param deg
     * @return
     */
    private static double convertDegreesToRadians(double deg) {
        return deg * (Math.PI/180);
    }

}