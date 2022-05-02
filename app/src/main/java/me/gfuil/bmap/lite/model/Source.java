package me.gfuil.bmap.lite.model;

import java.util.Arrays;

public class Source {
    private String[] trajectory_location;
    private String[] trajectory_detail;
    private String[] address;
    private String[][] address_location;
    private String[] extra_info;
    private String trajectory;



    public Source(String[] address, String[][] address_location, String[] extra_info, String trajectory, String[] trajectory_location, String[] trajectory_detail) {
        this.address = address;
        this.address_location = address_location;
        this.extra_info = extra_info;
        this.trajectory = trajectory;
        this.trajectory_location = trajectory_location;
        this.trajectory_detail = trajectory_detail;
    }

    public Source() {
    }

    public String[] getTrajectory_location() {
        return trajectory_location;
    }

    public void setTrajectory_location(String[] trajectory_location) {
        this.trajectory_location = trajectory_location;
    }

    public String[] getTrajectory_detail() {
        return trajectory_detail;
    }

    public void setTrajectory_detail(String[] trajectory_detail) {
        this.trajectory_detail = trajectory_detail;
    }
    public String[] getAddress() {
        return address;
    }

    public void setAddress(String[] address) {
        this.address = address;
    }

    public String[][] getAddress_location() {
        return address_location;
    }

    public void setAddress_location(String[][] address_location) {
        this.address_location = address_location;
    }

    public String[] getExtra_info() {
        return extra_info;
    }

    public void setExtra_info(String[] extra_info) {
        this.extra_info = extra_info;
    }

    public String getTrajectory() {
        return trajectory;
    }

    public void setTrajectory(String trajectory) {
        this.trajectory = trajectory;
    }

    @Override
    public String toString() {
        return "Source{" +
                "address=" + Arrays.toString(address) +
                ", address_location=" + Arrays.toString(address_location) +
                ", extra_info='" + Arrays.toString(extra_info) + '\'' +
                ", trajectory='" + trajectory + '\'' +
                ", trajectory_location=" + Arrays.toString(trajectory_location) +
                '}';
    }
}
