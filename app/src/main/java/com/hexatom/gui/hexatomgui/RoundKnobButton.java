package com.hexatom.gui.hexatomgui;

/**
File:              RoundKnobButton
Version:           1.0.0
Release Date:      November, 2013
License:           GPL v2
Description:       A round knob button to control volume and toggle between two states

****************************************************************************
Copyright (C) 2013 Radu Motisan  <radu.motisan@gmail.com>

http://www.pocketmagic.net

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
****************************************************************************/

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.util.Log;

/**
 * @brief Potentiometer (pan-pot or otherwise called rotary knob) GUI element.
 *
 * RoundKnobButton is a custom GUI element to emulate a classic rotary knob.
 */
public class RoundKnobButton extends RelativeLayout implements OnGestureListener,ServerProxy.GuiUpdateCallback {

    private GestureDetector     gestureDetector;
    private float               mAngleDown , mAngleUp;
    private ImageView           ivRotor;
    private Bitmap              bmpRotorOn , bmpRotorOff;
    private boolean             mState = false;
    private int                 m_nWidth = 0, m_nHeight = 0;
    private boolean             updating = false;

    interface RoundKnobButtonListener {
        public void onStateChange(boolean newstate) ;
        public void onRotate(int percentage);
    }

    private RoundKnobButtonListener m_listener;

    public void SetListener(RoundKnobButtonListener l) {
        m_listener = l;
    }

    public void SetState(boolean state) {
        mState = state;
        ivRotor.setImageBitmap(state?bmpRotorOn:bmpRotorOff);
    }

    public RoundKnobButton(Context context) {
        super(context);
        // we won't wait for our size to be calculated, we'll just store out fixed size
        int w=125;
        int h=125;
        m_nWidth = w;
        m_nHeight = h;
        //int back = R.drawable.progress;
        int rotoron = R.drawable.rotoron;
        int rotoroff = R.drawable.rotoroff;
        // create stator
        ImageView ivBack = new ImageView(context);
        //ivBack.setImageResource(back);
        RelativeLayout.LayoutParams lp_ivBack = new RelativeLayout.LayoutParams(w,h);
        lp_ivBack.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(ivBack, lp_ivBack);
        // load rotor images
        Bitmap srcon = BitmapFactory.decodeResource(context.getResources(), rotoron);
        Bitmap srcoff = BitmapFactory.decodeResource(context.getResources(), rotoroff);
        float scaleWidth = ((float) w) / srcon.getWidth();
        float scaleHeight = ((float) h) / srcon.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        bmpRotorOn = Bitmap.createBitmap(
                srcon, 0, 0,
                srcon.getWidth(),srcon.getHeight() , matrix , true);
        bmpRotorOff = Bitmap.createBitmap(
                srcoff, 0, 0,
                srcoff.getWidth(),srcoff.getHeight() , matrix , true);
        // create rotor
        ivRotor = new ImageView(context);
        ivRotor.setImageBitmap(bmpRotorOn);
        RelativeLayout.LayoutParams lp_ivKnob = new RelativeLayout.LayoutParams(w,h);//LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp_ivKnob.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(ivRotor, lp_ivKnob);
        // set initial state
        SetState(mState);
        // enable gesture detector
        gestureDetector = new GestureDetector(getContext(), this);
    }

    public RoundKnobButton(Context context, AttributeSet attrs) {
        super(context,attrs);
        // we won't wait for our size to be calculated, we'll just store out fixed size
        int w=125;
        int h=125;
        m_nWidth = w;
        m_nHeight = h;
        //int back = R.drawable.progress;
        int rotoron = R.drawable.rotoron;
        int rotoroff = R.drawable.rotoroff;
        // create stator
        ImageView ivBack = new ImageView(context);
        //ivBack.setImageResource(back);
        RelativeLayout.LayoutParams lp_ivBack = new RelativeLayout.LayoutParams(w,h);
        lp_ivBack.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(ivBack, lp_ivBack);
        // load rotor images
        Bitmap srcon = BitmapFactory.decodeResource(context.getResources(), rotoron);
        Bitmap srcoff = BitmapFactory.decodeResource(context.getResources(), rotoroff);
        float scaleWidth = ((float) w) / srcon.getWidth();
        float scaleHeight = ((float) h) / srcon.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        bmpRotorOn = Bitmap.createBitmap(
                srcon, 0, 0,
                srcon.getWidth(),srcon.getHeight() , matrix , true);
        bmpRotorOff = Bitmap.createBitmap(
                srcoff, 0, 0,
                srcoff.getWidth(),srcoff.getHeight() , matrix , true);
        // create rotor
        ivRotor = new ImageView(context);
        ivRotor.setImageBitmap(bmpRotorOn);
        RelativeLayout.LayoutParams lp_ivKnob = new RelativeLayout.LayoutParams(w,h);//LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp_ivKnob.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(ivRotor, lp_ivKnob);
        // set initial state
        SetState(mState);
        // enable gesture detector
        gestureDetector = new GestureDetector(getContext(), this);

    }

