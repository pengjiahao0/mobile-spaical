package me.gfuil.bmap.lite.utils;

import static me.gfuil.bmap.lite.activity.SearchTrajectoyActivity.JSON;

import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import me.gfuil.bmap.lite.adapter.TrajItem;
import me.gfuil.bmap.lite.algorithm.DistanceUtils;
import me.gfuil.bmap.lite.model.ESResult;
import me.gfuil.bmap.lite.model.Hits;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {
    public static ESResult queryFromES(String[] splits) {
        String url = "http://47.105.33.143:9200/risk_trajectory/_search";
        OkHttpClient okHttpClient = new OkHttpClient();
        splits = new String[]{"131.13", "31.13", "1600km"};
        Request request = null;
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

        final ESResult[] esResult = {null};
        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse( Call call, Response response) throws IOException {

                Gson gson = new Gson();
                assert response.body() != null;
                String responseText = response.body().string();
                responseText = responseText.replaceFirst("hits", "result");
                esResult[0] = gson.fromJson(responseText, ESResult.class);


            }

        });
        while (!call.isExecuted()){

        }
        Log.i("queryFromES", "onResponse: "+ esResult[0]);
        return esResult[0];
    }
}
