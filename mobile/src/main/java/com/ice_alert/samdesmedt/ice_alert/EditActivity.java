package com.ice_alert.samdesmedt.ice_alert;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditActivity extends Activity {

    DatabaseHandler dbHandler;
    TextView name, phone;
    Spinner rankSpinEdit;
    Contact tempContact;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        dbHandler = new DatabaseHandler(getApplicationContext());

        tempContact = MainActivity.Contacts.get(MainActivity.longClickedItemIndex);


        name = (TextView) findViewById(R.id.txtEditName);
        name.setText(tempContact.getName());

        phone = (TextView) findViewById(R.id.txtEditPhone);
        phone.setText(tempContact.getPhone());

        rankSpinEdit = (Spinner) findViewById(R.id.spinnerEdit);

        Integer[] items = new Integer[]{1,2,3,4,5};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, items);
        rankSpinEdit.setAdapter(adapter);

        rankSpinEdit.setSelection(tempContact.getRanking()-1);

        Button btnDone = (Button) findViewById(R.id.btnEditDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Contact contactExist = dbHandler.getContact(tempContact.getId());
                if(String.valueOf(phone.getText()).contains("+")){

                    if(name.getText().toString().isEmpty()){
                        Toast.makeText(getApplicationContext(),"Name can not be empty", Toast.LENGTH_SHORT).show();

                    }
                    else if(dbHandler.getContactPhone(phone.getText().toString()) != null && dbHandler.getContactPhone(phone.getText().toString()).getPhone().toString() == phone.getText().toString()){
                        toastNumberAlreadyExists();
                    }

                    else if(dbHandler.getContactRank(Integer.valueOf(rankSpinEdit.getSelectedItem().toString())) == null ){
                        updateContact(view);

                    }
                    else if (dbHandler.getContactRank(tempContact.getRanking()).getRanking() == Integer.valueOf(rankSpinEdit.getSelectedItem().toString())){
                        updateContact(view);
                    }
                    else if(dbHandler.getContactRank(Integer.valueOf(rankSpinEdit.getSelectedItem().toString())) != null){
                        toastRankAlreadyExists();
                    }
                    else{
                        toastRankAlreadyExists();
                    }

                }

                else {

                    Toast.makeText(getApplicationContext(),"Phone number must start with a country code (e.g +32)",Toast.LENGTH_LONG).show();

                }
            }
        });

        Button btnCancel = (Button) findViewById(R.id.btnEditCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent myIntent = new Intent(view.getContext(), MainActivity.class);
                startActivityForResult(myIntent, 0);

            }
        });


    }

    private void updateContact(View view){
        tempContact.setName(String.valueOf(name.getText()));
        tempContact.setPhone(String.valueOf(phone.getText()));
        tempContact.setRanking(Integer.valueOf(rankSpinEdit.getSelectedItem().toString()));

        // Update our database
        dbHandler.updateContact(tempContact);

        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();

        Intent myIntent = new Intent(view.getContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
    }

    private void toastRankAlreadyExists(){
        Toast.makeText(getApplicationContext(),"Another contact with the same ranking already exists.", Toast.LENGTH_SHORT).show();

    }

    private void toastNumberAlreadyExists(){
        Toast.makeText(getApplicationContext(),"Another contact with this phone number already exists.", Toast.LENGTH_SHORT).show();

    }

}