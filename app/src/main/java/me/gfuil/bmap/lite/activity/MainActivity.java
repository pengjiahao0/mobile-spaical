/*
 * This file is part of the BmapLite.
 * Copyright (C) 2019 gfuil 刘风广 <3021702005@qq.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.gfuil.bmap.lite.activity;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.utils.SpatialRelationUtil;
import com.amap.api.maps.utils.overlay.SmoothMoveMarker;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.mapapi.overlay.UserRouteOverlay;
import com.amap.mapapi.overlay.WalkRouteOverlay;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.gson.Gson;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.objectbox.Box;
import me.gfuil.bmap.lite.BApp;
import me.gfuil.bmap.lite.Index.GridIndex;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.adapter.SearchResultRecyclerAdapter;
import me.gfuil.bmap.lite.adapter.TrajItem;
import me.gfuil.bmap.lite.algorithm.DistanceUtils;
import me.gfuil.bmap.lite.algorithm.GPSConverterUtils;
import me.gfuil.bmap.lite.algorithm.RangeQuery;
import me.gfuil.bmap.lite.algorithm.RiskUtils;
import me.gfuil.bmap.lite.algorithm.TrajEvent;
import me.gfuil.bmap.lite.algorithm.TrajEventType;
import me.gfuil.bmap.lite.algorithm.TrajExtractor;
import me.gfuil.bmap.lite.base.BaseActivity;
import me.gfuil.bmap.lite.fragment.AmapFragment;
import me.gfuil.bmap.lite.fragment.BaiduMapFragment;
import me.gfuil.bmap.lite.interacter.ConfigInteracter;
import me.gfuil.bmap.lite.interacter.FavoriteInteracter;
import me.gfuil.bmap.lite.model.ESResult;
import me.gfuil.bmap.lite.model.Hits;
import me.gfuil.bmap.lite.model.MyPoiModel;
import me.gfuil.bmap.lite.model.TypeMap;
import me.gfuil.bmap.lite.model.TypeNavigation;
import me.gfuil.bmap.lite.model.TypePoi;
import me.gfuil.bmap.lite.model.TypeSearch;
import me.gfuil.bmap.lite.storage.MappedTrajectory;
import me.gfuil.bmap.lite.storage.ObjectBox;
import me.gfuil.bmap.lite.storage.POI;
import me.gfuil.bmap.lite.storage.RawTrajectory;
import me.gfuil.bmap.lite.storage.RiskyTrajectory;
import me.gfuil.bmap.lite.utils.AppUtils;
import me.gfuil.bmap.lite.utils.LogUtils;
import me.gfuil.bmap.lite.utils.PermissionUtils;
import me.gfuil.bmap.lite.utils.StatusBarUtils;
import me.gfuil.bmap.lite.utils.StringUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 首页
 *
 * @author gfuil
 */
public class MainActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, SearchResultRecyclerAdapter.OnSelectSearchResultListener {
    private static final String TAG = "BaseActivity";
    private static final String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int REQUEST_LOCATION = 100;
    public static final int REQUEST_SEARCH = 1000;
    private final static int TIME_UP_OTHER = 2;
    public static boolean IS_FOREGROUND = true;

    private TextView textSearch, mTextPoiName, mTextPoiDistance, mTextStreet, mTextCollection, mTextNearby, mTextDetails, mTextShare;
    private DrawerLayout mDrawer;
    private MenuItem menuItemClose, menuItemClear, menuItemDelete, mMenuRanging;
    private FloatingActionButton btnLine;
    private FrameLayout mLayPoi, mLaySearchResult;
    private BottomSheetBehavior mBehaviorPoi, mBehaviorSearchRseult;
    private BaiduMapFragment baiduMapFragment;
    private AmapFragment mAmapFragment;
    private String mTypeShortcuts;
    private RecyclerView mRecycleResult;
    private SearchResultRecyclerAdapter mSearchPoiResultAdapter;

    public static boolean isExit = false;
    private AMap amap;
    private PieChart pieChart;
    private PieData pieData;

    private com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton floatingMenuBtn;
    private RawTrajectory curTraj;

    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    private boolean isSpatialQueryMode=false;
    private TextView quitQueryTextView;
    //第一次加载时要设置地图监听滑动事件
    private boolean isFirstLoad=true;

    private long firstClick=0;
    private android.app.AlertDialog.Builder queryDialogBuilder;
    private Timer timer;
    private RouteSearch routeSearch;

//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case TIME_UP_OTHER:
//                    getSchemeData(getIntent());
//                    getOneStepData(getIntent());
//                default:
//                    break;
//            }
//        }
//    };

    private Handler handler=new Handler() {


        public void handleMessage(Message msg) {
            switch (msg.what){
                //矩形范围查询不为空
                case (1):
                    //查询不为空
//                    List<List<LatLng>> res= (List<List<LatLng>>) msg.obj;
//                    int i=0;
//                    for(List<LatLng> l:res){
//                        amap.addPolyline(new PolylineOptions()
//                                .useGradient(true)
//                                .color(getResources().getColor(R.color.blue))
//                                .width(10)
//                                .addAll(l));
//                        amap.addMarker(new MarkerOptions().title(String.valueOf(i)).icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_start)).position(l.get(0)));
//                        amap.addMarker(new MarkerOptions().title(String.valueOf(i)).icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_end)).position(l.get(l.size()-1)));
//                    }
                    Toast.makeText(MainActivity.this,"查询成功",Toast.LENGTH_SHORT).show();
                    break;
                case (0):
                    //查询为空
                    Toast.makeText(MainActivity.this, "查询结果为空", Toast.LENGTH_SHORT).show();
                    break;
                //POI映射不为空
                case (2):
                    Toast.makeText(MainActivity.this,"查询成功",Toast.LENGTH_SHORT).show();
                    break;
                case (3):
                    pieChart.setData(pieData);
                    pieChart.invalidate();
                    Legend l=pieChart.getLegend();
                    //l.setOrientation(Legend.LegendOrientation.VERTICAL);
                    l.setWordWrapEnabled(true);
                    pieChart.setVisibility(View.VISIBLE);
                    Description description = new Description();
                    description.setText("");
                    pieChart.setDescription(description);
                    Toast.makeText(MainActivity.this,"请注意风险！",Toast.LENGTH_SHORT).show();
                    break;
                case (4):
                    Toast.makeText(MainActivity.this,"分享成功",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.layout.activity_main);

        getData();
        ObjectBox.init(this);
//        checkTask();
//        initFloatingMenu();
        
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.i("new intent",intent.getExtras().getString("record"));
        getOneStepData(intent);
        getSchemeData(intent);
    }

    private void checkTask() {
        TimerTask task1 = new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(TIME_UP_OTHER);
            }
        };

        timer = new Timer(true);
        timer.schedule(task1, 1000);
    }

    private void getData() {
        if (null != getIntent() && null != getIntent().getDataString()) {
            mTypeShortcuts = getIntent().getDataString();
        }
    }

