package me.gfuil.bmap.lite.algorithm;

import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class RangeQuery {
    public static List<LatLng> queryByRec(String record,LatLng lt,LatLng rd){
        List<LatLng> res=generateLatLngs(record);
        double LatMin=rd.latitude;
        double LatMax=lt.latitude;
        double LngMin=lt.longitude;
        double LngMax=rd.longitude;
        for(LatLng l:res){
            if(l.latitude>=LatMin&&l.latitude<=LatMax&&l.longitude>=LngMin&&l.longitude<=LngMax){
                return res;
            }
        }
        return null;
    }

    public static List<LatLng> generateLatLngs(String record){
        List<LatLng> res=new ArrayList<>();
        String[] args=record.split(";");
        String[] lngs=args[0].split(",");
        String[] lats=args[1].split(",");
        assert lngs.length==lats.length;
        for(int i=0;i<lats.length;i++){
            res.add(new LatLng(Double.valueOf(lats[i]),Double.valueOf(lngs[i])));
        }
        return res;
    }

}
