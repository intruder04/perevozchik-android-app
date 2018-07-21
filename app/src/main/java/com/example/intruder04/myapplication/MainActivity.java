package com.example.intruder04.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;



public class MainActivity extends AppCompatActivity {

    private Button startButton;
    private Button stopButton;
    private TextView textView;
    private TextView speedView;
    private TextView distanceView;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private Integer updateInterval = 500;
    private Integer minDistance = 0;

    double previousLocationLat = 0;
    double previousLocationLon = 0;

    double meterCounter = 0;

    RequestQueue queue;

    String list = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context mContext = getApplicationContext();
        queue = Volley.newRequestQueue(mContext);


        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        textView = (TextView) findViewById(R.id.textView);
        speedView = (TextView) findViewById(R.id.speedView);
        distanceView = (TextView) findViewById(R.id.distanceView);


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double currentLat = location.getLatitude();
                double currentLon = location.getLongitude();
                double currentSpeed = location.getSpeed();


                textView.setText("lat:" + currentLat + " lon:" + currentLon + " spd:" + currentSpeed);

                if (location==null){
                    // if you can't get speed because reasons :)
                    speedView.setText("00 km/h");
                }
                else{
                    int speed=(int)(currentSpeed);

//                    int speed=(int) ((location.getSpeed()*3600)/1000);
                    Log.d("perev", "Speed - "+ speed);
                    speedView.setText(speed+" km/h");
                }


                if (previousLocationLat == 0) {
                    previousLocationLat = currentLat;
                    previousLocationLon = currentLon;
                } else {
                    if (previousLocationLat != currentLat || previousLocationLon != currentLon) {
                        float distance = getDistanceBetweenTwoPoints(previousLocationLat, previousLocationLon, currentLat, currentLon);
                        Log.d("perev", "Dist - " + distance);
                        meterCounter += distance;
                        distanceView.setText("\n curr dist:" + distance + " total: " + meterCounter);
                        previousLocationLat = currentLat;
                        previousLocationLon = currentLon;
                        list = list + Double.toString(currentLat) + '|' + Double.toString(currentLon) + ";";
                        Log.d("perev", "map - " + list);
                    } else {
                        Log.d("perev", "Location doesn't change! Ignoring dist calculation");
                    }

                }



            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET
            }, 10);
            return;
        } else {
            startButton();

        }

        stopButton();

    }

    protected void sendEmail(double Metercounter) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:may.viktor@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "test results");

        intent.putExtra(Intent.EXTRA_TEXT, list + "DIST:" + Metercounter);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    public float getDistanceBetweenTwoPoints(double lat1,double lon1,double lat2,double lon2) {

        float[] distance = new float[2];

        Location.distanceBetween( lat1, lon1,
                lat2, lon2, distance);

        return distance[0];
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startButton();
                return;
        }
    }


    private void startButton() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    list = "";
                    locationManager.requestLocationUpdates("gps", updateInterval, minDistance, locationListener);
                }
                catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void stopButton() {
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    if (locationManager != null && locationListener != null) {
                        locationManager.removeUpdates(locationListener);
                        distanceView.setText("\n FINAL DISTANCE: " + meterCounter);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        request();
                        sendEmail(meterCounter);
                    }
//                    locationManager = null;
                }
                catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }

        });
    }

    private void request() {
        final String url = "http://httpbin.org/get?param1=hello";

        // prepare the Request
                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response) {
                                // display response
                                Log.d("Response", response.toString());
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Error.Response", error.toString());
                            }
                        }
                );

        // add it to the RequestQueue
                queue.add(getRequest);

    }



}
