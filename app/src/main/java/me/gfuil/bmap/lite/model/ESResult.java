package me.gfuil.bmap.lite.model;

public class ESResult {
    private Result result;

    public ESResult(Result result) {
        this.result = result;
    }

    public ESResult() {
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ESResult{" +
                "result=" + result +
                '}';
    }
}
