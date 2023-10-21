package com.ice_alert.samdesmedt.ice_alert;

import android.content.Intent;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerServiceFromWear extends WearableListenerService {

    DatabaseHandler dbHandler;
    private static final String WEARPATH = "/from-wear";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        if (messageEvent.getPath().equals(WEARPATH)) {

            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra("isEmergency", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);


        }

    }


}