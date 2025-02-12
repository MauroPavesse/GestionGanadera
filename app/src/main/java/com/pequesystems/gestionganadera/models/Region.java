package com.pequesystems.gestionganadera.models;

import java.util.List;

public class Region {
    private String id;
    private String userId;
    private List<LatLngPoint> points;
    private String name;
    private int color;

    public Region() {
    }

    public Region(String id, String userId, List<LatLngPoint> points, String name, int color) {
        this.id = id;
        this.userId = userId;
        this.points = points;
        this.name = name;
        this.color = color;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public List<LatLngPoint> getPoints() {
        return points;
    }

    public void setPoints(List<LatLngPoint> points) {
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color){
        this.color = color;
    }

    /*
    public static class LatLngPoint {
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
    }*/
}
