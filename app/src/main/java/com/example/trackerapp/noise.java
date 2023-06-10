package com.example.trackerapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.Manifest;
import androidx.core.content.ContextCompat;


public class noise {
    private Context context;
    private NoiseChangeListener noiseChangeListener;
    private boolean isMonitoring;
    private LocationManager locationManager;
    private LocationListener locationListener;

    public noise(Context context) {
        this.context = context;
        isMonitoring = false;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void setNoiseChangeListener(NoiseChangeListener listener) {
        this.noiseChangeListener = listener;
    }

    public void startMonitoring() {
        isMonitoring = true;
        startLocationUpdates();
    }

    public void stopMonitoring() {
        isMonitoring = false;
        stopLocationUpdates();
    }

    private void startLocationUpdates() {
        if (locationManager != null) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (isMonitoring) {
                        double noiseLevel = getNoiseLevelFromLocation(location);
                        if (noiseChangeListener != null) {
                            String noiseCategory = classifyNoise(noiseLevel);
                            noiseChangeListener.onNoiseChanged(noiseLevel, noiseCategory);
                        }
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            };

            // Check if the ACCESS_FINE_LOCATION permission is granted
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    private void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    private double getNoiseLevelFromLocation(Location location) {
        // Perform noise measurement based on GPS location
        // Return the detected environmental noise value in the specified GPS location area
        // Insert code to obtain environmental noise based on GPS location
        // This may involve using noise sensors or third-party APIs
        return 0.0; // Replace with the actual environmental noise value
    }

    private String classifyNoise(double noiseLevel) {
        if (noiseLevel <= 40) {
            return "Basso";
        } else if (noiseLevel <= 90) {
            return "Medio";
        } else {
            return "Forte";
        }
    }

    public interface NoiseChangeListener {
        void onNoiseChanged(double noiseLevel, String noiseCategory);
    }
}
