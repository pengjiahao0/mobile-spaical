package me.gfuil.bmap.lite.storage;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class POI {
    @Id
    public long id;

    public String name;
    public String cat_L1;
    public String cat_L2;
    public String province;
    public String city;
    public String area;
    public String address;
    public double lat;
    public double lng;

    public POI(String name,String cat_L1,String cat_L2,String provice,String city,String area,String address,double lat,double lng){
        this.name=name;
        this.cat_L1=cat_L1;
        this.cat_L2=cat_L2;
        this.province=provice;
        this.city=city;
        this.area=area;
        this.address=address;
        this.lat=lat;
        this.lng=lng;
    }

}
