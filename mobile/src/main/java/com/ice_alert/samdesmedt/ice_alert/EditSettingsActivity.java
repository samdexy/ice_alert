package com.ice_alert.samdesmedt.ice_alert;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
/**
 * Created by samdesmedt on 16/04/2017.
 */

public class EditSettingsActivity extends Activity {

    DatabaseHandler dbHandler;
    TextView available, unavailable, message;
    TextView availableInstruction, unavailableInstruction, messageInstruction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_setting);

        dbHandler = new DatabaseHandler(getApplicationContext());

        final String availableText = dbHandler.getSettings(1).getAnswerAvailable().toString();
        final String unavailableText = dbHandler.getSettings(1).getAnswerUnavailable().toString();
        final String messageText = dbHandler.getSettings(1).getMessage().toString();

        final String instructionAvailableText = "This is the message you want to receive when the person is available. Emergency mode will stop if this answer is received.";
        final String instructionUnavailableText = "This is the message you want to receive when the person is unavailable. Emergency mode will continue to send a message to the next person in your contact list.";
        final String instructionMessageText = "This is the message you send to your contacts. Make sure you include both messages for available and unavailable so your contacts know how to respond properly.";

        availableInstruction = (TextView) findViewById(R.id.instructionSettings);
        unavailableInstruction = (TextView) findViewById(R.id.instructionSettings);
        messageInstruction = (TextView) findViewById(R.id.instructionSettings);


        if(SettingsActivity.count == 1){
            available = (TextView) findViewById(R.id.txtEditSetting);
            available.setText(availableText);
            availableInstruction.setText(instructionAvailableText);

        }

        if(SettingsActivity.count == 2){
            unavailable = (TextView) findViewById(R.id.txtEditSetting);
            unavailable.setText(unavailableText);
            unavailableInstruction.setText(instructionUnavailableText);
        }

        if(SettingsActivity.count == 3){
            message = (TextView) findViewById(R.id.txtEditSetting);
            message.setText(messageText);
            messageInstruction.setText(instructionMessageText);
        }




        Button btnSave = (Button) findViewById(R.id.btnEditSettingSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(SettingsActivity.count == 1){
                    final Settings tempAvailable = new Settings(1, String.valueOf(available.getText()), unavailableText,messageText);
                    tempAvailable.setAnswerAvailable(String.valueOf(available.getText()));
                    dbHandler.updateSetting(tempAvailable);
                }

                if(SettingsActivity.count == 2){
                    final Settings tempUnavailable = new Settings(1, availableText, String.valueOf(unavailable.getText()), messageText);
                    tempUnavailable.setAnswerUnavailable(String.valueOf(unavailable.getText()));
                    dbHandler.updateSetting(tempUnavailable);
                }

                if(SettingsActivity.count == 3){
                    final Settings tempMessage = new Settings(1, availableText, unavailableText, String.valueOf(message.getText()));
                    tempMessage.setMessage(String.valueOf(message.getText()));
                    dbHandler.updateSetting(tempMessage);
                }


                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();

                Intent myIntent = new Intent(view.getContext(), SettingsActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        final Button btnCancel = (Button) findViewById(R.id.btnEditSettingCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent myIntent = new Intent(view.getContext(), SettingsActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });


    }


}
