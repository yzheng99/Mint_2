package uk.ac.herts.mint.data;

import java.util.ArrayList;

public class PharmcyProvider {

    private static PharmcyProvider instance;
    public ArrayList<PharmcyDetail> pharmcyDetails = new ArrayList<>();

    public static PharmcyProvider getInstance() {
        if (instance == null)
            instance = new PharmcyProvider();
        return instance;
    }

    public void setPharmcyDetails(ArrayList<PharmcyDetail> pharmcyDetails){
        this.pharmcyDetails=pharmcyDetails;
    }
    public ArrayList<PharmcyDetail> getPharmcyDetails(){
        return pharmcyDetails;
    }

}
