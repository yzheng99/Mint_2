package uk.ac.herts.mint.data;


public class PharmcyDetail {


    public String name, address, distance, duration, lat, lng;

    public PharmcyDetail(String name, String address, String distance, String duration, String lat, String lng) {

        this.name = name;
        this.address = address;
        this.distance = distance;
        this.duration = duration;
        this.lat=lat;
        this.lng=lng;
    }
}
