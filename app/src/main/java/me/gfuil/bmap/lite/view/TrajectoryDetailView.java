package me.gfuil.bmap.lite.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class TrajectoryDetailView extends android.support.v7.widget.AppCompatTextView {
    public TrajectoryDetailView(Context context) {
        super(context);
    }

    public TrajectoryDetailView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TrajectoryDetailView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean isFocused() {
        return true;
    }
}