    private void getSchemeData(Intent intent) {
        if (null != intent) {
            if (null != intent.getDataString()) {
                mTypeShortcuts = intent.getDataString();
            }
            try {
                Uri uri = intent.getData();
                if (uri != null) {
                    String host = uri.getHost();
                    String dataString = intent.getDataString();
                    LogUtils.debug(dataString);
                    if ("maps.google.com".equals(host)) {
                        Map<String, String> requestMap = StringUtils.getUrlRequest(dataString);
                        String saddr = requestMap.get("saddr");
                        String daddr = requestMap.get("daddr");
                        String q = requestMap.get("q");

                        if (null != saddr && !saddr.isEmpty() && null != daddr && !daddr.isEmpty()) {

                            String[] startString = StringUtils.convertStrToArray(saddr, ",");
                            String[] endString = StringUtils.convertStrToArray(daddr, ",");

                            MyPoiModel startPoi = new MyPoiModel(TypeMap.TYPE_GOOGLE);
                            startPoi.setName("我的位置");
                            startPoi.setLatitude(Double.parseDouble(startString[0]));
                            startPoi.setLongitude(Double.parseDouble(startString[1]));

                            MyPoiModel endPoi = new MyPoiModel(TypeMap.TYPE_GOOGLE);
                            endPoi.setName("目的地");
                            endPoi.setLatitude(Double.parseDouble(endString[0]));
                            endPoi.setLongitude(Double.parseDouble(endString[1]));

                            Bundle bundle = new Bundle();
                            bundle.putParcelable("start", startPoi);
                            bundle.putParcelable("end", endPoi);
                            openActivity(RouteActivity.class, bundle, false);
                        } else if (null != q && !q.isEmpty()) {
                            double lat = Double.parseDouble(StringUtils.convertStrToArray(q, ",")[0]);
                            double lng = Double.parseDouble(StringUtils.convertStrToArray(q, ",")[1]);
                            String name = dataString.substring(dataString.indexOf("(") > 0 ? dataString.indexOf("(") : 0);
                            MyPoiModel poi = new MyPoiModel(BApp.TYPE_MAP);
                            poi.setName(!dataString.equals(name) ? name : "外部传来的地点");
                            poi.setLatitude(lat);
                            poi.setLongitude(lng);
                            if (TypeMap.TYPE_AMAP == BApp.TYPE_MAP && null != mAmapFragment) {
                                mAmapFragment.showOtherPoi(poi);
                            } else if (TypeMap.TYPE_BAIDU == BApp.TYPE_MAP && null != baiduMapFragment) {
                                baiduMapFragment.showOtherPoi(poi);
                            }
                        }

                    } else if (null != dataString && dataString.contains("detail")) {
                        Bundle bundle = new Bundle();
                        bundle.putString("url", dataString);
                        openActivity(WebActivity.class, bundle, false);
                    } else if (null != dataString && dataString.contains("geo:")) {

                        String keyword = dataString.substring(dataString.indexOf("geo:") + 4);
                        if (keyword.contains("?q=")) {
                            keyword = keyword.substring(0, keyword.indexOf("?q="));
                        } else if (keyword.contains("?z=")) {
                            keyword = keyword.substring(0, keyword.indexOf("?z="));
                        }
                        if (!"0,0".equals(keyword)) {
                            double lat = Double.parseDouble(StringUtils.convertStrToArray(keyword, ",")[0]);
                            double lng = Double.parseDouble(StringUtils.convertStrToArray(keyword, ",")[1]);

                            String name = dataString.substring(dataString.indexOf("(") > 0 ? dataString.indexOf("(") : 0);

                            MyPoiModel poi = new MyPoiModel(BApp.TYPE_MAP);
                            poi.setName(!dataString.equals(name) ? name : "外部传来的地点");
                            poi.setLatitude(lat);
                            poi.setLongitude(lng);
                            if (TypeMap.TYPE_AMAP == BApp.TYPE_MAP && null != mAmapFragment) {
                                mAmapFragment.showOtherPoi(poi);
                            } else if (TypeMap.TYPE_BAIDU == BApp.TYPE_MAP && null != baiduMapFragment) {
                                baiduMapFragment.showOtherPoi(poi);
                            }
                        } else {
                            keyword = dataString.substring(dataString.indexOf("?q=") + 3);
                            Bundle bundle = new Bundle();
                            bundle.putString("keyword", URLDecoder.decode(keyword, "UTF-8"));
                            bundle.putString("from", "MainActivity");
                            openActivity(SearchActivity.class, bundle, false);
                        }


                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getOneStepData(Intent intent) {
        if (null != intent) {
            if (Intent.ACTION_SEND.equals(intent.getAction()) && "text/plain".equals(intent.getType())) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    if (sharedText.contains("maps.google.com/maps?q=")) {
                        sharedText = sharedText.substring(sharedText.indexOf("q=") + 2);
                        if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP) {
                            sharedText += ",1";
                        } else if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU) {
                            sharedText += ",2";
                        }
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("keyword", sharedText);
                    bundle.putString("from", "MainActivity");
                    openActivity(SearchActivity.class, bundle, false);
                }
            }
        }
    }

    public void onStart() {
        super.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();
        IS_FOREGROUND = true;
        try {
            routeSearch = new RouteSearch(this);
        } catch (AMapException e) {
            e.printStackTrace();
        }
        routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
            @Override
            public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
                Log.i(TAG, "onDriveRouteSearched: " + i + busRouteResult.getPaths().toString());
            }

            @Override
            public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
                Log.i(TAG, "onDriveRouteSearched: " + i + driveRouteResult.getPaths().toString());
            }

            @Override
            public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
                Log.i(TAG, "onDriveRouteSearched: " + i + walkRouteResult.getPaths().toString());
                UserRouteOverlay walkRouteOverlay = new UserRouteOverlay(
                        getApplicationContext(), amap, walkRouteResult.getPaths().get(0),
                        walkRouteResult.getStartPos(),
                        walkRouteResult.getTargetPos());
                walkRouteOverlay.setNodeIconVisibility(false);
                walkRouteOverlay.removeFromMap();
                walkRouteOverlay.addToMap();
                walkRouteOverlay.zoomToSpan();
            }

