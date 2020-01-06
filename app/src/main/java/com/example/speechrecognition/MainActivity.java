package com.example.speechrecognition;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.ArrayList;
import java.util.Locale;

import httprequest.APICall;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button connectSpotify, disconnectSpotify;
    private TextView displayRecognizedText;
    private SpotifyAppRemote mSpotifyAppRemote;
    private static final String CLIENT_ID = "9b5abf82ca044bcab3e02d8b37c89486";
    private static final String REDIRECT_URI = "https://com.example.speechrecognition/callback/";
    private String mScopes="user-read-recently-played,user-library-modify,user-read-email,user-read-private";
    private int REQUEST_CODE_FOR_AUTHENTICATION=1002;
    int SPOTIFY = 1001;
    String mRecognizedText;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectSpotify = findViewById(R.id.connect);
        disconnectSpotify = findViewById(R.id.disconnect);
        displayRecognizedText = findViewById(R.id.recognizedtext);
        mPreferences=getSharedPreferences("PREFERENCE",0);
        mEditor=mPreferences.edit();
        authenticateSpotify();
        connectSpotify.setOnClickListener(v -> connectToSpotify());
        disconnectSpotify.setOnClickListener(v -> {
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
            Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
        });
    }

    private void authenticateSpotify() {
        AuthenticationRequest.Builder builder=new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN,REDIRECT_URI);
        builder.setScopes(new String[]{mScopes});
        AuthenticationRequest authenticationRequest=builder.build();
        AuthenticationClient.openLoginActivity(this,REQUEST_CODE_FOR_AUTHENTICATION,authenticationRequest);
    }

    private void connectToSpotify() {

        ConnectionParams lConnectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build();
        SpotifyAppRemote.connect(this, lConnectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                Log.d(TAG, "onConnected: ");
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
/*
            convert to switch
*/
            if (requestCode == SPOTIFY) {
                displayRecognizedText.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
                mRecognizedText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
                switch (mRecognizedText) {
                    case "play next":
                        playNext();
                        break;
                    case "play previous":
                        playPrevious();
                        break;
                }
            }
            else if(requestCode==REQUEST_CODE_FOR_AUTHENTICATION)
            {
                AuthenticationResponse authenticationResponse=AuthenticationClient.getResponse(resultCode,data);
                mEditor.putString("ACCESS_TOKEN",authenticationResponse.getAccessToken()).commit();
                Log.d(TAG, "onActivityResult: "+authenticationResponse.getAccessToken());
            }
            else {
                Toast.makeText(this, "Failed to recognize text", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void playPrevious() {

    }

    private void playNext() {
        String lAccessToken=mPreferences.getString("ACCESS_TOKEN","");
        new NetworkCall(lAccessToken).execute();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
            startActivityForResult(intent, SPOTIFY);
        }
        return super.onKeyDown(keyCode, event);
    }


    class NetworkCall extends AsyncTask<String,String,String>{

        String mAccessToken;

        public NetworkCall(String mAccessToken) {
            this.mAccessToken = mAccessToken;
        }

        @Override
        protected String doInBackground(String... strings) {
            String lResponse=APICall.getInstance().playNextAudio(mAccessToken);
            Log.d(TAG, "doInBackground: "+lResponse);
            return null;
        }
    }
}
