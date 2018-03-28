package tba.googleapitest;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.sql.Time;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int LOCATION_PERMISSION_REQ_CODE = 123;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private boolean mLocationPermissionsGranted = false;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private List<String> routePolylines;
    private List<String> locPolyline;
    private Button btRoute;
    private Spinner daySpinner;

    private ArrayList<CourseObject> courses = null;

    private boolean useDynamicDay = false;
    private int selectedDay = Calendar.MONDAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.i("testing","testing");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        displayLocationSettingsRequest(this);
        getLocationPermission();
        Bundle b = getIntent().getExtras();
        courses = b.getParcelableArrayList("courses");
        routePolylines = new ArrayList<String>();
        locPolyline = null;

        btRoute = (Button) findViewById(R.id.bt_route);
        btRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLocRequest();
            }
        });

        btRoute = (Button) findViewById(R.id.bt_refresh);
        btRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshMap();
            }
        });

        initDaySpinner();
    }

    private void initDaySpinner() {
        daySpinner = (Spinner) findViewById(R.id.day_spinner);
        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String val = adapterView.getItemAtPosition(pos).toString();
                if (val.equals("Monday")) {
                    selectedDay = Calendar.MONDAY;
                } else if (val.equals("Tuesday")) {
                    selectedDay = Calendar.TUESDAY;
                } else if (val.equals("Wednesday")) {
                    selectedDay = Calendar.WEDNESDAY;
                } else if (val.equals("Thursday")) {
                    selectedDay = Calendar.THURSDAY;
                } else if (val.equals("Friday")) {
                    selectedDay = Calendar.FRIDAY;
                }
                refreshMap();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(mLocationPermissionsGranted){
            try{
                googleMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(this);
                setUpMap();
                //setUpMarkersWithInfo(); this should be called while correctly implemented and partial code of setUpMap() is comment out.

                //call another method here so that the marker will be drawn with additional info
            }
            catch(SecurityException c){
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "SecurityException : " + c.getMessage());
            }
        }
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    public void getLocationPermission(){
        String[] permissions =  {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();


            }
            else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQ_CODE);
            }
        }
        else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        mLocationPermissionsGranted = false;
        switch (requestCode){
            case LOCATION_PERMISSION_REQ_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    initMap();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */


    public void onMyLocationClick(@NonNull Location location) {
        //Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    public boolean onMyLocationButtonClick() {
        //Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    //when the implementation of this function is done, the part of addMarker of setUpMap() ,
    // updateMap(),updateLocRoute() can be comment out while setUpMarkers() should be called wherever
    // the aforementioned three methods get called originally.
    private void setUpMarkersWithInfo(){
        for(CourseObject c : courses){
            String day = c.getDay();
            //Dilemma here : Getting LatLng when given a CourseObject
            //Suggestion here : When drawing markers, can we loop over courseObject instead of generated List<LatLng>

            //Right now solution:
            //step1 : check whether c is at current day selectedDay, which is int , if yes proceed to step 2 , else continue for loop
            //step2 : check whether current time is later than the end of course time , if yes add marker, else continue for loop

            //here point is the corresponding LatLng object of the course.
            //mMap.addMarker(new MarkerOptions().position(point).title(c.getCode).snippet(starting_time.toString() + " " + c.getLocation()));

        }

    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        long processing_time = 10;
        long current_time = 1000;
        Time starting_time = new Time(current_time);
        
//        LatLng pointA = new LatLng(0, 0);
//        LatLng pointB = new LatLng(2, 0);
//        mMap.addMarker(new MarkerOptions().position(pointA).title("Marker").snippet(starting_time.toString()));
//        current_time = current_time + processing_time;
//        starting_time.setTime(current_time);
//        mMap.addMarker(new MarkerOptions().position(pointB).title("Marker2").snippet(starting_time.toString()));
//        mMap.addPolyline(new PolylineOptions().add(pointA).add(pointB));
//
//        Log.i("but why", "???");

        refreshMap();

//        String encodedString = "_hniG|crcN{Ad@{@Xb@bDR|A@TJl@BReBd@u@T";
//        List<LatLng> points = decodePolyline(encodedString);
//        LatLng start = points.get(0);
//        LatLng end;
//        for (LatLng point: points) {
//            Log.i("Point", point.toString());
//            current_time = current_time + processing_time;
//            starting_time.setTime(current_time);
//            if (points.indexOf(point) != 0){
//                end = point;
//                mMap.addPolyline(new PolylineOptions().add(start).add(end));
//
//            }
//            if (points.indexOf(point) == points.size() -1 ||points.indexOf(point) == 0 ){
//                mMap.addMarker(new MarkerOptions().position(point).title("Marker").snippet(starting_time.toString()));
//                // NOTE here: in order to make the marker to display info while being clicked,
//                //,      instead of creating marker using for loop of point,
//                // is it possible to create marker using for loop of courseObject ??
//                //Then the schedule display will be feasible .
//            }
//            start = point;
//        }
//        Log.i("Points size", points.size() + "");

        //Log.i("state", mMap.getMyLocation().toString());
    }

    private void filterByCurrentTime(List<String> buildingCodes, List<String> times){
        String currentHour = String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        String currentMinute = String.valueOf(Calendar.getInstance().get(Calendar.MINUTE));
//        int currentTime = Integer.parseInt(currentHour + currentMinute);
//        Log.i("courses", String.valueOf(currentTime));
        for(int i=0;i<buildingCodes.size();i++){
//            if(currentTime > Integer.parseInt(times.get(i))){
            if(Integer.parseInt(currentHour)> Integer.parseInt(times.get(i).substring(0,1))) {
//                indicesToRemove.add(i);
                buildingCodes.remove(i);
                times.remove(i);
                i--;

            }
            else if(Integer.parseInt(currentHour) == Integer.parseInt(times.get(i).substring(0,1))){
                if(Integer.parseInt(currentMinute) > Integer.parseInt(times.get(i).substring(2,3))){
                    buildingCodes.remove(i);
                    times.remove(i);
                    i--;
                }
            }
        }
    }

    private void refreshMap() {
        List<List<String>> reqLists = parseCourses();
//        Log.i("courses", reqLists.get(0).toString());
//        Log.i("courses", reqLists.get(1).toString());
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
//        Log.i("courses", dayOfWeek);
//        Log.i("courses", String.valueOf(selectedDay));
        if (dayOfWeek == selectedDay) {
             filterByCurrentTime(reqLists.get(0), reqLists.get(1));
        }
//        Log.i("courses", reqLists.get(0).toString());
//        Log.i("courses", reqLists.get(1).toString());
        if (!reqLists.isEmpty()&& !reqLists.get(0).isEmpty() && !reqLists.get(1).isEmpty()) {
            String result = HttpRequester.getRoute("routes",this, reqLists.get(0), reqLists.get(1));
            //Toast.makeText(this, "Result: " + result, Toast.LENGTH_LONG).show();
        }
        else{
            mMap.clear();
            Toast.makeText(this, "No Classes Today", Toast.LENGTH_LONG).show();
        }
    }

    private List<List<String>> parseCourses() {
        List<List<String>> rtn = new ArrayList<List<String>>();
        if (courses != null) {
            List<String> bcodes = new ArrayList<String>();
            List<String> times = new ArrayList<String>();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            String dayOfWeek = Integer.toString(cal.get(Calendar.DAY_OF_WEEK));
            if (!useDynamicDay) {
                dayOfWeek = Integer.toString(selectedDay);
            }
            for (CourseObject c: courses) {
                if (dayOfWeek.equals(c.getDay())) {
                    bcodes.add(c.getbuildingCode());
                    times.add(c.getStartTime());
                }
            }
            rtn.add(bcodes);
            rtn.add(times);
        }

        return rtn;
    }

    public void updateMap(List<String> polylines, boolean clearMap) {
        routePolylines = polylines;
        if (mMap != null) {
            if (clearMap) {
                mMap.clear();
            }
            int i = 0;
            double[] colour = {0.0, 30.0, 60, 90, 120, 240, 180, 210, 270, 300};
            // colour goes from red, orange, yellow, green, blue, cyan
            for (String polyline: polylines) {
                List<LatLng> points = decodePolyline(polyline);
                LatLng start = points.get(0);
                LatLng end;
                for (LatLng point: points) {
                    Log.i("Point", point.toString());
                    if (points.indexOf(point) != 0){
                        end = point;
                        mMap.addPolyline(new PolylineOptions().add(start).add(end));

                    }
                    if (points.indexOf(point) == points.size() -1 ||points.indexOf(point) == 0 ){
                        mMap.addMarker(new MarkerOptions().position(point).title(Integer.toString(i)).draggable(true)
                                .icon(BitmapDescriptorFactory.defaultMarker((float)(colour[i]))));
                    }
                    start = point;
                }
                i++;
            }
        }
    }

    public void requestCallback(String key, List<String> polylines) {
        if (key.equals("routes")) {
            updateMap(polylines, true);
            //setUpMarkersWithInfo(); this should be called while correctly implemented and partial code of updateMap() is comment out.
        } else if(key.equals("location")){
            updateLocRoute(polylines);
            //setUpMarkersWithInfo(); this should be called while correctly implemented and partial code of updateLocRoute() is comment out.
        }
    }

    public void sendLocRequest() {
        List<List<String>> reqLists = parseCourses();
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == selectedDay) {
            filterByCurrentTime(reqLists.get(0), reqLists.get(1));
        }
        if (mMap.isMyLocationEnabled() && !reqLists.isEmpty() && !reqLists.get(0).isEmpty() && !reqLists.get(1).isEmpty()) {
            String bcode = reqLists.get(0).get(0);
            String latLng = Double.toString(mMap.getMyLocation().getLatitude()) + "," +Double.toString(mMap.getMyLocation().getLongitude());
            HttpRequester.requestPathFromLocToClass("location", this, latLng, bcode);
        }
        else{
            Toast.makeText(this, "No Classes Today", Toast.LENGTH_LONG).show();
        }
    }

    public void updateLocRoute(List<String> polylines) {
        mMap.clear();
        locPolyline = polylines;
        if (mMap != null) {
            mMap.clear();
            for (String polyline: polylines) {
                List<LatLng> points = decodePolyline(polyline);
                LatLng start = points.get(0);
                LatLng end;
                for (LatLng point: points) {
                    Log.i("Point", point.toString());
                    if (points.indexOf(point) != 0){
                        end = point;
                        mMap.addPolyline(new PolylineOptions().add(start).add(end));

                    }
                    if (points.indexOf(point) == points.size() -1 ||points.indexOf(point) == 0 ){
                        mMap.addMarker(new MarkerOptions().position(point));
                    }
                    start = point;
                }
            }
        }

        if (routePolylines != null && !routePolylines.isEmpty()) {
            updateMap(routePolylines, false);
            //setUpMarkersWithInfo(); this should be called while correctly implemented and partial code of updateMap() is comment out.
        }
    }

    private List<LatLng> decodePolyline(String polylineString) {
        List<LatLng> rtn = new ArrayList<LatLng>();
        int index = 0;
        int lat = 0;
        int lng = 0;
        while (index < polylineString.length()) {
            int shift = 0;
            int res = 0;
            int b = 0;
            //Latitude decode
            while (true) {
                b = polylineString.charAt(index) - 63;
                index++;
                res |= (b & 0x1f) << shift;
                shift += 5;
                if (b < 0x20) {
                    break;
                }
            }
            int latIncr = 0;
            if ((res & 1) != 0) {
                latIncr = ~(res >> 1);
            } else {
                latIncr = res >> 1;
            }
            lat += latIncr;

            shift = 0;
            res = 0;
            //Longitude decode
            while (true) {
                b = polylineString.charAt(index) - 63;
                index++;
                res |= (b & 0x1f) << shift;
                shift += 5;
                if (b < 0x20) {
                    break;
                }
            }
            int lngIncr = 0;
            if ((res & 1) != 0) {
                lngIncr = ~(res >> 1);
            } else {
                lngIncr = res >> 1;
            }
            lng += lngIncr;

            LatLng point = new LatLng(lat / 100000.0, lng / 100000.0);
            rtn.add(point);
        }
        return rtn;
    }
}