            @Override
            public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
                Log.i(TAG, "onDriveRouteSearched: " + i + rideRouteResult.getPaths().toString());
            }
        });
        //常亮
        ConfigInteracter configInteracter = new ConfigInteracter(this);
        if (configInteracter.isScreenLightAlways()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if(isFirstLoad){
            initMap1();
            isFirstLoad=false;
        }

        showChoosedTraj("","", new String[0]);
        Intent intent=getIntent();
        if(intent!=null){
            Bundle bundle=intent.getExtras();
            if(bundle!=null) {
                if(bundle.getString("id")!=null) {
                    String id = bundle.getString("id");
                    String record = bundle.getString("record");
                    String starttime=bundle.getString("starttime");
                    String endtime=bundle.getString("endtime");
                    Box<RawTrajectory> rawTrajectoryBox = ObjectBox.get().boxFor(RawTrajectory.class);
                    curTraj = rawTrajectoryBox.get(Long.valueOf(id));
                    showChoosedAddress(id, bundle.getString("ads_record"), bundle.getStringArray("ads_extra"));
                    showChoosedTraj(id, record, bundle.getStringArray("extra"));
                }
//                initFloatingMenu();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        IS_FOREGROUND = false;
    }

    @Override
    protected void initView(int layoutID) {
        super.initView(layoutID);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        textSearch = getView(R.id.text_search);
        mTextPoiName = getView(R.id.text_poi_name);
        mTextPoiDistance = getView(R.id.text_poi_distance);
        mTextCollection = getView(R.id.text_collection);
        mTextStreet = getView(R.id.text_street);
        mTextNearby = getView(R.id.text_nearby);
        mTextDetails = getView(R.id.text_details);
        mTextShare = getView(R.id.text_share);
        mRecycleResult = getView(R.id.recycler_result);

//        mLayPoi = getView(R.id.lay_poi);
//        mLaySearchResult = getView(R.id.lay_search_result);
//
//        btnLine = getView(R.id.fab_line);
//        //mBtnRoute = getView(R.id.fab_route);
//        //btnLine.setOnClickListener(this);
//        textSearch.setOnClickListener(this);
//
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        mRecycleResult.setLayoutManager(layoutManager);
//        mRecycleResult.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        pieChart = getView(R.id.pic_chart);
        pieChart.setVisibility(View.INVISIBLE);

        mLayPoi = getView(R.id.lay_poi);

        quitQueryTextView=findViewById(R.id.quit_query);
        quitQueryTextView.setVisibility(View.INVISIBLE);
        quitQueryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitQueryTextView.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, "退出空间查询模式", Toast.LENGTH_SHORT).show();
                isSpatialQueryMode=false;
                amap.getUiSettings().setAllGesturesEnabled(true);
                amap.clear();
            }
        });

        mLaySearchResult = getView(R.id.lay_search_result);


        btnLine = getView(R.id.fab_line);

        btnLine.setOnClickListener(this);
        textSearch.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleResult.setLayoutManager(layoutManager);
        mRecycleResult.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = getView(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mMenuRanging = navigationView.getMenu().findItem(R.id.nav_ranging);
        if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP) {
            navigationView.getMenu().findItem(R.id.nav_change_map).setTitle("切换百度地图");
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU) {
            navigationView.getMenu().findItem(R.id.nav_change_map).setTitle("切换高德地图");
        }


        int statusHeight = StatusBarUtils.getStatusBarHeight(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtils.setStatusBarColor(this, Color.TRANSPARENT);

            CardView cardView = getView(R.id.card_view);
            CoordinatorLayout.LayoutParams layoutParams2 = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AppUtils.dip2Px(this, 45));
            layoutParams2.topMargin = statusHeight + AppUtils.dip2Px(this, 10);
            layoutParams2.rightMargin = AppUtils.dip2Px(this, 10);
            layoutParams2.leftMargin = AppUtils.dip2Px(this, 10);
            layoutParams2.bottomMargin = AppUtils.dip2Px(this, 10);
            cardView.setLayoutParams(layoutParams2);

