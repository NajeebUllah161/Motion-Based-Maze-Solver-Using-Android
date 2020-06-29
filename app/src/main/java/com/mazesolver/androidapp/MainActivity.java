package com.mazesolver.androidapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private Button search_Btn;
    private Button connect_Btn;
    private ListView listView;
    private BluetoothAdapter myBTadapter;
    private static final int BT_ENABLE_REQUEST = 10;
    private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private int mBufferSize = 50000; //Default
    public static final String DEVICE_EXTRA = "com.mazesolver.androidapp.SOCKET";
    private static final String DEVICE_LIST = "com.mazesolver.androidapp.devicelist";
    private static final String DEVICE_LIST_SELECTED = "com.mazesolver.androidapp.devicelistselected";
    public static final String DEVICE_UUID = "com.mazesolver.androidapp.uuid";
    public static final String BUFFER_SIZE = "com.mazesolver.androidapp.buffersize";
    private CheckBox btnCheckbox, gyroCheckbox, imgCheckbox;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        search_Btn = findViewById(R.id.search_Btn);
        connect_Btn = findViewById(R.id.connect_Btn);
        listView = findViewById(R.id.list_View);

        btnCheckbox = findViewById(R.id.btnCheckbox);
        gyroCheckbox = findViewById(R.id.gyroCheckbox);
        imgCheckbox = findViewById(R.id.imgCheckbox);


        // Activity restore itself into a previous state under this if condition "if this activity is not null means it has already been invoked before"
        // it uses the reference to the bundle object to restore data in that bundle.

        if (savedInstanceState != null) {
            ArrayList<BluetoothDevice> list = savedInstanceState.getParcelableArrayList(DEVICE_LIST);

            if (list != null) {
                initList(list);
                MyAdapter adapter = (MyAdapter) listView.getAdapter();
                int selectedIndex = savedInstanceState.getInt(DEVICE_LIST_SELECTED);

                if (selectedIndex != -1) {
                    adapter.setSelectedIndex(selectedIndex);
                    connect_Btn.setEnabled(true);
                }
            } else {
                initList(new ArrayList<BluetoothDevice>());
            }

        } else {
            initList(new ArrayList<BluetoothDevice>());
        }


        gyroCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    btnCheckbox.setChecked(false);
                    imgCheckbox.setChecked(false);
                }

            }
        });


        btnCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    gyroCheckbox.setChecked(false);
                    imgCheckbox.setChecked(false);
                }

            }
        });

        imgCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    gyroCheckbox.setChecked(false);
                    btnCheckbox.setChecked(false);

                }
            }
        });


        search_Btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {


                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(45); //You can manage the blinking time with this parameter
                animation.setStartOffset(20);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setRepeatCount(Animation.ABSOLUTE);
                search_Btn.startAnimation(animation);

                myBTadapter = BluetoothAdapter.getDefaultAdapter();

                if (myBTadapter == null) {
                    // Check if your android ]\\\=device has a bluetooth or not //

                    Toast.makeText(getApplicationContext(), "Bluetooth not found !", Toast.LENGTH_SHORT).show();
                } else if (!myBTadapter.isEnabled()) {
                    // IF you device has bluetooth but it's not turned on then Asks permission to turn bluetooth on 'allow or decline ' //

                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, BT_ENABLE_REQUEST);

                } else {
                    // Execute the task with Specified Parameters ((Params... params)) //
                    new SearchDevices().execute();
                }

            }
        });

        connect_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(45); //You can manage the blinking time with this parameter
                animation.setStartOffset(20);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setRepeatCount(Animation.ABSOLUTE);
                connect_Btn.startAnimation(animation);

                try {


                    BluetoothDevice device = ((MyAdapter) (listView.getAdapter())).getSelectedItem();

                    if (btnCheckbox.isChecked()) {

                        Intent intent = new Intent(getApplicationContext(), ButtonsActivity.class);
                        intent.putExtra(DEVICE_EXTRA, device);
                        intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                        intent.putExtra(BUFFER_SIZE, mBufferSize);

                        startActivity(intent);

                    } else if (gyroCheckbox.isChecked()) {

                        Intent intent = new Intent(getApplicationContext(), OrientationSensorActivity.class);
                        intent.putExtra(DEVICE_EXTRA, device);
                        intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                        intent.putExtra(BUFFER_SIZE, mBufferSize);

                        startActivity(intent);


                    } else if (imgCheckbox.isChecked()) {

                        Intent intent = new Intent(getApplicationContext(), ImageProcessingActivity.class);
                        intent.putExtra(DEVICE_EXTRA, device);
                        intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());

                        startActivity(intent);

                    } else {

                        msg("Check At-least one Checkbox to continue !");

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    msg("First search and select a device then click connect");

                }
            }

        });

    }


    // The onPause() method is used to write any persistent data (such as user edits) to storage //
    //Called when the activity loses foreground state, is no longer focusable or before transition to stopped/hidden or destroyed state. The activity is still visible to
    // user, so it's recommended to keep it visually active and continue updating the UI.

    @Override
    protected void onPause() {
        super.onPause();
    }

    // Called when the activity is no longer visible to the user. This may happen either because a new activity is being started on top, an existing one is being brought in
    // front of this one, or this one is being destroyed. This is typically used to stop animations and refreshing the UI, etc.

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // This is msg method which is called certain-times //

    private void msg(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();

    }


    // Initialize the list adapter //

    private void initList(List<BluetoothDevice> objects) {
        final MyAdapter adapter = new MyAdapter(getApplicationContext(), R.layout.list_item, R.id.lstContent, objects);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedIndex(position);
                connect_Btn.setEnabled(true);

            }
        });

    }


    // AsyncTask is a helper class which allows you to do background work 'few seconds of work' //

    // In this class we only search for already paired devices which are paired through 'Settings> Bluetooth' in your Android device //

    private class SearchDevices extends AsyncTask<Void, Void, List<BluetoothDevice>> {

        // This is auto generated method by this helper-class 'AsyncTask' //

        @Override
        protected List<BluetoothDevice> doInBackground(Void... voids) {

            // This 'Set' is just like a set in mathematics that contains a Set of non-duplicated elements //

            Set<BluetoothDevice> pairedDevices = myBTadapter.getBondedDevices();

            // This 'List' is an Ordered collection of elements means we know which element is inserted at which position in this list and we can access it by using it's integer index (listOFDevices.IndexOF(anyIntegerValue)) //

            List<BluetoothDevice> listOfDevices = new ArrayList<BluetoothDevice>();

            //Now using For-each loop to add each pairedDevice into the listOfDevices ArrayList //

            for (BluetoothDevice device : pairedDevices) {

                listOfDevices.add(device);

            }


            return listOfDevices;

        }

        // This onPostExecute (built-in method) method accepts the values returned by doInBackGround method //

        @Override
        protected void onPostExecute(List<BluetoothDevice> listOfDevices) {

            super.onPostExecute(listOfDevices);

            //System.out.println(listDevices);

            if (listOfDevices.size() > 0) {

                MyAdapter adapter = (MyAdapter) listView.getAdapter();
                adapter.replaceItems(listOfDevices);

            } else {
                msg("No paired devices found, please pair your serial BT device and try again");
            }


        }


    }

    // Making sure we are not doing discovery anymore //

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    // This class extends to a Custom adapter which shows the list of Paired-Devices //

    private class MyAdapter extends ArrayAdapter<BluetoothDevice> {

        private int selectedIndex;
        private Context context;  // This is used for Obtaining access to Database and Preferences //
        private int selectedColor = Color.parseColor("#abcdef");
        private List<BluetoothDevice> myList;

        // Constructor for initializing Datamembers  //

        public MyAdapter(Context ctx, int resource, int textViewResourceId, List<BluetoothDevice> objects) {

            // super is used here to invoke constructor //

            super(ctx, resource, textViewResourceId, objects);
            context = ctx;
            myList = objects;
            selectedIndex = -1;


        }


        public void setSelectedIndex(int position) {
            selectedIndex = position;
            notifyDataSetChanged();
        }

        public BluetoothDevice getSelectedItem() {

            return myList.get(selectedIndex);
        }


        public int getCount() {

            return myList.size();
        }


        public BluetoothDevice getItem(int position) {

            return myList.get(position);
        }


        public long getItemId(int position) {

            return position;
        }


        // Notifies the attached observers that the underlying data has been changed and any View reflecting the data set should refresh itself. //

        public void replaceItems(List<BluetoothDevice> list) {
            myList = list;
            notifyDataSetChanged();
        }


        /*public List<BluetoothDevice> getEntireList()

        {

            return myList;

        }
*/

        // This class is used in getView method //

        private class ViewHolder {
            TextView tv;
        }


        // This method gets a View that displays the data at the specified position in the data set. //

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            ViewHolder holder;

            // If view where available bluetooth devices are to be shown is empty add a view otherwise get already existed view and put
            // it on the position where it should be .

            if (convertView == null) {
                vi = LayoutInflater.from(context).inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.tv = vi.findViewById(R.id.lstContent);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            if (selectedIndex != -1 && position == selectedIndex) {

                holder.tv.setBackgroundColor(selectedColor);
            } else {
                holder.tv.setBackgroundColor(Color.WHITE);
            }

            BluetoothDevice device = myList.get(position);
            holder.tv.setText(device.getName() + "\n " + device.getAddress());
            return vi;

        }

    }

}
