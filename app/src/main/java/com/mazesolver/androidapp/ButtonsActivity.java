package com.mazesolver.androidapp;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ButtonsActivity extends AppCompatActivity {

    Button servo1Clock, servo1AntiClock, servo2Clock, servo2AntiClock;
    private static final String TAG = "ButtonsActivity";
    private int mMaxChars = 50000; // Default change this to string
    private UUID DeviceUUID; // Universally Unique Identifier
    private BluetoothSocket BTsocket; // Virtual socket of AS
    private BluetoothDevice Device;


    // Making variable of a class down there and we are using it in various ways to Read input //
    private ReadInput ReadThread = null;
    private boolean IsUserInitiatedDisconnect = false;
    private boolean IsBluetoothConnected = false;
    private int currentApiVersion;


    private ProgressDialog progressDialog;

    String servo1C = "1", servo1A = "2", servo2C = "3", servo2A = "4";

    // Change buttons colors to error message color //
    static final int Button1 = 0;
    static final int Button2 = 2;
    static final int Button3 = 4;
    static final int Button4 = 6;

    // Restore Buttons Colors to Original Color //
    static final int restoreBtn1Clr = 1;
    static final int restoreBtn2Clr = 3;
    static final int restoreBtn3Clr = 5;
    static final int restoreBtn4Clr = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Three things we send from Main-activity when we started new activity  //
        // Those three things were : The available device we want to connect with, which was in list of bluetooth devices...
        // ...AND UUID of device AND buffer size  //

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        // Taking address of the bluetooth which we selected in Arraylist in MainActivity //


        Device = b.getParcelable(MainActivity.DEVICE_EXTRA);
        DeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
        mMaxChars = b.getInt(MainActivity.BUFFER_SIZE);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servo_controlling);

        // To get full screen View
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        currentApiVersion = android.os.Build.VERSION.SDK_INT;

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {

            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }

        // Casting Activity buttons here //
        servo1Clock = findViewById(R.id.servo1Clock);
        servo1AntiClock = findViewById(R.id.servo1AntiClock);
        servo2Clock = findViewById(R.id.servo2Clock);
        servo2AntiClock = findViewById(R.id.servo2AntiClock);

        // Now creating on-Click events to move the Servo's //
        servo1Clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(45); //You can manage the blinking time with this parameter
                animation.setStartOffset(20);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setRepeatCount(Animation.ABSOLUTE);
                servo1Clock.startAnimation(animation);


                try {


                    BTsocket.getOutputStream().write(servo1C.getBytes());

                    // Received value from Ardunio to Help improve User Interface //

                    int receivedValue = BTsocket.getInputStream().read();
                    String receivedFromArduino = Integer.toString(receivedValue);
                    Log.e("servo1CReturnedValue", receivedFromArduino);


                    //Blink the button to notify the user that further action is not possible //
                    if (receivedFromArduino.contains("120")) {

                        // handler.sendEmptyMessageDelayed(RED, 45);
                        handler.sendEmptyMessageDelayed(Button1, 0);

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        });

        servo1AntiClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(45); //You can manage the blinking time with this parameter
                animation.setStartOffset(20);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setRepeatCount(Animation.ABSOLUTE);
                servo1AntiClock.startAnimation(animation);

                try {


                    BTsocket.getOutputStream().write(servo1A.getBytes());

                    // Received value from Arduino To improve User-interface //
                    int receivedValue = BTsocket.getInputStream().read();
                    String receivedFromArduino = Integer.toString(receivedValue);
                    //         Log.e("servo1ACReturnedValue", receivedFromArduino);


                    //Blink the button to notify the user that further action is not possible //
                    if (receivedFromArduino.contains("70")) {

                        //handler1.sendEmptyMessageDelayed(RED, 45);
                        handler.sendEmptyMessageDelayed(Button2, 0);

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        });


        servo2Clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(45); //You can manage the blinking time with this parameter
                animation.setStartOffset(20);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setRepeatCount(Animation.ABSOLUTE);
                servo2Clock.startAnimation(animation);

                try {

                    BTsocket.getOutputStream().write(servo2C.getBytes());


                    // Received value from Arduino To improve User-interface //
                    int receivedValue = BTsocket.getInputStream().read();
                    String receivedFromArduino = Integer.toString(receivedValue);
                    Log.e("servo2CReturnedValue", receivedFromArduino);


                    // Blink the button to notify the user that further action is not possible //
                    if (receivedFromArduino.contains("115")) {

                        // handler2.sendEmptyMessageDelayed(RED, 45);
                        handler.sendEmptyMessageDelayed(Button3, 0);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        });


        servo2AntiClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(45); //You can manage the blinking time with this parameter
                animation.setStartOffset(20);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setRepeatCount(Animation.ABSOLUTE);
                servo2AntiClock.startAnimation(animation);

                try {


                    BTsocket.getOutputStream().write(servo2A.getBytes());
                    int receivedValue = BTsocket.getInputStream().read();
                    String receivedFromArduino = Integer.toString(receivedValue);
                    Log.e("servo2ACReturnedValue", receivedFromArduino);


                    // Shows Error notification to the user that further action is not possible //

                    if (receivedFromArduino.contains("55")) {

                        //handler3.sendEmptyMessageDelayed(RED, 45);
                        handler.sendEmptyMessageDelayed(Button4, 0);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        });


        // Setting on-long click listeners to prevent long pressing as well as notifying user that it's not used here

        servo1Clock.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                handler.sendEmptyMessageDelayed(Button1, 0);

                Toast.makeText(ButtonsActivity.this, "Long-Press Event is not Supported !", Toast.LENGTH_SHORT).show();

                return true;
            }
        });


        servo1AntiClock.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                handler.sendEmptyMessageDelayed(Button2, 0);

                Toast.makeText(ButtonsActivity.this, "Long-Press Event is not Supported !", Toast.LENGTH_SHORT).show();

                return true;
            }
        });


        servo2Clock.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                handler.sendEmptyMessageDelayed(Button3, 0);

                Toast.makeText(ButtonsActivity.this, "Long-Press Event is not Supported !", Toast.LENGTH_SHORT).show();

                return true;
            }
        });


        servo2AntiClock.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                handler.sendEmptyMessageDelayed(Button4, 0);

                Toast.makeText(ButtonsActivity.this, "Long-Press Event is not Supported !", Toast.LENGTH_SHORT).show();

                return true;
            }
        });


    }


    @Override
    protected void onPause() {
        if (BTsocket != null && IsBluetoothConnected) {
            new DisConnectBT().execute();
        }

        Log.d(TAG, "Paused");

        super.onPause();
    }

    @Override
    protected void onResume() {
        if (BTsocket == null || !IsBluetoothConnected) {

            new ConnectBT().execute();

        }

        Log.d(TAG, "Resumed");

        super.onResume();
    }

    @Override
    protected void onStop() {

        Log.d(TAG, "Stopped");

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {

            // From it's name  it is clear when we click on connect it shows this Text on a Progress bar

            progressDialog = ProgressDialog.show(ButtonsActivity.this, "Hold on", "Connecting");


        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                if (BTsocket == null || !IsBluetoothConnected) {
                    // Selecting the bluetooth we chose in Array list and connecting it's socket with out Virtual Android Studio
                    // Socket //

                    BTsocket = Device.createInsecureRfcommSocketToServiceRecord(DeviceUUID);

                    // Taking Default local bluetooth Adapter(Used to transmit Send/Receive signals) and canceling it's
                    // availability to other devices as it's already connected with one  //

                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

                    // this line of Code Actually Connects out Virtual Android Studio Bluetooth
                    // Socket with the Socket of Out Bluetooth Module and now data transfer/Receive is started //

                    BTsocket.connect();
                }

            } catch (IOException e) {
                // Unable to connect to device`

                e.printStackTrace();

                mConnectSuccessful = false;

            }

            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!mConnectSuccessful) {

                Toast.makeText(getApplicationContext(), "Could not connect to device.Please turn on your Hardware", Toast.LENGTH_LONG).show();

                //Finishes this Activity when connection is not established //
                // After this activity finishes , Main activity will be onResumed //
                finish();
            } else {

                msg("Connected to device");

                IsBluetoothConnected = true;

                ReadThread = new ReadInput(); // Kick off input reader
            }

            progressDialog.dismiss();
        }

    }


    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            msg("Disconnecting...");
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (ReadThread != null) {
                ReadThread.stop();

                while (ReadThread.isRunning()) ;

                // Wait until it stops

                ReadThread = null;

            }

            try {

                BTsocket.close();

            } catch (IOException e) {

                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            IsBluetoothConnected = false;

            if (IsUserInitiatedDisconnect) {
                finish();
            }

        }

    }


    // Creating a thread to use multi-tasking, when one task causes delay, other thread is invoked by the thread-scheduler from list of ready to run threads //

    private class ReadInput implements Runnable {

        private boolean bStop = false;
        private Thread t;

        public ReadInput() {
            t = new Thread(this, "Input Thread");
            t.start();
        }


        public boolean isRunning() {

            return t.isAlive();
        }

        @Override
        public void run() {
            InputStream inputStream;

            try {
                inputStream = BTsocket.getInputStream();

                while (!bStop) {

                    byte[] buffer = new byte[256];

                    if (inputStream.available() > 0) {
                        inputStream.read(buffer);

                        int i = 0;

                        // This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4


                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                        }

                        final String strInput = new String(buffer, 0, i);

                        //  If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix


                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {

                e.printStackTrace();
            } catch (InterruptedException e) {

                e.printStackTrace();
            }

        }


        public void stop() {
            bStop = true;
        }

    }


    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();

    }


    // These handlers set delay between two actions


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {

                // For Servo1 Clock-wise Button //
                case Button1:
                    servo1Clock.setBackgroundColor(android.graphics.Color.parseColor("#D81818"));
                    handler.sendEmptyMessageDelayed(restoreBtn1Clr, 45);
                    break;
                case restoreBtn1Clr:
                    servo1Clock.setBackgroundColor(android.graphics.Color.parseColor("#79B87C"));
                    break;

                // For Servo1 Anti-Clock-wise Button //
                case Button2:
                    servo1AntiClock.setBackgroundColor(android.graphics.Color.parseColor("#D81818"));
                    handler.sendEmptyMessageDelayed(restoreBtn2Clr, 45);
                    break;
                case restoreBtn2Clr:
                    servo1AntiClock.setBackgroundColor(android.graphics.Color.parseColor("#79B87C"));
                    break;

                // For Servo2 Clock-wise Button //
                case Button3:
                    servo2Clock.setBackgroundColor(android.graphics.Color.parseColor("#D81818"));
                    handler.sendEmptyMessageDelayed(restoreBtn3Clr, 45);
                    break;
                case restoreBtn3Clr:
                    servo2Clock.setBackgroundColor(android.graphics.Color.parseColor("#79B87C"));
                    break;

                // For Servo1 Anti-Clock-wise Button //
                case Button4:
                    servo2AntiClock.setBackgroundColor(android.graphics.Color.parseColor("#D81818"));
                    handler.sendEmptyMessageDelayed(restoreBtn4Clr, 45);
                    break;
                case restoreBtn4Clr:
                    servo2AntiClock.setBackgroundColor(android.graphics.Color.parseColor("#79B87C"));
                    break;

            }

        }
    };


}
