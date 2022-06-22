package uk.ac.herts.mint.login;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Callback;
import retrofit2.Response;
import uk.ac.herts.mint.API.APIClient;
import uk.ac.herts.mint.API.ApiInterface;
import uk.ac.herts.mint.API.ResultDistanceMatrix;
import uk.ac.herts.mint.ListDemoActivity;
import uk.ac.herts.mint.R;
import uk.ac.herts.mint.data.MedicineSample;
import uk.ac.herts.mint.data.PharmcyDetail;
import uk.ac.herts.mint.data.PlacesPOJO;
import uk.ac.herts.mint.data.RecyclerViewAdapter;
import uk.ac.herts.mint.data.PlaceModel;


public class LoginMainActivity extends AppCompatActivity {
    String PLACETYPE="pharmacy";

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 101;
    List<PlaceModel> placeModels;
    public ArrayList<PharmcyDetail> pharmcyDetails = new ArrayList<>();
    ApiInterface apiService;

    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    String latitude, longitude;
    String latLngString;
    LatLng latLng;
    String postCode="";

    AutoCompleteTextView autocomplete_medicine_name;
    AutoCompleteTextView autocomplete_medicine_type;
    AutoCompleteTextView autocomplete_medicine_option;
    RecyclerView recyclerView;
    EditText editText;
    EditText postCodeText;
    Button button;
    Button btnPharmcy;
    List<PlacesPOJO.PharmcyShop> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        postCodeText=(EditText) (findViewById(R.id.editTextTextPostalAddress));

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            else {
                fetchLocation();
            }
        } else {
            fetchLocation();
        }


        apiService = APIClient.getClient().create(ApiInterface.class);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        autocomplete_medicine_name = (AutoCompleteTextView)
                findViewById(R.id.atx_medicine);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this,android.R.layout.select_dialog_item, MedicineSample.medicine_name);

        autocomplete_medicine_name.setThreshold(1);
        autocomplete_medicine_name.setAdapter(adapter);

        //get the spinner from the xml.
        Spinner dropdown_type = findViewById(R.id.spinner_type);

        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter_type = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, MedicineSample.medicine_type);
        //set the spinners adapter to the previously created one.
        dropdown_type.setAdapter(adapter_type);

        //get the spinner from the xml.
        Spinner dropdown_option = findViewById(R.id.spinner_option);
        //create a list of items for the spinner.
        String[] items = new String[]{"1", "2", "three"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter_option = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, MedicineSample.medicine_option);
        //set the spinners adapter to the previously created one.
        dropdown_option.setAdapter(adapter_option);

        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        btnPharmcy = (Button) findViewById(R.id.btPharmcy);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postCode=postCodeText.getText().toString();
                fetchStores();
            }
        });

        btnPharmcy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ListDemoActivity.class);
                startActivity(intent);
            }
        });

    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                LoginMainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                LoginMainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                double lat = locationGPS.getLatitude();
                double lon = locationGPS.getLongitude();
                latitude = String.valueOf(lat);
                longitude = String.valueOf(lon);
            //    showLocation.setText("Your Location: " + "\n" + "Latitude: " + latitude + "\n" + "Longitude: " + longitude);



            } else {
                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLocationFromPostCode(String code){
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        List<android.location.Address> address = null;

        if (geoCoder != null) {
            try {
                address = geoCoder.getFromLocationName(code, 10);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (address.size() > 0) {
                Address first = address.get(0);
                double lat = first.getLatitude();
                double lon = first.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(lon);

                latLngString = latitude + "," + longitude;
                latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            }
        }
    }
    private void fetchStores() {

        if (postCode.length()>1){
            getLocationFromPostCode(postCode);
        }

        //Call<PlacesPOJO.Root> call = apiService.doPlaces(placeType, latLngString,"\""+ businessName +"\"", true, "distance", APIClient.GOOGLE_PLACE_API_KEY);

        retrofit2.Call<PlacesPOJO.Root> call = apiService.doPlaces(PLACETYPE, latLngString, "pharmacy", true, "distance", APIClient.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<PlacesPOJO.Root>() {
            @Override
            public void onResponse(retrofit2.Call<PlacesPOJO.Root> call, Response<PlacesPOJO.Root> response) {
                PlacesPOJO.Root root = response.body();


                if (response.isSuccessful()) {

                    if (root.status.equals("OK")) {

                        results = root.pharmcyShop;
                        placeModels = new ArrayList<>();
                        for (int i = 0; i < results.size(); i++) {

                            if (i == 10)
                                break;
                            PlacesPOJO.PharmcyShop info = results.get(i);

                            fetchDistance(info);

                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "No matches found near you", Toast.LENGTH_SHORT).show();
                    }

                } else if (response.code() != 200) {
                    Toast.makeText(getApplicationContext(), "Error " + response.code() + " found.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(retrofit2.Call<PlacesPOJO.Root> call, Throwable t) {
                call.cancel();
            }
        });

    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                } else {
                    fetchLocation();
                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(LoginMainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void fetchLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            getLocation();
        }
        getLocation();

        latLngString = latitude + "," + longitude;
        latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

//        SmartLocation.with(this).location()
//                .oneFix()
//                .start(new OnLocationUpdatedListener() {
//                    @Override
//                    public void onLocationUpdated(Location location) {
//                        latLngString = location.getLatitude() + "," + location.getLongitude();
//                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                    }
//                });
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void fetchDistance(final PlacesPOJO.PharmcyShop info) {
        pharmcyDetails.clear();
        retrofit2.Call<ResultDistanceMatrix> call = apiService.getDistance(APIClient.GOOGLE_PLACE_API_KEY, latLngString, info.geometry.locationOfPharmcy.lat + "," + info.geometry.locationOfPharmcy.lng);
        call.enqueue(new Callback<ResultDistanceMatrix>() {
            @Override
            public void onResponse(retrofit2.Call<ResultDistanceMatrix> call, Response<ResultDistanceMatrix> response) {
                ResultDistanceMatrix resultDistance = response.body();
                if ("OK".equalsIgnoreCase(resultDistance.status)) {

                    ResultDistanceMatrix.InfoDistanceMatrix infoDistanceMatrix = resultDistance.rows.get(0);
                    ResultDistanceMatrix.InfoDistanceMatrix.DistanceElement distanceElement = infoDistanceMatrix.elements.get(0);
                    if ("OK".equalsIgnoreCase(distanceElement.status)) {
                        ResultDistanceMatrix.InfoDistanceMatrix.ValueItem itemDuration = distanceElement.duration;
                        ResultDistanceMatrix.InfoDistanceMatrix.ValueItem itemDistance = distanceElement.distance;
                        String totalDistance = String.valueOf(itemDistance.text);
                        String totalDuration = String.valueOf(itemDuration.text);

                        placeModels.add(new PlaceModel(info.name, info.vicinity, totalDistance, totalDuration));

                        pharmcyDetails.add(new PharmcyDetail(info.name, info.vicinity, totalDistance, totalDuration,info.geometry.locationOfPharmcy.lat,info.geometry.locationOfPharmcy.lng));

                        if (placeModels.size() == 10 || placeModels.size() == results.size()) {
                            RecyclerViewAdapter adapterStores = new RecyclerViewAdapter(results, placeModels);
                            recyclerView.setAdapter(adapterStores);
                            uk.ac.herts.mint.data.PharmcyProvider.getInstance().setPharmcyDetails(pharmcyDetails);
                        }
                    }
                }


            }

            @Override
            public void onFailure(retrofit2.Call<ResultDistanceMatrix> call, Throwable t) {
                call.cancel();
            }
        });

    }
}
