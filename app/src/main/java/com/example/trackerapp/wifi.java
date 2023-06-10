package com.example.trackerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

public class wifi {

    private Context context;
    private WifiManager wifiManager;
    private WifiReceiver wifiReceiver;
    private SignalChangeListener signalChangeListener;
    private LocationManager locationManager;
    private LocationListener locationListener;

    public wifi(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void setSignalChangeListener(SignalChangeListener listener) {
        this.signalChangeListener = listener;
    }

    public void startMonitoring() {
        context.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
        startLocationUpdates();
    }

    public void stopMonitoring() {
        context.unregisterReceiver(wifiReceiver);
        stopLocationUpdates();
    }

    private void startLocationUpdates() {
        if (locationManager != null) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Handle location updates if needed
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            };

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

    private class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int signalStrength = wifiInfo.getRssi();
                if (signalChangeListener != null) {
                    String signalCategory = classifySignal(signalStrength);
                    signalChangeListener.onSignalChanged(signalStrength, signalCategory);
                }
            }
        }
    }

    private String classifySignal(int signalStrength) {
        if (signalStrength >= -50) {
            return "Ottimo";
        } else if (signalStrength >= -60) {
            return "Accettabile";
        } else {
            return "Pessimo";
        }
    }

    public interface SignalChangeListener {
        void onSignalChanged(int signalStrength, String signalCategory);
    }
}
