package org.shunya.punter.gui;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.awt.*;

/**
 * Created by munish on 9/20/2015.
 */
public class PunterDimension {
    private double width;
    private double height;

    public PunterDimension() {
    }

    public PunterDimension(Dimension dimension) {
        this.width = dimension.getWidth();
        this.height = dimension.getHeight();
    }

    public PunterDimension(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @JsonIgnore
    public Dimension getDimension(){
        return new Dimension((int)width, (int)height);
    }
}
