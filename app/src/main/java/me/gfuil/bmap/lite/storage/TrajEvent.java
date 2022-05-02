package me.gfuil.bmap.lite.storage;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class TrajEvent {
    @Id
    public long id;
    public long starttime;
    public long endtime;
    public int event_type;

    public long location_id;

}
