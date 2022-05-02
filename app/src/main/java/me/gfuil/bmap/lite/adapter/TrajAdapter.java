package me.gfuil.bmap.lite.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import me.gfuil.bmap.lite.R;

public class TrajAdapter extends ArrayAdapter<TrajItem> {
    private int resourceId;
    public TrajAdapter(Context context, int textResourseId, List<TrajItem> trajs){
        super(context,textResourseId,trajs);
        resourceId=textResourseId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        TrajItem traj=getItem(position);
        View view;
        ViewHolder vh;
        if(null==convertView){
            view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            vh = new ViewHolder() ;
            vh.idTextView = view.findViewById(R.id.traj_id);
            vh.recordTextView = null;

            view.setTag(vh);
        }
        else{
            view = convertView;
            vh= (ViewHolder) view.getTag();
        }

        vh.idTextView.setText(String.valueOf(traj.id));
        vh.recordTextView.setText(traj.record);
        return view;
    }
}

class ViewHolder{
    TextView idTextView;
    TextView recordTextView;
}

