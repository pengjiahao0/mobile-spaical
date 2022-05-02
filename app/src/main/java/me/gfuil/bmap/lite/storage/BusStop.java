package me.gfuil.bmap.lite.storage;

import io.objectbox.BoxStore;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

@Entity
public class BusStop {
    @Id
    public long id;

    public String stop_id;

    public String stop_name;


    public ToMany<BusLine> busLines = new ToMany<>(this,BusStop_.busLines);

    transient BoxStore __boxStore;

    public double lat;

    public double lng;

    public BusStop(String stop_id,String stop_name,double lat,double lng){
        this.stop_id=stop_id;
        this.stop_name=stop_name;
        this.lat=lat;
        this.lng=lng;
    }


}
