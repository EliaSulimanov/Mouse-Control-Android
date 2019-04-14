package com.EliaSulimanov.mousecontrolandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import java.io.PrintWriter;
import java.net.Socket;

public class TouchpadActivity extends AppCompatActivity {

    //region HIDE UI ATTRIBUTES
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    //endregion

    //region HIDE UI METHODS
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    //endregion

    String connectionKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_touchpad);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        connectionKey = intent.getExtras().getString("key");

        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        //region DUMMY LISTENERS
        /*
         * TextView click listener
         */
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //activity keeps closing if I remover this listener
            }
        });

        /*
         * LinearLayout touch listener
         */
        mControlsView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        //endregion

        //Listen to touch on the view and send data to the server
        final View touchView = findViewById(R.id.touchView);
        touchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    new SendMessageTask().execute("m," + event.getRawX() + "," + event.getRawY()); //TODO check orientation
                }
                 return true;
            }
        });

        final View leftButtonView = findViewById(R.id.leftButtonView);
        leftButtonView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        new SendMessageTask().execute("m," + event.getRawX() + "," + event.getRawY()); //TODO check orientation
                        break;
                    case MotionEvent.ACTION_DOWN:
                        new SendMessageTask().execute("l"); //TODO test double click
                        break;
                }
                return true;
            }
        });

        final View rightButtonView = findViewById(R.id.rightButtonView);
        rightButtonView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        new SendMessageTask().execute("m," + event.getRawX() + "," + event.getRawY()); //TODO check orientation
                        break;
                    case MotionEvent.ACTION_DOWN:
                        new SendMessageTask().execute("r");
                        break;
                }
                return true;
            }
        });
    }
    @SuppressLint("StaticFieldLeak")
    class SendMessageTask extends AsyncTask<String, Void, Boolean>
    {
        protected Boolean doInBackground(String... message)
        {
            Socket socket = null;
            try {
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;

                socket = new Socket("192.168." + connectionKey.replace('-', '.'), 3333);
                PrintWriter outWriter = new PrintWriter(socket.getOutputStream(), true);

                switch (message[0].charAt(0)) {
                    case 'r':
                        outWriter.println("r");
                        break;
                    case 'l':
                        outWriter.println("l");
                        break;
                    case 'm':
                        String[] coordinates = message[0].split(",");
                        /*
                         * I'm sending integers as awt robot can only handle ints
                         * TODO check if all the screen is reachable
                         */
                        String x = String.valueOf((Integer.parseInt(coordinates[1].substring(0, coordinates[1].indexOf("."))) * 100) / width);
                        String y = String.valueOf((Integer.parseInt(coordinates[2].substring(0, coordinates[2].indexOf("."))) * 100) / height);
                        outWriter.println("m," + x + "," + y);
                        break;
                    default:
                        outWriter.println("i");
                        break;
                }
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
