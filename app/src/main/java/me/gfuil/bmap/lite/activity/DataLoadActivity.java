package me.gfuil.bmap.lite.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.maple.filepickerlibrary.FilePicker;
import com.maple.filepickerlibrary.model.EssFile;
import com.maple.filepickerlibrary.util.Const;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.query.Query;
import io.objectbox.relation.ToMany;
import me.gfuil.bmap.lite.Index.GridIndex;
import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.algorithm.TrajEvent;
import me.gfuil.bmap.lite.algorithm.TrajExtractor;
import me.gfuil.bmap.lite.storage.BusLine;
import me.gfuil.bmap.lite.storage.BusLine_;
import me.gfuil.bmap.lite.storage.BusStop;
import me.gfuil.bmap.lite.storage.BusStop_;
import me.gfuil.bmap.lite.storage.MappedTrajectory;
import me.gfuil.bmap.lite.storage.ObjectBox;
import me.gfuil.bmap.lite.storage.POI;
import me.gfuil.bmap.lite.storage.RawTrajectory;
import me.gfuil.bmap.lite.storage.RiskyTrajectory;
import me.gfuil.bmap.lite.storage.StopOrder;



public class DataLoadActivity extends AppCompatActivity {


    private Button chooseFileBtn;
    private Button loadDataBtn;
    private Button buildIndexBtn;
    private Button deleteBtn;

    private Button mapTrajBtn;

    private Spinner fileTypeSpin;
    private String fileTypeStr;

    private Spinner dataTypeSpin;
    private String dataTypeStr;

    private String dataFilePath="";



    private Handler handler=new Handler() {

        public void handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case (1):
                    Toast.makeText(DataLoadActivity.this, "POI导入成功", Toast.LENGTH_SHORT).show();
                    break;
                case (0):
                    Toast.makeText(DataLoadActivity.this,"POI导入失败",Toast.LENGTH_SHORT).show();
                    break;
                case (3):
                    Toast.makeText(DataLoadActivity.this,"POI索引创建失败",Toast.LENGTH_SHORT).show();
                    break;
                case (2):
                    Toast.makeText(DataLoadActivity.this,"POI索引创建成功",Toast.LENGTH_SHORT).show();
                    break;
                case (4):
                    Toast.makeText(DataLoadActivity.this,"轨迹映射成功",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_load);
        ObjectBox.init(this);
        initView();
    }


