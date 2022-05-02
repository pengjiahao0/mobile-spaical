package me.gfuil.bmap.lite.model;

public class Total {
    private Integer value;
    private String relation;

    public Total(Integer value, String relation) {
        this.value = value;
        this.relation = relation;
    }

    public Total() {
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    @Override
    public String toString() {
        return "Total{" +
                "value=" + value +
                ", relation='" + relation + '\'' +
                '}';
    }
}
