package com.mazesolver.androidapp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.opencv.android.OpenCVLoader;

import java.util.UUID;

// Purpose: Provides an interface for the user to take a picture of a maze.

public class ImageProcessingActivity extends Activity {

    // Starts OpenCV
    static {
        OpenCVLoader.initDebug();
    }

    public static final String IMAGE_URI = "com.mazesolver.androidapp.IMAGE_URI";
    static final int REQUEST_TAKE_PHOTO = 1, REQUEST_LOAD_PHOTO = 2;
    private final int IMG_SCALE_FACTOR = 10;
    private static final String TAG = "ImageProcessingActivity";
    private Uri mImageURI;
    private FirebaseAnalytics mFirebaseAnalytics;
    private int currentApiVersion;
    private UUID DeviceUUID; // Universally Unique Identifier
    private BluetoothDevice Device;
    public static final String DEVICE_EXTRA = "com.mazesolver.androidapp.SOCKET";
    public static final String DEVICE_UUID = "com.mazesolver.androidapp.uuid";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        // Taking address of the bluetooth which we selected in Arraylist in MainActivity //


        Device = b.getParcelable(MainActivity.DEVICE_EXTRA);
        DeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));

        super.onCreate(savedInstanceState);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.imgprocessing);

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


        // Creates the buttons with their listeners
        // Button takePhotoButton = findViewById(R.id.takePhotoButton);
        Button loadPhotoButton = findViewById(R.id.loadPhotoButton);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/press_start_2p.ttf");
        loadPhotoButton.setTypeface(font);
        //takePhotoButton.setTypeface(font);

        TextView txt = findViewById(R.id.textView);
        txt.setTypeface(font);

/*
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePhotoIntent();
            }
        });
*/
        loadPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchLoadPhotoIntent();
            }
        });
    }

    // Starts the intent and take photo of Maze
/*
    private void dispatchTakePhotoIntent() {

     try {


         Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

         // Ensure that there's a camera activity to handle the intent
         if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

             // Create the File where the photo should go
             File photoFile = null;
             try {
                 photoFile = createImageFile();
             } catch (IOException ex) {
                 // Error occurred while creating the File
                 Log.e(TAG, "dispatchTakePhotoIntent: " + "Error Creating file");
             }

             // Continue only if the File was successfully created
             if (photoFile != null) {
                 mImageURI = FileProvider.getUriForFile(this,
                         "com.mazesolver.androidapp.fileprovider",
                         photoFile);

                 // Takes the picture
                 takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageURI);
                 startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
             }
         }
     }
     catch(Exception e)
        {

       e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();

        }
    }

*/
    // Starts the intent to load a photo from the gallery.

    private void dispatchLoadPhotoIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose Picture"), REQUEST_LOAD_PHOTO);
    }


    // Creates space to take an image of the maze.
    // The file that the image will occupy
    // Throws IOException If the file cannot be created
/*
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "MAZE_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );
        return image;
    }
*/

    // Starts CornerSelectActivity with the image data grabbed from this activity.
    // Describes whether the user loaded or captured a photo
    // Successful or unsuccessful
    // The intent storing the image URI

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (resultCode == RESULT_OK) {
                Intent cornerIntent = new Intent(this, CornerSelectActivity.class);
                if (requestCode == REQUEST_LOAD_PHOTO || requestCode == REQUEST_TAKE_PHOTO) {
                    if (requestCode == REQUEST_LOAD_PHOTO) {
                        mImageURI = data.getData();

                        // Log with Fire-base
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.VALUE, "Loaded Photo");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    } else {
 /*                   // Log with Fire-base
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.VALUE, "Captured Photo");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
*/

                    }


                    cornerIntent.putExtra(IMAGE_URI, mImageURI.toString());
                    cornerIntent.putExtra(DEVICE_EXTRA, Device);
                    cornerIntent.putExtra(DEVICE_UUID, DeviceUUID.toString());

                    startActivity(cornerIntent);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
    }


    @Override
    protected void onStop() {

        super.onStop();
    }


}
