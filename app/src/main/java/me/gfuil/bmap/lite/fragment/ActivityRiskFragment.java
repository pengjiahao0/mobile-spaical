package me.gfuil.bmap.lite.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import com.king.view.circleprogressview.CircleProgressView;

import me.gfuil.bmap.lite.R;

public class ActivityRiskFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String trajId;
    private String trajRecord;

    private ListView riskTrajList;
    private CircleProgressView circleProgressView;

    public ActivityRiskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ActivityRiskFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ActivityRiskFragment newInstance(String param1, String param2) {
        ActivityRiskFragment fragment = new ActivityRiskFragment();
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
        View view = inflater.inflate(R.layout.fragment_activity_risk, container, false);
        circleProgressView = view.findViewById(R.id.risk_circle);
        riskTrajList = view.findViewById(R.id.risk_traj);


        return view;
    }

    public void onResume(){
        super.onResume();
        Bundle bundle=getArguments();
        if(bundle!=null){
            trajId = bundle.getString("id");
            trajRecord = bundle.getString("record");
            Log.i("Activity risk fragment",trajId+"\t"+trajRecord);
        }

    }
}