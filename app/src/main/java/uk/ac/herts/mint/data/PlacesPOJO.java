package uk.ac.herts.mint.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlacesPOJO {

    public class Root implements Serializable {

        @SerializedName("results")
        public List<PharmcyShop> pharmcyShop = new ArrayList<>();
        @SerializedName("status")
        public String status;
    }

    public class PharmcyShop implements Serializable {


        @SerializedName("geometry")
        public Geometry geometry;
        @SerializedName("vicinity")
        public String vicinity;
        @SerializedName("name")
        public String name;

    }

    public class Geometry implements Serializable{

        @SerializedName("location")
        public LocationOfPharmcy locationOfPharmcy;

    }

    public class LocationOfPharmcy implements Serializable {

        @SerializedName("lat")
        public String lat;
        @SerializedName("lng")
        public String lng;
    }
}
