package com.EliaSulimanov.mousecontrolandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
                            Intent mouseIntent = new Intent(view.getContext(), TouchpadActivity.class);
                            mouseIntent.putExtra("key", keyInputString);
                            view.getContext().startActivity(mouseIntent);
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
        if (numOfDashes != 1)
            return false;
        return true;
    }

}
