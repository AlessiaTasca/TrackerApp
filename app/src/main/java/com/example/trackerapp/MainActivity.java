package com.example.trackerapp;

import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Interface signalMonitor;
    private wifi wifiMonitor;
    private noise noiseMonitor;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private List<MonitoringData> monitoringDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.MainActivity);

        // Initialize the support for the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize the monitors
        signalMonitor = new Interface(this);
        wifiMonitor = new wifi(this);
        noiseMonitor = new noise(this);

        // Initialize the monitoring data list
        monitoringDataList = new ArrayList<>();

        // Start the monitors
        signalMonitor.startMonitoring();
        wifiMonitor.startMonitoring();
        noiseMonitor.startMonitoring();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop the monitors
        signalMonitor.stopMonitoring();
        wifiMonitor.stopMonitoring();
        noiseMonitor.stopMonitoring();
    }

    private void saveMonitoringDataToJson() {
        JSONArray jsonArray = new JSONArray();
        for (MonitoringData monitoringData : monitoringDataList) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("wifi_signal", monitoringData.getWifiSignal());
                jsonObject.put("lte_signal", monitoringData.getLteSignal());
                jsonObject.put("white_noise", monitoringData.getWhiteNoise());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Get the external storage directory
        File dir = Environment.getExternalStorageDirectory();

        // Create the file
        File file = new File(dir, "monitoraggi.json");

        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(jsonArray.toString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateMonitoringData(double wifiSignal, double lteSignal, double whiteNoise) {
        // Update the monitoring data
        MonitoringData monitoringData = new MonitoringData(wifiSignal, lteSignal, whiteNoise);
        monitoringDataList.add(monitoringData);

        // Save the monitoring data to JSON
        saveMonitoringDataToJson();

        // Update the map with the new data
        // ...
    }

    private static class MonitoringData {
        private double wifiSignal;
        private double lteSignal;
        private double whiteNoise;

        public MonitoringData(double wifiSignal, double lteSignal, double whiteNoise) {
            this.wifiSignal = wifiSignal;
            this.lteSignal = lteSignal;
            this.whiteNoise = whiteNoise;
        }

        public double getWifiSignal() {
            return wifiSignal;
        }

        public double getLteSignal() {
            return lteSignal;
        }

        public double getWhiteNoise() {
            return whiteNoise;
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Check if the ACCESS_FINE_LOCATION permission has been granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Enable the "My Location" button on the map
            googleMap.setMyLocationEnabled(true);

            // Get the last known location
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    // Location found, update the map
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                }
            });
        } else {
            // Request the ACCESS_FINE_LOCATION permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if the permission request is granted
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, reload the map
            onMapReady(googleMap);
        }
    }
}
