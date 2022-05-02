package me.gfuil.bmap.lite.storage;

import io.objectbox.BoxStore;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

@Entity
public class City {
    @Id
    public long id;
    public String name;
    public double lon;
    public double lat;
    public ToMany<Node> nodes;
    public ToMany<Edge> edges;

    transient BoxStore __boxStore;

    public City(){
        this.nodes = new ToMany<>(this,City_.nodes);
        this.edges = new ToMany<>(this,City_.edges);
    }

    public City(String name, double lon,double lat){
        this();
        this.name=name;
        this.lon=lon;
        this.lat=lat;

    }


}
