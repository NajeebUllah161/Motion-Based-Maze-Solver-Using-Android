package com.mazesolver.androidapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mazesolver.androidapp.pathfinding.Asolution;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.wang.avi.AVLoadingIndicatorView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import android.graphics.Point;
import android.widget.Toast;


// Purpose: Parses the maze image into an array then computes and displays its solution.
// Adjust Steps of Instructions on line 691 with tilttime / (Greater value = Shorter Steps, and vice versa)
public class SolutionActivity extends Activity {

    private static final String TAG = "SolutionActivity";
    private final int SOLVING_SCALE_FACTOR = 2; // The amount the maze scales down before using A*
    private final int VIEW_SCALE_FACTOR = 4;
    private final int MAZE_SOLVED = 1, MAZE_NOT_SOLVED = 0, IMG_DEBUG = -1, RECYCLE_IMG = -2;
    private final long MIN_LOAD_TIME = 1000; // The min time to show the loading icon
    private Point mazeCorner;
    private ImageView imageView;
    private TextView loadingText;
    private TextView failText;
    private AVLoadingIndicatorView loadingIcon;
    private Button backButton, sendInstructions_Btn;
    private Handler mHandler;
    private FirebaseAnalytics mFirebaseAnalytics;

    // This is used to cause Delay between values, Delay time can be adjusted
    private int[] realLifeInstructionsGlobal;
    int value = 0;
    static int count = -1;
    Handler handle = new Handler();

    Runnable r = new Runnable() {
        @Override
        public void run() {
            sendInstructionsToArduino();
        }
    };

    // Instantiates the UI elements and starts the solution thread.

    private int mMaxChars = 50000; // Default change this to string
    private UUID DeviceUUID; // Universally Unique Identifier
    private BluetoothSocket BTsocket; // Virtual socket of AS


    // Making variable of a class down there and we are using it in various ways to Read input //
    private ReadInput ReadThread = null;
    private boolean IsUserInitiatedDisconnect = false;
    private boolean IsBluetoothConnected = false;

    private BluetoothDevice Device;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent1 = getIntent();
        Bundle b = intent1.getExtras();

        // Taking address of the bluetooth which we selected in Arraylist in MainActivity //


        Device = b.getParcelable(MainActivity.DEVICE_EXTRA);
        DeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
        mMaxChars = b.getInt(MainActivity.BUFFER_SIZE);