    private void initView() {
        chooseFileBtn = findViewById(R.id.choose_file);
        loadDataBtn = findViewById(R.id.load_data);
        deleteBtn = findViewById(R.id.delete_data);
        fileTypeSpin = findViewById(R.id.file_type);
        dataTypeSpin = findViewById(R.id.data_type);
        buildIndexBtn = findViewById(R.id.build_index);
        mapTrajBtn = findViewById(R.id.map_traj);

        ArrayAdapter<CharSequence> fileTypeAdapter = ArrayAdapter.createFromResource(this, R.array.file_type_array, android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> dataTypeAdapter = ArrayAdapter.createFromResource(this, R.array.data_type_array, android.R.layout.simple_spinner_dropdown_item);
        fileTypeSpin.setAdapter(fileTypeAdapter);
        fileTypeSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fileTypeStr = fileTypeSpin.getSelectedItem().toString();
                //Toast.makeText(DataLoadActivity.this,fileTypeStr,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fileTypeStr = "csv";
            }

        });

        dataTypeSpin.setAdapter(dataTypeAdapter);
        dataTypeSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dataTypeStr = dataTypeSpin.getSelectedItem().toString();
                Toast.makeText(DataLoadActivity.this,dataTypeStr,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                dataTypeStr = "Trajectory";
            }
        });

        chooseFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilePicker.from(DataLoadActivity.this)
                        .chooseForMimeType().setTargetPath("/storage/emulated/0/Download").isSingle().requestCode(1).setFileTypes(fileTypeStr).start();

            }
        });

        loadDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataFilePath.length() == 0) {
                    Toast.makeText(DataLoadActivity.this, "请选择文件", Toast.LENGTH_SHORT).show();
                } else {

                    if(dataTypeStr.equals("公交站点")){
                        loadBusStop();
                    }
                    else if(dataTypeStr.equals("公交路线")){
                        loadBusLine();
                    }
                    else if(dataTypeStr.equals("站点顺序")){
                        loadBusStopOrder();
                    }
                    else if(dataTypeStr.equals("POI数据")){
                        loadPOI();
                    }
                    else if(dataTypeStr.equals("轨迹数据")){

                    }
                    else if(dataTypeStr.equals("公交出行轨迹")){
                        loadTrajectoryByBusData();
                    }

                    else if(dataTypeStr.equals("公交出行轨迹")){

                    }

                    else if(dataTypeStr.equals("病例活动")){
                        loadRiskyTrajectory();
                    }

                }
            }
        });

        /*deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dataTypeStr.equals("公交站点")){
                    new Thread(()->{
                        Box<BusStop> busStopBox = ObjectBox.get().boxFor(BusStop.class);
                        busStopBox.removeAll();
                        Log.i("Bus stop", "deleted");
                    }).start();

                }
                else if(dataTypeStr.equals("公交路线")){
                    new Thread(()->{
                        Box<BusLine> busLineBox = ObjectBox.get().boxFor(BusLine.class);
                        busLineBox.removeAll();
                        Log.i("Bus line", "deleted");
                    }).start();

                }
                else if(dataTypeStr.equals("站点顺序")){
                    new Thread(()->{
                        Box<StopOrder> stopOrderBox = ObjectBox.get().boxFor(StopOrder.class);
                        stopOrderBox.removeAll();
                        Log.i("Bus stop order", "deleted");
                    }).start();

                }
                else if(dataTypeStr.equals("POI数据")){
                    new Thread(()->{
                        Box<POI> poiBox=ObjectBox.get().boxFor(POI.class);
                        poiBox.removeAll();
                        Log.i("POI", "deleted");
                    }).start();
                }
                else if(dataTypeStr.equals("轨迹数据")){
                    //
                }

            }
        });*/

        //为POI及Stop构建索引
        buildIndexBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildIndex();
            }
        });

        mapTrajBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String filePath = getApplicationContext().getFilesDir().getAbsolutePath();
                                String fileName = filePath + "/" + "wuhan_poi_index";
                                GridIndex.loadIndex(fileName);
                                Box<RawTrajectory> rawTrajectoryBox = ObjectBox.get().boxFor(RawTrajectory.class);
                                Box<MappedTrajectory> mappedTrajectoryBox = ObjectBox.get().boxFor(MappedTrajectory.class);
                                Box<POI> poiBox = ObjectBox.get().boxFor(POI.class);
                                List<RawTrajectory> rawTrajectoryList = rawTrajectoryBox.getAll();
                                Log.i("raw trajectory", String.valueOf(rawTrajectoryList.size()));
                                for (RawTrajectory rawT : rawTrajectoryList) {
                                    if (rawT.mappedTrajectoryToOne.getTarget() == null) {
                                        List<TrajEvent> POIs = TrajExtractor.executeForPOIs(rawT.record, rawT.starttime, rawT.endtime);
                                        StringBuffer record = new StringBuffer();
                                        for (TrajEvent e : POIs) {
                                            POI poi = poiBox.get(e.loc.id);
                                            record.append(e.loc.id);
                                            record.append(",");
                                            if (poi != null) {
                                                record.append(poi.name.replace(",", "").replace("，", ""));
                                                record.append(",");
                                            }
                                            record.append(e.starttime);
                                            record.append(",");
                                            record.append(e.endtime);
                                            record.append(";");
                                        }
                                        MappedTrajectory mappedTrajectory = new MappedTrajectory();
                                        //MappedTrajectory mappedTrajectory = rawT.mappedTrajectoryToOne.getTarget();
                                        mappedTrajectory.record = record.toString();
                                        mappedTrajectory.rawTrajectoryToOne.setTarget(rawT);
                                        rawT.mappedTrajectoryToOne.setTarget(mappedTrajectory);
                                        rawTrajectoryBox.put(rawT);
                                        mappedTrajectoryBox.put(mappedTrajectory);
                                        Log.i("mapping", record.toString());
                                    }
                                }
                                Message msg = Message.obtain();
                                msg.what = 2;
                                handler.sendMessage(msg);
                            }
                        }

                        ).start();
                    }
                }
        );


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=Activity.RESULT_OK){
            Toast.makeText(this, "文件选择错误", Toast.LENGTH_SHORT).show();
        }
        if(resultCode == Activity.RESULT_OK && requestCode==1){

            ArrayList<EssFile> essFileList = data.getParcelableArrayListExtra(Const.EXTRA_RESULT_SELECTION);
            EssFile file=essFileList.get(0);
            //获取文件路径
            dataFilePath=file.getAbsolutePath();
            Toast.makeText(this, dataFilePath, Toast.LENGTH_SHORT).show();

        }
    }

    public void buildIndex(){
        String filePath=getApplicationContext().getFilesDir().getAbsolutePath();
        final String fileName=filePath+"/"+"wuhan_poi_index";
        Log.i("poi_index","file path"+fileName);
        Toast.makeText(this, filePath, Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
            Message msg=Message.obtain();
            msg.what=2;
            Box<POI> poiBox=ObjectBox.get().boxFor(POI.class);
            Box<BusStop> busStopBox=ObjectBox.get().boxFor(BusStop.class);
            List<BusStop> stops=busStopBox.getAll();
            List<POI> pois=poiBox.getAll();

            Map<Integer,List<Integer>> poi_map=new HashMap<>();
            for(POI p:pois){
                long id=p.id;
                double lat=p.lat;
                double lng=p.lng;
                int grid_id= GridIndex.getLocGridId(lat,lng);
                if(poi_map.containsKey(grid_id)){
                    poi_map.get(grid_id).add((int) id);
                }
                else{
                    poi_map.put(grid_id,new ArrayList<Integer>());
                    poi_map.get(grid_id).add((int) id);
                }
            }
            for(BusStop b:stops){
                long id=b.id;
                double lat=b.lat;
                double lng=b.lng;
                int grid_id = GridIndex.getLocGridId(lat,lng);
                if(poi_map.containsKey(grid_id)){
                    poi_map.get(grid_id).add((int) -id);
                }
                else{
                    poi_map.put(grid_id,new ArrayList<Integer>());
                    poi_map.get(grid_id).add((int) -id);
                }

            }


            File poiIndexFile=new File(fileName);
            if(poiIndexFile.exists()){

                Log.e("poi_index","索引文件已存在");
            }
            else{
                try(BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(poiIndexFile)))){
                    for(Map.Entry<Integer,List<Integer>> p:poi_map.entrySet()){
                        String gridId=String.valueOf(p.getKey());
                        List<Integer> l=p.getValue();
                        String poisVal="";
                        for(Integer i:l){
                            poisVal+=","+String.valueOf(i);
                        }
                        String line=gridId+poisVal;
                        Log.i("poi_index ",line);
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                    }
                }
                catch (Exception e){
                    Log.e("poi_index","索引创建失败");
                    msg.what=3;
                }
            }
            handler.sendMessage(msg);

        }}).start();

    }

    public void loadPOI(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                File dataFile = new File(dataFilePath);
                if (!dataFile.exists()) {
                    Toast.makeText(DataLoadActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
                } else {
                    try (BufferedReader bufferedReader =
                                 new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), "UTF-8"))) {
                        String line;
                        int count = 0;
                        Box<POI> poiBoxStore = ObjectBox.get().boxFor(POI.class);

                        while ((line = bufferedReader.readLine()) != null) {
                            String[] vals = line.split(",");
                            if (count != 0) {
                                //Log.i("poi info",line);
                                String name = vals[1];
                                String cat_L1 = vals[2];
                                String cat_L2 = vals[3];
                                String province = vals[4];
                                String city = vals[5];
                                String area = vals[6];
                                String address = vals[7];

                                double lat = Double.valueOf(vals[8]);
                                double lng = Double.valueOf(vals[9]);

                                POI poi = new POI(name, cat_L1, cat_L2, province, city, area, address, lat, lng);
                                poiBoxStore.put(poi);
                                //Log.i("poi id",String.valueOf(poi.id));
                                Log.i("poi info", name + cat_L1 + cat_L2 + province + city + area + address + lat + lng);
                            }

                            count++;
                        }
                        Log.i("poi number", String.valueOf(count));
                        count = poiBoxStore.getAll().size();
                        Log.i("poi number", String.valueOf(count));
                    } catch (IOException e) {
                        Toast.makeText(DataLoadActivity.this, "数据导入异常", Toast.LENGTH_SHORT).show();
                    }

                }

                Message msg = Message.obtain();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }).start();

    }

    public void loadBusStop(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File dataFile = new File(dataFilePath);
                if (!dataFile.exists()) {
                    //Toast.makeText(DataLoadActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
                } else {
                    int count = 0;
                    try (BufferedReader bufferedReader =
                                 new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), "UTF-8"))) {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            if (count == 0) {
                                count++;
                                continue;
                            }
                            String[] vals = line.split(",");
                            String id = vals[0];
                            String name = vals[1];
                            double lng = Double.valueOf(vals[2]);
                            double lat = Double.valueOf(vals[3]);
                            Box<BusStop> stopBox = ObjectBox.get().boxFor(BusStop.class);
                            BusStop stop = new BusStop(id, name, lat, lng);
                            stopBox.put(stop);
                            Log.i("Bus stop", stop.toString());
                            count++;

                        }
                        Log.i("Bus stop", "number is " + count);
                    } catch (IOException e) {
                        Log.i("Bus stop", "IO error " + count);
                        e.printStackTrace();
                    } catch (Exception e) {
                        Log.i("Bus stop", "import error " + count);
                        e.printStackTrace();
                    }


                }

                Message msg = Message.obtain();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }
        ).start();
    }

    public void loadBusLine(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File dataFile = new File(dataFilePath);
                if (!dataFile.exists()) {
                    //Toast.makeText(DataLoadActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
                } else {
                    try (BufferedReader bufferedReader =
                                 new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), "UTF-8"))) {
                        String line;
                        int count = 0;
                        while ((line = bufferedReader.readLine()) != null) {
                            if (count == 0) {
                                count++;
                                continue;
                            }
                            String[] vals = line.split(",");
                            String name = vals[0];
                            String lineno = vals[1];
                            int direction = Integer.valueOf(vals[2]);
                            String starttime = vals[5];
                            String endtime = vals[6];
                            Box<BusLine> busLineBox = ObjectBox.get().boxFor(BusLine.class);
                            BusLine busLine = new BusLine(lineno, name, starttime, endtime, direction);
                            busLineBox.put(busLine);
                            Log.i("Bus line", busLine.toString());
                            count++;

                        }
                        Log.i("Bus line", "number is " + count);

                    } catch (IOException e) {
                        e.printStackTrace();

                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }

                Message msg = Message.obtain();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }
        ).start();
    }

    public void loadBusStopOrder(){
        new Thread(new Runnable() {
            @Override
            public void run() {
            File dataFile = new File(dataFilePath);
            if (!dataFile.exists()) {
                Toast.makeText(DataLoadActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
            }else {
                try (BufferedReader bufferedReader =
                             new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), "UTF-8"))) {
                    String line;
                    int count = 0;
                    while((line=bufferedReader.readLine())!=null) {
                        if (count == 0) {
                            count++;
                            continue;
                        }
                        String[] vals = line.split(",");
                        String lineno=vals[0];
                        int direction=Integer.valueOf(vals[1]);
                        String stop_id=vals[2];
                        int order=Integer.valueOf(vals[3]);
                        Box<StopOrder> stopOrderBox=ObjectBox.get().boxFor(StopOrder.class);
                        Box<BusStop> busStopBox=ObjectBox.get().boxFor(BusStop.class);
                        Box<BusLine> busLineBox=ObjectBox.get().boxFor(BusLine.class);
                        Query<BusLine> busLineQuery=busLineBox.query().equal(BusLine_.line_no,lineno).equal(BusLine_.direction,direction).build();
                        Query<BusStop> busStopQuery=busStopBox.query().equal(BusStop_.stop_id,stop_id).build();
                        List<BusLine> busLineList=busLineQuery.find();
                        List<BusStop> busStopList=busStopQuery.find();
                        if(busLineList.size()!=1){
                            Log.i("Bus line","There is no busline for "+lineno);
                            continue;
                        }
                        if(busStopList.size()!=1){
                            Log.i("Bus stop","There is no bus stop for "+stop_id);
                            continue;
                        }
                        StopOrder stopOrder = new StopOrder(order,direction,busStopList.get(0));
                        BusStop busStop = busStopList.get(0);
                        BusLine busLine = busLineList.get(0);
                        stopOrderBox.put(stopOrder);
                        if(busLine.stopOrders==null) {
                            busLine.stopOrders = new ToMany<>(busLine, BusLine_.stopOrders);
                        }
                        busLine.stopOrders.add(stopOrder);
                        if(busStop.busLines==null) {
                            busStop.busLines = new ToMany<>(busStop, BusStop_.busLines);
                        }
                        busStop.busLines.add(busLine);

                        busLineBox.put(busLine);
                        busStopBox.put(busStop);
                        count++;
                        Log.i("Bus stop order","current number is "+count);
                    }
                    Log.i("Bus stop order","number is "+count);

                }catch (IOException e){
                    e.printStackTrace();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            Message msg = Message.obtain();
            msg.what = 1;
            handler.sendMessage(msg);
        }}
        ).start();
    }

    public void loadRiskyTrajectory(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File dataFile = new File(dataFilePath);
                if (!dataFile.exists()) {
                    Log.i("load risky trajectory", "file not exists");
                } else {
                    try (BufferedReader bufferedReader =
                                 new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), "UTF-8"))) {
                        String line;
                        int count = 0;
                        Box<RiskyTrajectory> riskyTrajectoryBox = ObjectBox.get().boxFor(RiskyTrajectory.class);
                        riskyTrajectoryBox.removeAll();
                        while ((line = bufferedReader.readLine()) != null) {

                            RiskyTrajectory riskyTrajectory = new RiskyTrajectory(line);
                            riskyTrajectoryBox.put(riskyTrajectory);
                            count++;

                        }
                        Log.i("Trajectory by bus ", "number is " + count);

                    } catch (IOException e) {
                        e.printStackTrace();

                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }

                Message msg = Message.obtain();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }).start();
    }


    public void loadTrajectoryByBusData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File dataFile = new File(dataFilePath);
                if (!dataFile.exists()) {

                } else {
                    try (BufferedReader bufferedReader =
                                 new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), "UTF-8"))) {
                        String line;
                        int count = 0;
                        while ((line = bufferedReader.readLine()) != null) {
                            if (count == 0) {
                                count++;
                                continue;
                            }
                            String[] vals = line.split("-");
                            long starttime = Long.valueOf(vals[0]);
                            long endtime = Long.valueOf(vals[1]);
                            String record = vals[2];

                            Box<RawTrajectory> rawTrajectoryBox = ObjectBox.get().boxFor(RawTrajectory.class);
                            RawTrajectory rawTrajectory = new RawTrajectory();
                            rawTrajectory.starttime = starttime;
                            rawTrajectory.endtime = endtime;
                            rawTrajectory.record = record;
                            rawTrajectoryBox.put(rawTrajectory);
                            Log.i("Trajectory by bus", rawTrajectory.toString());
                            count++;

                        }
                        Log.i("Trajectory by bus ", "number is " + count);

                    } catch (IOException e) {
                        e.printStackTrace();

                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }

                Message msg = Message.obtain();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }).start();
    }

}
