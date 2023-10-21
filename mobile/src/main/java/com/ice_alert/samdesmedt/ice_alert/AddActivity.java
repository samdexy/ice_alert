package com.ice_alert.samdesmedt.ice_alert;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Int4;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by samdesmedt on 06/04/2017.
 */

public class AddActivity extends Activity {

    DatabaseHandler dbHandler;
    List<Contact> Contacts = new ArrayList<Contact>();
    ArrayAdapter<Contact> contactAdapter;
    EditText nameTxt, phoneTxt, rankTxt;
    Spinner rankSpin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        dbHandler = new DatabaseHandler(getApplicationContext());

        nameTxt = (EditText) findViewById(R.id.txtName);
        phoneTxt = (EditText) findViewById(R.id.txtPhone);
        rankSpin = (Spinner) findViewById(R.id.spinner1);

        Integer[] items = new Integer[]{1,2,3,4,5};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, items);
        rankSpin.setAdapter(adapter);

        final Button addBtn = (Button) findViewById(R.id.btnAdd);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Contact contact = new Contact(dbHandler.getContactsCount(), String.valueOf(nameTxt.getText()), String.valueOf(phoneTxt.getText()), Integer.valueOf(rankSpin.getSelectedItem().toString()));

                if(nameTxt.getText().toString().isEmpty()){

                    Toast.makeText(getApplicationContext(), String.valueOf("Name can not be empty"), Toast.LENGTH_SHORT).show();

                }

                else {

                    if (dbHandler.getContactRank(contact.getRanking()) == null) {

                        if(String.valueOf(phoneTxt.getText()).contains("+")){

                            if(dbHandler.getContactPhone(contact.getPhone()) == null){

                                dbHandler.createContact(contact);
                                Contacts.add(contact);
                                //contactAdapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), String.valueOf(nameTxt.getText()) + " has been added to your Contacts!", Toast.LENGTH_SHORT).show();

                                Intent myIntent = new Intent(view.getContext(), MainActivity.class);
                                startActivityForResult(myIntent, 0);

                                return;
                            }

                            else {
                                Toast.makeText(getApplicationContext(),"Another contact with this phone number already exists.",Toast.LENGTH_LONG).show();

                            }

                        }

                        else {
                            Toast.makeText(getApplicationContext(),"Phone number must start with a country code (e.g +32)",Toast.LENGTH_LONG).show();
                        }

                    }

                    else {
                        Toast.makeText(getApplicationContext(), String.valueOf("A contact with ranking " + contact.getRanking() + " already exists. Please set a different ranking."), Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });

        nameTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                addBtn.setEnabled(String.valueOf(nameTxt.getText()).trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent myIntent = new Intent(view.getContext(), MainActivity.class);
                startActivityForResult(myIntent, 0);

            }
        });

    }

}
