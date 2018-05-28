package com.example.bojidar.sofiapublictransportapp.model;

import java.util.List;

/**
 * Created by Bojidar on 2/20/2018.
 */

public class Line {

    private String lineNumber;
    private int type;
    private String arrivals;

    public Line(String lineNumber, int type, String arrivals) {
        this.lineNumber = lineNumber;
        this.type = type;
        this.arrivals = arrivals;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getArrivals() {
        return arrivals;
    }

    public void setArrivals(String arrivals) {
        this.arrivals = arrivals;
    }
}