//            FrameLayout layStatus = getView(R.id.lay_status);
//            layStatus.setLayoutParams(new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusHeight));
//            layStatus.setVisibility(View.VISIBLE);

        }

        initMap();

        showPoiLay(null, -1);

        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU) {
            mAmapFragment = AmapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.lay_content, mAmapFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();

        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP) {
            mAmapFragment = AmapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.lay_content, mAmapFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        }


    }

    private void initMap() {
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU) {
            baiduMapFragment = BaiduMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.lay_content, baiduMapFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP) {
            mAmapFragment = AmapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.lay_content, mAmapFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        }

        mBehaviorPoi = BottomSheetBehavior.from(mLayPoi);
        mBehaviorPoi.setHideable(true);
        mBehaviorPoi.setState(BottomSheetBehavior.STATE_HIDDEN);
        mBehaviorPoi.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    showPoiLay(null, -1);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBehaviorPoi.setState(BottomSheetBehavior.STATE_HIDDEN);
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (-1 <= slideOffset && 0 >= slideOffset) {
                    float behaviorHeight = getResources().getDimension(R.dimen.bottom_poi_option_height);
                    btnLine.setTranslationY((-1 - slideOffset) * (behaviorHeight - AppUtils.dip2Px(MainActivity.this, 66)));
                    if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
                        baiduMapFragment.setBtnLocationTranslationY((-1 - slideOffset) * (behaviorHeight - AppUtils.dip2Px(MainActivity.this, 40)));
                    } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
                        mAmapFragment.setBtnLocationTranslationY((-1 - slideOffset) * (behaviorHeight - AppUtils.dip2Px(MainActivity.this, 40)));
                    }
                }
            }
        });

        mBehaviorSearchRseult = BottomSheetBehavior.from(mLaySearchResult);
        mBehaviorSearchRseult.setState(BottomSheetBehavior.STATE_HIDDEN);
        mBehaviorSearchRseult.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    btnLine.show();

                } else {
                    btnLine.hide();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }


    private void initMap1() {

        final LatLng[] points = new LatLng[2];
        final PolygonOptions polygonOptions=new PolygonOptions();
        polygonOptions.strokeColor(getResources().getColor(R.color.keyword11));
        polygonOptions.strokeWidth(2f);
        polygonOptions.fillColor(Color.parseColor("#11000000"));
        amap=mAmapFragment.mAmap;
        final Polygon[] rec = {amap.addPolygon(polygonOptions)};
        amap.getUiSettings().setZoomGesturesEnabled(false);

        amap.setOnMapTouchListener(new AMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.i("Action down", String.valueOf(firstClick));
                        if(isSpatialQueryMode) {
                            double down_x = motionEvent.getX();
                            double down_y = motionEvent.getY();
                            Point p = new Point();
                            p.set((int) down_x, (int) down_y);
                            LatLng left_up = amap.getProjection().fromScreenLocation(p);
                            points[0] = left_up;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(isSpatialQueryMode) {
                            if (null != rec[0]) {
                                rec[0].remove();
                            }
                            polygonOptions.getPoints().clear();
                            double moving_x = motionEvent.getX();
                            double moving_y = motionEvent.getY();
                            Point movingP = new Point();
                            movingP.set((int) moving_x, (int) moving_y);
                            LatLng right_down = amap.getProjection().fromScreenLocation(movingP);
                            points[1] = right_down;
                            List<LatLng> res = createRectangle(points[0], right_down);
                            for (LatLng l : res) {
                                polygonOptions.add(l);
                            }
                            rec[0] = amap.addPolygon(polygonOptions);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if(isSpatialQueryMode) {
                            if(queryDialogBuilder==null) {
                                queryDialogBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
                                queryDialogBuilder.setTitle("提示");
                                queryDialogBuilder.setMessage("是否查询当前区域内的轨迹");
                                queryDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                List<List<LatLng>> res = queryTrajInRect(points[0], points[1]);
                                                Message msg = Message.obtain();
                                                msg.obj = res;
                                                if (res.size() == 0) {
                                                    msg.what = 0;
                                                } else {
                                                    msg.what = 1;
                                                }
                                                handler.sendMessage(msg);
                                            }
                                        }).start();
                                    }
                                });
                                queryDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(rec[0]!=null){
                                            rec[0].remove();
                                            amap.clear();
                                        }

                                    }
                                });
                            }
                            queryDialogBuilder.show();
                        }

                        break;
                }
            }
        });

        amap.setMyLocationEnabled(true);

    }

    public void initFloatingMenu(){
        if(floatingMenuBtn!=null){
            floatingMenuBtn.setVisibility(View.VISIBLE);
            return;
        }
        final ImageView fabIconNew = new ImageView(this);
        fabIconNew.setImageDrawable(getResources().getDrawable(R.drawable.plus));
        floatingMenuBtn = new com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton.Builder(this).setContentView(fabIconNew)
                .setPosition(com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton.POSITION_LEFT_CENTER)
                .build();

        SubActionButton.Builder rLSubBuilder = new SubActionButton.Builder(this);
        ImageView rLIcon1 = new ImageView(this);
        ImageView rLIcon2 = new ImageView(this);
        ImageView rLIcon3 = new ImageView(this);
        ImageView rLIcon4 = new ImageView(this);
        ImageView rLIcon5 = new ImageView(this);
        ImageView rLIcon6 = new ImageView(this);
        // 映射至bus路线,映射至POI,附近风险,关闭菜单
        rLIcon1.setImageDrawable(getResources().getDrawable(R.drawable.amap_bus));
        rLIcon2.setImageDrawable(getResources().getDrawable(R.drawable.amap_through));
        rLIcon3.setImageDrawable(getResources().getDrawable(R.drawable.ic_info_24dp));
        rLIcon4.setImageDrawable(getResources().getDrawable(R.drawable.ic_undo_24dp));
        rLIcon5.setImageDrawable(getResources().getDrawable(R.drawable.ic_share_18dp));
        rLIcon6.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_white_24dp));

        final FloatingActionMenu rightLowerMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(rLSubBuilder.setContentView(rLIcon1).build())
                .addSubActionView(rLSubBuilder.setContentView(rLIcon2).build())
                .addSubActionView(rLSubBuilder.setContentView(rLIcon3).build())
                .addSubActionView(rLSubBuilder.setContentView(rLIcon5).build())
                .addSubActionView(rLSubBuilder.setContentView(rLIcon6).build())
                .addSubActionView(rLSubBuilder.setContentView(rLIcon4).build())
                .attachTo(floatingMenuBtn).build();

        //显示Bus路线
        rLIcon1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //计算bus路线
                if(curTraj==null){
                    Toast.makeText(MainActivity.this, "请选择一条轨迹数据", Toast.LENGTH_SHORT).show();
                }
                else{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String filePath = getApplicationContext().getFilesDir().getAbsolutePath();
                            String fileName = filePath + "/" + "wuhan_poi_index";
                            GridIndex.loadIndex(fileName);
                            Log.i("curTraj", curTraj.record);
                            List<TrajEvent> events = TrajExtractor.executeForBusLines(curTraj.record, curTraj.starttime, curTraj.endtime);
                            for (TrajEvent e : events) {
                                LatLng latLng = GPSConverterUtils.bd09_To_Gcj02(e.loc.lat, e.loc.lng);
                                if (e.getTrajEventType() == TrajEventType.ON_BUS) {
                                    amap.addMarker(new MarkerOptions().title("上车").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)).position(latLng));

                                } else {
                                    amap.addMarker(new MarkerOptions().title("下车").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)).position(latLng));

                                }
                            }

                            Message msg = Message.obtain();
                            if (events.size() == 0) {
                                msg.what = 0;
                            } else {
                                msg.what = 2;
                            }

                            handler.sendMessage(msg);
                        }
                    }).start();
                }
            }
        });


        //显示POI
        rLIcon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curTraj==null){
                    Toast.makeText(MainActivity.this, "请选择一条轨迹数据", Toast.LENGTH_SHORT).show();
                }
                else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String filePath = getApplicationContext().getFilesDir().getAbsolutePath();
                            String fileName = filePath + "/" + "wuhan_poi_index";
                            GridIndex.loadIndex(fileName);
                            Box<POI> poiBox = ObjectBox.get().boxFor(POI.class);
                            Log.i("curTraj", curTraj.record);
                            List<TrajEvent> events = TrajExtractor.executeForPOIs(curTraj.record, curTraj.starttime, curTraj.endtime);
                            //绘制经过的POI
                            for (TrajEvent te : events) {
                                Log.i("POI　mapping", te.toString());
                                POI poi = poiBox.get(te.loc.id);
                                String name = poi.name;
                                LatLng l = GPSConverterUtils.bd09_To_Gcj02(te.loc.lat, te.loc.lng);
                                amap.addMarker(new MarkerOptions().title(name).icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_through)).position(l));
                            }

                            Message msg = Message.obtain();
                            if (events.size() == 0) {
                                msg.what = 0;
                            } else {
                                msg.what = 2;
                            }

                            handler.sendMessage(msg);

                        }
                    }
                    ).start();
                }
            }
        });


        //显示风险POI,以及附近的风险轨迹
        rLIcon3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curTraj==null){
                    Toast.makeText(MainActivity.this, "请选择一条轨迹数据", Toast.LENGTH_SHORT).show();
                }
                else{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("curTraj", curTraj.record);

                            Box<RiskyTrajectory> riskyTrajectoryBox = ObjectBox.get().boxFor(RiskyTrajectory.class);
                            MappedTrajectory mappedTrajectory = curTraj.mappedTrajectoryToOne.getTarget();
                            List<RiskyTrajectory> riskyTrajectoryList = riskyTrajectoryBox.getAll();
                            List<RiskyTrajectory> candidateTrajs = new ArrayList<>();
                            List<Double> riskVals = new ArrayList<>();
                            for (RiskyTrajectory traj : riskyTrajectoryList) {
                                double riskval = RiskUtils.calculateRisk(mappedTrajectory, traj);
                                if (riskval > 0.0000000000001) {
                                    candidateTrajs.add(traj);
                                    riskVals.add(riskval);
                                }
                            }
                            int totalnum = riskVals.size();
                            Message msg = Message.obtain();
                            if (totalnum == 0) {
                                msg.what = 2;
                            } else {
                                msg.what = 3;
                            }
                            if (totalnum != 0) {
                                int startValue = Color.parseColor("#FF0000");
                                int endValue = Color.parseColor("#00FF00");
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                List<PieEntry> pieEntries = new ArrayList<>();
                                List<Integer> colors = new ArrayList<>();
                                for (int i = 0; i < candidateTrajs.size(); i++) {
                                    int color = (int) argbEvaluator.evaluate((float) i / totalnum, startValue, endValue);
                                    RiskyTrajectory riskyTrajectory = candidateTrajs.get(i);
                                    List<TrajEvent> events = RiskUtils.generateTrajEvents(riskyTrajectory.record);
                                    List<LatLng> latLngs = new ArrayList<>();
                                    colors.add(color);
                                    for (TrajEvent e : events) {
                                        LatLng latLng = new LatLng(e.loc.lat, e.loc.lng);
                                        StringBuffer d = new StringBuffer();
                                        d.append(sdf.format(e.starttime));
                                        d.append("-");
                                        d.append(sdf.format(e.endtime));
                                        amap.addMarker(new MarkerOptions().title(d.toString()).position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.risk)));
                                        latLngs.add(latLng);
                                    }
                                    pieEntries.add(new PieEntry(Float.valueOf(String.valueOf(riskVals.get(i))), String.valueOf(i)));
                                    amap.addPolyline(new PolylineOptions().color(color).addAll(latLngs).width(10).setDottedLine(true));
                                }
                                PieDataSet pieDataSet = new PieDataSet(pieEntries, "相似度");
                                pieDataSet.setColors(colors);
                                pieData = new PieData(pieDataSet);
                                pieData.setDrawValues(true);
                                pieData.setValueTextSize(12f);
                            }

                            handler.sendMessage(msg);
                        }
                    }).start();
                }
            }
        });

        rLIcon4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rightLowerMenu.close(false);
                //floatingMenuBtn.setVisibility(View.INVISIBLE);
                pieChart.setVisibility(View.INVISIBLE);
                amap.clear();

            }
        });

        rLIcon5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = Message.obtain();
                        msg.what = 4;
                        handler.sendMessage(msg);

                    }
                }).start();
            }
        });

        rLIcon6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSpatialQueryMode=true;
                if(quitQueryTextView.getVisibility()!=View.VISIBLE){
                    quitQueryTextView.setVisibility(View.VISIBLE);
                }
                Toast.makeText(MainActivity.this, "进入空间查询模式", Toast.LENGTH_SHORT).show();
                amap.getUiSettings().setAllGesturesEnabled(false);

            }
        });

        rightLowerMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu floatingActionMenu) {
                fabIconNew.setRotation(0);
                PropertyValuesHolder pvhR=PropertyValuesHolder.ofFloat(View.ROTATION,45);
                ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(fabIconNew, pvhR);
                animation.start();
            }

            @Override
            public void onMenuClosed(FloatingActionMenu floatingActionMenu) {
                fabIconNew.setRotation(45);
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 0);
                ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(fabIconNew, pvhR);
                animation.start();
            }
        });



    }

    private void showChoosedTraj(String id,String record, String[] extra){
//        record = "116.478935,116.478939,116.478912,116.478912,116.478998,116.478998,116.479282,116.479658,116.480151,116.480784,116.480784,116.481149,116.481573,116.481863,116.482072,116.482362,116.483633,116.48367,116.484648;39.997761,39.997825,39.998549,39.998549,39.998555,39.998555,39.99856,39.998528,39.998453,39.998302,39.998302,39.998184,39.997997,39.997846,39.997718,39.997718,39.998935,39.998968,39.999861";
        Log.i("record",record);


        if(record.length() > 5){
            String[] latlngs=record.split(";");
            String[] lngs=latlngs[0].split(",");
            String[] lats=latlngs[1].split(",");
            assert (lngs.length==lats.length);
            List<LatLng> coors=new ArrayList<>();
            for(int i=0;i<lngs.length;i++){
                coors.add(new LatLng(Double.parseDouble(lats[i]),Double.parseDouble(lngs[i])));
            }
            Log.i("In main activity:",coors.toString());
            int index = 0;
            LatLng preLatLng = coors.get(0);
            // 绘画轨迹
            for (int i = 1; i < coors.size(); i++) {
                RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(new LatLonPoint(preLatLng.latitude, preLatLng.longitude), new LatLonPoint(coors.get(i).latitude, coors.get(i).longitude));
                int mode = 0;
                RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WALK_DEFAULT);
                routeSearch.calculateWalkRouteAsyn(query);
                preLatLng = coors.get(i);
            }
            for (LatLng latLng: coors) {
                String title = index < extra.length ? extra[index++] : "non";
                if (title.startsWith(",") || title.startsWith(":") || title.startsWith("；") || title.startsWith("。")) {
                    title = title.substring(1);
                }
                if (title.endsWith(",") || title.endsWith(":") || title.endsWith("；") || title.endsWith("。")) {
                    title = title.substring(0, title.length() - 1);
                }
                amap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding_2)).position(latLng).title(title));
            }

