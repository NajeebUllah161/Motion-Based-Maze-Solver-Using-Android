package com.mazesolver.androidapp;

//import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class OrientationSensorActivity extends AppCompatActivity implements SensorEventListener {


    // Bluetooth stuff
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    // SPP UUID
    private BluetoothDevice Device;
    private int mMaxChars = 50000; // Default change this to string
    private UUID DeviceUUID; // Universally Unique Identifier


    private SensorManager mSensorManager;
    private TextView txtY, txtZ;
    private ImageView rightArrow, leftArrow, upArrow, downArrow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        // Taking address of the bluetooth which we selected in Arraylist in MainActivity //
        // Create a pointer to the device by its MAC address

        Device = b.getParcelable(MainActivity.DEVICE_EXTRA);
        DeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
        mMaxChars = b.getInt(MainActivity.BUFFER_SIZE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        // keeps the full-screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // keep the screen awake


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        btAdapter = BluetoothAdapter.getDefaultAdapter();


        // Angle of Y,Z co-ordinates will be displayed here in degree radians
        txtY = findViewById(R.id.txtY);
        txtZ = findViewById(R.id.txtZ);

        rightArrow = findViewById(R.id.rightArrow);
        leftArrow = findViewById(R.id.leftArrow);
        upArrow = findViewById(R.id.upArrow);
        downArrow = findViewById(R.id.downArrow);

        // Hide image view
        rightArrow.setVisibility(View.GONE);
        leftArrow.setVisibility(View.GONE);
        upArrow.setVisibility(View.GONE);
        downArrow.setVisibility(View.GONE);

        //  checkBTState();

        // Register the Accelerometer sensor
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);


    }


    @Override
    protected void onResume() {
        super.onResume();


        try {
            btSocket = Device.createRfcommSocketToServiceRecord(DeviceUUID);
        } catch (IOException e) {
            errorExit("Error", "In onResume() happened the following error: " + e.getMessage() + ".");
        }

        btAdapter.cancelDiscovery();

        // Resuming the bluetooth connection
        try {
            btSocket.connect();
            Toast.makeText(getApplicationContext(), "Connected to device", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            try {
                btSocket.close();
                Toast.makeText(getApplicationContext(), "Could not connect to device.Please turn on your Hardware", Toast.LENGTH_SHORT).show();
                finish();
            } catch (IOException e2) {
                errorExit("Error", "Uunable to close connection after connection failure: " + e2.getMessage() + ".");
            }
        }

/*        // Create a data stream so we can talk to server.
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Error", "Stream creation failed: " + e.getMessage() + ".");
        }
*/
    }


    @Override
    protected void onPause() {
        super.onPause();

        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }

        mSensorManager.unregisterListener(this);
        Toast.makeText(getApplicationContext(), "Disconnecting...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


        int prevValY = 0;
        int prevValZ = 0;

        int y = (int) sensorEvent.values[1];
        int z = (int) sensorEvent.values[2];


        String yValue = "Y value: ";
        String zValue = "Z value: ";
        txtY.setText(yValue + y);
        txtZ.setText(zValue + z);

        //Orientation along Y-axis

        if (y > 0) {
            upArrow.setVisibility(View.VISIBLE);
            downArrow.setVisibility(View.GONE);
        } else if (y == 0) {
            upArrow.setVisibility(View.GONE);
            downArrow.setVisibility(View.GONE);

        } else if (y < 0) {
            upArrow.setVisibility(View.GONE);
            downArrow.setVisibility(View.VISIBLE);
        }


        // Arrows help us show orientation
        // Orientation along Z-axis

        if (z > 0) {
            leftArrow.setVisibility(View.VISIBLE);
            rightArrow.setVisibility(View.GONE);
        } else if (z == 0) {
            rightArrow.setVisibility(View.GONE);
            leftArrow.setVisibility(View.GONE);
        } else if (z < 0) {
            rightArrow.setVisibility(View.VISIBLE);
            leftArrow.setVisibility(View.GONE);
        }

        if (prevValY != y) {
            if (y % 5 == 0) {
                sendOrientationY(y);
                //  dataSent.setText("Data Sent: " + y);

                prevValY = y;
            } else {
                //dataSent.setText("No Data Sent: Not divisible");
                prevValY = y;
            }
        } else {
            Log.e("DataSent", "No Data Sent");
            //dataSent.setText("No Data Sent: Same Value");
        }


        if (prevValZ != z) {
            if (z % 5 == 1 || z % 5 == -1) {
                if (z < 0) {
                    z = z * -1;
                    // sendOrientationZ(z);
                } else if (z > 0) {
                    z = z * -1;
                    // sendOrientationZ(z);
                }

                sendOrientationZ(z);
                // dataSent.setText("Data Sent: " + z);

                prevValZ = z;
            } else {
                //dataSent.setText("No Data Sent: Not divisible");
                prevValZ = z;
            }
        } else {

            Log.e("DataSent", "No Data Sent");
            //dataSent.setText("No Data Sent: Same Value");
        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    private void errorExit(String title, String message) {
        Toast msg = Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_SHORT);
        msg.show();
        finish();
    }


    private void sendOrientationY(int yPosition) {
        if (btSocket != null) {
            try {

                // Create the command that will be sent to arduino.
                String value = yPosition + ":";
                // String must be converted in its bytes to be sent on serial
                // communication
                btSocket.getOutputStream().write(value.getBytes());
            } catch (IOException e) {
                //dataSent.setText("Error passing data");
            }
        }
    }


    private void sendOrientationZ(int zPosition) {
        if (btSocket != null) {
            try {
                // Create the command that will be sent to arduino.
                String value = zPosition + ";";
                Log.e("txtYPosition", value);
                // String must be converted in its bytes to be sent on serial
                // communication
                btSocket.getOutputStream().write(value.getBytes());
            } catch (IOException e) {
                // dataSent.setText("Error passing data");
            }
        }
    }


}
