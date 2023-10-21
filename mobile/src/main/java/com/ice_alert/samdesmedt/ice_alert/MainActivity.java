package com.ice_alert.samdesmedt.ice_alert;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity {

    private static final int EDIT = 0, DELETE = 1;
    String mobile;
    String message;

    static List<Contact> Contacts = new ArrayList<Contact>();
    ListView contactListView;
    Uri imageUri = Uri.parse("android.resource://org.intracode.contactmanager/drawable/no_user_logo.png");
    DatabaseHandler dbHandler;
    static int longClickedItemIndex;
    ArrayAdapter<Contact> contactAdapter;
    final public static int SEND_SMS = 101;
    private Speaker speaker;
    private final int CHECK_CODE = 0x1;
    private BroadcastReceiver smsReceiver;
    TextToSpeech tts;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    private static final int SPEECH_REQUEST_CODE = 1001;

    //counts how many times user has called for help
    int counter;

    String[] words = {"help","help me","i need help","help me please"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        counter = 1;

        ImageView activatedIcon = (ImageView) findViewById(R.id.activatedIcon);
        TextView activatedText = (TextView) findViewById(R.id.activatedText);

        activatedIcon.setVisibility(View.GONE);
        activatedText.setVisibility(View.GONE);

        contactListView = (ListView) findViewById(R.id.listView);

        dbHandler = new DatabaseHandler(getApplicationContext());
        final String message = dbHandler.getSettings(1).getMessage();

        Intent intent = getIntent();
        boolean isEmergency = intent.getBooleanExtra("isEmergency", false);

        if(isEmergency){
            startEmergencyMode(message);
        }


        if (dbHandler.getContactsCount() == 0 ){
            activatedText.setVisibility(View.VISIBLE);
            activatedText.setText("Please add contacts to your contact list...");
            activatedText.setTextColor(Color.WHITE);
        }



        registerForContextMenu(contactListView);

        contactListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                longClickedItemIndex = position;
                return false;
            }
        });


        final Button btnSend = (Button) findViewById(R.id.btnSendMessage);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                displaySpeechRecognizer();

            }
        });

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("emergency");
        tabSpec.setContent(R.id.TabEmergency);
        tabSpec.setIndicator("Emergency");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("mycontacts");
        tabSpec.setContent(R.id.tabContactList);
        tabSpec.setIndicator("My Contacts");
        tabHost.addTab(tabSpec);


        TextView name1 = (TextView) findViewById(R.id.textViewName1);
        TextView name2 = (TextView) findViewById(R.id.textViewName2);
        TextView name3 = (TextView) findViewById(R.id.textViewName3);
        TextView name4 = (TextView) findViewById(R.id.textViewName4);
        TextView name5 = (TextView) findViewById(R.id.textViewName5);


        if (dbHandler.getContactRank(1) != null) {
            name1.setText(String.valueOf(dbHandler.getContactRank(1).getName().toString()));
        }

        if (dbHandler.getContactRank(2) != null) {
            name2.setText(String.valueOf(dbHandler.getContactRank(2).getName().toString()));
        }

        if (dbHandler.getContactRank(3) != null) {
            name3.setText(String.valueOf(dbHandler.getContactRank(3).getName().toString()));
        }

        if (dbHandler.getContactRank(4) != null) {
            name4.setText(String.valueOf(dbHandler.getContactRank(4).getName().toString()));
        }

        if (dbHandler.getContactRank(5) != null) {
            name5.setText(String.valueOf(dbHandler.getContactRank(5).getName().toString()));
        }



        final FloatingActionButton actionBtn = (FloatingActionButton) findViewById(R.id.myFAB);
        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {


                Intent myIntent = new Intent(view.getContext(), AddActivity.class);
                startActivityForResult(myIntent, 0);

            }


        });


        //start settings activity
        final Button btnSettings = (Button) findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent myIntent = new Intent(view.getContext(), SettingsActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        if (dbHandler.getContactsCount() != 0)
            Contacts.clear();
            Contacts.addAll(dbHandler.getAllContacts());


        populateList();
        checkTTS();
        initializeSMSReceiver();
        registerSMSReceiver();
    }

    public void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }


    private void registerSMSReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);
    }

    private void initializeSMSReceiver(){
        smsReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {



                dbHandler = new DatabaseHandler(getApplicationContext());

                final String answerAvailable = dbHandler.getSettings(1).getAnswerAvailable().toUpperCase();
                final String answerUnvailable = dbHandler.getSettings(1).getAnswerUnavailable().toUpperCase();
                final String messageText = dbHandler.getSettings(1).getMessage();
                Bundle data  = intent.getExtras();

                Object[] pdus = (Object[]) data.get("pdus");

                for(int i=0;i<pdus.length;i++){

                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    String address = smsMessage.getOriginatingAddress(); //+324



                    if(address.equals(dbHandler.getContactRank(1).getPhone())){

                        if(smsMessage.getDisplayMessageBody().toString().toUpperCase().contains(answerAvailable)){

                            Toast.makeText(context,"Help is on its way",Toast.LENGTH_LONG).show();
                            speak("help is on its way");
                            cancelEmergency();
                        }

                        else if(smsMessage.getDisplayMessageBody().toString().toUpperCase().contains(answerUnvailable) && dbHandler.getContactRank(2) != null){
                            Toast.makeText(context,"your first contact isn't available. Sending message to next contact.",Toast.LENGTH_LONG).show();
                            speak("your first contact isn't available. Sending message to next contact.");
                            checkAndroidVersion(dbHandler.getContactRank(2).getPhone(),messageText);

                        }

                        else if(dbHandler.getContactRank(2) != null){
                            Toast.makeText(context,"message not clear. Sending message to next contact just in case",Toast.LENGTH_LONG).show();
                            speak("message not clear. Sending message to next contact just in case");

                            checkAndroidVersion(dbHandler.getContactRank(2).getPhone(),messageText);
                        }

                        else if(smsMessage.getDisplayMessageBody().toString().toUpperCase().contains(answerUnvailable)) {
                            Toast.makeText(context,"your first contact isn't available. Please add more contacts to your contact list.",Toast.LENGTH_LONG).show();
                            speak("your first contact isn't available. Please add more contacts to your contact list.");
                            cancelEmergency();
                        }
                        else {

                            Toast.makeText(context,"The message was unclear. Please add more contacts to your contact list.",Toast.LENGTH_LONG).show();
                            speak("The message was unclear. Please add more contacts to your contact list.");
                            cancelEmergency();
                        }

                    }



                    else if (address.equals(dbHandler.getContactRank(2).getPhone())){

                        if(smsMessage.getDisplayMessageBody().toString().toUpperCase().contains(answerAvailable)){

                            Toast.makeText(context,"Help is on its way",Toast.LENGTH_LONG).show();
                            speak("help is on its way");
                            cancelEmergency();
                        }

                        else if(smsMessage.getDisplayMessageBody().toString().toUpperCase().contains(answerUnvailable) && dbHandler.getContactRank(3) != null){
                            Toast.makeText(context,"Your second contact isn't available. Sending message to next contact.",Toast.LENGTH_LONG).show();
                            speak("your second contact isn't available. Sending message to next contact.");
                            checkAndroidVersion(dbHandler.getContactRank(3).getPhone(),messageText);

                        }

                        else if(dbHandler.getContactRank(3) != null){
                            Toast.makeText(context,"Message not clear. Sending message to next contact just in case",Toast.LENGTH_LONG).show();
                            speak("message not clear. Sending message to next contact just in case");

                            checkAndroidVersion(dbHandler.getContactRank(3).getPhone(),messageText);
                        }

                        else if(smsMessage.getDisplayMessageBody().toString().toUpperCase().contains(answerUnvailable)) {
                            Toast.makeText(context,"your second contact isn't available. Please add more contacts to your contact list.",Toast.LENGTH_LONG).show();
                            speak("your second contact isn't available. Please add more contacts to your contact list.");
                            cancelEmergency();

                        }
                        else {

                            Toast.makeText(context,"The message was unclear. Please add more contacts to your contact list.",Toast.LENGTH_LONG).show();
                            speak("The message was unclear. Please add more contacts to your contact list.");
                            cancelEmergency();
                        }

                    }




                    else if (address.equals(dbHandler.getContactRank(3).getPhone())){

                        if(smsMessage.getDisplayMessageBody().toString().toUpperCase().contains(answerAvailable)){

                            Toast.makeText(context,"Help is on its way",Toast.LENGTH_LONG).show();
                            speak("help is on its way");
                            cancelEmergency();
                        }

                        else if(smsMessage.getDisplayMessageBody().toString().toUpperCase().contains(answerUnvailable) && dbHandler.getContactRank(4) != null){
                            Toast.makeText(context,"Your third isn't available. Sending message to next contact.",Toast.LENGTH_LONG).show();
                            speak("Your third contact isn't available. Sending message to next contact.");
                            checkAndroidVersion(dbHandler.getContactRank(4).getPhone(),messageText);
                        }

                        else if(dbHandler.getContactRank(4) != null){
                            Toast.makeText(context,"Message not clear. Sending message to next contact just in case",Toast.LENGTH_LONG).show();
                            speak("message not clear. Sending message to next contact just in case");
                            checkAndroidVersion(dbHandler.getContactRank(4).getPhone(),messageText);
                        }

                        else if(smsMessage.getDisplayMessageBody().toString().toUpperCase().contains(answerUnvailable)) {
                            Toast.makeText(context,"your third contact isn't available. Please add more contacts to your contact list.",Toast.LENGTH_LONG).show();
                            speak("your third contact isn't available. Please add more contacts to your contact list.");
                            cancelEmergency();

                        }
                        else {

                            Toast.makeText(context,"The message was unclear. Please add more contacts to your contact list.",Toast.LENGTH_LONG).show();
                            speak("The message was unclear. Please add more contacts to your contact list.");
                            cancelEmergency();
                        }

                    }




                    else if (address.equals(dbHandler.getContactRank(4).getPhone())){

                        if(smsMessage.getDisplayMessageBody().toString().toUpperCase().contains(answerAvailable)){
                            Toast.makeText(context,"Help is on its way",Toast.LENGTH_LONG).show();
                            speak("help is on its way");
                            cancelEmergency();
                        }

                        else if(smsMessage.getDisplayMessageBody().toString().toUpperCase().contains(answerUnvailable) && dbHandler.getContactRank(5) != null){
                            Toast.makeText(context,"Your fourth contact isn't available. Sending message to next contact.",Toast.LENGTH_LONG).show();
                            speak("Your fourth contact isn't available. Sending message to next contact.");
                            checkAndroidVersion(dbHandler.getContactRank(5).getPhone(),messageText);
                        }

                        else if(dbHandler.getContactRank(5) != null){
                            Toast.makeText(context,"Message not clear. Sending message to next contact just in case",Toast.LENGTH_LONG).show();
                            speak("message not clear. Sending message to next contact just in case");
                            checkAndroidVersion(dbHandler.getContactRank(5).getPhone(),messageText);
                        }

                        else if(smsMessage.getDisplayMessageBody().toString().toUpperCase().contains(answerUnvailable)) {
                            Toast.makeText(context,"your fourth contact isn't available. Please add more contacts to your contact list.",Toast.LENGTH_LONG).show();
                            speak("your fourth contact isn't available. Please add more contacts to your contact list.");
                            cancelEmergency();

                        }
                        else if(smsMessage.getDisplayMessageBody().toString().toUpperCase().contains(answerUnvailable)) {
                            Toast.makeText(context,"your fourth contact isn't available. Please add more contacts to your contact list.",Toast.LENGTH_LONG).show();
                            speak("your fourth contact isn't available. Please add more contacts to your contact list.");
                            cancelEmergency();

                        }
                        else {

                            Toast.makeText(context,"The message was unclear. Please add more contacts to your contact list.",Toast.LENGTH_LONG).show();
                            speak("The message was unclear. Please add more contacts to your contact list.");
                            cancelEmergency();
                        }

                    }

                    else if (address.equals(dbHandler.getContactRank(5).getPhone())){

                        if(smsMessage.getDisplayMessageBody().toString().toUpperCase().contains(answerAvailable)){

                            Toast.makeText(context,"Help is on its way",Toast.LENGTH_LONG).show();
                            speak("help is on its way");
                            cancelEmergency();
                        }

                        else if(smsMessage.getDisplayMessageBody().toString().toUpperCase().contains(answerUnvailable)){
                            Toast.makeText(context,"Your fifth contact isn't available. Starting over.",Toast.LENGTH_LONG).show();
                            speak("Your fifth contact isn't available. Starting over.");
                            cancelEmergency();
                            startEmergencyMode(messageText);

                        }

                        else{
                            Toast.makeText(context,"The message was unclear. Starting over",Toast.LENGTH_LONG).show();
                            speak("The message was unclear. Starting over");
                            cancelEmergency();
                            startEmergencyMode(messageText);

                        }


                    }

                }


            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case SEND_SMS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    counter = 1;
                    dbHandler = new DatabaseHandler(getApplicationContext());
                    final String message = dbHandler.getSettings(1).getMessage();
                    startEmergencyMode(message);

                } else {
                    cancelEmergency();
                    Toast.makeText(MainActivity.this, "SEND_SMS Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        menu.setHeaderIcon(R.drawable.pencil_icon);
        menu.setHeaderTitle("Contact Options");
        menu.add(Menu.NONE, EDIT, menu.NONE, "Edit Contact");
        menu.add(Menu.NONE, DELETE, menu.NONE, "Delete Contact");
    }


    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case EDIT:
                Intent editContactIntent = new Intent(getApplicationContext(), EditActivity.class);
                startActivityForResult(editContactIntent, 2);
                break;
            case DELETE:
                dbHandler.deleteContact(Contacts.get(longClickedItemIndex));
                Contacts.remove(longClickedItemIndex);
                contactAdapter.notifyDataSetChanged();
                finish();
                startActivity(getIntent());
                break;
        }

        return super.onContextItemSelected(item);
    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
            cancelEmergency();
        }
    }

    public void checkAndroidVersion(String mobile, String message){


        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS);
            if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS}, SEND_SMS);
                return;
            }else{
                sendSMS(mobile, message);
            }
        } else {
            sendSMS(mobile, message);
        }
    }

    public void broadcastIntent(View view){
        Intent intent = new Intent();
        intent.setAction("com.ice_alert.samdesmedt.ice_alert.SMS_RECEIVED");
        sendBroadcast(intent);
    }

    private void checkTTS(){
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);

            final String message = dbHandler.getSettings(1).getMessage();



            if (Arrays.asList(words).contains(spokenText)) {

                if( dbHandler.getContactsCount() != 0){
                    startEmergencyMode(message);

                }
                else{
                    Toast.makeText(getApplicationContext(),"Please add contacts!",Toast.LENGTH_LONG).show();
                }

            }

            else {
                Toast.makeText(getApplicationContext(),"Please try that again",Toast.LENGTH_LONG).show();
                cancelEmergency();

            }
        }

    }

    private void populateList() {
        contactAdapter = new ContactListAdapter();
        contactListView.setAdapter(contactAdapter);
    }

    private void cancelEmergency(){
        dbHandler = new DatabaseHandler(getApplicationContext());
        if(dbHandler.getContactsCount() != 0){
            ImageView activatedIcon = (ImageView) findViewById(R.id.activatedIcon);
            TextView activatedText = (TextView) findViewById(R.id.activatedText);

            activatedIcon.setVisibility(View.GONE);
            activatedText.setVisibility(View.GONE);
            counter = 1;
        }

    }

    private void speak(final String text){

        tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {

                if(status == TextToSpeech.SUCCESS){
                    int result=tts.setLanguage(Locale.US);
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Toast.makeText(getApplicationContext(),"Please download english voice data",Toast.LENGTH_LONG).show();
                        Intent install = new Intent();
                        install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        startActivity(install);
                    }
                    else{
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);

                    }

                }
                else
                    Log.e("error", "Initilization Failed!");
            }

        });
    }

    public void startEmergencyMode(String messageText){

        ImageView activatedIcon = (ImageView) findViewById(R.id.activatedIcon);
        TextView activatedText = (TextView) findViewById(R.id.activatedText);

        activatedIcon.setVisibility(View.VISIBLE);
        activatedText.setVisibility(View.VISIBLE);


        if(counter == 1){
            if(dbHandler.getContactRank(1) != null) {
                counter++;
                checkAndroidVersion(dbHandler.getContactRank(1).getPhone(), messageText);
            }
            else {
                Toast.makeText(getApplicationContext(),"please add a contact",Toast.LENGTH_LONG).show();
                cancelEmergency();
            }
        }
        else if(counter == 2){
            if(dbHandler.getContactRank(2) != null) {
                activatedMessage();
                counter++;
                checkAndroidVersion(dbHandler.getContactRank(2).getPhone(),messageText);
            }
            else {
                Toast.makeText(getApplicationContext(),"please add a second contact",Toast.LENGTH_LONG).show();
                cancelEmergency();
                counter = 2;
            }
        }
        else if(counter == 3){
            if(dbHandler.getContactRank(3) != null) {
                activatedMessage();
                counter++;
                checkAndroidVersion(dbHandler.getContactRank(3).getPhone(),messageText);
            }
            else {
                Toast.makeText(getApplicationContext(),"please add a third contact",Toast.LENGTH_LONG).show();
                cancelEmergency();
                counter = 3;
            }
        }
        else if(counter == 4){
            if(dbHandler.getContactRank(4) != null) {
                activatedMessage();
                counter++;
                checkAndroidVersion(dbHandler.getContactRank(4).getPhone(),messageText);
            }
            else {
                Toast.makeText(getApplicationContext(),"please add a fourth contact",Toast.LENGTH_LONG).show();
                cancelEmergency();
                counter = 4;
            }
        }
        else if(counter == 5){
            if(dbHandler.getContactRank(5) != null) {
                activatedMessage();
                counter++;
                checkAndroidVersion(dbHandler.getContactRank(5).getPhone(),messageText);
            }
            else {
                Toast.makeText(getApplicationContext(),"please add a fifth contact",Toast.LENGTH_LONG).show();
                cancelEmergency();
                counter = 5;
            }
        }

        else {
            counter = 1;
        }





    }

private void activatedMessage(){
    Toast.makeText(getApplicationContext(),"Emergency mode activated",Toast.LENGTH_LONG).show();
}
    private class ContactListAdapter extends ArrayAdapter<Contact> {
        public ContactListAdapter() {
            super (MainActivity.this, R.layout.listview_item, Contacts);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.listview_item, parent, false);



            Contact currentContact = Contacts.get(position);

            //contacts page
            TextView name = (TextView) view.findViewById(R.id.contactName);
            name.setText(currentContact.getName());
            TextView phone = (TextView) view.findViewById(R.id.phoneNumber);
            phone.setText(currentContact.getPhone());
            TextView ranking = (TextView) view.findViewById(R.id.contactRanking);
            ranking.setText(String.valueOf(currentContact.getRanking()));
            return view;
        }
    }


    @Override
    protected void onStop()
    {
        unregisterReceiver(smsReceiver);
        super.onStop();
    }


}
