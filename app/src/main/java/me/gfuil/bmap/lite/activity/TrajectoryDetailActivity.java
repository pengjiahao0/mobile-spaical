package me.gfuil.bmap.lite.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import me.gfuil.bmap.lite.R;
import me.gfuil.bmap.lite.fragment.ActivityRecordFragment;
import me.gfuil.bmap.lite.fragment.ActivityRiskFragment;
import me.gfuil.bmap.lite.fragment.TrajectoryDetailFragment;

public class TrajectoryDetailActivity extends AppCompatActivity {
    private ActivityRiskFragment activityRiskFragment;
    private ActivityRecordFragment activityRecordFragment;
    private TrajectoryDetailFragment trajectoryDetailFragment;

    private String trajRecord;
    private String trajId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trajectory_detail);


        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        Intent intent = getIntent();
        trajId = intent.getExtras().getString("id");
        trajRecord = intent.getExtras().getString("record");
        Log.i("Trajectory detail:", trajId+"\t"+trajRecord);

        initView();

        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
                hideAllFragment();
                menuItem.setChecked(true);
                switch(menuItem.getItemId()) {
                    case R.id.navigation_home:
                        fragmentTransaction.show(trajectoryDetailFragment);
                        Log.i("Trajectory detail activity:","transfer to trajectoryDetailFragment");
                        break;
                    case R.id.navigation_dashboard:
                        fragmentTransaction.show(activityRecordFragment);
                        Log.i("Trajectory detail activity:","transfer to activityRecordFragment");
                        break;
                    case R.id.navigation_notifications:
                        fragmentTransaction.show(activityRiskFragment);
                        Log.i("Trajectory detail activity:","transfer to activityRiskFragment");
                        break;
                }
                fragmentTransaction.commit();
                return true;

            }
        });

    }


    public void hideAllFragment(){
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.hide(trajectoryDetailFragment);
        transaction.hide(activityRecordFragment);
        transaction.hide(activityRiskFragment);
        transaction.commit();

        Log.i("Trajectory detail activity:","hide all fragments");
    }


    private void initView(){
        Bundle bundle = new Bundle();
        bundle.putString("id",trajId);
        bundle.putString("record",trajRecord);
        if(activityRecordFragment==null){
            activityRecordFragment = new ActivityRecordFragment();
            activityRecordFragment.setArguments(bundle);
        }

        if(activityRiskFragment==null){
            activityRiskFragment= new ActivityRiskFragment();
            activityRiskFragment.setArguments(bundle);
        }
        if(trajectoryDetailFragment==null){
            trajectoryDetailFragment = new TrajectoryDetailFragment();
            trajectoryDetailFragment.setArguments(bundle);
        }

        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.nav_host_fragment,activityRecordFragment);
        fragmentTransaction.add(R.id.nav_host_fragment,activityRiskFragment);
        fragmentTransaction.add(R.id.nav_host_fragment,trajectoryDetailFragment);
        hideAllFragment();
        fragmentTransaction.show(trajectoryDetailFragment);
        fragmentTransaction.commit();
    }





}