package me.gfuil.bmap.lite.storage;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

@Entity
public class Edge {
    @Id
    public long id;

    public ToMany<Node> nodes;


}
