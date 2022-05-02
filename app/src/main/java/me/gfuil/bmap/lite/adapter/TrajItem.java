package me.gfuil.bmap.lite.adapter;

public class TrajItem {
    public int id;
    public int presentId;
    public String record;
    public String start_time;
    public String end_time;
    public long starttimeVal;
    public long endtimeVal;
    public double distance;
    public String[] actions;
    public String ads_record;
    public String[] address_info;
    public TrajItem(int id, String start_time, String end_time, String record, int presentId, long starttimeVal, long endtimeVal, double distance, String[] actions, String ads_record, String[] address_info){
        this.id=id;
        this.start_time=start_time;
        this.end_time=end_time;
        this.record=record;
        this.presentId=presentId;
        this.starttimeVal=starttimeVal;
        this.endtimeVal=endtimeVal;
        this.distance=distance;
        this.actions = actions;
        this.ads_record = ads_record;
        this.address_info = address_info;
    }

    public boolean equals(Object obj){
        if(null==obj)
            return false;
        if(obj instanceof TrajItem) {
            TrajItem item = (TrajItem) obj;
            return this.id==item.id && this.start_time.equals(item.start_time)&&this.end_time.equals(item.end_time);
        }
        return false;
    }

    public int hashCode(){
        String s=id+start_time+end_time;
        return s.hashCode();
    }
}
