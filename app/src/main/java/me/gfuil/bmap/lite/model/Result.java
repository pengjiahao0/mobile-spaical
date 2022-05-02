package me.gfuil.bmap.lite.model;

import java.util.Arrays;

public class Result {
    private Total total;
    private Hits[] hits;
    private Float max_score;

    public Result(Total total, Hits[] hits, Float max_score) {
        this.total = total;
        this.hits = hits;
        this.max_score = max_score;
    }

    public Result() {
    }

    public Total getTotal() {
        return total;
    }

    public void setTotal(Total total) {
        this.total = total;
    }

    public Hits[] getHits() {
        return hits;
    }

    public void setHits(Hits[] hits) {
        this.hits = hits;
    }

    public Float getMaxScore() {
        return max_score;
    }

    public void setMaxScore(Float max_score) {
        this.max_score = max_score;
    }

    @Override
    public String toString() {
        return "Result{" +
                "total=" + total +
                ", hits=" + Arrays.toString(hits) +
                ", max_score=" + max_score +
                '}';
    }
}
