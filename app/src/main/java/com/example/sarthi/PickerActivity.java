package com.example.sarthi;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sarthi.plugin.DirectionPolylinePlugin;
import com.example.sarthi.utils.CheckInternet;
import com.example.sarthi.utils.TransparentProgressDialog;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mmi.services.api.directions.DirectionsCriteria;
import com.mmi.services.api.directions.MapmyIndiaDirections;
import com.mmi.services.api.directions.models.DirectionsResponse;
import com.mmi.services.api.directions.models.DirectionsRoute;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

//import com.example.utils.TransparentProgressDialog;
//import com.mapmyindia.sdk.demo.java.plugin.DirectionPolylinePlugin;
//import com.mapmyindia.sdk.demo.java.utils.CheckInternet;


public class PickerActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener {

    Intent intent;
    LatLng driverLocation, riderLocation;
    TextView userDetails;

    private MapboxMap mapmyIndiaMap;

    FirebaseFirestore db=FirebaseFirestore.getInstance();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    AlertDialog alertDialog;
    Location lastKnownLocation;
    private MapView mapView;
    private TransparentProgressDialog transparentProgressDialog;
    private String profile = DirectionsCriteria.PROFILE_DRIVING;
    //private TabLayout profileTabLayout;
    private String resource = DirectionsCriteria.RESOURCE_ROUTE;
    private LinearLayout directionDetailsLayout;
    private TextView tvDistance, tvDuration;
    LocationComponent locationComponent;
    private DirectionPolylinePlugin directionPolylinePlugin;
    GeoFire geoFire;
    String riderId;
    Button endStartRide,cancelride;
    DatabaseReference ref;

    static Boolean isrideStart = false;

    Boolean flag = false;

    LatLng userSource, userDest, destination;
    LocationEngine locationEngine;

    Boolean started = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);
        mapView = findViewById(R.id.map_view);
        userDetails=(TextView)findViewById(R.id.userDetails);

        //profileTabLayout = findViewById(R.id.tab_layout_profile);
        //RadioGroup rgResource = findViewById(R.id.rg_resource_type);

        directionDetailsLayout = findViewById(R.id.direction_details_layout);
        tvDistance = findViewById(R.id.tv_distance);
        tvDuration = findViewById(R.id.tv_duration);
        endStartRide = findViewById(R.id.start_ride);
        //cancelride = findViewById(R.id.cancel_ride);
