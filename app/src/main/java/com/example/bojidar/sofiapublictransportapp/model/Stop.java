package com.example.bojidar.sofiapublictransportapp.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Bojidar on 2/20/2018.
 */

public class Stop implements Serializable {

//    enum TransportType{
//        BUS,TRAM,TROLEY}

    private String stopName;
    private String stopID;
    private double lat;
    private double lng;
    private List<Line> lines;
    private List<Integer> typesTransport;

    public Stop(String stopName, String stopID, double lat, double lng, List<Integer> typesTransport) {
        this.stopName = stopName;
        this.stopID = stopID;
        this.lat = lat;
        this.lng = lng;
        this.typesTransport = typesTransport;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getStopID() {
        return stopID;
    }

    public void setStopID(String stopID) {
        this.stopID = stopID;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    public List<Integer> getTypesTransport() {
        return typesTransport;
    }

    public void setTypesTransport(List<Integer> typesTransport) {
        this.typesTransport = typesTransport;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stop stop = (Stop) o;

        if (stopID != stop.stopID) return false;
        if (Double.compare(stop.lat, lat) != 0) return false;
        if (Double.compare(stop.lng, lng) != 0) return false;
        if (stopName != null ? !stopName.equals(stop.stopName) : stop.stopName != null)
            return false;
        if (lines != null ? !lines.equals(stop.lines) : stop.lines != null) return false;
        return typesTransport != null ? typesTransport.equals(stop.typesTransport) : stop.typesTransport == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = stopName.hashCode();
        result = 31 * result + stopID.hashCode();
        temp = Double.doubleToLongBits(lat);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lng);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (lines != null ? lines.hashCode() : 0);
        result = 31 * result + (typesTransport != null ? typesTransport.hashCode() : 0);
        return result;
    }
}
