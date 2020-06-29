package com.mazesolver.androidapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

// Purpose: Selection dots for the bounds of the maze


public class Dot extends View {
    private int RADIUS;
    private int mPosX;
    private int mPosY;
    private Rect boundingBox;  // The area that the user can interact with the dot
    private int feather;  // Allows for a bigger bounding box than the actual shape
    private Rect boundingScreenRect; // The rect that defines where the dot can move
    private Paint myPaint;
    private Point mLastTouch = new Point();


    private static String TAG = "Dot";


    // Initializes the Dot.
    // Context The context.
    // attrs Any attributes.

    public Dot(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Initializes the Dot.
    // The x coordinate of the center of the dot
    // The y coordinate of the center of the dot
    // The radius of the dot

    public Dot(Context context, AttributeSet attrs, int x, int y, int radius) {
        super(context, attrs);

        int maxWidth = getResources().getDisplayMetrics().widthPixels;
        int maxHeight = getResources().getDisplayMetrics().heightPixels;
        boundingScreenRect = new Rect(0, 0, maxWidth, maxHeight);

        mPosX = x;
        mPosY = y;
        this.RADIUS = radius;
        feather = (int) (RADIUS * 2.2);

        boundingBox = new Rect(x - (RADIUS + feather), y - (RADIUS + feather),
                x + (RADIUS + feather),
                y + (RADIUS + feather));

        myPaint = new Paint();
        myPaint.setColor(Color.argb(255, 84, 110, 122));
        myPaint.setAntiAlias(true);
    }

    // Moves the dot when it's touched and dragged.
    // The touch event.

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // Remember where we last started for dragging
                mLastTouch.set((int) event.getX(), (int) event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                final Point touch = new Point((int) event.getX(), (int) event.getY());

                // Calculate the change in x and y from the dragging
                int newX = mPosX + touch.x - mLastTouch.x;
                int newY = mPosY + touch.y - mLastTouch.y;

                // If it's within the bounds, translate the dot to those coordinates
                if (boundingScreenRect.contains(newX, newY)) {
                    mPosX = newX;
                    mPosY = newY;

                    boundingBox.offsetTo(mPosX - (RADIUS + feather), mPosY - (RADIUS + feather));

                    // Set this as the new "last" touch
                    mLastTouch = touch;
                }
                break;
        }
        return true;
    }


    // Draws the Dot
    // The canvas to draw the Dot on.

    public void draw(Canvas canvas) {
        super.draw(canvas);

        canvas.drawCircle(mPosX, mPosY, RADIUS, myPaint);

    }


    // Sets the bounds in which the Dot can move
    // The width that the dots may move from (0 to width)
    // The height that the dots may move from (0 to height)

    public void setBounds(int width, int height) {
        boundingScreenRect = new Rect(0, 0, width, height);
    }


    // Gets the location of the Dot.
    // The location of the Dot

    public Point getLocation() {
        return new Point(mPosX, mPosY);
    }


    // See if the feathered bounding box for the Dot contains a given point.
    // The x-coordinate
    // The x-coordinate
    // Whether or not the dot's bounding box contains the coordinates

    public boolean contains(int x, int y) {
        return boundingBox.contains(x, y);
    }
}