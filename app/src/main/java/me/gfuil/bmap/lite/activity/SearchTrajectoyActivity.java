package me.gfuil.bmap.lite.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.objectbox.Box;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.adapter.TrajItem;
import me.gfuil.bmap.lite.algorithm.DistanceUtils;
import me.gfuil.bmap.lite.model.ESResult;
import me.gfuil.bmap.lite.model.Hits;
import me.gfuil.bmap.lite.storage.MappedTrajectory;
import me.gfuil.bmap.lite.storage.ObjectBox;
import me.gfuil.bmap.lite.storage.RawTrajectory;
import me.gfuil.bmap.lite.utils.HttpUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SearchTrajectoyActivity extends AppCompatActivity {

    private static final String TAG = "SearchTrajectoyActivity";
    private Date startDate;
    private Date endDate;

    private boolean hasStartTime=false;
    private boolean hasEndTime=false;

    private ImageView startDateView;
    private ImageView endDateView;
    private EditText searchText;

    private ListView trajListView;
    private ListView trajDetailListView;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private List<TrajItem> choosedItems = new ArrayList<>();

    TrajAdapter trajAdapter;
    TrajDetailAdapter trajDetailAdapter;
    private String searchKeyword;

    private List<TrajItem> trajList;
    private List<String[]> trajDetailList;
    private Date choosedDate;
    private Handler handler=new Handler() {


        public void handleMessage(Message msg) {
            switch (msg.what){
                case (1):
                    trajAdapter.notifyDataSetChanged();
                    trajListView.setAdapter(trajAdapter);
                    trajDetailAdapter.notifyDataSetChanged();
                    trajDetailListView.setAdapter(trajDetailAdapter);
                    //searchText.setText(searchKeyword.replace("\n",""));
                    break;
                case (0):
                    Toast.makeText(SearchTrajectoyActivity.this,"查询失败",Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_trajectoy);
        ObjectBox.init(this);
        initView();
    }



    private void initView(){
        startDate=new Date();
        endDate=new Date();
        startDateView=findViewById(R.id.start_time);
        endDateView=findViewById(R.id.end_time);
        searchText=findViewById(R.id.text_search);
        trajList=new ArrayList<>();
        trajDetailList = new ArrayList<>();
        trajListView=findViewById(R.id.list_result);
        trajDetailListView=findViewById(R.id.list_detail);
        startDateView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showStartDatePickDialog(startDate);
                        hasStartTime=true;
                        Log.i("Start Date choosed",startDate.toString());
                    }
                }
        );
        endDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndDatePickDialog(endDate);
                hasEndTime=true;
                Log.i("End Date choosed",endDate.toString());
            }
        });

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            // 输入回车符时判定输入完成
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //Toast.makeText(getApplicationContext(), searchText.getText().toString(), Toast.LENGTH_SHORT).show();
                trajList.clear();
                trajDetailList.clear();
                searchRawTrajectoryByPOI(searchText.getText().toString());