//            amap.addMarker(new MarkerOptions().title("起点").icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_start)).position(coors.get(0)));
//            amap.addMarker(new MarkerOptions().title("终点").icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_end)).position(coors.get(coors.size()-1)));
//            Polyline polyline=amap.addPolyline(new PolylineOptions().useGradient(true).color(getResources().getColor(R.color.blue)).width(10).addAll(coors));
//            amap.addPolyline(polyline.getOptions());
            amap.moveCamera(CameraUpdateFactory.changeLatLng(coors.get(0)));

        }
        else{
            Log.i("In main activity:","record is null");
        }
    }

    private void showChoosedAddress(String id,String record, String[] extra){
//        record = "116.478935,116.478939,116.478912,116.478912,116.478998,116.478998,116.479282,116.479658,116.480151,116.480784,116.480784,116.481149,116.481573,116.481863,116.482072,116.482362,116.483633,116.48367,116.484648;39.997761,39.997825,39.998549,39.998549,39.998555,39.998555,39.99856,39.998528,39.998453,39.998302,39.998302,39.998184,39.997997,39.997846,39.997718,39.997718,39.998935,39.998968,39.999861";
        Log.i("record",record);


        if(record.length() > 5){
            String[] latlngs=record.split(";");
            String[] lngs=latlngs[0].split(",");
            String[] lats=latlngs[1].split(",");
            assert (lngs.length==lats.length);
            List<LatLng> coors=new ArrayList<>();
            for(int i=0;i<lngs.length;i++){
                coors.add(new LatLng(Double.parseDouble(lats[i]),Double.parseDouble(lngs[i])));
            }
            Log.i("In main activity:",coors.toString());
            int index = 0;

            for (LatLng latLng: coors) {
                String title = index < extra.length ? extra[index++] : "non";
                if (title.startsWith(",") || title.startsWith(":") || title.startsWith("；") || title.startsWith("。")) {
                    title = title.substring(1);
                }
                if (title.endsWith(",") || title.endsWith(":") || title.endsWith("；") || title.endsWith("。")) {
                    title = title.substring(0, title.length() - 1);
                }
                amap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding_2)).position(latLng).title(title));
            }