        super.onCreate(savedInstanceState);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Removes the title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_solution);

        // Casting buttons
        imageView = findViewById(R.id.imageView);
        loadingText = findViewById(R.id.loadingText);
        loadingIcon = findViewById(R.id.loadingIcon);
        failText = findViewById(R.id.failText);
        backButton = findViewById(R.id.backButton);
        sendInstructions_Btn = findViewById(R.id.sendInstructn_Btn);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/press_start_2p.ttf");
        loadingText.setTypeface(font);
        failText.setTypeface(font);
        backButton.setTypeface(font);
        sendInstructions_Btn.setTypeface(font);
        failText.setVisibility(View.INVISIBLE);
        backButton.setVisibility(View.INVISIBLE);
        startLoading();

        // Loads the intent
        final Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        final int[][] corners = (int[][]) bundle.getSerializable(CornerSelectActivity.CORNERS);

        // Loads the intent image as a bitmap for processing
        Uri imgUri = Uri.parse(bundle.getString(ImageProcessingActivity.IMAGE_URI));
        try {
            Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);

            // Rotate the image if its wider than it is long
            if (image.getWidth() > image.getHeight())
                image = rotateBitmap(image, 90);

            mHandler = new MazeUIHandler(Looper.getMainLooper(), image);

            Runnable solnRunnable = new SolutionRunnable(image, corners);
            new Thread(solnRunnable).start();
            //image.recycle();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onCreate: Error loading image", e);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        sendInstructions_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(45); //You can manage the blinking time with this parameter
                animation.setStartOffset(20);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setRepeatCount(Animation.ABSOLUTE);
                sendInstructions_Btn.startAnimation(animation);

                sendInstructionsToArduino();

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (imageView.getDrawable() != null) {
            Bitmap currentBMP = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            Log.i(TAG, "onStop: Recycled current bmp dims: " + currentBMP.getWidth() + ", " + currentBMP.getHeight());
            Log.i(TAG, "onStop: Recycling current BMP in imageView");

            currentBMP.recycle();
        }

        mHandler.sendEmptyMessage(RECYCLE_IMG);
        imageView.setImageDrawable(null);
        Log.d(TAG, "Stopped");

    }


    // The handler that interacts with the solution thread to display the maze's results in the main
    // UI thread.

    private class MazeUIHandler extends Handler {
        private boolean debugging;
        private Bitmap image;


        // The constructor for the UI handler.
        // The main thread's looper.
        // The image containing the maze.

        private MazeUIHandler(Looper myLooper, Bitmap image) {
            super(myLooper);
            this.image = image;
            debugging = false;
        }


        // Receives the solution thread's updates and displays the results on the main UI.

        // The message containing the maze's results.

        public void handleMessage(Message msg) {
            int state = msg.what;

            switch (state) {
                case MAZE_SOLVED:
                    if (!debugging) {
                        Bundle b = (Bundle) msg.obj;
                        Stack<int[]> path = (Stack<int[]>) b.getSerializable("path");
                        int[][] binaryMaze = (int[][]) b.getSerializable("binary");

                        stopLoading();
                        drawSolution(path, binaryMaze, image);
                        Log.i(TAG, "handleMessage: Original img dims: " + image.getWidth() + ", " + image.getHeight());
                        Log.i(TAG, "handleMessage: Image: " + image);
                        image.recycle();

                        backButton.setVisibility(View.VISIBLE);

                        Bundle fireB = new Bundle();
                        fireB.putString("RESULT", "MAZE_NOT_SOLVED");
                        mFirebaseAnalytics.logEvent("MAZE_PROCESSED", fireB);
                    }
                    break;
                case MAZE_NOT_SOLVED:
                    if (!debugging) {
                        stopLoading();
                        failText.setVisibility(View.VISIBLE);
                        backButton.setVisibility(View.VISIBLE);
                        image.recycle();

                        Log.i(TAG, "handleMessage: MAZE_NOT_SOLVED");
                        Bundle fireB = new Bundle();
                        fireB.putString("RESULT", "MAZE_NOT_SOLVED");
                        mFirebaseAnalytics.logEvent("MAZE_PROCESSED", fireB);
                    }
                    break;
                case IMG_DEBUG:
                    debugging = true;

                    stopLoading();

                    Bundle b1 = (Bundle) msg.obj;
                    image = b1.getParcelable("img");
                    Log.i(TAG, "handleMessage: displaying debug");

                    imageView.setImageBitmap(image);
                    image.recycle();
                    backButton.setVisibility(View.VISIBLE);
                    break;
                case RECYCLE_IMG:
                    if (image != null && !image.isRecycled()) {
                        Log.i(TAG, "handleMessage: Recycled image in handler");
                        image.recycle();
                        image = null;
                    }
                    //imageView.setImageDrawable(null);
                    break;
            }
        }
    }


    // A thread to solve the maze in the background while the loading icon is being displayed.

    private class SolutionRunnable implements Runnable {
        private Bitmap image;
        private int[][] corners;


        // The constructor for the solution runnable thread
        // The image containing the maze
        // The four corners that define the user's selected region

        private SolutionRunnable(Bitmap image, int[][] corners) {
            // store parameter for later user
            this.image = image;
            this.corners = corners;
        }


        // Crops the maze, converts it to a binary array, and runs A* over it to find the solution.
        // Sends the path and the maze to mHandler to be drawn in the main thread.

        public void run() {
            int state;
            final long startTime = System.currentTimeMillis();

            Mat croppedMaze = getCroppedMaze(corners, image);
            int[][] croppedBinaryMaze = CVUtils.getBinaryArray(croppedMaze);
            croppedMaze.release();

            // Runs A* on the maze and gets the solution stack
            Stack<int[]> solution = null;
            try {
                Asolution mySol = new Asolution(croppedBinaryMaze);
                solution = mySol.getPath();

                if (solution == null)
                    state = MAZE_NOT_SOLVED;
                else
                    state = MAZE_SOLVED;

            } catch (Exception e) {
                Log.e(TAG, "run: Exception when solving maze: ", e);
                state = MAZE_NOT_SOLVED;

                Bundle fireB = new Bundle();
                fireB.putString("RESULT", "ERROR_SOLVING_MAZE");
                fireB.putString(FirebaseAnalytics.Param.VALUE, e.getMessage());
                mFirebaseAnalytics.logEvent("MAZE_PROCESSED", fireB);
            }

            final long executionTime = System.currentTimeMillis() - startTime;
            Log.i(TAG, "run: Execution time: " + executionTime + " ms");

            if (executionTime < MIN_LOAD_TIME) // Forces the loading icon showing for MIN_LOAD_TIME
                android.os.SystemClock.sleep(MIN_LOAD_TIME - executionTime);

            Bundle fireB = new Bundle();
            fireB.putString(FirebaseAnalytics.Param.VALUE, executionTime / 1000 + " s");
            mFirebaseAnalytics.logEvent("SOLVE_TIME", fireB);

            Bundle b = new Bundle();
            b.putSerializable("path", solution);
            b.putSerializable("binary", croppedBinaryMaze);
            Message completeMessage = mHandler.obtainMessage(state, b);
            Log.i(TAG, "run: Image: " + image);
            completeMessage.sendToTarget();
        }
    }


    // Hides the loading icon and text.

    private void stopLoading() {
        loadingIcon.setVisibility(View.INVISIBLE);
        loadingText.setVisibility(View.INVISIBLE);
    }


    // Shows the loading icon and text.

    private void startLoading() {
        loadingIcon.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);
    }


    // A debug method to prematurely send a Mat to mHandler. This is used to view the Mat of the maze
    // in the solution thread before it is solved.

    // The Mat to be viewed.

    private void sendMat2Handler(Mat mat) {
        Bundle b = new Bundle();
        b.putParcelable("img", CVUtils.mat2BMP(mat));
        Message completeMessage = mHandler.obtainMessage(IMG_DEBUG, b);
        completeMessage.sendToTarget();
    }


    // Locates the maze in the selected region, crops it and downscales it.
    // The four corners that define the user's selected region
    // The image containing the maze
    // The cropped and binary-ized maze

    private Mat getCroppedMaze(int[][] corners, Bitmap image) {
        // Converts the bitmap to an OpenCV matrix
        Mat img_matrix = new Mat();
        Bitmap bmp32 = image.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, img_matrix);
        bmp32.recycle();

        // Convert to gray, blur, and threshold.
        Imgproc.cvtColor(img_matrix, img_matrix, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(img_matrix, img_matrix, new Size(11, 11), 0);
        Imgproc.adaptiveThreshold(img_matrix, img_matrix, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 55, 5);

        // Crops the image AFTER the thresholding to avoid those border lines
        img_matrix = CVUtils.cropQuadrilateral(img_matrix, corners);

        // Gets the two contours with the longest perimeter. Since mazes can have a line drawn
        // through them that splits them in half (the solution), the maze is actually two separate
        // objects, necessitating us to retrieve two contours instead of one.
        List<MatOfPoint> mazePerim = CVUtils.largest2PerimContours(img_matrix);

        if (mazePerim != null) {
            // Get the bounding rects
            ArrayList<Rect> rects = new ArrayList<>();
            for (MatOfPoint c : mazePerim)
                rects.add(Imgproc.boundingRect(c));

            // Combine the bounding rects of the maze perimeter into one, encompassing bounding rect
            Rect combined = CVUtils.combineRects(rects.get(0), rects.get(1));

            // Contracts the bounding box to help eliminate whitespace on the edge of the maze
            final int contract_px = 3;
            combined.x += contract_px;
            combined.y += contract_px;
            combined.width -= contract_px * 2;
            combined.height -= contract_px * 2;

            int[][] bounds = new int[][]{
                    {combined.x, combined.y},
                    {combined.x + combined.width, combined.y},
                    {combined.x + combined.width, combined.y + combined.height},
                    {combined.x, combined.y + combined.height}
            };

            //img_matrix = CVUtils.cropQuadrilateral(img_matrix, bounds);
            img_matrix = img_matrix.submat(combined);

            // Find the lowest x and y coords because that will define the rect that the first pass
            // cropped maze was inside of
            int lowestX = corners[0][0], lowestY = corners[0][1];
            for (int[] p : corners) {
                if (p[0] < lowestX)
                    lowestX = p[0];
                if (p[1] < lowestY)
                    lowestY = p[1];
            }

            // The point in which the newly cropped maze lies in the original image.
            mazeCorner = new Point(combined.x + lowestX, combined.y + lowestY);
        }

        // Resize the image
        Size dstSize = new Size(img_matrix.width() / SOLVING_SCALE_FACTOR, img_matrix.height() / SOLVING_SCALE_FACTOR);
        Mat dst = new Mat();
        Imgproc.resize(img_matrix, dst, dstSize, 1, 1, Imgproc.INTER_AREA);

        img_matrix = dst;

        Log.i(TAG, "getCroppedMaze: Scaled image size: " + dstSize);

        Imgproc.adaptiveThreshold(img_matrix, img_matrix, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, 30);

        return img_matrix;
    }


    // Overlays the solution on top of the original maze image.
    // The solution of the maze
    // The binary array representing the maze
    // The original image containing the maze

    private void drawSolution(Stack<int[]> path, int[][] mazetrix, Bitmap image) {
        // Sets the value of the solution pixels to 2
        int lastpointx = 0;
        int lastpointy = 0;
        int check = 0;
        int[] shortestpath = new int[path.size()];
        ArrayList<Integer> shortestPathArray = new ArrayList<Integer>();
        int daignolindex = 0;
        int dir = 0;
        boolean west_north = false;
        boolean west_south = false;
        boolean east_north = false;
        boolean east_south = false;
        for (int[] coords : path) {
            if (check == 0) {
                lastpointy = coords[0];
                lastpointx = coords[1];
                check++;
            }

            mazetrix[coords[0]][coords[1]] = 2;
            if (lastpointx > coords[1] && lastpointy == coords[0]) {
                if (west_north == true || west_south == true || east_north == true || east_south == true) {
                    for (int k = 0; k < daignolindex; k++) {
                        shortestPathArray.add(dir);
                        Log.i(TAG, "direction= daignols  " + dir + " : " + coords[1] + "," + coords[0]);
                    }
                    daignolindex = 0;
                    west_north = false;
                    west_south = false;
                    east_north = false;
                    east_south = false;
                }
                Log.i(TAG, "direction= 3 : West " + coords[1] + "," + coords[0]);
                shortestPathArray.add(3);
                lastpointx = coords[1];
                lastpointy = coords[0];
            } else if (lastpointx < coords[1] && lastpointy == coords[0]) {
                if (west_north == true || west_south == true || east_north == true || east_south == true) {
                    for (int k = 0; k < daignolindex; k++) {
                        shortestPathArray.add(dir);
                        Log.i(TAG, "direction= daignols  " + dir + " : " + coords[1] + "," + coords[0]);
                    }
                    daignolindex = 0;
                    west_north = false;
                    west_south = false;
                    east_north = false;
                    east_south = false;
                }
                Log.i(TAG, "direction= 1 : East " + coords[1] + "," + coords[0]);
                shortestPathArray.add(1);
                lastpointy = coords[0];
                lastpointx = coords[1];
            } else if (lastpointy > coords[0] && lastpointx == coords[1]) {
                if (west_north == true || west_south == true || east_north == true || east_south == true) {
                    for (int k = 0; k < daignolindex; k++) {
                        shortestPathArray.add(dir);
                        Log.i(TAG, "direction= daignols  " + dir + " : " + coords[1] + "," + coords[0]);
                    }
                    daignolindex = 0;
                    west_north = false;
                    west_south = false;
                    east_north = false;
                    east_south = false;
                }
                Log.i(TAG, "direction= 4 : North " + coords[1] + "," + coords[0]);
                shortestPathArray.add(4);
                lastpointx = coords[1];
                lastpointy = coords[0];
            } else if (lastpointy < coords[0] && lastpointx == coords[1]) {
                if (west_north == true || west_south == true || east_north == true || east_south == true) {
                    for (int k = 0; k < daignolindex; k++) {
                        shortestPathArray.add(dir);
                        Log.i(TAG, "direction= daignols  " + dir + " : " + coords[1] + "," + coords[0]);
                    }
                    daignolindex = 0;
                    west_north = false;
                    west_south = false;
                    east_north = false;
                    east_south = false;
                }
                Log.i(TAG, "direction= 2 : South  " + coords[1] + "," + coords[0]);
                shortestPathArray.add(2);
                lastpointy = coords[0];
                lastpointx = coords[1];


            }


            //Daignols-------------------------------------------------------------------------------->
            else if (lastpointx > coords[1] && lastpointy > coords[0]) {
                if (west_south == true || east_north == true || east_south == true) {
                    for (int k = 0; k < daignolindex; k++) {
                        shortestPathArray.add(dir);
                        Log.i(TAG, "direction= daignols  " + dir + " : " + coords[1] + "," + coords[0]);
                    }
                    daignolindex = 0;
                    west_south = false;
                    east_north = false;
                    east_south = false;
                }
                //west-North
                dir = 4;
                daignolindex++;
                shortestPathArray.add(3);
                Log.i(TAG, "direction= 3 : West " + coords[1] + "," + coords[0]);
                lastpointy = coords[0];
                lastpointx = coords[1];
                west_north = true;
            } else if (lastpointx > coords[1] && lastpointy < coords[0]) {
                //west-south
                if (west_north == true || east_north == true || east_south == true) {
                    for (int k = 0; k < daignolindex; k++) {
                        shortestPathArray.add(dir);
                        Log.i(TAG, "direction= daignols  " + dir + " : " + coords[1] + "," + coords[0]);
                    }
                    daignolindex = 0;
                    west_north = false;
                    east_north = false;
                    east_south = false;
                }
                dir = 2;
                daignolindex++;
                shortestPathArray.add(3);
                Log.i(TAG, "direction= 3 : West " + coords[1] + "," + coords[0]);
                lastpointy = coords[0];
                lastpointx = coords[1];
                west_south = true;
            } else if (lastpointx < coords[1] && lastpointy > coords[0]) {

                if (west_south == true || west_north == true || east_south == true) {
                    for (int k = 0; k < daignolindex; k++) {
                        shortestPathArray.add(dir);
                        Log.i(TAG, "direction= daignols  " + dir + " : " + coords[1] + "," + coords[0]);
                    }
                    daignolindex = 0;
                    west_south = false;
                    west_north = false;
                    east_south = false;
                }
                dir = 4;
                daignolindex++;
                shortestPathArray.add(1);
                Log.i(TAG, "direction= 1 : East " + coords[1] + "," + coords[0]);
                lastpointy = coords[0];
                lastpointx = coords[1];
                east_north = true;
            } else if (lastpointx < coords[1] && lastpointy < coords[0]) {
                //East-South
                if (west_south == true || west_north == true || east_north == true) {
                    for (int k = 0; k < daignolindex; k++) {
                        shortestPathArray.add(dir);
                        Log.i(TAG, "direction= daignols  " + dir + " : " + coords[1] + "," + coords[0]);
                    }
                    daignolindex = 0;
                    west_south = false;
                    west_north = false;
                    east_north = false;
                }
                dir = 2;
                daignolindex++;
                shortestPathArray.add(1);
                Log.i(TAG, "direction= 1 : East " + coords[1] + "," + coords[0]);
                lastpointy = coords[0];
                lastpointx = coords[1];
                east_south = true;
            } else {
                if (west_north == true || west_south == true || east_north == true || east_south == true) {
                    for (int k = 0; k < daignolindex; k++) {
                        shortestPathArray.add(dir);
                        Log.i(TAG, "direction= daignols  " + dir + " : " + coords[1] + "," + coords[0]);
                    }
                    daignolindex = 0;
                    west_north = false;
                    west_south = false;
                    east_north = false;
                    east_south = false;
                }
                Log.i(TAG, "wrong side " + coords[1] + "," + coords[0]);
                lastpointy = coords[0];
                lastpointx = coords[1];
            }

        }
        if (west_north == true || west_south == true || east_north == true || east_south == true) {
            for (int k = 0; k < daignolindex; k++) {
                shortestPathArray.add(dir);
                Log.i(TAG, "direction= daignols  " + dir + " : ");
            }
            daignolindex = 0;
            west_north = false;
            west_south = false;
            east_north = false;
            east_south = false;
        }
        ArrayList<Integer> pathDirection = new ArrayList<>();
        int lastindex = 0;
        int lastnumber = shortestPathArray.get(0);
        for (int index = 0; index < shortestPathArray.size() - 1; index++) {

            if (lastnumber != shortestPathArray.get(index) || index == shortestPathArray.size() - 2) {
                int tilttime = index - lastindex;
                int totalgroups = tilttime / 20;  // Adjust Steps of Instructions
                for (int m = 0; m <= totalgroups; m++) {
                    pathDirection.add(shortestPathArray.get(index - 1));
                }
                lastnumber = shortestPathArray.get(index);
                lastindex = index;

            }

        }
        ArrayList<Integer> startToEndPath = new ArrayList<>();
        for (int i = pathDirection.size() - 1; i >= 0; i--) {
            Log.i(TAG, "Final Path: " + pathDirection.get(i));
            //This is where the path is Found and iterated
            startToEndPath.add(pathDirection.get(i));
        }


        int[][] pixOut = new int[mazetrix.length][mazetrix[0].length];

        // The radius that the path "puffs" out in
        final int bloomAmount = (int) ((mazetrix.length * mazetrix[0].length) * Math.pow(SOLVING_SCALE_FACTOR, 2) / 50000);
        Log.i(TAG, "drawSolution: bloom: " + bloomAmount);

        // Colors the solution
        final int opaque = (int) ((long) 0xff << 24) | 0xff << 16; // Encodes it in hexadecimal sRGB color space
        final int translucent = (0xff) << 16;
        for (int i = 0; i < mazetrix.length; i++) {
            for (int j = 0; j < mazetrix[0].length; j++) {

                // If pixel is part of the solution, make the color opaque red, otherwise transparent
                if (mazetrix[i][j] == 2) {
                    pixOut[i][j] = opaque;
//                    Log.i(TAG, "pixel location in path : "+ i+","+j );

                    // Draw the bloom
                    int y;
                    for (int x = -bloomAmount; x <= bloomAmount; x++) {
                        y = (int) Math.round(Math.sqrt(bloomAmount * bloomAmount - x * x));
                        for (int k = -y; k <= y; k++) {
                            if (0 <= i + k && i + k < mazetrix.length && 0 <= j + x && j + x < mazetrix[0].length)
                                if (mazetrix[i + k][j + x] == 0)
                                    pixOut[i + k][j + x] = opaque;
                                else if (mazetrix[i + k][j + x] == 1)
                                    break;
                        }
                    }
                } else {
                    pixOut[i][j] = translucent;
                }
            }
        }

        // Create a bitmap out of the solution and scale it according to SCALE_FACTOR
        int[] pixels = get1DArray(pixOut);
        Bitmap solution = Bitmap.createBitmap(pixels, mazetrix[0].length, mazetrix.length, Bitmap.Config.ARGB_8888);
        solution = Bitmap.createScaledBitmap(
                solution,
                solution.getWidth() * SOLVING_SCALE_FACTOR,
                solution.getHeight() * SOLVING_SCALE_FACTOR,
                true
        );

        // Overlay the image
        Bitmap out = putOverlay(image, solution, mazeCorner.x, mazeCorner.y);

        Log.i(TAG, "drawSolution: Image: " + image);
        Log.i(TAG, "drawSolution: Solution: " + solution);

        solution.recycle();
        image.recycle();
        out = Bitmap.createScaledBitmap(
                out,
                out.getWidth() / VIEW_SCALE_FACTOR,
                out.getHeight() / VIEW_SCALE_FACTOR,
                true
        );

        imageView.setImageBitmap(out);

        Log.i(TAG, "drawSolution: Out img dims: " + out.getWidth() + ", " + out.getHeight());

        // Sending Real life instructions to a Method which will send it to Arduino

        realLifeInstructions(startToEndPath);
    }


    // Overlays on bitmap on top of another.
    // The base image
    // The overlaid image
    // The x-coordinate of where the top left corner of overlay is placed onto base
    // The y-coordinate of where the top left corner of overlay is placed onto base
    // The bitmap of the overlay imaged on top of the base

    private Bitmap putOverlay(Bitmap base, Bitmap overlay, int x, int y) {
        // Copy the bmp to ensure that it's mutable
        Bitmap baseCpy = base.copy(Bitmap.Config.ARGB_8888, true);

        Paint p = new Paint();
        p.setColor(Color.RED);

        // Draw the solution
        Canvas canvas = new Canvas(baseCpy);
        canvas.drawBitmap(overlay, x, y, null);

        return baseCpy;
    }


    // Converts a 2D array into a 1D array.
    // The inputted 2D array to be converted to 1D
    // The 1D version of the inputted 2D array

    private int[] get1DArray(int[][] arr) {
        int[] ret = new int[arr.length * arr[0].length];
        int count = 0;

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                ret[count] = arr[i][j];
                count += 1;
            }
        }
        return ret;
    }


    // Rotates a bitmap image by a specified angle.
    // The bitmap to be rotated
    // The angle to rotate the bitmap at
    // The rotated bitmap

    private static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void displayMat(Mat mat) {
        Bitmap bmp = CVUtils.mat2BMP(mat);
        if (bmp != null)
            imageView.setImageBitmap(bmp);
        else
            Log.e(TAG, "displayMat: Bitmap is null!");
    }

    private void realLifeInstructions(ArrayList RLI) {
        int[] reallifeInstructions = new int[RLI.size()];


        for (int i = 0; i < reallifeInstructions.length; i++) {
            reallifeInstructions[i] = (int) RLI.get(i);
        }
        String realLifeInstructionsString = Arrays.toString(reallifeInstructions);
        Log.e("RLI", realLifeInstructionsString);
        RealLifeInstructionsReady(reallifeInstructions);
    }

    // Passing real-life instructions to a global array for free of use
    private void RealLifeInstructionsReady(int[] realLifeInstructions) {

        realLifeInstructionsGlobal = realLifeInstructions;

    }

    // Sending instructions with 2 seconds ( to prevent buffer over-flow) delay to Arduino
    private void sendInstructionsToArduino() {


        try {


            count += 1;

            if (count < realLifeInstructionsGlobal.length) {

                Log.e("Values", Integer.toString(realLifeInstructionsGlobal[count]));

                String directions = Integer.toString(realLifeInstructionsGlobal[count]);
                BTsocket.getOutputStream().write(directions.getBytes());

                handle.postDelayed(r, 2000);

            } else {
                count = -1;
                value = 0;

                handle.removeCallbacks(r);
            }
        } catch (Exception e) {
            msg(e.toString());
        }


    }


    // Bluetooth Connectivity establish here

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

            progressDialog = ProgressDialog.show(SolutionActivity.this, "Hold on", "Connecting");


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


}
