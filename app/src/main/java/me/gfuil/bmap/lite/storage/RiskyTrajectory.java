package me.gfuil.bmap.lite.storage;

import io.objectbox.BoxStore;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class RiskyTrajectory {
    @Id
    public long id;

    // "poi_id,t_s,t_e;...;"
    public String record;

    public RiskyTrajectory(String record){
        this.record=record;
    }

    public RiskyTrajectory(){

    }

    transient BoxStore __boxStore;

}
