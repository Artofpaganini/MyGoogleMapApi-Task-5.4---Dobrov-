package by.andresen.intern.dobrov.mygooglemaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private boolean locationPermissionGranted = false;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        getLocationPermission();

    }

    private LatLng getRandomLocation(LatLng point, int radius) {

        List<LatLng> randomPoints = new ArrayList<>();
        List<Float> randomDistances = new ArrayList<>();
        Location myLocation = new Location("");
        myLocation.setLatitude(point.latitude);
        myLocation.setLongitude(point.longitude);

        for (int i = 0; i < 5; i++) {
            double myCurrentLatitudeX = point.latitude;
            double myCurrentLongitudeY = point.longitude;

            Random random = new Random();

            // конвертируем радиус из  метров в градусы
            double radiusInDegrees = radius / 111000f;

            double u = random.nextDouble();
            double v = random.nextDouble();
            double w = radiusInDegrees * Math.sqrt(u);
            double t = 2 * Math.PI * v;
            double x = w * Math.cos(t);
            double y = w * Math.sin(t);

            double new_x = x / Math.cos(myCurrentLongitudeY);

            double randomLatitudeX = new_x + myCurrentLatitudeX;
            double foundLongitude = y + myCurrentLongitudeY;
            LatLng randomLatLng = new LatLng(randomLatitudeX, foundLongitude);
            randomPoints.add(randomLatLng);
            Location randomLocation = new Location("");
            randomLocation.setLatitude(randomLatLng.latitude);
            randomLocation.setLongitude(randomLatLng.longitude);
            randomDistances.add(randomLocation.distanceTo(myLocation));
        }
        int indexOfNearestPointToCentre = randomDistances.indexOf(Collections.min(randomDistances));
        return randomPoints.get(indexOfNearestPointToCentre);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: MAP IS READY");
        map = googleMap;
        if (locationPermissionGranted) {
            getMyCurrentLocation();

            if (ActivityCompat.checkSelfPermission(this, FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, COURSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            map.setMyLocationEnabled(true);
        }
    }


    //добавление радиуса
    private void drawCircle(LatLng location) {
        CircleOptions options = new CircleOptions();
        options.center(location);
        options.radius(5000);
        options.strokeWidth(10);
        options.strokeColor(Color.RED);

        Log.d(TAG, "drawCircle: DRAW THE CIRCLE");
        map.addCircle(options);

        for (int i = 0; i < 5; i++) {
            Log.d(TAG, "drawCircle: ADDED MARKER #" + i);
            map.addMarker(new MarkerOptions().position(getRandomLocation(location, (int) options.getRadius())));
        }


    }


    private void getMyCurrentLocation() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (locationPermissionGranted) {
                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                            drawCircle(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));

                            Log.d(TAG, "onComplete: WE FOUND A LOCATION");
                        } else {
                            Log.d(TAG, "onComplete: LOCATION IS NULL, can  make a checking  location  again , by  gps and  wifi");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getMyCurrentLocation: SECURITY EXCEPTION!!!");
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }


    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Log.d(TAG, "initMap: PREPARE ON MAP");
        mapFragment.getMapAsync(this);
    }

    //проверка  на  то что бы порлучить местоположение, приблизительно и точно
    private void getLocationPermission() {

        String[] permissions = {FINE_LOCATION, COURSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationPermissionGranted = true;
                initMap();
            }

        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);

        }
        Log.d(TAG, "getLocationPermission: GET LOCATION PERMISSION ");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        Log.d(TAG, "onRequestPermissionsResult: CALLED ");
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {

                if (grantResults.length > 0) {

                    for (int i = 0; i < grantResults.length; i++) {

                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            locationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: PERMISSION FAILED");
                            return;
                        }
                    }

                    Log.d(TAG, "onRequestPermissionsResult: PERMISSION GRANTED");
                    locationPermissionGranted = true;
                    initMap();
                }
            }
        }
    }

}