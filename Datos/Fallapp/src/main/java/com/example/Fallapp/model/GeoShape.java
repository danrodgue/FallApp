package com.example.Fallapp.model;

import com.mongodb.client.model.geojson.Geometry;

import java.util.List;

public class GeoShape {

    private GeometryMap geometry;

    public GeometryMap getGeometry() {
        return geometry;
    }

    public void setGeometry(GeometryMap geometry) {
        this.geometry = geometry;
    }
}