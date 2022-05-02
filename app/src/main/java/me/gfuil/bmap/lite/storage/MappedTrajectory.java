package me.gfuil.bmap.lite.storage;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

@Entity
public class MappedTrajectory {

    @Id
    public long id;

    public ToOne<RawTrajectory> rawTrajectoryToOne;

    // "poi_id,t_s,t_e;....;"
    public String record;

    public MappedTrajectory(){

    }



}
