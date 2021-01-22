package com.example.sarthi;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sarthi.SendNotificationPack.APIService;
import com.example.sarthi.SendNotificationPack.Client;
import com.example.sarthi.SendNotificationPack.Data;
import com.example.sarthi.SendNotificationPack.MyResponse;
import com.example.sarthi.SendNotificationPack.NotificationSender;
import com.example.sarthi.SendNotificationPack.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.mapboxsdk.MapmyIndia;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mmi.services.account.MapmyIndiaAccountManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class DriverActivity extends AppCompatActivity  implements OnMapReadyCallback, LocationEngineListener {

    ArrayList<Double> requests = new ArrayList<>();
    AlertDialog alertDialog;
    ProgressDialog progressDialog;
    double radius = 1000.0;

    boolean flag = false;

    Location lastKnownLocation;

    FirebaseFirestore db=FirebaseFirestore.getInstance();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    ArrayList<Double> latitude = new ArrayList<>();
    ArrayList<Double> longitude = new ArrayList<>();

    ArrayList<Double> destLatitude = new ArrayList<>();
    ArrayList<Double> destLongitude = new ArrayList<>();
    ArrayList<String> riderIDs = new ArrayList<>();
    String riderId;

    Location driverLocation = new Location("");

    float distance;
    MapView mapView;
    LocationComponent locationComponent;

    LocationEngine locationEngine;
    MapboxMap mapmyIndiaMap;


    HashMap<String, Double> riderLocation = new HashMap<String, Double>();

    DatabaseReference ref;

    APIService apiService;
    private String riderIDToSendNotif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);


        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        mapView = findViewById(R.id.map_view);
        //mapView.onCreate(savedInstanceState);
       MapmyIndiaAccountManager.getInstance().setRestAPIKey("j6fwc3moyp8sttsnjh6ujm2osc2lvree");
        MapmyIndiaAccountManager.getInstance().setMapSDKKey("w5akjw9hls9qxdegtvaotoomzp77swkp");
        MapmyIndiaAccountManager.getInstance().setAtlasClientId("33OkryzDZsLz2lmTOKKh8gH7apaplrO3EXknMVxedv-quL2_rLH4OLVUtq5Zd23BMGh6BbS_YbYlhjigCfLg5bJ3dDK09N0b9-iSCeRSh-4McbpbDEUcug==");
        MapmyIndiaAccountManager.getInstance().setAtlasClientSecret("lrFxI-iSEg-MyQs5i0w6OqurUc_XlS9jTjx4ChXwrCaJFXR-UXHI-6Z3m5XkfR2tCzIb7K8vAiknygOlFuYhBxshnVVmsmU_8Fnp6LPe8jC3mC_-fMgRO2pm10eeelFQ");
        MapmyIndiaAccountManager.getInstance().setAtlasGrantType("client_credentials");
        MapmyIndia.getInstance(getApplicationContext());
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //getting driver info

        Intent intent = getIntent();
        //DriverId = intent.getStringExtra("driverId");


        UpdateToken();
        progressDialog = new ProgressDialog(DriverActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Fetching available requests..");
        progressDialog.show();
        retrieveData();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("driverWorking");



        //asking for gps access
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

    }

    private void enableLocation() {
        LocationComponentOptions options = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.colorAccent))
                .build();
        // Get an instance of the component LocationComponent
        if(mapmyIndiaMap == null) {
            Log.i("MapMyIndiaNull", "not found location");
        }
        else
            locationComponent = mapmyIndiaMap.getLocationComponent();
        // Activate with options
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            return;
        }
        if(locationComponent==null)
            Log.i("LocationComponentNull","Not found");
        else {
            locationComponent.activateLocationComponent(this, options);
            // Enable to make component visiblelocationEngine
            locationComponent.setLocationComponentEnabled(true);
            locationEngine = locationComponent.getLocationEngine();

            locationEngine.addLocationEngineListener(this);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
        }
    }

    @Override
    public void onConnected() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            return;
        }
        locationEngine.requestLocationUpdates();

    }

    @Override
    public void onLocationChanged(Location location) {

        lastKnownLocation = location;
        Timber.i(String.valueOf(lastKnownLocation.getLongitude()));
        Timber.i(String.valueOf(lastKnownLocation.getLatitude()));
        if(lastKnownLocation != null && !flag)
        {
            flag = true;
            retrieveData();
        }
        mapmyIndiaMap.animateCamera(CameraUpdateFactory.
                newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),16));


    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapmyIndiaMap = mapboxMap;
        enableLocation();
    }

    @Override
    public void onMapError(int i, String s) {
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (locationEngine != null) {
            locationEngine.removeLocationEngineListener(this);
            locationEngine.addLocationEngineListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationEngine != null)
            locationEngine.removeLocationEngineListener(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationEngineListener(this);
            locationEngine.removeLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    //function to send notification to rider
    public void sendNotifications(String usertoken, String title, String message) {
        Log.i("checkingReach","reached");
        Data data = new Data(title, message);
        Log.i("final token", usertoken);
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        Toast.makeText(DriverActivity.this, "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }

    private void UpdateToken(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        Log.i("CheckToken",refreshToken);
        com.example.sarthi.SendNotificationPack.Token token= new Token(refreshToken);
        FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }

    public void go(String id) {

        FirebaseUser user=firebaseAuth.getCurrentUser();


        FirebaseDatabase.getInstance().getReference().child("Tokens").child(id).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userToken = dataSnapshot.getValue(String.class);
                FirebaseUser user=firebaseAuth.getCurrentUser();

                DocumentReference documentReference=db.collection("data").document(user.getUid());

                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()) {
                                //getDriverDetails = "Name: " + document.get("name").toString() + "\n" + "Phn No: " + document.get("phone").toString()
                                //      + "\n" + "Car Name: " + document.get("car model").toString() + "\n" + "Car Number: " + document.get("car number").toString();
                                sendNotifications( userToken, "YOUR RIDE IS ON IT'S WAY!!",
                                        "Driver Name: " + document.get("name").toString() + "\n" + "Phone No: " + document.get("phone").toString()
                                                + "\n" + "Car Model: " + document.get("car model").toString() + "\n" +
                                                "Car Number: " + document.get("car number").toString());
                            }
                        }

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //distanceWithinRadius();

    }

    private void distanceWithinRadius() {
        // progressDialog.show();
        boolean flag = false;
        for(int i=0; i<requests.size(); i++){
            if(requests.get(i)<=radius){
                flag = true;
                riderIDToSendNotif = riderIDs.get(i);
                progressDialog.hide();
                showAlertDialogBox(i);
            }
        }
        //progressDialog.hide();
        if(flag == false)
        {
            progressDialog.hide();
            Toast.makeText(this, "No rides available in this radius", Toast.LENGTH_SHORT).show();
        }

    }

    public void showAlertDialogBox(int position){

        alertDialog = new AlertDialog.Builder(DriverActivity.this).setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Ride available!").setMessage("Do you want to take this ride?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {



                            String userToken;
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                go(riderIDs.get(position));




                                /*FirebaseDatabase.getInstance().getReference().child("Tokens").child("E4xBJV1MT4X2xndrmPqhzeCWtW43")
                                        .child("token").
                                        addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                userToken = dataSnapshot.getValue(String.class);
                                                //go(riderIDs.get(position));
                                                riderIDToSendNotif = riderIDs.get(position);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });*/

                                Intent intent = new Intent(DriverActivity.this, PickerActivity.class);

                                intent.putExtra("dest Lat", destLatitude.get(position));

                                intent.putExtra("dest Lang", destLongitude.get(position));

                                intent.putExtra("requestLat", latitude.get(position));

                                Log.i("request lat", latitude.get(position).toString());

                                intent.putExtra("requestLang", longitude.get(position));

                                Log.i("request lang", longitude.get(position).toString());

                                intent.putExtra("driverLat", lastKnownLocation.getLatitude());

                                Log.i("driver lat", String.valueOf(lastKnownLocation.getLatitude()));

                                intent.putExtra("driverLang", lastKnownLocation.getLongitude());

                                intent.putExtra("riderID", riderIDs.get(position));

                                Log.i("driver lang", String.valueOf(lastKnownLocation.getLongitude()));


                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Tokens").child(riderIDs.get(position));
                                //ref.removeValue();

                                ref = FirebaseDatabase.getInstance().getReference().child("request").child(riderIDs.get(position));

                                ref.removeValue();

                                ref = FirebaseDatabase.getInstance().getReference().child("AcceptedRequest").child(riderIDs.get(position));
                                ref.setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());

                                requests.clear();
                                latitude.clear();
                                longitude.clear();
                                //riderIDs.clear();
                                flag = false;

                                startActivity(intent);

                            }
                        }
                ).setNegativeButton("NO",null).show();
        //calling the Driver's Map

    }


    void retrieveData(){

        ref = FirebaseDatabase.getInstance("https://fir-auth-52437-default-rtdb.firebaseio.com/").getReference().child("request");


        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    //retrieving data from the firebase database

                    Log.i("key value", postSnapshot.getKey());
                    riderIDs.add(postSnapshot.getKey());

                    Double lat = Double.parseDouble(postSnapshot.child("UserPickupLatitude").getValue().toString()) ;
                    Double lang = Double.parseDouble(postSnapshot.child("UserPickupLongitude").getValue().toString());
                    Double destLat = Double.parseDouble(postSnapshot.child("UserDestinationLatitude").getValue().toString()) ;
                    Double destLang = Double.parseDouble(postSnapshot.child("UserDestinationLongitude").getValue().toString());
                    Log.i("lat",String.valueOf(lat));
                    Log.i("longg",String.valueOf(lang));


                    Location riderLocation = new Location("");
                    riderLocation.setLatitude(lat);
                    riderLocation.setLongitude(lang);

                    latitude.add(lat);
                    longitude.add(lang);

                    destLatitude.add(destLat);
                    destLongitude.add(destLang);
                    Log.i("DestinationChecklat", String.valueOf(destLat));
                    Log.i("DestinationChecklang", String.valueOf(destLang));


                    if(lastKnownLocation != null) {

                        Double driverLat = lastKnownLocation.getLatitude();
                        Double driverLong = lastKnownLocation.getLongitude();

                        distance = lastKnownLocation.distanceTo(riderLocation);

                        //calculating distance in km

                        DecimalFormat decimalFormat = new DecimalFormat("#.#");

                        requests.add(Double.valueOf(decimalFormat.format(distance / 1000)));

                        Log.i("requests", String.valueOf(distance));
                        //adapter.notifyDataSetChanged();
                    }
                    else
                        Log.i("DriverError", "null current location");

                }

                distanceWithinRadius();



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //to check nearby requests for driver
    public void checkRequests(View view) {
        go(riderIDToSendNotif);
        distanceWithinRadius();
    }

}