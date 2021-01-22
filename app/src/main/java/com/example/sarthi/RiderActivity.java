package com.example.sarthi;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sarthi.SendNotificationPack.APIService;
import com.example.sarthi.SendNotificationPack.Client;
import com.example.sarthi.SendNotificationPack.Token;
import com.example.sarthi.adapter.AutoSuggestAdapter;
import com.example.sarthi.plugin.DirectionPolylinePlugin;
import com.example.sarthi.utils.CheckInternet;
import com.example.sarthi.utils.TransparentProgressDialog;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.mapboxsdk.MapmyIndia;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
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
import com.mmi.services.account.MapmyIndiaAccountManager;
import com.mmi.services.api.autosuggest.MapmyIndiaAutoSuggest;
import com.mmi.services.api.autosuggest.model.AutoSuggestAtlasResponse;
import com.mmi.services.api.autosuggest.model.ELocation;
import com.mmi.services.api.directions.DirectionsCriteria;
import com.mmi.services.api.directions.MapmyIndiaDirections;
import com.mmi.services.api.directions.models.DirectionsResponse;
import com.mmi.services.api.directions.models.DirectionsRoute;
import com.mmi.services.api.textsearch.MapmyIndiaTextSearch;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RiderActivity extends AppCompatActivity implements OnMapReadyCallback, TextWatcher, TextView.OnEditorActionListener, LocationEngineListener {

    private MapboxMap mapmyIndiaMap;
    private EditText autoSuggestText;
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private TransparentProgressDialog transparentProgressDialog;
    private Handler handler;
    MapView mapView;
    DatabaseReference ref;
    LocationComponent locationComponent;
    LocationEngine locationEngine;
    AlertDialog alertDialog;
    Map<String, Double> userDetails = new HashMap<>();
    DatabaseReference mDatabase;
    int count=0;
    ProgressDialog progressDialog;
    Intent intent;

    Location pickupLocation;

    private String profile = DirectionsCriteria.PROFILE_DRIVING;
    private TabLayout profileTabLayout;
    private String resource = DirectionsCriteria.RESOURCE_ROUTE;
    private LinearLayout directionDetailsLayout;
    private TextView tvDistance, tvDuration;
    Double userLat, userLang;
    private DirectionPolylinePlugin directionPolylinePlugin;
    GeoFire geoFire;
    String riderId,userId;

    Button bookCab;
    //String receiveRiderId;
    APIService apiService;

    ConstraintLayout constrainLayout;
    private Location currentLocation;
    boolean pickUpFlag=false, currentPickup = true;

    EditText searchBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        //constrainLayout = findViewById(R.id.constraintLayout);

        bookCab = findViewById(R.id.confirm);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

       MapmyIndiaAccountManager.getInstance().setRestAPIKey("j6fwc3moyp8sttsnjh6ujm2osc2lvree");
        MapmyIndiaAccountManager.getInstance().setMapSDKKey("w5akjw9hls9qxdegtvaotoomzp77swkp");
        MapmyIndiaAccountManager.getInstance().setAtlasClientId("33OkryzDZsLz2lmTOKKh8gH7apaplrO3EXknMVxedv-quL2_rLH4OLVUtq5Zd23BMGh6BbS_YbYlhjigCfLg5bJ3dDK09N0b9-iSCeRSh-4McbpbDEUcug==");
        MapmyIndiaAccountManager.getInstance().setAtlasClientSecret("lrFxI-iSEg-MyQs5i0w6OqurUc_XlS9jTjx4ChXwrCaJFXR-UXHI-6Z3m5XkfR2tCzIb7K8vAiknygOlFuYhBxshnVVmsmU_8Fnp6LPe8jC3mC_-fMgRO2pm10eeelFQ");
        MapmyIndiaAccountManager.getInstance().setAtlasGrantType("client_credentials");

        MapmyIndia.getInstance(this);

        //intent = new Intent(RiderActivity.this, RiderMapActivity.class);
        //receiveRiderId=intent.getStringExtra("riderId");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("request");

        searchBar = (EditText) findViewById(R.id.auto_suggest);

        searchBar.setHint("Enter your pickup location..");

        UpdateToken();

        //asking for gps access
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        bookCab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentPickup == true)
                {
                    if(userDetails.isEmpty() == false) {

                        searchBar.setText("");
                        bookCab.setText("Book Cab");
                        currentPickup = false;
                        searchBar.setHint("Enter your destination location..");

                    }
                    else
                        showToast("Please select a pickup location");

                }


                else
                {
                    bookTheCab();
                }
            }
        });

        /*mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                    count = (int) snapshot.getChildrenCount();
                Log.i("main ", String.valueOf(count));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        //assert mapFragment != null;
        //mapFragment.getMapAsync(this);

        directionDetailsLayout = findViewById(R.id.direction_details_layout);
        tvDistance = findViewById(R.id.tv_distance);
        tvDuration = findViewById(R.id.tv_duration);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        initReferences();
        initListeners();

        progressDialog = new ProgressDialog(RiderActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Looking for nearby Cabs..");
        //progressDialog.show();

        alertDialog = new AlertDialog.Builder(RiderActivity.this).setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Take your current location as pickup location?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {


                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pickUpFlag = true;
                                searchBar.setHint("Enter your destination location..");
                                currentPickup = false;
                            }
                        }
                ).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        searchBar.setVisibility(View.VISIBLE);
                        initReferences();
                        initListeners();
                        bookCab.setText("Go");
                    }
                }).show();
    }




    private void initListeners() {
        autoSuggestText.addTextChangedListener(this);
        autoSuggestText.setOnEditorActionListener(this);
    }

    private void initReferences() {
        constrainLayout=findViewById(R.id.constraintLayout);
        autoSuggestText = findViewById(R.id.auto_suggest);
        recyclerView = findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(RiderActivity.this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setVisibility(View.GONE);
        transparentProgressDialog = new TransparentProgressDialog(this, R.drawable.circle_loader, "");
        handler = new Handler();
        constrainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                recyclerView.setVisibility(View.GONE);
                return false;
            }
        });
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
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationEngine.requestLocationUpdates();

    }

    @Override
    public void onLocationChanged(Location location) {

        Log.i("rideractivity","active");
        currentLocation = location;
        Log.i("current", String.valueOf(location.getLatitude()));

        mapmyIndiaMap.animateCamera(CameraUpdateFactory.
                newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),16));
        if(pickUpFlag == false && currentPickup == true) {
            pickupLocation = currentLocation;
            Log.i("pickup current", String.valueOf(location.getLatitude()));
            userDetails.put("UserPickupLatitude", location.getLatitude());
            userDetails.put("UserPickupLongitude", location.getLongitude());
        }



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

    @Override
    public void onMapReady(MapboxMap mapmyIndiaMap) {
        this.mapmyIndiaMap = mapmyIndiaMap;
        enableLocation();

        mapmyIndiaMap.setPadding(20, 20, 20, 20);


        /*if(currentLocation != null) {
            Log.i("Lat", String.valueOf(currentLocation.getLatitude()));
            Log.i("Long", String.valueOf(currentLocation.getLongitude()));

            mapmyIndiaMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                    Double.parseDouble(String.valueOf(currentLocation.getLatitude())),
                    Double.parseDouble(String.valueOf(currentLocation.getLongitude()))), 10));

        }
        else
            showToast("null user location");*/



    }



    public void selectedPlace(ELocation eLocation)
    {
        String add = "Latitude: " + eLocation.latitude + " longitude: " + eLocation.longitude;

        Log.i("Elocation lat", String.valueOf(eLocation.latitude));
        Log.i("Elocation long", String.valueOf(eLocation.longitude));
        if(eLocation.latitude != null && eLocation.longitude != null)
        {
            mapmyIndiaMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                    Double.parseDouble(eLocation.latitude),
                    Double.parseDouble(eLocation.longitude)), 10));
            addMarker(Double.parseDouble(eLocation.latitude), Double.parseDouble(eLocation.longitude));
            showToast(add);

            if(currentPickup == false) {
                userDetails.put("UserDestinationLatitude", Double.parseDouble(eLocation.latitude));
                userDetails.put("UserDestinationLongitude", Double.parseDouble(eLocation.longitude));
            }
            else {
                pickupLocation = new Location("");
                pickupLocation.setLongitude(Double.parseDouble(eLocation.longitude));
                pickupLocation.setLatitude(Double.parseDouble(eLocation.latitude));
                userDetails.put("UserPickupLatitude", Double.parseDouble(eLocation.latitude));
                userDetails.put("UserPickupLongitude", Double.parseDouble(eLocation.longitude));
            }
        }
        else
            showToast("Some Error Occurred");
    }



    private void addMarker(double latitude, double longitude) {
        mapmyIndiaMap.clear();
        mapmyIndiaMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));
    }


    private void callAutoSuggestApi(String searchString) {
        MapmyIndiaAutoSuggest.builder()
                .query(searchString)
                .build()
                .enqueueCall(new Callback<AutoSuggestAtlasResponse>() {
                    @Override
                    public void onResponse(Call<AutoSuggestAtlasResponse> call, Response<AutoSuggestAtlasResponse> response)
                    {
                        if (response.code() == 200)
                        {
                            if (response.body() != null)
                            {
                                ArrayList<ELocation> suggestedList = response.body().getSuggestedLocations();
                                if (suggestedList.size() > 0)
                                {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    AutoSuggestAdapter autoSuggestAdapter = new AutoSuggestAdapter(suggestedList, eLocation -> {
                                        if(eLocation != null)
                                        {
                                            selectedPlace(eLocation);
                                            recyclerView.setVisibility(View.GONE);
                                        }
                                    });
                                    recyclerView.setAdapter(autoSuggestAdapter);
                                }
                            } else {
                                showToast("Not able to get value, Try again.");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AutoSuggestAtlasResponse> call, Throwable t) {
                        showToast(t.toString());
                    }
                });

    }


    private void callTextSearchApi(String searchString) {
        MapmyIndiaTextSearch.builder()
                .query(searchString)
                .build().enqueueCall(new Callback<AutoSuggestAtlasResponse>() {
            @Override
            public void onResponse(Call<AutoSuggestAtlasResponse> call, Response<AutoSuggestAtlasResponse> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        ArrayList<ELocation> suggestedList = response.body().getSuggestedLocations();
                        if (suggestedList.size() > 0) {
                            recyclerView.setVisibility(View.VISIBLE);
                            AutoSuggestAdapter autoSuggestAdapter = new AutoSuggestAdapter(suggestedList, eLocation -> {
                                selectedPlace(eLocation);
                                recyclerView.setVisibility(View.GONE);
                            });
                            recyclerView.setAdapter(autoSuggestAdapter);
                        }
                    } else {
                        showToast("Not able to get value, Try again.");
                    }
                }
            }

            @Override
            public void onFailure(Call<AutoSuggestAtlasResponse> call, Throwable t) {
                showToast(t.toString());
            }
        });
    }

    private void show() {
        transparentProgressDialog.show();
    }

    private void hide() {
        transparentProgressDialog.dismiss();
    }

    private void showToast(String msg) {
        Toast.makeText(RiderActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        handler.postDelayed(() -> {
            recyclerView.setVisibility(View.GONE);
            if (s.length() < 3)
                recyclerView.setAdapter(null);

            if (s != null && s.toString().trim().length() < 2) {
                recyclerView.setAdapter(null);
                return;
            }

            if (s.length() > 2) {
                if (CheckInternet.isNetworkAvailable(RiderActivity.this)) {
                    callTextSearchApi(s.toString());
                } else {
                    showToast("Please check Internet!!");
                }
            }
        }, 3);
    }


    @Override
    public void afterTextChanged(Editable s) {
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId== EditorInfo.IME_ACTION_SEARCH){
            callTextSearchApi(v.getText().toString());
            autoSuggestText.clearFocus();
            InputMethodManager in = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(autoSuggestText.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    public void bookTheCab()
    {
        if(userDetails.isEmpty() == false && userDetails.size() == 4) {
            //userDetails.put("riderId", Double.valueOf(receiveRiderId));
            mDatabase.child(String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getUid())).setValue(userDetails);

            bookCab.setVisibility(View.INVISIBLE);
            progressDialog.show();
            Runnable progressRunnable = new Runnable() {
                @Override
                public void run() {
                    progressDialog.hide();
                }
            };
            Handler pdCanceller = new Handler();
            pdCanceller.postDelayed(progressRunnable, 30000);

            String userId;
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            ref = FirebaseDatabase.getInstance("https://fir-auth-52437-default-rtdb.firebaseio.com/").getReference().child("AcceptedRequest");

            final String[] driverID = {""};
            //ref.addValueEventListener(new)


            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(userId).exists()) {
                        retrievedata();
                        progressDialog.hide();

                        /*intent = new Intent(RiderActivity.this, RiderMapActivity.class);
                        if(currentLocation != null)
                        {

                            intent.putExtra("UserCurrentLatitude",currentLocation.getLatitude());
                            intent.putExtra("UserCurrentLongitude", currentLocation.getLongitude());
                        }
                        startActivity(intent);
                        finish();*/
                    } else
                        showToast("No rides Available");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    //theLayout.setVisibility(View.GONE);
                    bookCab.setVisibility(View.VISIBLE);
                }
            });
        }
        else
            showToast("Unable to get your current location :(");



        //jab tak driver accept ni krega, show loading screen.
    }

    private void UpdateToken(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        //Log.i("CheckToken",refreshToken);
        com.example.sarthi.SendNotificationPack.Token token= new Token(refreshToken);
        FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }

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

    protected CameraPosition setCameraAndTilt() {
        return new CameraPosition.Builder().target(new LatLng(
                28.551087, 77.257373)).zoom(11).tilt(0).build();
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
                    Toast.makeText(RiderActivity.this, response.message() + response.code(), Toast.LENGTH_LONG).show();
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

    void retrievedata()
    {
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance("https://fir-auth-52437-default-rtdb.firebaseio.com/").getReference().child("AcceptedRequest").child(userId);

        final String[] driverID = {""};
        //ref.addValueEventListener(new)


        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                driverID[0] = snapshot.getValue().toString();
                Log.i("driverId", String.valueOf(driverID[0]));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ref = FirebaseDatabase.getInstance("https://fir-auth-52437-default-rtdb.firebaseio.com/").getReference().child("driversWorking").child(driverID[0]);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                GeoFire geoFire = new GeoFire(ref);

                geoFire.getLocation(driverID[0], new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        if (location != null) {
                            Double lat = location.latitude;
                            Double lang = location.longitude;
                            Log.i("geofireLat", String.valueOf(location.latitude));
                            Log.i("geofireLong", String.valueOf(location.longitude));
                            Location location1 = new Location("");
                            location1.setLatitude(lat);
                            location1.setLongitude(lang);
                            float distance = currentLocation.distanceTo(location1);
                            if(pickupLocation != null && distance > 2.0) {
                                getDirections(new LatLng(lat, lang), new LatLng(pickupLocation.getLatitude(), pickupLocation.getLongitude()));

                                Log.i("currentLat ", String.valueOf(currentLocation.getLatitude()));
                                Log.i("currentLong ", String.valueOf(currentLocation.getLongitude()));
                            }

                            if(distance < 2.0){
                                showToast("Your ride has arrived!!");
                            }

                            //System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));
                        } else {
                            //System.out.println(String.format("There is no location for key %s in GeoFire", key));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.err.println("There was an error getting the GeoFire location: " + databaseError);
                    }
                });
                //if(PickerActivity.isrideStart == false) {

                //Log.i("checkingSnapshot", String.valueOf(snapshot.child("0")));
                //Log.i("checkingSnapshot1", String.valueOf(snapshot.child("1")));

                // Double lat = Double.parseDouble(snapshot.child("0").getValue().toString());

                //Double lang = Double.parseDouble(snapshot.child("1").getValue().toString());

                //Log.i("driverLat", String.valueOf(lat));
                //getDirections(new LatLng(lat, lang), new LatLng(userLat, userLang));
                //}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}