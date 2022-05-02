package me.gfuil.bmap.lite.algorithm;

public class Location {

    public long timestamp;
    public double speed;
    public double lat;
    public double lng;


    public Location(long timestamp,double speed,double lat,double lng){
        this.timestamp=timestamp;
        this.speed=speed;
        this.lat=lat;
        this.lng=lng;
    }


}
