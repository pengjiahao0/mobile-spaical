package me.gfuil.bmap.lite.algorithm;


import android.util.Log;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import me.gfuil.bmap.lite.Index.GridIndex;

// Map trajectory into a series of POIs or bus stops
public class TrajExtractor {

    public static double distance_threshold=20; // distance to stop or poi(m)
    public static int size_threshold=25;
    public static double speed_threshold=5;     //(m/s)

    public static double distance_to_stop_threshold=50;

    public static List<Point> centers ;



    public static void init(){
        centers = new ArrayList<>();


    }

    public static List<TrajEvent> execute(String trajRecord,long starttime,long endtime){
        List<Location> locs = generateLocations(trajRecord,starttime,endtime);
        if(locs==null){
            return null;
        }
        List<TrajEvent> events = new ArrayList<>();

        List<Location> curCluster = new ArrayList<>();
        Point curPoi=null;
        Location preLoc=null;
        TrajEventType state=TrajEventType.WALKING;

        for(int i=0;i<locs.size();i++) {
            Location curLoc = locs.get(i);

            // Check whether user is ON_BUS by speed
            if (curLoc.speed > speed_threshold) {
                if (state != TrajEventType.ON_BUS) {
                    Point stop;
                    if (curCluster.size() != 0) {
                        LatLng center=centerOfCluster(curCluster);
                        stop = GridIndex.queryStop(center.latitude,center.longitude);
                    }
                    else{
                        stop = GridIndex.queryStop(curLoc.lat,curLoc.lng);
                    }
                    state=TrajEventType.ON_BUS;
                    events.add(new TrajEvent(TrajEventType.ON_BUS,curLoc.timestamp,curLoc.timestamp,stop));
                }
            } else {
                if(state==TrajEventType.ON_BUS){
                    Point stop=GridIndex.queryStop(curLoc.lat,curLoc.lng);
                    if(stop!=null){
                        events.add(new TrajEvent(TrajEventType.OFF_BUS,curLoc.timestamp,curLoc.timestamp,stop));
                        state=TrajEventType.OFF_BUS;
                    }

                }else{
                    if(curCluster.size()==0){
                        curCluster.add(curLoc);
                    }
                    else{
                        if(distanceToCenter(curCluster,curLoc)<distance_threshold){
                            curCluster.add(curLoc);
                        }
                        else{
                            if(curCluster.size()>size_threshold){
                                LatLng center=centerOfCluster(curCluster);
                                Point poi=GridIndex.queryPOIorStop(center.latitude,center.longitude);
                                events.add(new TrajEvent(TrajEventType.VISTING,curCluster.get(0).timestamp,curCluster.get(curCluster.size()-1).timestamp,poi));
                            }
                            state=TrajEventType.WALKING;
                            curCluster.clear();
                        }
                    }
                }
            }
        }

        return events;
    }

    public static List<TrajEvent> executeForPOIs(String trajRecord,long starttime,long endtime)  {
        List<TrajEvent> events=new ArrayList<>();
        List<Location> locs = generateLocations(trajRecord,starttime,endtime);
        List<Location> curCluster=new ArrayList<>();


        for(int i=0;i<locs.size();i++){
            Location l=locs.get(i);
            if(curCluster.size()==0){
                curCluster.add(l);
            }
            else{
                if(distanceToCenter(curCluster,l)<=distance_threshold){
                    curCluster.add(l);
                }else{
                    if(curCluster.size()>=size_threshold){
                        LatLng center = centerOfCluster(curCluster);

                        center=GPSConverterUtils.gcj02_To_Bd09(center.latitude,center.longitude);

                        Log.i("Cluster center",center.latitude+","+center.longitude);
                        Point poi=GridIndex.queryPOIorStop(center.latitude,center.longitude);
                        if(poi!=null){
                            events.add(new TrajEvent(TrajEventType.VISTING,curCluster.get(0).timestamp,
                                    curCluster.get(curCluster.size()-1).timestamp,poi));

                        }
                        curCluster.clear();
                    }else{
                        curCluster.clear();
                    }
                }
            }
        }

        return events;
    }

