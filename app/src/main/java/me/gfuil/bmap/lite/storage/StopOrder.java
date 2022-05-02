package me.gfuil.bmap.lite.storage;

import io.objectbox.BoxStore;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

@Entity
public class StopOrder {
    @Id
    public long id;

    public ToOne<BusStop> busStopToOne;


    //public String stop_id;

    public int order;

    transient BoxStore __boxStore;

    public int direction;

    //public String line_no;

    public StopOrder(int order,int direction,BusStop busStop){
        this.order=order;
        this.direction=direction;
        busStopToOne.setTarget(busStop);

    }

    // default构造函数，避免ToOne初始化问题
    public StopOrder(){}



}
