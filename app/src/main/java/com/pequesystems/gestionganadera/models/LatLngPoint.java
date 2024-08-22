package com.pequesystems.gestionganadera.models;

public class LatLngPoint {
    private double latitude;
    private double longitude;

    public LatLngPoint() {
    }

    public LatLngPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters y setters
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
