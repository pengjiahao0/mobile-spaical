package me.gfuil.bmap.lite.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.Query;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.storage.ObjectBox;
import me.gfuil.bmap.lite.storage.RawTrajectory;
import me.gfuil.bmap.lite.storage.RawTrajectory_;

public class TrajectoryDetailFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView dateTextView;
    private MapView mapView;
    private TextView distance;
    private TextView time;
    private TextView avgSpeed;

    private String trajId;
    private String trajRecord;

    private AMap amap;


    public TrajectoryDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TrajectoryDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TrajectoryDetailFragment newInstance(String param1, String param2) {
        TrajectoryDetailFragment fragment = new TrajectoryDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View  view = inflater.inflate(R.layout.fragment_trajectory_detail, container, false);
        dateTextView =  view.findViewById(R.id.date);
        mapView = view.findViewById(R.id.detail_map);
        mapView.onCreate(savedInstanceState);
        if(amap==null)
            amap = mapView.getMap();
        distance = view.findViewById(R.id.distance_status);
        time = view.findViewById(R.id.time_status);
        avgSpeed = view.findViewById(R.id.avg_speed_status);



        return view;
    }




    public void onResume() {
        super.onResume();
        mapView.onResume();
        //显示轨迹
        Bundle bundle =getArguments();
        if(bundle!=null){
            trajId=getArguments().getString("id");
            trajRecord=getArguments().getString("record");
            Log.i("Trajectory detail fragment", trajId+"\t"+trajRecord);
        }
        List<LatLng> res=getLatLngs(trajRecord);
        amap.addMarker(new MarkerOptions().title("起点").position(res.get(0)));
        amap.addMarker(new MarkerOptions().title("终点").position(res.get(res.size()-1)));
        amap.addPolyline(new PolylineOptions().useGradient(true).color(getResources().getColor(R.color.blue)).width(20).addAll(res));
        amap.moveCamera(CameraUpdateFactory.changeLatLng(res.get(0)));
        Box<RawTrajectory> rawTrajectoryBox=ObjectBox.get().boxFor(RawTrajectory.class);
        long id=Long.valueOf(trajId);
        Query<RawTrajectory> rawTrajectoryQuery=rawTrajectoryBox.query().equal(RawTrajectory_.id,id).build();
        RawTrajectory rawTrajectory = rawTrajectoryQuery.findFirst();
        rawTrajectoryQuery.close();
        long starttime = rawTrajectory.starttime;
        long endtime = rawTrajectory.endtime;
        double length=getDistance(res);
        distance.setText(String.valueOf(length/1000));


    }

    public List<LatLng> getLatLngs(String record){
        List<LatLng> res=new ArrayList<>();
        String[] args=record.split(";");
        assert args.length==2;
        String[] lngs=args[0].split(",");
        String[] lats=args[1].split(",");
        assert lngs.length== lats.length;
        for(int i=0;i<lngs.length;i++){
            double lng=Double.valueOf(lngs[i]);
            double lat=Double.valueOf(lats[i]);
            res.add(new LatLng(lat,lng));
        }

        return res;
    }

    public double getDistance(List<LatLng> latLngs){
        if(latLngs.size()<2){
            return 0;
        }
        LatLng pre = latLngs.get(0);
        double distance=0;
        for(int i=1;i<latLngs.size();i++){
            LatLng cur=latLngs.get(i);
            distance+= AMapUtils.calculateLineDistance(pre,cur);
            pre=cur;
        }
        return distance;
    }




    public void onPause(){
        super.onPause();
        mapView.onPause();
    }

    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
    }

}