//        profileTabLayout.setVisibility(View.GONE);

        intent = getIntent();
        riderId = intent.getStringExtra("riderID");
        userDest =  new LatLng(intent.getDoubleExtra("dest Lat", 0), intent.getDoubleExtra("dest Lang", 0));

        endStartRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(started)
                {
                    endRide();
                    started = false;
                }
                else
                {
                    started = true;
                    startRide();
                }
            }
        });

        /*profileTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (mapmyIndiaMap == null) {
                    if (profileTabLayout.getTabAt(0) != null) {
                        Objects.requireNonNull(profileTabLayout.getTabAt(0)).select();
                        return;
                    }
                }
                switch (tab.getPosition()) {
                    case 0:
                        profile = DirectionsCriteria.PROFILE_DRIVING;
                        //rgResource.setVisibility(View.VISIBLE);
                        break;

                    case 1:
                        profile = DirectionsCriteria.PROFILE_BIKING;
                        //rgResource.check(R.id.rb_without_traffic);
                        //rgResource.setVisibility(View.GONE);
                        break;

                    case 2:
                        profile = DirectionsCriteria.PROFILE_WALKING;
                        //rgResource.check(R.id.rb_without_traffic);
                        //rgResource.setVisibility(View.GONE);
                        break;

                    default:
                        break;
                }

                intent = getIntent();

                driverLocation = new LatLng(intent.getDoubleExtra("driverLat", 0), intent.getDoubleExtra("driverLang", 0));
                riderLocation = new LatLng(intent.getDoubleExtra("requestLat", 0), intent.getDoubleExtra("requestLang", 0));

                if(flag == false)
                    getDirections(driverLocation, riderLocation);
                else
                    getDirections(userSource, userDest);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });*/

        /*rgResource.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.rb_without_traffic:
                    resource = DirectionsCriteria.RESOURCE_ROUTE;
                    break;

                case R.id.rb_with_traffic:
                    resource = DirectionsCriteria.RESOURCE_ROUTE_TRAFFIC;
                    break;

                case R.id.rb_with_route_eta:
                    resource = DirectionsCriteria.RESOURCE_ROUTE_ETA;
                    break;

                default:
                    break;
            }
            intent = getIntent();

            driverLocation = new LatLng(intent.getDoubleExtra("driverLat", 0), intent.getDoubleExtra("driverLang", 0));
            riderLocation = new LatLng(intent.getDoubleExtra("requestLat", 0), intent.getDoubleExtra("requestLang", 0));

            if(flag == false)
                getDirections(driverLocation, riderLocation);
            else
                getDirections(userSource, userDest);
        });*/
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        transparentProgressDialog = new TransparentProgressDialog(this, R.drawable.circle_loader, "");


        FirebaseUser user=firebaseAuth.getCurrentUser();

        DocumentReference documentReference=db.collection("data").document(riderId);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        String getUserDetails="User Phone No: "+document.get("phone").toString();
                        Log.i("userPhoneNo",getUserDetails);
                        userDetails.setText(getUserDetails);
                    }
                }

            }
        });


    }

    @Override
    public void onMapReady(MapboxMap mapmyIndiaMap) {
        this.mapmyIndiaMap = mapmyIndiaMap;
        enableLocation();

        mapmyIndiaMap.setPadding(20, 20, 20, 20);
//        profileTabLayout.setVisibility(View.VISIBLE);

        mapmyIndiaMap.setCameraPosition(setCameraAndTilt());
        if (CheckInternet.isNetworkAvailable(PickerActivity.this)) {
            intent = getIntent();

            driverLocation = new LatLng(intent.getDoubleExtra("driverLat", 0), intent.getDoubleExtra("driverLang", 0));
            riderLocation = new LatLng(intent.getDoubleExtra("requestLat", 0), intent.getDoubleExtra("requestLang", 0));

            getDirections(driverLocation, riderLocation);
        } else {
            Toast.makeText(this,("Please Check Internet Connection"), Toast.LENGTH_SHORT).show();
        }
    }

    //private void sendArrivedNotification(String riderId) {
      //  Token token= new Token(riderId);
        //Notification notification=new Notification(String.format("Driver has arrived"));
    //}

    /**
     * Set Camera Position
     *
     * @return camera position
     */
    protected CameraPosition setCameraAndTilt() {
        return new CameraPosition.Builder().target(new LatLng(
                28.551087, 77.257373)).zoom(14).tilt(0).build();
    }

    /**
     * Show Progress Dialog
     */
    private void progressDialogShow() {
        transparentProgressDialog.show();
    }

    /**
     * Hide Progress dialog
     */
    private void progressDialogHide() {
        transparentProgressDialog.dismiss();
    }

    /**
     * Get Directions
     */
    private void getDirections(LatLng source, LatLng dest) {
        progressDialogShow();


        MapmyIndiaDirections.builder()
                .origin(Point.fromLngLat(source.getLongitude(), source.getLatitude()))
                .destination(Point.fromLngLat(dest.getLongitude(), dest.getLatitude()))
                .profile(profile)
                .resource(resource)
                .steps(true)
                .alternatives(false)
                .overview(DirectionsCriteria.OVERVIEW_FULL).build().enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        DirectionsResponse directionsResponse = response.body();
                        List<DirectionsRoute> results = directionsResponse.routes();

                        if (results.size() > 0) {
                            mapmyIndiaMap.clear();
                            DirectionsRoute directionsRoute = results.get(0);
                            if (directionsRoute != null && directionsRoute.geometry() != null) {
                                drawPath(PolylineUtils.decode(directionsRoute.geometry(), Constants.PRECISION_6));
                                updateData(directionsRoute);
                            }
                        }
                    }
                } else {
                    Toast.makeText(PickerActivity.this, response.message() + response.code(), Toast.LENGTH_LONG).show();
                }
                progressDialogHide();
            }

            @Override
            public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable t) {
                progressDialogHide();
                t.printStackTrace();

            }
        });


    }

    /**
     * Update Route data
     *
     * @param directionsRoute route data
     */
    private void updateData(@NonNull DirectionsRoute directionsRoute) {
        if (directionsRoute.distance() != null && directionsRoute.distance() != null) {
            directionDetailsLayout.setVisibility(View.VISIBLE);
            tvDuration.setText("(" + getFormattedDuration(directionsRoute.duration()) + ")");
            tvDistance.setText(getFormattedDistance(directionsRoute.distance()));
        }
    }

    /**
     * Get Formatted Distance
     *
     * @param distance route distance
     * @return distance in Kms if distance > 1000 otherwise in mtr
     */
    private String getFormattedDistance(double distance) {

        if ((distance / 1000) < 1) {
            return distance + "mtr.";
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        return decimalFormat.format(distance / 1000) + "Km";
    }

    /**
     * Get Formatted Duration
     *
     * @param duration route duration
     * @return formatted duration
     */
    private String getFormattedDuration(double duration) {
        long min = (long) (duration % 3600 / 60);
        long hours = (long) (duration % 86400 / 3600);
        long days = (long) (duration / 86400);
        if (days > 0L) {
            return days + " " + (days > 1L ? "Days" : "Day") + " " + hours + " " + "hr" + (min > 0L ? " " + min + " " + "min." : "");
        } else {
            return hours > 0L ? hours + " " + "hr" + (min > 0L ? " " + min + " " + "min" : "") : min + " " + "min.";
        }
    }

    /**
     * Add polyline along the points
     *
     * @param waypoints route points
     */
    private void drawPath(@NonNull List<Point> waypoints) {
        ArrayList<LatLng> listOfLatLng = new ArrayList<>();
        for (Point point : waypoints) {
            listOfLatLng.add(new LatLng(point.latitude(), point.longitude()));
        }

        if(directionPolylinePlugin == null) {
            directionPolylinePlugin = new DirectionPolylinePlugin(mapmyIndiaMap, mapView, profile);
            directionPolylinePlugin.createPolyline(listOfLatLng);
        } else {
            directionPolylinePlugin.updatePolyline(profile, listOfLatLng);

        }
//        mapmyIndiaMap.addPolyline(new PolylineOptions().addAll(listOfLatLng).color(Color.parseColor("#3bb2d0")).width(4));
        LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(listOfLatLng).build();
        mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 30));
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
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public void startRide() {

        endStartRide.setText("END RIDE");
        intent = getIntent();
        //cancelride.setVisibility(View.GONE);

        isrideStart=true;


        flag = true;

        userDest = new LatLng(intent.getDoubleExtra("dest Lat", 0), intent.getDoubleExtra("dest Lang", 0));
        userSource = new LatLng(intent.getDoubleExtra("requestLat", 0), intent.getDoubleExtra("requestLang", 0));
        Log.i("destination", String.valueOf(userDest.getLongitude()));
        Log.i("source", String.valueOf(userSource));
        getDirections(userSource, userDest);

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
        }
        mapmyIndiaMap.animateCamera(CameraUpdateFactory.
                newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),16));
        if(getApplicationContext()!=null && lastKnownLocation != null){

            /*if(!customerId.equals("") && mLastLocation!=null && location != null){
                rideDistance += mLastLocation.distanceTo(location)/1000;
            }*/
            lastKnownLocation = location;


            LatLng latLng = new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()),16));
            getDirections(latLng, userDest);

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            //DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking");
            //GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);

            geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));

        }

    }

    private void enableLocation() {
        LocationComponentOptions options = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.colorAccent))
                .build();
        // Get an instance of the component LocationComponent
        if(mapmyIndiaMap==null) {
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
        if(locationComponent == null)
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

    public void cancelRide(View view) {
        alertDialog = new AlertDialog.Builder(PickerActivity.this).setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Cancel Ride!").setMessage("Are you sure?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {


                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking").child(userId);
                                //DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("customerRequest");
                                refWorking.removeValue();
                                refWorking = FirebaseDatabase.getInstance().getReference("AcceptedRequest").child(riderId);
                                refWorking.removeValue();

                                Intent intent = new Intent(PickerActivity.this, DriverActivity.class);

                                startActivity(intent);

                            }
                        }
                ).setNegativeButton("NO",null).show();
    }

    public void endRide() {




        alertDialog = new AlertDialog.Builder(PickerActivity.this).setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("End Ride!").setMessage("Are you sure?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking").child(userId);
                                //DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("customerRequest");
                                refWorking.removeValue();

                                refWorking = FirebaseDatabase.getInstance().getReference("AcceptedRequest").child(riderId);
                                refWorking.removeValue();

                                Intent intent = new Intent(PickerActivity.this, DriverActivity.class);

                                startActivity(intent);

                            }
                        }
                ).setNegativeButton("NO",null).show();


    }


}