//                String url = "http://47.105.33.143:9200/risk_trajectory/_search";
                String url = "http://47.105.33.143:9200/risk_trajectory/_search?size=30";
                if (searchText.length() > 1) url += "&q=trajectory:" + searchText.getText().toString();
                String[] splits = searchText.getText().toString().split(" ");
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = null;
                if (splits.length == 3) {
                    url = "http://47.105.33.143:9200/risk_trajectory/_search";
                    String json = "{\n" +
                            "  \"query\": {\n" +
                            "    \"bool\": {\n" +
                            "      \"must\": {\n" +
                            "        \"match_all\": {}\n" +
                            "      },\n" +
                            "      \"filter\": {\n" +
                            "        \"geo_distance\": {\n" +
                            "          \"distance\": \"" + splits[2] + "\",\n" +
                            "          \"location\": {\n" +
                            "            \"lat\": " + splits[1] + ",\n" +
                            "            \"lon\": " + splits[0] +"\n" +
                            "          }\n" +
                            "        }\n" +
                            "      }\n" +
                            "    }\n" +
                            "  }\n" +
                            "}";
                    RequestBody body = RequestBody.create(JSON, json);
                    request = new Request.Builder()
                            .url(url)
                            .post(body)//默认就是GET请求，可以不写
                            .build();
                    Log.i(TAG, "RangeQuery: \n" + json);
                }else {
                    request = new Request.Builder()
                            .url(url)
                            .get()//默认就是GET请求，可以不写
                            .build();
                }


                Call call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.w(TAG, "onEditorAction: setOnEditorActionListener", e);
                    }

                    @Override
                    public void onResponse( Call call, Response response) throws IOException {
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
                                Log.i(TAG, "onEditorAction: " + startDate);
                                Log.i(TAG, "onEditorAction: " + hasStartTime);
                                Log.i(TAG, "onEditorAction: " + endDate);
                                Log.i(TAG, "onEditorAction: " + hasEndTime);
                                assert start != null;
                                assert end != null;
                                if (hasStartTime && hasEndTime) {
                                    Log.i(TAG, "onEditorAction: " + start.getTime());
                                    Log.i(TAG, "onEditorAction: " + startDate.getTime());
                                    Log.i(TAG, "onEditorAction: " + end.getTime());
                                    Log.i(TAG, "onEditorAction: " + endDate.getTime());
                                    if (end.getTime() < endDate.getTime() && start.getTime() > startDate.getTime()) {
                                        trajList.add(new TrajItem(i, sdf.format(start), sdf.format(end), record, i++, start.getTime(), end.getTime(), DistanceUtils.getTrajdistance(record), extra, adsRecord, adsExtra));
                                    }
                                }else if (hasEndTime) {
                                    if (end.getTime() < endDate.getTime()) {
                                        trajList.add(new TrajItem(i, sdf.format(start), sdf.format(end), record, i++, start.getTime(), end.getTime(), DistanceUtils.getTrajdistance(record), extra, adsRecord, adsExtra));
                                    }
                                }else if (hasStartTime) {
                                    if (start.getTime() > startDate.getTime()) {
                                        trajList.add(new TrajItem(i, sdf.format(start), sdf.format(end), record, i++, start.getTime(), end.getTime(), DistanceUtils.getTrajdistance(record), extra, adsRecord, adsExtra));
                                    }
                                }else {
                                    trajList.add(new TrajItem(i, sdf.format(start), sdf.format(end), record, i++, start.getTime(), end.getTime(), DistanceUtils.getTrajdistance(record), extra, adsRecord, adsExtra));
                                }
                            }

                        }
                        hasStartTime = false;
                        hasEndTime = false;
                        Message msg = Message.obtain();
                        msg.what = 1;
                        handler.sendMessage(msg);
                        for (TrajItem item: trajList) {
                            trajDetailList.add(item.actions);
                        }
                    }

                });

                searchText.setText(searchText.getText().toString().replace("\n",""));

                return false;
            }
        });
        trajAdapter=new TrajAdapter(SearchTrajectoyActivity.this, R.layout.trajectory_item,trajList);

        trajDetailAdapter = new TrajDetailAdapter(SearchTrajectoyActivity.this, R.layout.trajectory_detail_item,trajDetailList);
        trajListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        trajDetailListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        trajDetailListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        trajListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // 点击以后跳转到主界面对轨迹进行可视化
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(SearchTrajectoyActivity.this,MainActivity.class);
                TrajItem item= (TrajItem) trajList.get(position);
                Bundle bundle=new Bundle();
                bundle.putString("id", String.valueOf(item.id));
                bundle.putString("record",item.record);
                bundle.putString("starttime", String.valueOf(item.starttimeVal));
                bundle.putString("endtime",String.valueOf(item.endtimeVal));
                bundle.putStringArray("extra", item.actions);
                bundle.putString("ads_record", item.ads_record);
                bundle.putStringArray("ads_extra", item.address_info);
                intent.putExtras(bundle);
                Log.i("Choosed item:",String.valueOf(position));
                Log.i("choosed item:", String.valueOf(Integer.valueOf(item.id)));
                Log.i("choose record:", String.valueOf(item.record));

                startActivity(intent);
                /*Intent intent = new Intent(SearchTrajectoyActivity.this,TrajectoryDetailActivity.class);
                Bundle bundle=new Bundle();
                TrajItem item= (TrajItem) trajList.get(position);
                bundle.putString("id", String.valueOf(item.id));
                bundle.putString("record",item.record);
                intent.putExtras(bundle);
                startActivity(intent);*/

            }
        });

        // 设置多选之后的操作
        trajListView.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                TrajItem item= (TrajItem) trajListView.getItemAtPosition(position);
                if(trajListView.isItemChecked(position)){
                    choosedItems.add(item);
                }
                else{
                    choosedItems.remove(item);
                }
                trajAdapter.notifyDataSetChanged();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater mi=getMenuInflater();
                mi.inflate(R.menu.search_activity_menu,menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch(item.getItemId()){
                    case R.id.select_all:
                        selectALl();
                        Toast.makeText(SearchTrajectoyActivity.this, "select all", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.delete_all:
                        deleteAll();
                        Toast.makeText(SearchTrajectoyActivity.this, "delete all", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                trajListView.clearChoices();
                trajAdapter.notifyDataSetChanged();
            }
        });

    }

    public void onResume(){
        super.onResume();
        hasStartTime=false;
        hasEndTime=false;
    }

    private void showStartDatePickDialog(final Date date){
        Calendar c=Calendar.getInstance();
        DatePickerDialog dpd=new DatePickerDialog(SearchTrajectoyActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date.setYear(year-1900);
                date.setMonth(month);
                date.setDate(dayOfMonth);
                date.setHours(0);
                date.setMinutes(0);
                date.setSeconds(0);
                Log.i("Choosed time",date.toString());
            }
        },c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
        dpd.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar instance = Calendar.getInstance();
                instance.set(year, month, day);
                startDate = instance.getTime();
            }
        });
    }
    private void showEndDatePickDialog(final Date date){
        Calendar c=Calendar.getInstance();
        DatePickerDialog dpd=new DatePickerDialog(SearchTrajectoyActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date.setYear(year-1900);
                date.setMonth(month);
                date.setDate(dayOfMonth);
                date.setHours(0);
                date.setMinutes(0);
                date.setSeconds(0);
                Log.i("Choosed time",date.toString());
            }
        },c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
        dpd.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar instance = Calendar.getInstance();
                instance.set(year, month, day);
                endDate = instance.getTime();
            }
        });
    }
    private void searchRawTrajectoryByPOI(final String poi){

        new Thread(new Runnable() {
            @Override
            public void run() {
                trajList.clear();
                trajDetailList.clear();
                Box<RawTrajectory> rtrajBox = ObjectBox.get().boxFor(RawTrajectory.class);
                List<RawTrajectory> rtrajList = rtrajBox.getAll();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                int i = 1;
                for (RawTrajectory l : rtrajList) {
                    String starttime = formatter.format(new Date(l.starttime));
                    String endtime = formatter.format(new Date(l.endtime));
                    double distance = DistanceUtils.getTrajdistance(l.record);
                    String record = l.record;

                    Log.i("search progress", "starttime: " + String.valueOf(l.starttime));
                    Log.i("search progress", "endtime: " + String.valueOf(l.endtime));
                    Log.i("search progress", String.valueOf(startDate.getTime()));
                    Log.i("search progress", String.valueOf(endDate.getTime()));
                    Log.i("search progress", poi);
                    if (hasStartTime && l.starttime < startDate.getTime()) {
                        continue;
                    }
                    if (hasEndTime && l.endtime > endDate.getTime()) {
                        continue;
                    }
                    MappedTrajectory mappedTrajectory = l.mappedTrajectoryToOne.getTarget();
                    if (mappedTrajectory == null) {
                        continue;
                    } else {
                        if (!mappedTrajectory.record.contains(poi)) {
                            continue;
                        }
                    }
//                    trajList.add(new TrajItem((int) l.id, starttime, endtime, record, i, l.starttime, l.endtime, distance, ));
                    i++;
                }

                hasStartTime = false;
                hasEndTime = false;
                Message msg = Message.obtain();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }).start();

    }



    public void selectALl(){
        for(int i=0;i<trajAdapter.getCount();i++){
            trajListView.setItemChecked(i,true);
        }
        trajAdapter.notifyDataSetChanged();
    }

    public void deleteAll(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Box<RawTrajectory> rtrajBox = ObjectBox.get().boxFor(RawTrajectory.class);
                for (TrajItem item : choosedItems) {
                    rtrajBox.remove(item.id);
                    trajList.remove(item);
                    trajDetailList.remove(item);
                }
                choosedItems.clear();
                Message msg = Message.obtain();
                msg.what = 1;
                handler.sendMessage(msg);

            }
        }).start();
    }




    class TrajAdapter extends ArrayAdapter<TrajItem> {
        private int resourceId;

        public TrajAdapter(Context context, int textResourseId, List<TrajItem> trajs){
            super(context,textResourseId,trajs);
            resourceId=textResourseId;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            TrajItem traj=getItem(position);

            ViewHolder vh;
            if(null==view){
                view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
                vh = new ViewHolder() ;
                vh.idTextView = view.findViewById(R.id.traj_id);
                vh.startTimeTextView = view.findViewById(R.id.start_time);
                vh.endTimeTextView = view.findViewById(R.id.end_time);
                vh.distanceView=view.findViewById(R.id.distance_val);
                view.setTag(vh);
            }
            else{
                vh = (ViewHolder) view.getTag();
            }
            vh.idTextView.setText(String.valueOf(traj.presentId));
            vh.startTimeTextView.setText(traj.start_time);
            vh.endTimeTextView.setText(traj.end_time);
            vh.distanceView.setText(new BigDecimal(traj.distance/1000).setScale(2,BigDecimal.ROUND_HALF_UP)+"(km)");
            if(trajListView.isItemChecked(position)){
                view.setBackgroundColor(Color.parseColor("#33ABA1"));
            }
            else{
                view.setBackgroundColor(Color.TRANSPARENT);
            }

            return view;
        }

        class ViewHolder{
            TextView idTextView;
            TextView startTimeTextView;
            TextView endTimeTextView;
            TextView distanceView;
        }

    }
    class TrajDetailAdapter extends ArrayAdapter<String[]> {
        private int resourceId;

        public TrajDetailAdapter(Context context, int textResourseId, List<String[]> trajs){
            super(context,textResourseId,trajs);
            resourceId=textResourseId;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            String[] traj=getItem(position);

            ViewHolder vh;
            if(null==view){
                view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
                vh = new ViewHolder() ;
                vh.trajDetailTextView = view.findViewById(R.id.traj_detail);
                view.setTag(vh);
            }
            else{
                vh = (ViewHolder) view.getTag();
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (String s: traj) {
                stringBuilder.append(s);
                if (stringBuilder.length() > 64) break;
            }

            vh.trajDetailTextView.setText(stringBuilder.toString());
            if(trajDetailListView.isItemChecked(position)){
                view.setBackgroundColor(Color.parseColor("#33ABA1"));
            }
            else{
                view.setBackgroundColor(Color.TRANSPARENT);
            }

            return view;
        }

        class ViewHolder{
            TextView trajDetailTextView;
        }

    }

}