    public RoundKnobButton(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        // we won't wait for our size to be calculated, we'll just store out fixed size
        int w=125;
        int h=125;
        m_nWidth = w;
        m_nHeight = h;
        //int back = R.drawable.progress;
        int rotoron = R.drawable.rotoron;
        int rotoroff = R.drawable.rotoroff;
        // create stator
        ImageView ivBack = new ImageView(context);
        //ivBack.setImageResource(back);
        RelativeLayout.LayoutParams lp_ivBack = new RelativeLayout.LayoutParams(w,h);
        lp_ivBack.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(ivBack, lp_ivBack);
        // load rotor images
        Bitmap srcon = BitmapFactory.decodeResource(context.getResources(), rotoron);
        Bitmap srcoff = BitmapFactory.decodeResource(context.getResources(), rotoroff);
        float scaleWidth = ((float) w) / srcon.getWidth();
        float scaleHeight = ((float) h) / srcon.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        bmpRotorOn = Bitmap.createBitmap(
                srcon, 0, 0,
                srcon.getWidth(),srcon.getHeight() , matrix , true);
        bmpRotorOff = Bitmap.createBitmap(
                srcoff, 0, 0,
                srcoff.getWidth(),srcoff.getHeight() , matrix , true);
        // create rotor
        ivRotor = new ImageView(context);
        ivRotor.setImageBitmap(bmpRotorOn);
        RelativeLayout.LayoutParams lp_ivKnob = new RelativeLayout.LayoutParams(w,h);//LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp_ivKnob.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(ivRotor, lp_ivKnob);
        // set initial state
        SetState(mState);
        // enable gesture detector
        gestureDetector = new GestureDetector(getContext(), this);
    }

    private float cartesianToPolar(float x, float y) {
        return (float) -Math.toDegrees(Math.atan2(x - 0.5f, y - 0.5f));
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) return true;
        else return super.onTouchEvent(event);
    }

    public boolean onDown(MotionEvent event) {
        float x = event.getX() / ((float) getWidth());
        float y = event.getY() / ((float) getHeight());
        mAngleDown = cartesianToPolar(1 - x, 1 - y);// 1- to correct our custom axis direction
        return true;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        float x = e.getX() / ((float) getWidth());
        float y = e.getY() / ((float) getHeight());
        mAngleUp = cartesianToPolar(1 - x, 1 - y);// 1- to correct our custom axis direction

        // if we click up the same place where we clicked down, it's just a button press
        if (! Float.isNaN(mAngleDown) && ! Float.isNaN(mAngleUp) && Math.abs(mAngleUp-mAngleDown) < 10) {
            SetState(!mState);
            if (m_listener != null) m_listener.onStateChange(mState);
        }
        return true;
    }

    public void setRotorPosAngle(float deg) {
        if (deg > 180) deg = deg - 360;
        Matrix matrix=new Matrix();
        ivRotor.setScaleType(ScaleType.MATRIX);
        matrix.postRotate((float) deg, m_nWidth/2, m_nHeight/2);//getWidth()/2, getHeight()/2);
        ivRotor.setImageMatrix(matrix);
    }

    public void setRotorPercentage(int percentage) {
        int posDegree = percentage;
        if (posDegree < 0) posDegree = 360 + posDegree;
        setRotorPosAngle(posDegree);
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float x = e2.getX() / ((float) getWidth());
        float y = e2.getY() / ((float) getHeight());
        float rotDegrees = cartesianToPolar(1 - x, 1 - y);// 1- to correct our custom axis direction
        if (! Float.isNaN(rotDegrees)) {
            // instead of getting 0-> 180, -180 0 , we go for 0 -> 360
            float posDegrees = rotDegrees;
            if (rotDegrees < 0) posDegrees = 360 + rotDegrees;
            // deny full rotation, start start and stop point, and get a linear scale
            // rotate our imageview
            setRotorPosAngle(posDegrees);
            // get a linear scale
            float scaleDegrees = rotDegrees + 360; // given the current parameters, we go from 0 to 300
            if(scaleDegrees >= 360) scaleDegrees = scaleDegrees - 360;
            //normalize to [0,1)
            int percent = (int)(((float)(scaleDegrees)/
                    ((float)360))*100);
            if (m_listener != null) m_listener.onRotate(percent);
            return true; //consumed
        } else
            return false; // not consumed
    }

    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) { return false; }

    public void onLongPress(MotionEvent e) {    }

    @Override
    public void update(String value){
        if(!mState){
            float angle = (Integer.parseInt(value)/(float)100) * 360;
            setRotorPosAngle(angle);
            if (m_listener != null) m_listener.onRotate(Integer.parseInt(value));
        }
    }


}