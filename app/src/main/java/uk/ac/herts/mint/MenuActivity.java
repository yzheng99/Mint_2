package uk.ac.herts.mint;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import uk.ac.herts.mint.login.LoginMainActivity;

public class MenuActivity extends AppCompatActivity {

    private static FrameLayout frameLayout;

    private Button bt_nhs, signin, signup, resetpass;
    private EditText inputemail, inputpassword;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);


        frameLayout=findViewById(R.id.frame_login);
        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);

        //       FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();


            //Displaying Toast with Hello Javatpoint message

//            if ((mAuth.getCurrentUser() != null)){
//                String email=mAuth.getCurrentUser().getEmail();
//                Intent intent = new Intent(getApplicationContext(), LoginMainActivity.class);
//                startActivity(intent);
//                finish();
//            }

        //--------------------NHS-----------
        bt_nhs = findViewById(R.id.bt_nhs);
        bt_nhs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            getPrecription();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
//                Intent intent = new Intent(getApplicationContext(), uk.ac.herts.mint.login.RegisterActivity.class);
//                startActivity(intent);
            }
        });
        //--------------------end

        inputemail = findViewById(R.id.input_username4);
        inputpassword = findViewById(R.id.input_password4);

        signin = findViewById(R.id.button_login4);
        signup = findViewById(R.id.button_register4);
        resetpass = findViewById(R.id.button_forgot_password4);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = inputemail.getText().toString()+"";
                final String password = inputpassword.getText().toString()+"";

                try {
                    if(password.length()>0 && email.length()>0) {
                        pd.show();
                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(MenuActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(),
                                                    "Authentication Failed",
                                                    Toast.LENGTH_LONG).show();
                                            Log.v("error", task.getException().getMessage());
                                        } else {

                                            Intent intent = new Intent(getApplicationContext(), LoginMainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                        pd.dismiss();
                                    }
                                });
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Please fill all the field.", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), uk.ac.herts.mint.login.RegisterActivity.class);
                startActivity(intent);
            }
        });

        resetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), uk.ac.herts.mint.login.RestorePassword.class);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void getPrecription(){
        try {

            URL url = new URL("https://sandbox.api.service.nhs.uk/electronic-prescriptions/FHIR/R4/Task/$release");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("accept", "application/fhir+json");
            http.setRequestProperty("NHSD-Session-URID", "555254240100");
            http.setRequestProperty("X-Request-ID", "60E0B220-8136-4CA5-AE46-1D97EF59D068");
            http.setRequestProperty("X-Correlation-ID", "11C46F5F-CDEF-4865-94B2-0EE0EDCC26DA");
            http.setRequestProperty("Content-Type", "application/fhir+json");

            String data = "{\n  \"resourceType\": \"Parameters\",\n  \"id\": \"854b706a-c6e5-11ec-9d64-0242ac120002\",\n  \"parameter\": [\n    {\n      \"name\": \"owner\",\n      \"valueIdentifier\": {\n        \"system\": \"https://fhir.nhs.uk/Id/ods-organization-code\",\n        \"value\": \"VNCEL\"\n      }\n    },\n    {\n      \"name\": \"status\",\n      \"valueCode\": \"accepted\"\n    },\n    {\n      \"name\": \"agent\",\n      \"resource\": {\n        \"resourceType\": \"PractitionerRole\",\n        \"id\": \"16708936-6397-4e03-b84f-4aaa790633e0\",\n        \"identifier\": [\n          {\n            \"system\": \"https://fhir.nhs.uk/Id/sds-role-profile-id\",\n            \"value\": \"555086415105\"\n          }\n        ],\n        \"practitioner\": {\n          \"identifier\": {\n            \"system\": \"https://fhir.nhs.uk/Id/sds-user-id\",\n            \"value\": \"3415870201\"\n          },\n          \"display\": \"Jackie Clark\"\n        },\n        \"organization\": {\n          \"identifier\": {\n            \"system\": \"https://fhir.nhs.uk/Id/ods-organization-code\",\n            \"value\": \"RHM\"\n          },\n          \"display\": \"UNIVERSITY HOSPITAL SOUTHAMPTON NHS FOUNDATION TRUST\"\n        },\n        \"code\": [\n          {\n            \"coding\": [\n              {\n                \"system\": \"https://fhir.hl7.org.uk/CodeSystem/UKCore-SDSJobRoleName\",\n                \"code\": \"R8000\",\n                \"display\": \"Clinical Practitioner Access Role\"\n              }\n            ]\n          }\n        ],\n        \"telecom\": [\n          {\n            \"system\": \"phone\",\n            \"value\": \"02380798431\",\n            \"use\": \"work\"\n          }\n        ]\n      }\n    }\n  ]\n}";

            byte[] out = data.getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);

            System.out.println(http.getResponseCode() + " " + http.getResponseMessage());

            if (http.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + http.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((http.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            http.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    //
    private void getPatientbyID(){
        try {

            URL url = new URL("https://sandbox.api.service.nhs.uk/personal-demographics/FHIR/R4/Patient/9000000009");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestProperty("accept", "application/fhir+json");
            http.setRequestProperty("NHSD-Session-URID", "555254240100");
            http.setRequestProperty("X-Request-ID", "60E0B220-8136-4CA5-AE46-1D97EF59D068");
            http.setRequestProperty("X-Correlation-ID", "11C46F5F-CDEF-4865-94B2-0EE0EDCC26DA");

            System.out.println(http.getResponseCode() + " " + http.getResponseMessage());

            if (http.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + http.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((http.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            http.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    private void getRelatedPatient(){
        try {

            URL url = new URL(
                    "https://sandbox.api.service.nhs.uk/personal-demographics/FHIR/R4/Patient/9000000009/RelatedPerson");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestProperty("accept", "application/fhir+json");
            http.setRequestProperty("NHSD-Session-URID", "555254240100");
            http.setRequestProperty("X-Request-ID", "60E0B220-8136-4CA5-AE46-1D97EF59D068");
            http.setRequestProperty("X-Correlation-ID", "11C46F5F-CDEF-4865-94B2-0EE0EDCC26DA");

            System.out.println(http.getResponseCode() + " " + http.getResponseMessage());

            if (http.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + http.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((http.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            http.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
    }
}
