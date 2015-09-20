package org.shunya.punter.gui;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.awt.*;

/**
 * Created by munish on 9/20/2015.
 */
public class PunterPoint {
    private double x;
    private double y;

    public PunterPoint() {
    }

    public PunterPoint(Point point) {
        this.x = point.getX();
        this.y = point.getY();
    }

    public PunterPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @JsonIgnore
    public Point getLocation() {
        return new Point((int) x, (int) y);
    }
}
