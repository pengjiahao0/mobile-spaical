package me.gfuil.bmap.lite.storage;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Node {
    @Id
    public long id;
    public double lon;
    public double lat;

}
