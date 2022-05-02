package com.amap.mapapi.overlay;

import android.content.Context;
import android.graphics.Color;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.WalkPath;

public class UserRouteOverlay extends WalkRouteOverlay{
    /**
     * 通过此构造函数创建步行路线图层。
     *
     * @param context 当前activity。
     * @param amap    地图对象。
     * @param path    步行路线规划的一个方案。详见搜索服务模块的路径查询包（com.amap.api.services.route）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/route/WalkStep.html" title="com.amap.api.services.route中的类">WalkStep</a></strong>。
     * @param start   起点。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类<strong><a href="../../../../../../Search/com/amap/api/services/core/LatLonPoint.html" title="com.amap.api.services.core中的类">LatLonPoint</a></strong>。
     * @param end     终点。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类<strong><a href="../../../../../../Search/com/amap/api/services/core/LatLonPoint.html" title="com.amap.api.services.core中的类">LatLonPoint</a></strong>。
     * @since V2.1.0
     */
    public UserRouteOverlay(Context context, AMap amap, WalkPath path, LatLonPoint start, LatLonPoint end) {
        super(context, amap, path, start, end);
    }

    @Override
    protected int getWalkColor() {
        return Color.parseColor("#fa0000");
    }

    @Override
    protected void addStationMarker(MarkerOptions options) {

    }

    @Override
    protected void addStartAndEndMarker() {

    }
}
