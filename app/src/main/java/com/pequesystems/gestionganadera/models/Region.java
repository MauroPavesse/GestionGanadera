package com.pequesystems.gestionganadera.models;

import java.util.List;

public class Region {
    private String id;
    private String usuarioId;
    private List<LatLngPoint> points;
    private String nombre;

    public Region() {
    }

    public Region(String id, String usuarioId, List<LatLngPoint> points, String nombre) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.points = points;
        this.nombre = nombre;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioId() { return usuarioId; }

    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public List<LatLngPoint> getPoints() {
        return points;
    }

    public void setPoints(List<LatLngPoint> points) {
        this.points = points;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

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
    }
}
