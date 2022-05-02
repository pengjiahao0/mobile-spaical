package me.gfuil.bmap.lite.storage;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

@Entity
public class RawTrajectory {
    @Id
    public long id;

    /*
     * @record
     * Raw trajectory data in a string format
     * "x1,x2,x3...;y1,y2,y3,..."
     * @track_id
     * trajectory id
     * @object_type
     * there are many object in road map, like AV, AGENT,etc.
     */

    public long starttime;
    public long endtime;

    public String track_id;
    public String object_type;
    public String city;

    // The trajectory data

    public String record;


    public RawTrajectory(){

    }

    public ToOne<MappedTrajectory> mappedTrajectoryToOne;

}
