package me.gfuil.bmap.lite.algorithm;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class DistanceUtils {
    public static double getTrajdistance(String record){
        double distance = 0;
        String[] strs=record.split(";");
        assert strs.length==2;
        String[] lats=strs[1].split(",");
        String[] lngs=strs[0].split(",");
        assert lats.length==lngs.length;
        int trajLength=lats.length;

        if(trajLength<2)
            return 0;
        List<LatLng> l=new ArrayList<>();
        for(int i=0;i<trajLength;i++){
            l.add(new LatLng(Double.valueOf(lats[i]),Double.valueOf(lngs[i])));

        }
        LatLng pre=l.get(0);
        for(int i=1;i<l.size();i++){
            LatLng cur=l.get(i-1);
            distance+= AMapUtils.calculateLineDistance(pre,cur);
            pre=cur;
        }
        return distance;
    }
}
