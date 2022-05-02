package me.gfuil.bmap.lite.algorithm;

import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import me.gfuil.bmap.lite.storage.MappedTrajectory;
import me.gfuil.bmap.lite.storage.ObjectBox;
import me.gfuil.bmap.lite.storage.POI;
import me.gfuil.bmap.lite.storage.RiskyTrajectory;

public class RiskUtils {

    public static double calculateRisk(MappedTrajectory mappedTrajectory, RiskyTrajectory riskyTrajectory){
        List<TrajEvent> t1=generateTrajEvents(mappedTrajectory.record);
        List<TrajEvent> t2=generateTrajEvents(riskyTrajectory.record);
        long totaltime=0;
        long overlaptime=0;
        for(TrajEvent e2 : t2){
            totaltime+=e2.endtime-e2.starttime;
            for(TrajEvent e1 : t1){
                if(e1.loc.id==e2.loc.id){
                    long st=Math.max(e1.starttime,e2.starttime);
                    long et=Math.min(e1.endtime,e2.endtime);
                    long overlap=Math.max(et-st,0);
                    overlaptime+=overlap;
                }
            }
        }
        if(totaltime==0||overlaptime==0)
            return 0;

        return (double)overlaptime/totaltime;
    }

    public static List<TrajEvent> generateTrajEvents(String record){
        List<TrajEvent> res=new ArrayList<>();
        String[] vals = record.split(";");
        Box<POI> poiBox = ObjectBox.get().boxFor(POI.class);
        for(int i=0;i<vals.length;i++){
            String val=vals[i];
            if(val.equals("")){
                continue;
            }
            else{
                String[] vs = val.split(",");
                long id = Long.valueOf(vs[0]);
                POI p=poiBox.get(id);
                LatLng latLng = GPSConverterUtils.bd09_To_Gcj02(p.lat,p.lng);
                long starttime=Long.valueOf(vs[2]);
                long endtime=Long.valueOf(vs[3]);
                TrajEvent e=new TrajEvent(TrajEventType.VISTING,starttime,endtime,new Point((int) id,latLng.latitude,latLng.longitude,1));
                res.add(e);
            }
        }
        return res;
    }

}
