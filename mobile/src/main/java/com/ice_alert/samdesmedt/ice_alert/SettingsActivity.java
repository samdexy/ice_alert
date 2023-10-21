package com.ice_alert.samdesmedt.ice_alert;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by samdesmedt on 16/04/2017.
 */

public class SettingsActivity extends Activity {

    static public int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        count = 0;

        //go back
        final Button btnBack = (Button) findViewById(R.id.btnBackSettings);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent myIntent = new Intent(view.getContext(), MainActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        //answer for available
        final Button btnAvailable = (Button) findViewById(R.id.btn_available);
        final Button btnUnavailable = (Button) findViewById(R.id.btn_unavailable);
        final Button message = (Button) findViewById(R.id.btn_message);

        btnAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = 1;
                editMode(view);
            }
        });

        btnUnavailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = 2;
                editMode(view);
            }
        });

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = 3;
                editMode(view);
            }
        });

    }

    private void editMode(View v){

        Intent myIntent = new Intent(v.getContext(), EditSettingsActivity.class);
        startActivityForResult(myIntent, 0);
    };
}
