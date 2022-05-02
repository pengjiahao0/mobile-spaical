package me.gfuil.bmap.lite.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;



import me.gfuil.bmap.lite.R;


public class ActivityRecordFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button poiMapBtn;
    private ListView poiList;

    private String trajId;
    private String trajRecord;

    public ActivityRecordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ActivityRecordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ActivityRecordFragment newInstance(String param1, String param2) {
        ActivityRecordFragment fragment = new ActivityRecordFragment();
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
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_activity_record, container, false);
        poiMapBtn = view.findViewById(R.id.rawToPoisBtn);
        poiList = view.findViewById(R.id.poi_list);


        return view;
    }

    public void onResume(){
        super.onResume();
        //加载轨迹poi活动，对应listview
        Bundle bundle=getArguments();
        if(bundle!=null){
            trajId = bundle.getString("id");
            trajRecord = bundle.getString("record");
            Log.i("Trajectory activity record",trajId+"\t"+trajRecord);
        }


    }
}