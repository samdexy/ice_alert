package com.ice_alert.samdesmedt.ice_alert;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String WEARABLE_MAIN = "WearableMain";

    String[] words = {"help","help me","i need help","help me please"};

    private Node mNode;
    private GoogleApiClient mGoogleApiClient;
    private static final String WEAR_PATH = "/from-wear";

    //speechrecognition
    private static final int SPEECH_REQUEST_CODE = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize mGoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        Button btnAction = (Button) findViewById(R.id.btnAction);
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //displaySpeechRecognizer();
                sendMessage();

            }
        });

    }

    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);



            if (Arrays.asList(words).contains(spokenText)) {

                sendMessage();
            }

            else {

                Toast.makeText(getApplicationContext(),"Please try again",Toast.LENGTH_LONG).show();

            }
        }
    }


    private void sendMessage() {
        if (mNode != null && mGoogleApiClient != null) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient,
                    mNode.getId(), WEAR_PATH, null)
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.d(WEARABLE_MAIN, "Failed message: " +
                                        sendMessageResult.getStatus().getStatusCode());
                            } else {
                                Toast.makeText(getApplicationContext(), "Message Sent",
                                        Toast.LENGTH_LONG).show();

                                Log.d(WEARABLE_MAIN, "Message succeeded");
                            }
                        }
                    });
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            if (node != null && node.isNearby()) {
                                mNode = node;
                                Log.d(WEARABLE_MAIN, "Connected to " + mNode.getDisplayName());
                            }
                        }
                        if (mNode == null) {
                            Log.d(WEARABLE_MAIN, "Not connected!");
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }
}
