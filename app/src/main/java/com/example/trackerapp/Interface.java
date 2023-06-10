package com.example.trackerapp;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class Interface {
    private Context context;
    private TelephonyManager telephonyManager;
    private SignalChangeListener signalChangeListener;

    public Interface(Context context) {
        this.context = context;
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public void setSignalChangeListener(SignalChangeListener listener) {
        this.signalChangeListener = listener;
    }

    public void startMonitoring() {
        telephonyManager.listen(signalStrengthListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    public void stopMonitoring() {
        telephonyManager.listen(signalStrengthListener, PhoneStateListener.LISTEN_NONE);
    }

    private PhoneStateListener signalStrengthListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            // Ottieni la potenza del segnale per UMTS (3G)
            int umtsSignalStrength = signalStrength.getGsmSignalStrength();

            if (signalChangeListener != null) {
                String signalCategory = classifySignal(umtsSignalStrength);
                signalChangeListener.onSignalChanged(umtsSignalStrength, signalCategory);
            }
        }
    };

    private String classifySignal(int signalStrength) {
        if (signalStrength > -65) {
            return "Ottimale";
        } else if (signalStrength > -87) {
            return "Non ottimale";
        } else {
            return "Pessimo";
        }
    }

    public interface SignalChangeListener {
        void onSignalChanged(int signalStrength, String signalCategory);
    }
}
