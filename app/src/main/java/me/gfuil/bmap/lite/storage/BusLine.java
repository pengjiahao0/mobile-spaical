package me.gfuil.bmap.lite.storage;

import io.objectbox.BoxStore;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

@Entity
public class BusLine {
    @Id
    public long id;

    public String line_no;

    public String line_name;

    // 0 or 1
    public int direction;


    public ToMany<StopOrder> stopOrders = new ToMany<>(this,BusLine_.stopOrders);

    public String start_time;

    public String end_time;

    transient BoxStore __boxStore;

    public BusLine(String line_no,String line_name,String start_time,String end_time,int direction){
        this.line_no=line_no;
        this.line_name=line_name;
        this.start_time=start_time;
        this.end_time=end_time;
        this.direction=direction;
    }



}