    public static List<TrajEvent> executeForBusLines(String trajRecord,long starttime,long endtime){
        List<TrajEvent> events=new ArrayList<>();
        List<Location> locs = generateLocations(trajRecord,starttime,endtime);
        List<Location> curCluster=new ArrayList<>();
        boolean onBus=false;
        for(int i=0;i<locs.size();i++){
            Location l=locs.get(i);
            if(l.speed>=speed_threshold){
                if(onBus){
                    continue;
                }
                else{
                    LatLng latLng=GPSConverterUtils.gcj02_To_Bd09(l.lat,l.lng);
                    Point p=GridIndex.queryStop(latLng.latitude,latLng.longitude);
                    if(p==null){
                        continue;
                    }
                    LatLng pLatLng=GPSConverterUtils.bd09_To_Gcj02(p.lat,p.lng);
                    double distance=AMapUtils.calculateLineDistance(new LatLng(l.lat,l.lng),new LatLng(pLatLng.latitude,pLatLng.longitude));
                    if(distance<=distance_to_stop_threshold){
                        onBus=true;
                        events.add(new TrajEvent(TrajEventType.ON_BUS,l.timestamp,l.timestamp,p));
                    }
                }
            }
            else{
                if(onBus){
                    LatLng latLng=GPSConverterUtils.gcj02_To_Bd09(l.lat,l.lng);
                    Point p=GridIndex.queryStop(latLng.latitude,latLng.longitude);
                    if(p==null){
                        continue;
                    }
                    LatLng pLatLng=GPSConverterUtils.bd09_To_Gcj02(p.lat,p.lng);
                    double distance=AMapUtils.calculateLineDistance(new LatLng(l.lat,l.lng),new LatLng(pLatLng.latitude,pLatLng.longitude));
                    if(distance<=distance_to_stop_threshold){
                        onBus=false;
                        events.add(new TrajEvent(TrajEventType.OFF_BUS,l.timestamp,l.timestamp,p));
                    }
                }
                else{
                    continue;
                }

            }
        }

        return events;
    }




    public static List<Location> generateLocations(String trajRecord,long starttime,long endtime){
        String[] strs=trajRecord.split(";");
        assert strs.length==2;
        String[] lats=strs[1].split(",");
        String[] lngs=strs[0].split(",");
        assert lats.length==lngs.length;
        int trajLength=lats.length;

        if(trajLength<=2)
            return null;

        double time_step=2;

        List<Location> locList=new ArrayList<>();

        for(int i=0;i<trajLength;i++){
            if(i==0){
                locList.add(new Location(starttime,0,Double.valueOf(lats[i]),Double.valueOf(lngs[i])));
            }
            /*else if(i==trajLength-1){
                locList.add(new Location(endtime,0,Double.valueOf(lats[i]),Double.valueOf(lngs[i])));
            }*/
            else{

                LatLng pre = new LatLng(Double.valueOf(lats[i-1]),Double.valueOf(lngs[i-1]));
                LatLng cur = new LatLng(Double.valueOf(lats[i]),Double.valueOf(lngs[i]));

                double distance = AMapUtils.calculateLineDistance(pre,cur);
                double speed=distance/time_step;
                long timestamp= (long) (starttime+i*time_step*1000);
                locList.add(new Location(timestamp,speed,Double.valueOf(lats[i]),Double.valueOf(lngs[i])));

            }
        }


        return locList;
    }

    public static double distanceToCenter(List<Location> locs,Location cur){

        double dis=AMapUtils.calculateLineDistance(centerOfCluster(locs),new LatLng(cur.lat,cur.lng));
        return dis;
    }

    public static LatLng centerOfCluster(List<Location> locs){
        double latCenter=0;
        double lngCenter=0;
        for(Location l:locs){
            latCenter+=l.lat;
            lngCenter+=l.lng;
        }
        latCenter=latCenter/locs.size();
        lngCenter=lngCenter/locs.size();

        return new LatLng(latCenter,lngCenter);
    }

}

