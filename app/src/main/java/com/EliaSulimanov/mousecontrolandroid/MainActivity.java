package com.EliaSulimanov.mousecontrolandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View view) {
                        EditText keyInput = (EditText) findViewById(R.id.keyInput);
                        String keyInputString = keyInput.getText().toString();
                        if (!CheckKey(keyInputString)) {
                            Toast.makeText(getApplicationContext(), "Bad key", Toast.LENGTH_SHORT).show();
                        } else {
                            if(!CheckWifi(view.getContext()))
                            {
                                Toast.makeText(getApplicationContext(), "No WIFI", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                try{
                                    new CheckIfServerAlive().execute(keyInputString);
                                    Intent mouseIntent = new Intent(view.getContext(), TouchpadActivity.class);
                                    mouseIntent.putExtra("key", keyInputString);
                                    view.getContext().startActivity(mouseIntent);
                                }
                                catch (Exception e)
                                {
                                    Toast.makeText(getApplicationContext(), "No server found", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    }
                });
    }

    /**
     * Validate key input
     * @param key Key to validate
     * @return false for bad key
     */
    private boolean CheckKey(String key)
    {
        int numOfDashes = 0;
        for (char c : key.toCharArray()) {
            if (numOfDashes > 1)
                return false;
            if (c < '0' || c > '9')
                if (c != '-')
                    return false;
                else
                    numOfDashes++;
        }
        if(key.charAt(0) == '-' || key.charAt(key.length() - 1) == '-')
            return false;
        if (numOfDashes != 1)
            return false;
        return true;
    }

    /**
     * Validate WIFI connected
     * @param context current context
     * @return false if no WIFI connected
     */
    private boolean CheckWifi(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @SuppressLint("StaticFieldLeak")
    static class CheckIfServerAlive extends AsyncTask<String, Void, Boolean>
    {
        protected Boolean doInBackground(String... key)
        {
            Socket socket = null;
            try {
                socket = new Socket("192.168." + key[0].replace('-', '.'), 3333);
                PrintWriter outWriter = new PrintWriter(socket.getOutputStream(), true);
                socket.close();
                return true;
            }
            catch (Exception e)
            {
                Log.i("ES", e.toString());
            }

            return null;
        }
    }
}
