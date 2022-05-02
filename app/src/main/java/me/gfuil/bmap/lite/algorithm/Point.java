package me.gfuil.bmap.lite.algorithm;

public class Point {
    public int id;
    public double lat;
    public double lng;
    // 0 for bus stop or 1 for poi
    public int type;

    public Point(int id,double lat,double lng,int type){
        this.id=id;
        this.lat=lat;
        this.lng=lng;
        this.type=type;
    }

    public String toString(){
        return "("+id+","+lat+","+lng+")";
    }


}