//            amap.addMarker(new MarkerOptions().title("起点").icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_start)).position(coors.get(0)));
//            amap.addMarker(new MarkerOptions().title("终点").icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_end)).position(coors.get(coors.size()-1)));
//            Polyline polyline=amap.addPolyline(new PolylineOptions().useGradient(true).color(getResources().getColor(R.color.blue)).width(10).addAll(coors));
//            amap.addPolyline(polyline.getOptions());
            amap.moveCamera(CameraUpdateFactory.changeLatLng(coors.get(0)));

        }
        else{
            Log.i("In main activity:","record is null");
        }
    }

    public void showPoiLay(final MyPoiModel poi, final int distance) {
        if (null != poi) {
            mBehaviorPoi.setState(BottomSheetBehavior.STATE_EXPANDED);
            mTextPoiName.setText(poi.getName());
            mTextPoiDistance.setVisibility(View.VISIBLE);
            if (1000 > distance && 0 < distance) {
                mTextPoiDistance.setText(distance + "米");
            } else if (1000 <= distance) {
                mTextPoiDistance.setText(distance / 1000 + "公里");
            } else {
                mTextPoiDistance.setVisibility(View.GONE);
            }

            if (!"我的位置".equals(poi.getName())) {
                textSearch.setHint(poi.getName());
                mTextShare.setVisibility(View.GONE);
                mTextDetails.setVisibility(View.VISIBLE);
            } else {
                String info = "";

                if (poi.getAccuracy() > 0) {
                    info += "精度" + (int) poi.getAccuracy() + "米以内";
                }
                if (poi.getAltitude() != 0) {
                    info += "  海拔" + (int) poi.getAltitude() + "米";
                }

                mTextPoiDistance.setText(info);
                mTextPoiDistance.setVisibility(View.VISIBLE);
                mTextShare.setVisibility(View.VISIBLE);
                mTextDetails.setVisibility(View.GONE);
                mTextShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareMyLoc();
                    }
                });
            }
        } else {
            textSearch.setHint("搜索地点");
            mBehaviorPoi.setState(BottomSheetBehavior.STATE_HIDDEN);
            mTextDetails.setOnClickListener(null);
            mTextStreet.setOnClickListener(null);
            mTextCollection.setOnClickListener(null);
            mTextNearby.setOnClickListener(null);
            mTextShare.setOnClickListener(null);

            btnLine.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    routeLine();
                }
            });

            return;
        }
        btnLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                int typeRoute = new ConfigInteracter(MainActivity.this).getTypeRoute();
                if (typeRoute == 0) {
                    if (distance <= 1000) {
                        bundle.putSerializable("typeNavi", TypeNavigation.WALK);
                    } else {
                        bundle.putSerializable("typeNavi", TypeNavigation.DRIVE);
                    }
                } else if (typeRoute == 1) {
                    bundle.putSerializable("typeNavi", TypeNavigation.WALK);
                } else if (typeRoute == 3) {
                    bundle.putSerializable("typeNavi", TypeNavigation.BIKE);
                } else if (typeRoute == 4) {
                    bundle.putSerializable("typeNavi", TypeNavigation.DRIVE);
                } else if (typeRoute == 2) {
                    bundle.putSerializable("typeNavi", TypeNavigation.BUS);
                }
                if (null != BApp.MY_LOCATION) {
                    bundle.putParcelable("start", BApp.MY_LOCATION);
                }
                bundle.putParcelable("start", BApp.MY_LOCATION);
                bundle.putParcelable("end", poi);
                openActivity(RouteActivity.class, bundle, false);
            }
        });
        mTextDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == poi.getUid() || poi.getUid().isEmpty()) {
                    onMessage("没有详情信息");
                    return;
                }
                Bundle bundle = new Bundle();
                if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU) {
                    bundle.putString("uid", poi.getUid());
                    bundle.putString("url", "https://map.baidu.com/mobile/webapp/place/detail/qt=inf&uid=" + poi.getUid());
                } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP) {
                    bundle.putString("url", "http://m.amap.com/detail/index/poiid=" + poi.getUid());
                }
                bundle.putParcelable("poi", poi);
                openActivity(WebActivity.class, bundle, false);
            }
        });
        mTextStreet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("poi", poi);
                openActivity(PanoramaActivity.class, bundle, false);
            }
        });
        mTextCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collection(poi);


            }
        });
        mTextNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("type", TypeSearch.NEARBY);
                bundle.putParcelable("nearby", poi);
                bundle.putString("from", "MainActivity");
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_SEARCH);
            }
        });

    }

    private void collection(final MyPoiModel poi) {
        final EditText editName = new EditText(MainActivity.this);
        if (null != poi && null != poi.getName()) editName.setText(poi.getName());
        editName.setHint("请填写名称");
        editName.setSingleLine(true);
        editName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});
        final EditText editInfo = new EditText(MainActivity.this);
        editInfo.setHint("请填写备注信息(非必填)");
        editInfo.setSingleLine(true);
        editInfo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});

        LinearLayout lay = new LinearLayout(MainActivity.this);
        lay.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(30, 5, 30, 5);
        lay.addView(editName, layoutParams);
        lay.addView(editInfo, layoutParams);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("备注");
        builder.setView(lay);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editName.getText().toString().trim();
                String info = editInfo.getText().toString().trim();
                if (name.isEmpty()) {
                    AppUtils.closeKeyboard(editInfo, MainActivity.this);
                    onMessage("未填写名称无法收藏");
                    return;
                }
                poi.setAddress(info);
                poi.setName(name);
                FavoriteInteracter favoriteInteracter = new FavoriteInteracter(MainActivity.this);
                int i = favoriteInteracter.addFavorite(poi);
                if (i > 0) {
                    if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && baiduMapFragment != null) {
                        baiduMapFragment.getFavoriteList();
                    } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && mAmapFragment != null) {
                        mAmapFragment.getFavoriteList();
                    }
                    onMessage("收藏成功");
                } else {
                    onMessage("收藏失败，请尝试修改名称");
                }
                favoriteInteracter.destroy();

                AppUtils.closeKeyboard(editInfo, MainActivity.this);

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppUtils.closeKeyboard(editInfo, MainActivity.this);
            }
        });
        builder.create().show();
    }

    private void shareMyLoc() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "我在这里");
        intent.putExtra(Intent.EXTRA_TEXT, "我在这里 http://maps.google.com/maps?q=" + BApp.MY_LOCATION.getLatitude() + "," + BApp.MY_LOCATION.getLongitude());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, "分享"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menuItemClose = menu.findItem(R.id.action_close);
        menuItemClear = menu.findItem(R.id.action_clear);
        menuItemDelete = menu.findItem(R.id.action_delete);
        menu.findItem(R.id.action_real_time_traffic).setChecked((new ConfigInteracter(this).isTrafficEnable()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (R.id.action_close == id) {

            if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment && baiduMapFragment.isModeRanging()) {
                changeModeRanging(false);
            } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment && mAmapFragment.isModeRanging()) {
                changeModeRanging(false);
            }
            if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
                baiduMapFragment.clearMarker();
            } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
                mAmapFragment.clearMarker();
            }
            showPoiLay(null, -1);
            mBehaviorSearchRseult.setState(BottomSheetBehavior.STATE_HIDDEN);
            menuItemClose.setVisible(false);
            textSearch.setHint("搜索地点");
            mSearchPoiResultAdapter = null;
            mRecycleResult.setAdapter(null);
        } else if (R.id.action_clear == id) {
            clearRangingPoi();
        } else if (R.id.action_delete == id) {
            deleteRangingPoi();
        } else if (R.id.action_real_time_traffic == id) {
            changeTraffic(item);
        } else if (R.id.action_look_angle == id) {
            changeAngle(item);
        } else if (R.id.action_satellite_map == id) {
            changeMapType(item);
        }

        return super.onOptionsItemSelected(item);
    }


    private void clearRangingPoi() {
        setRangingDistance(0);
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
            baiduMapFragment.clearRangingPoi();
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
            mAmapFragment.clearRangingPoi();
        }
    }

    private void deleteRangingPoi() {
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
            baiduMapFragment.deleteRangingPoi();
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
            mAmapFragment.deleteRangingPoi();
        }

    }

    public void setRangingDistance(double distance) {
        if (null != textSearch) {
            String dis = "";
            if (2000 > distance) {
                dis += (int) distance + "m";
            } else {
                dis += String.format("%.1f", distance / 1000) + "km";
            }
            textSearch.setHint(dis);
        }
    }

    public void verifyPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        } else {
            if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
                baiduMapFragment.initLocationSdk();
            } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
                mAmapFragment.initAmapSdk();
            }
        }
    }

    private void requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            showAlertDialog("权限申请", "感谢使用，为了您更好的使用体验，请授予应用获取位置的权限，否则您可能无法正常使用，谢谢您的支持。", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (PermissionUtils.verifyPermissions(grantResults)) {
                if (null != baiduMapFragment) {
                    baiduMapFragment.initLocationSdk();
                }
            } else {
                onMessage("您没有授予所需权限");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != timer) {
            timer.cancel();
        }
        if (isExit) {
            System.exit(0);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_SEARCH == resultCode) {
            mDrawer.closeDrawer(Gravity.START);
            if (null != data && null != data.getExtras()) {

                MyPoiModel poiInfo = data.getExtras().getParcelable("poi");
                List<MyPoiModel> poiAll = data.getExtras().getParcelableArrayList("poiAll");
                int position = data.getExtras().getInt("position");
                if (null != poiInfo) {
                    textSearch.setHint(poiInfo.getName());
                } else if (null != poiAll && !poiAll.isEmpty()) {
                    textSearch.setHint(poiAll.get(position).getName());
                    setSearchResultAdapter(position, poiAll);
                }

                if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
                    baiduMapFragment.onActivityResult(requestCode, resultCode, data);
                } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
                    mAmapFragment.onActivityResult(requestCode, resultCode, data);
                }

            }
        }
    }

    private void setSearchResultAdapter(int position, List<MyPoiModel> poiAll) {
        if (null == mSearchPoiResultAdapter) {
            mSearchPoiResultAdapter = new SearchResultRecyclerAdapter(this, poiAll, BApp.MY_LOCATION);
            mSearchPoiResultAdapter.setOnSelectSearchResultListener(this);
            mRecycleResult.setAdapter(mSearchPoiResultAdapter);
        } else {
            mSearchPoiResultAdapter.setList(poiAll);
            mSearchPoiResultAdapter.notifyDataSetChanged();
        }
        mRecycleResult.scrollToPosition(position);
        mBehaviorSearchRseult.setState(BottomSheetBehavior.STATE_EXPANDED);
        menuItemClose.setVisible(true);
    }

    public void showSearchResultLay(boolean show) {
        if (show) {
            mBehaviorSearchRseult.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            mBehaviorSearchRseult.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }


    private void gotoSearch() {
        String keyword = textSearch.getHint().toString().trim();

        Bundle bundle = new Bundle();
        bundle.putSerializable("type", TypeSearch.CITY);
        if (!"智能巡航".equals(keyword) && !"搜索地点".equals(keyword)) {
            bundle.putString("keyword", keyword);
        }
        bundle.putString("from", "MainActivity");
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_SEARCH);

        textSearch.setHint("搜索地点");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDrawer.isDrawerOpen(Gravity.START)) {
                mDrawer.closeDrawer(Gravity.START);
            } else if (mBehaviorPoi.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mBehaviorPoi.setState(BottomSheetBehavior.STATE_HIDDEN);

            } else {
                exitApp();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出
     */
    private long exitTime = 0;// 记录按返回键时间

    private void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            onMessage("再按一次退出应用程序");
            exitTime = System.currentTimeMillis();
        } else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            isExit = true;
            BApp.exitApp();
        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.text_search == v.getId()) {
            if (mMenuRanging.isChecked()) {
                onMessage("测距模式！可点击右上角关闭按钮x退出该模式");
            } else {
                gotoSearch();
            }
        }
    }

    private void routeLine() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("start", BApp.MY_LOCATION);
        openActivity(RouteActivity.class, bundle, false);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_change_map) {
            changeMap(item);
            mDrawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_ranging) {
            changeModeRanging(!item.isChecked());
            mDrawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_offline_map) {
            openActivity(OfflineMapActivity.class);
        } else if (id == R.id.nav_about) {
            openActivity(AboutActivity.class);
        } else if (id == R.id.nav_favorite) {
            Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
            startActivityForResult(intent, REQUEST_SEARCH);
        } else if (id == R.id.nav_setting) {
            openActivity(SettingActivity.class);
        }else if (id==R.id.record_traj){
            openActivity(RecordTrajActivity.class);
        }else if(id==R.id.search_trajectory){
            openActivity(SearchTrajectoyActivity.class);
        }else if(id==R.id.show_trajectory){
            showAllRiskTrajectory();
            mDrawer.closeDrawer(GravityCompat.START);
        }
        else if(id==R.id.nav_load_data){
            openActivity(DataLoadActivity.class);
        }

        return true;
    }


    private void showAllRiskTrajectory() {
        String url = "http://47.105.33.143:9200/risk_trajectory/_search?size=100";
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.w(TAG, "onEditorAction: setOnEditorActionListener", e);
            }

            @Override
            public void onResponse( Call call, Response response) throws IOException {
                List<TrajItem> trajList = new ArrayList<>();
                Log.w(TAG, "onEditorAction: setOnEditorActionListener"+ response.code(), null);
                Gson gson = new Gson();
                assert response.body() != null;
                String responseText = response.body().string();
                responseText = responseText.replaceFirst("hits", "result");
                ESResult esResult = gson.fromJson(responseText, ESResult.class);
                Log.i(TAG, "onResponse: " + esResult.toString());
                int i = 1;
                for (Hits hits: esResult.getResult().getHits()) {

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    StringBuilder lng = new StringBuilder();
                    StringBuilder lat = new StringBuilder();
                    StringBuilder adsLng = new StringBuilder();
                    StringBuilder adsLat = new StringBuilder();
//                    Log.i(TAG, "onEditorAction: " + hits.toString());

                    Date start = new Date(System.currentTimeMillis());
                    Date end = new Date(0);
                    String[] adsExtra = new String[hits.get_source().getAddress_location() == null ? 0 : hits.get_source().getAddress_location().length];
                    if (hits.get_source().getAddress_location() != null) {

                        for (int k = 0; k < hits.get_source().getAddress_location().length; k++) {
                            String[] adsLngLat = hits.get_source().getAddress_location()[k][0].split(",");
                            if (adsLng.length() != 0) {
                                adsLng.append(",");
                                adsLat.append(",");
                            }
                            adsLng.append(adsLngLat[0]);
                            adsLat.append(adsLngLat[1]);
                            String adsInfo = hits.get_source().getAddress()[k];
                            adsExtra[k] = "居住地：" + adsInfo;
                        }
                    }

                    List<String> actionList = new ArrayList<>();
                    if (hits.get_source().getTrajectory_detail() != null) {
                        String[] splits = hits.get_source().getTrajectory_detail();
                        Arrays.sort(splits);
                        for (String str: splits) {
                            if (!str.contains("@"))break;
                            actionList.add(str.split("@")[1]);
                        }
                    }
//                    Log.i(TAG, "onEditorAction: " + map);
//                    Log.i(TAG, "onEditorAction: " + Arrays.toString(hits.get_source().getTrajectory_location()));
                    String[] extra = new String[hits.get_source().getTrajectory_location() == null ? 0 : hits.get_source().getTrajectory_location().length];
                    if (hits.get_source().getTrajectory_location() != null) {
                        String[] splits = hits.get_source().getTrajectory_location();
                        Arrays.sort(splits);

                        for (int k = 0; k < hits.get_source().getTrajectory_location().length; k++) {

                            String[] splitStrs = splits[k].split("@");
                            String[] traLngLat = splitStrs[1].split(",");
                            if (lng.length() != 0) {
                                lng.append(",");
                                lat.append(",");
                            }
                            lng.append(traLngLat[0]);
                            lat.append(traLngLat[1]);
                            try {
                                Date actDate = sdf.parse(splitStrs[0]);
                                assert actDate != null;
                                if (actDate.getTime() < start.getTime() ) start = actDate;
                                extra[k] = splitStrs[0]+ "：" +actionList.get(k);
                                if (end.getTime() < actDate.getTime()) end = actDate;
//                                Log.i(TAG, "onEditorAction: " + actDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

//                    Log.i(TAG, "onEditorAction: " + Arrays.toString(extra));
//                    Log.i(TAG, "onEditorAction: " + Arrays.toString(adsExtra));

                    String record = lng.toString() + ";" + lat.toString();
                    String adsRecord = adsLng.toString() + ";" + adsLat.toString();
//                    Log.i(TAG, "onEditorAction: " + record);
//                    Log.i(TAG, "onEditorAction: " + adsRecord);
                    if (record.length() > 5 ) {
                            trajList.add(new TrajItem(i, sdf.format(start), sdf.format(end), record, i++, start.getTime(), end.getTime(), DistanceUtils.getTrajdistance(record), extra, adsRecord, adsExtra));
                    }

                }

                for (TrajItem trajItem: trajList) {
                    showChoosedTraj("0", trajItem.record, trajItem.actions);
                    showChoosedAddress("0", trajItem.ads_record, trajItem.address_info);
                }
            }

        });
    }

    private void changeMap(MenuItem item) {
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU) {
            if (null == mAmapFragment) {
                mAmapFragment = AmapFragment.newInstance();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.lay_content, mAmapFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();

            BApp.TYPE_MAP = TypeMap.TYPE_AMAP;
            item.setTitle("切换百度地图");
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP) {
            if (new ConfigInteracter(this).getNightMode() == 2) {
                Toast.makeText(this, "夜间模式下百度地图可能需要重启应用后生效", Toast.LENGTH_LONG).show();
            }

            if (null == baiduMapFragment) {
                baiduMapFragment = BaiduMapFragment.newInstance();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.lay_content, baiduMapFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();

            BApp.TYPE_MAP = TypeMap.TYPE_BAIDU;
            item.setTitle("切换高德地图");
        }

        new ConfigInteracter(this).setTypeMap(BApp.TYPE_MAP);
    }

    private void changeModeRanging(boolean isChecked) {
        showPoiLay(null, -1);
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
            baiduMapFragment.setModeRanging(isChecked);
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
            mAmapFragment.setModeRanging(isChecked);
        }
        mMenuRanging.setChecked(isChecked);
        menuItemDelete.setVisible(isChecked);
        menuItemClear.setVisible(isChecked);
        menuItemClose.setVisible(isChecked);
        if (isChecked) {
            textSearch.setHint("0m");
        } else {
            textSearch.setHint("搜索地点");
        }
    }

    public void changeTraffic(MenuItem item) {
        ConfigInteracter configInteracter = new ConfigInteracter(this);
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
            boolean isTraffic = !baiduMapFragment.isTrafficEnabled();
            baiduMapFragment.setTrafficEnabled(isTraffic);
            item.setChecked(isTraffic);

            configInteracter.setTrafficEnable(isTraffic);
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
            boolean isTraffic = !mAmapFragment.isTrafficEnabled();
            mAmapFragment.setTrafficEnabled(isTraffic);
            item.setChecked(isTraffic);

            configInteracter.setTrafficEnable(isTraffic);
        }
    }

    private void changeAngle(MenuItem item) {
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
            boolean isLookAngle = !(baiduMapFragment.getMapStatus().overlook == -45);
            if (isLookAngle) {
                item.setTitle("平视角度");

                MapStatus ms = new MapStatus.Builder(baiduMapFragment.getMapStatus()).overlook(-45).build();
                baiduMapFragment.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));

            } else {
                item.setTitle("俯视(3D)角度");
                MapStatus ms = new MapStatus.Builder(baiduMapFragment.getMapStatus()).overlook(90).build();
                baiduMapFragment.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
            }
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
            if ("平视角度".equals(item.getTitle())) {
                mAmapFragment.changeTilt(0);
                item.setTitle("俯视(3D)角度");
            } else {
                mAmapFragment.changeTilt(45);
                item.setTitle("平视角度");
            }
        }
    }

    private void changeMapType(MenuItem item) {
        ConfigInteracter interacter = new ConfigInteracter(this);
        if (BApp.TYPE_MAP == TypeMap.TYPE_BAIDU && null != baiduMapFragment) {
            boolean isSatellite = !(baiduMapFragment.getMapType() == BaiduMap.MAP_TYPE_NORMAL);
            if (isSatellite) {
                item.setTitle("卫星图像");
                baiduMapFragment.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            } else {
                item.setTitle("平面地图");
                baiduMapFragment.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
            }
        } else if (BApp.TYPE_MAP == TypeMap.TYPE_AMAP && null != mAmapFragment) {
            boolean isSatellite = !(mAmapFragment.getMapType() == AMap.MAP_TYPE_NORMAL || mAmapFragment.getMapType() == AMap.MAP_TYPE_NIGHT);
            if (isSatellite) {
                item.setTitle("卫星图像");
                if (interacter.getNightMode() == 2) {
                    mAmapFragment.setMapType(AMap.MAP_TYPE_NIGHT);
                } else {
                    mAmapFragment.setMapType(AMap.MAP_TYPE_NORMAL);
                }

            } else {
                item.setTitle("平面地图");
                mAmapFragment.setMapType(AMap.MAP_TYPE_SATELLITE);
            }
        }
    }


    @Override
    public void onMessage(String msg) {
        hideProgress();
        Snackbar.make(btnLine, msg, Snackbar.LENGTH_SHORT).show();
    }

    public void firstLocationComplete() {
        if ("search".equals(mTypeShortcuts)) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("type", TypeSearch.CITY);
            bundle.putString("from", "MainActivity");
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_SEARCH);
        }
    }

    @Override
    public void onClick(MyPoiModel poi) {
        if (TypeMap.TYPE_BAIDU == BApp.TYPE_MAP && null != baiduMapFragment) {
            if (poi.getTypePoi() == TypePoi.BUS_LINE || poi.getTypePoi() == TypePoi.SUBWAY_LINE) {
                baiduMapFragment.mTypePoi = poi.getTypePoi();
                baiduMapFragment.searchBusLine(poi.getCity(), poi.getUid());
                showPoiLay(null, -1);
            } else {
                baiduMapFragment.showOtherPoi(poi);
            }
        } else if (TypeMap.TYPE_AMAP == BApp.TYPE_MAP && null != mAmapFragment) {
            mAmapFragment.showOtherPoi(poi);
        }
    }

    private List<LatLng> createRectangle(LatLng leftup,LatLng rightdown){
        LatLng p1=new LatLng(leftup.latitude,leftup.longitude);
        LatLng p2=new LatLng(leftup.latitude,rightdown.longitude);
        LatLng p3=new LatLng(rightdown.latitude,rightdown.longitude);
        LatLng p4=new LatLng(rightdown.latitude,leftup.longitude);
        List<LatLng> res=new ArrayList<>();
        res.add(p1);
        res.add(p2);
        res.add(p3);
        res.add(p4);
        return res;
    }

    public List<List<LatLng>> queryTrajInRect(LatLng lu,LatLng rd){
        List<List<LatLng>> res=new ArrayList<>();
        Box<RawTrajectory> rawTrajectoryBox = ObjectBox.get().boxFor(RawTrajectory.class);
        List<RawTrajectory>  rawTrajectoryList=rawTrajectoryBox.getAll();
        for(RawTrajectory traj:rawTrajectoryList){
            List<LatLng> cur= RangeQuery.queryByRec(traj.record,lu,rd);
            if(cur!=null){
                res.add(cur);
            }
        }
        return res;
    }

}
