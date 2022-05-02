package me.gfuil.bmap.lite.activity;

import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;



import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.objectbox.Box;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.fragment.AmapFragment;
import me.gfuil.bmap.lite.model.AmapLocation;
import me.gfuil.bmap.lite.storage.ObjectBox;
import me.gfuil.bmap.lite.storage.RawTrajectory;
import me.gfuil.bmap.lite.utils.LogUtils;

public class RecordTrajActivity extends AppCompatActivity {
    private AmapFragment mapFragment;
    private MapView mapview;
    private AMap amap;
    private FrameLayout lay_content;
    private FloatingActionButton startRecordBtn;

    private FloatingActionButton closeBtn;
    private boolean startRecord = true;
    private TextView distancePanel;
    private TextView timePanel;
    private TextView speedPanel;


    private PolylineOptions polylineOptions;


    private List<AmapLocation> locations;

    private int delay=1000; // acquire current location per 2s

    private long interval=2000; //2s

    private Timer timer;

    private TimerTask task;

    private String provider;

    private long seconds=0;

    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;

    //private Polyline polyline;
    private List<LatLng> latLngs;
    private boolean isFirstLoaded=true;

    private boolean isStartPoint=true;

    private LocationSource.OnLocationChangedListener mListener;

    private Handler handler= new Handler(){


        public void handleMessage(Message msg){
            switch(msg.what){
                case (1):
                    Toast.makeText(RecordTrajActivity.this, "添加成功", Toast.LENGTH_LONG).show();
                    break;
                case (0):
                    Toast.makeText(RecordTrajActivity.this,"添加失败",Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_traj);
        ObjectBox.init(this);
        initView();
        try {
            initLocationService();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 定位结果回调
     *
     * @param aMapLocation 位置信息类
     */
    private AMapLocationListener aMapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (null == aMapLocation)
                return;
            if (aMapLocation.getErrorCode() == 0) {

                //定位成功
                mListener.onLocationChanged(aMapLocation);

                RecordTrajActivity.this.updateLocation(aMapLocation);


            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                LogUtils.error("AmapErr", errText);
            }
        }
    };



    public void initLocationService() throws Exception {


        locations=new ArrayList<>();
        latLngs=new ArrayList<>();
        polylineOptions=new PolylineOptions();
        polylineOptions.color(getResources().getColor(R.color.blue));
        polylineOptions.width(20);
        polylineOptions.useGradient(true);



        if(null==mLocationClient){
            mLocationClient = new AMapLocationClient(this);
            //设置定位属性
            mLocationOption = new AMapLocationClientOption();
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
            mLocationOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
            mLocationOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
            mLocationOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
            mLocationOption.setNeedAddress(false);//可选，设置是否返回逆地理地址信息。默认是true
            mLocationOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
            mLocationOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
            AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
            mLocationOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
            mLocationOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
            mLocationOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
            mLocationOption.setMockEnable(true);
            mLocationOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.ZH);//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.setLocationListener(aMapLocationListener);



        }



    }

    public void updateLocation(AMapLocation amapLocation){
        double lon=amapLocation.getLongitude();
        double lat=amapLocation.getLatitude();
        long timestamp=amapLocation.getTime();
        if(isStartPoint){
            amap.addMarker(new MarkerOptions().position(new LatLng(lat,lon)).title("起点"));
            isStartPoint=false;
        }
        locations.add(new AmapLocation(lon,lat,timestamp));
        //latLngs.add(new LatLng(lat,lon));
        double distance=getDistance(locations);
        double miles=distance/1000;
        distancePanel.setText(decimalFormat.format(miles));
        double avg_speed;
        if(seconds>0) {
            avg_speed = (distance / seconds) * 3.6;
            speedPanel.setText(decimalFormat.format(avg_speed));
        }
        else{
            avg_speed=0;
            speedPanel.setText(decimalFormat.format(avg_speed));
        }
        polylineOptions.add(new LatLng(lat,lon));
        amap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 18));
        amap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(lat,lon)));

        amap.addPolyline(polylineOptions);
        LogUtils.info("location info",latLngs.toString());

    }

    public double getDistance(List<AmapLocation> locations){
        double distance = 0;
        if (locations == null || locations.size() == 0) {
            return distance;
        }
        for (int i = 0; i < locations.size() - 1; i++) {
            LatLng firstLatLng = new LatLng(locations.get(i).lat,locations.get(i).lon);
            LatLng secondLatLng = new LatLng(locations.get(i+1).lat,locations.get(i+1).lon);
            double betweenDis = AMapUtils.calculateLineDistance(firstLatLng,
                    secondLatLng);
            distance = (distance + betweenDis);
        }
        return distance;
    }

    private void initView(){
        distancePanel = findViewById(R.id.distance_status);
        timePanel = findViewById(R.id.time_status);
        speedPanel = findViewById(R.id.avg_speed_status);


        startRecordBtn = findViewById(R.id.start);

        startRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(startRecord){
                    startRecordBtn.setImageResource(android.R.drawable.ic_media_pause);
                    startRecord=false;
                    mLocationClient.startLocation();
                    startTimer();
                }
                else{
                    startRecordBtn.setImageResource(android.R.drawable.ic_media_play);
                    startRecord=true;
                    mLocationClient.stopLocation();
                    stopTimer();
                }

            }
        });

        closeBtn = findViewById(R.id.close);
        closeBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(RecordTrajActivity.this).setTitle("确认退出吗？")
                        .setIcon(android.R.drawable.ic_menu_help)
                        .setPositiveButton("保存并退出", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“确认”后的操作,保存轨迹数据

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        RawTrajectory rtraj = new RawTrajectory();
                                        if (null != locations) {
                                            rtraj.starttime = locations.get(0).timestamp;
                                            rtraj.endtime = locations.get(locations.size() - 1).timestamp;
                                        }
                                        StringBuffer lons = new StringBuffer();
                                        StringBuffer lats = new StringBuffer();
                                        for (int i = 0; i < locations.size() - 1; i++) {
                                            lons.append(locations.get(i).lon).append(',');
                                            lats.append(locations.get(i).lat).append(',');
                                        }
                                        lons.append(locations.get(locations.size() - 1).lon);
                                        lats.append(locations.get(locations.size() - 1).lat);
                                        lons.append(';').append(lats);
                                        rtraj.record = lons.toString();
                                        Message m = Message.obtain();
                                        m.what = 1;
                                        Box<RawTrajectory> rawtraj = ObjectBox.get().boxFor(RawTrajectory.class);
                                        //Box<MappedTrajectory> mappedTrajectoryBox = ObjectBox.get().boxFor(MappedTrajectory.class);
                                        //MappedTrajectory mappedTrajectory = new MappedTrajectory();
                                        //List<TrajEvent> mappedPOIs = TrajExtractor.executeForPOIs(rtraj.record, rtraj.starttime, rtraj.endtime);
                                        //List<TrajEvent> mappedBusLine = TrajExtractor.executeForBusLines(rtraj.record, rtraj.starttime, rtraj.endtime);


                                        rawtraj.put(rtraj);

                                        handler.sendMessage(m);
                                    }
                                }
                                ).start();

                            }
                        })
                        .setNegativeButton("直接退出", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“返回”后的操作,这里不设置没有任何操作
                                //latLngs=null;
                                finish();
                            }
                        }).show();
            }
        });



        initMap();



    }

    private void initMap(){
        mapFragment=new AmapFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.record_traj_content, mapFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();



    }

    private void loadMap(){
        amap=mapFragment.mAmap;
        amap.setLocationSource(new LocationSource() {
            @Override
            public void activate(OnLocationChangedListener listener) {
                mListener=listener;

            }

            @Override
            public void deactivate() {
                mListener=null;
                if(mLocationClient!=null){
                    mLocationClient.stopLocation();
                    mLocationClient.onDestroy();
                }
                mLocationClient=null;
            }
        });
    }


    public void onResume() {
        super.onResume();
        if(isFirstLoaded) {
            loadMap();
            isFirstLoaded=false;
        }


    }



    private void startTimer(){
        if(null==timer){
            timer=new Timer();
        }
        if(null==task){
            task=new TimerTask() {
                @Override
                public void run() {
                    seconds++;
                    timePanel.setText(secondsFormat(seconds));
                }
            };
        }

        timer.schedule(task,0,delay);

    }

    private void stopTimer(){
        if(null!=timer){
            timer.cancel();
            timer=null;
        }
        if(null!=task){
            task.cancel();
            task=null;
        }
    }

    private String secondsFormat(long seconds){
        String s="";
        long min=seconds/60;
        long sec=seconds-60*min;

        if(min<=9){
            s+="0"+min;
        }else{
            s+=min;
        }
        s+=":";
        if(sec<=9){
            s+="0"+sec;
        }
        else{
            s+=sec;
        }

        return s;
    }

}