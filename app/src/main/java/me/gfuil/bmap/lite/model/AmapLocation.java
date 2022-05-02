package me.gfuil.bmap.lite.model;

public class AmapLocation {
    public double lon;
    public double lat;
    public long timestamp;
    public AmapLocation(double lon, double lat, long timestamp){
        this.lon=lon;
        this.lat=lat;
        this.timestamp=timestamp;
    }
}
