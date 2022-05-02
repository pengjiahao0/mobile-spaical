package me.gfuil.bmap.lite.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class SyncLinearLayout extends LinearLayout {
    public SyncLinearLayout(Context context) {
        super(context);
    }

    public SyncLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SyncLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SyncLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获得listview的个数
        int count=getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            try {
                child.dispatchTouchEvent(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
