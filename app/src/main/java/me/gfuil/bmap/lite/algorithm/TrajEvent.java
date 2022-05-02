package me.gfuil.bmap.lite.algorithm;

public class TrajEvent {

    TrajEventType trajEventType;

    public long starttime;
    public long endtime;

    public Point loc;

    public TrajEvent(TrajEventType type,long starttime,long endtime,Point point){
        this.trajEventType=type;
        this.starttime=starttime;
        this.endtime=endtime;
        this.loc=point;
    }

    public TrajEventType getTrajEventType(){
        return trajEventType;
    }

    public String toString(){
        String res=starttime+","+endtime+"\t"+loc.toString();

        return res;
    }

}
