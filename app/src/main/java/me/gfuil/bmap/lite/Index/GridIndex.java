package me.gfuil.bmap.lite.Index;

import android.util.Log;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import me.gfuil.bmap.lite.algorithm.Point;
import me.gfuil.bmap.lite.storage.BusStop;
import me.gfuil.bmap.lite.storage.ObjectBox;
import me.gfuil.bmap.lite.storage.POI;

/*
 同时存储POI和Bus stop, POI id为正值,Bus stop id为负值
 */
public class GridIndex {
    public static String indexPath;

    public static Map<Integer, List<Integer>> map;

    // 以武汉为例

    public static double lat_max=32;
    public static double lng_max=115;
    public static double lat_min=30;
    public static double lng_min=113;

    public static double lat_delta=0.005;
    public static double lng_delta=0.005;
    // 180
    public static int col_num = (int) ((lng_max-lng_min)/lng_delta);
    //  80
    public static int row_num = (int) ((lat_max-lat_min)/lat_delta);

    public static boolean isLoaded=false;

    public static void init(String path){
        indexPath=path;
    }

    public static List<Integer> getLocGridIds(double lat,double lng){
        if(lat>lat_max||lat<lat_min||lng>lng_max||lng<lng_min){
            return null;
        }
        int row = (int) ((lat-lat_min)/lat_delta);
        int col = (int) ((lng-lng_min)/lng_delta);
        List<Integer> res=new ArrayList<>();
        int loc=row*col_num+col;
        res.add(loc);
        res.add(loc-col_num);
        res.add(loc+col_num);
        res.add(loc+1);
        res.add(loc-1);
        res.add(loc-col_num+1);
        res.add(loc-col_num-1);
        res.add(loc+col_num+1);
        res.add(loc+col_num-1);
        return res;
    }

    public static int getLocGridId(double lat,double lng) {
        if (lat > lat_max || lat < lat_min || lng > lng_max || lng < lng_min) {
            return -1;
        }
        int row = (int) ((lat-lat_min)/lat_delta);
        int col = (int) ((lng-lng_min)/lng_delta);
        return row*col_num+col;
    }

    public static void loadIndex(String indexPath){
        File file=new File(indexPath);
        if(isLoaded){
            return;
        }
        map = new HashMap<>();
        if((!file.exists())){
            Log.e("load grid index","file not exists");
            return;
        }

        try(BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(new FileInputStream(file)))){
            String line;

            while((line=bufferedReader.readLine())!=null){
                String[] vals=line.split(",");
                List<Integer> values=new ArrayList<>();
                int gridId=Integer.valueOf(vals[0]);
                if(gridId==-1){
                    continue;
                }
                for(int i=1;i<vals.length;i++){
                    values.add(Integer.valueOf(vals[i]));
                }
                map.put(gridId,values);
            }

        }catch (Exception e){
            isLoaded=false;
            e.printStackTrace();
            return;
        }
        Log.i("Load index","索引加载成功");
        isLoaded=true;

    }

    public static Point queryPOIorStop(double lat, double lng){
        List<Integer> gridIds=getLocGridIds(lat,lng);
        Point point;
        if(gridIds==null||gridIds.size()==0){
            return null;
        }

        else{
            double min_distance=Double.MAX_VALUE;
            int targetid=0;
            LatLng latLng=new LatLng(lat,lng);
            List<Integer> candidates=new ArrayList<>();

            for(Integer i:gridIds){
                if(map.containsKey(i)){
                    candidates.addAll(map.get(i));
                }

            }

            Box<POI> poiBox = ObjectBox.get().boxFor(POI.class);
            Box<BusStop> stopBox = ObjectBox.get().boxFor(BusStop.class);
            for(int i:candidates){
                if(i<0){
                    BusStop busStop=stopBox.get(-i);
                    double distance = AMapUtils.calculateLineDistance(latLng,new LatLng(busStop.lat,busStop.lng));
                    if(distance<min_distance){
                        targetid=i;
                        min_distance= distance;
                    }
                }
                else{
                    POI poi=poiBox.get(i);
                    double distance = AMapUtils.calculateLineDistance(latLng, new LatLng(poi.lat,poi.lng));
                    if(distance<min_distance){
                        targetid=i;
                        min_distance=distance;
                    }
                }
            }

            if(targetid<0){
                BusStop busStop=stopBox.get(-targetid);
                point = new Point(-targetid,busStop.lat,busStop.lng,0);
            }
            else{
                POI poi=poiBox.get(targetid);
                point = new Point(targetid,poi.lat,poi.lng,1);
            }
        }

        return point;
    }

    public static Point queryStop(double lat,double lng){
        List<Integer> gridIds=getLocGridIds(lat,lng);
        Point point;
        if(gridIds==null||gridIds.size()==0){
            return null;
        }
        else {
            double min_distance = Double.MAX_VALUE;
            int targetid = 0;
            LatLng latLng = new LatLng(lat, lng);
            List<Integer> candidates=new ArrayList<>();

            for(Integer i:gridIds){
                if(map.containsKey(i)){
                    candidates.addAll(map.get(i));
                }

            }

            //Box<POI> poiBox = ObjectBox.get().boxFor(POI.class);
            Box<BusStop> stopBox = ObjectBox.get().boxFor(BusStop.class);
            for (int i : candidates) {
                if (i < 0) {
                    BusStop busStop = stopBox.get(-i);
                    double distance = AMapUtils.calculateLineDistance(latLng, new LatLng(busStop.lat, busStop.lng));
                    if (distance < min_distance) {
                        targetid = i;
                        min_distance=distance;
                    }
                }
            }
            BusStop busStop=stopBox.get(-targetid);
            point = new Point(-targetid,busStop.lat,busStop.lng,0);
        }

        return point;
    }


}
