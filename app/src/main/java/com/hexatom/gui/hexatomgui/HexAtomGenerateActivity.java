package com.hexatom.gui.hexatomgui;

/**
 * @author  Cory Ma & Ken Zyma & David Day
 * @version 1.0
 * @since   2014-12-5
 */

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.view.View.OnClickListener;

/**
 * @brief Activity for generating atoms.
 *
 * HexAtomGenerateActivity is the activity where a user may generate atoms and control parameters
 * which have to do with the generation of atoms. This is bound to service ServerProxy for 'talking'
 * with the Hex Atom server, which is used to send messages using ServerProxy.sendMessage(message)
 * and updated from the server using callback routines which may be registered.
 */
public class HexAtomGenerateActivity extends Activity
{
    private static final String TAG = "HexAtomGenerateActivity";
    private View mapView;
    private LinearLayout swipeChangeView;
    private AtomDisplayButton currentDiameterButton,maxDiameterButton;
    private RoundKnobButton tempo,erasure,rotation,quantum;
    private TextView tempoDisplay,erasureDisplay,rotateDisplay,quantumDisplay;
    private RadioGroup selectorGroup;

    ScaleGestureDetector scaleGestureDetector;
    float startX,startY,endX,endY;
    //modes of use for scale gesture
    static final int NONE = 0;
    static final int ONE_FINGER_DRAG = 1;
    static final int TWO_FINGERS_DRAG = 2;
    int mode = NONE;

    private static ServerProxy serverProxy;
    private Intent spIntent;
    private static boolean serverBound = false;

    private final static String[] atomArray = {"a0", "a1", "a2", "a3", "a4", "a5",
            "a6", "a7", "a8", "a9", "a10", "a11"};
    private float scale;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_hex_atom_generate);

        selectorGroup = (RadioGroup)findViewById(R.id.SelectorGroup);

        for (int i = 0; i < selectorGroup.getChildCount(); i++) {
            ((ToggleButton)selectorGroup.getChildAt(i)).setAlpha(new Float(0.3));
            ((ToggleButton)selectorGroup.getChildAt(i)).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v)
                {
                    if(((ToggleButton)v).isChecked()){
                        v.setAlpha(new Float(1));
                    }else{
                        v.setAlpha(new Float(0.3));
                    }
                }
            });
        }

        mapView = findViewById(R.id.MapView);
        this.currentDiameterButton = (AtomDisplayButton)findViewById(R.id.currentDiameterButton);
        this.maxDiameterButton = (AtomDisplayButton)findViewById(R.id.maxDiameterButton);
        swipeChangeView = (LinearLayout)findViewById(R.id.changeView_LL);

        this.tempo = (RoundKnobButton)findViewById(R.id.tempo);
        this.tempoDisplay = (TextView)findViewById(R.id.tempo_display);
        this.rotation =(RoundKnobButton)findViewById(R.id.rotation);
        this.rotateDisplay = (TextView)findViewById(R.id.rotate_display);
        this.erasure = (RoundKnobButton)findViewById(R.id.erasure);
        this.erasureDisplay = (TextView)findViewById(R.id.erasure_display);
        this.quantum = (RoundKnobButton)findViewById(R.id.quantum);
        this.quantumDisplay = (TextView)findViewById(R.id.quantum_display);
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        this.tempo.SetListener(new RoundKnobButton.RoundKnobButtonListener() {
           @Override
           public void onStateChange(boolean newstate){

           }
           @Override
           public void onRotate(int percentage){
               if(tempoDisplay != null) {
                   tempoDisplay.setText(Integer.toString(percentage));
               }
           }
       });
        this.rotation.SetListener(new RoundKnobButton.RoundKnobButtonListener() {
            @Override
            public void onStateChange(boolean newstate){

            }
            @Override
            public void onRotate(int percentage){
                if(rotateDisplay != null) {
                    rotateDisplay.setText(Integer.toString(percentage));
                }
            }
        });
        this.quantum.SetListener(new RoundKnobButton.RoundKnobButtonListener() {
            @Override
            public void onStateChange(boolean newstate){

            }
            @Override
            public void onRotate(int percentage){
                if(quantumDisplay != null) {
                    quantumDisplay.setText(Integer.toString(percentage));
                }
            }
        });
        this.erasure.SetListener(new RoundKnobButton.RoundKnobButtonListener() {
            @Override
            public void onStateChange(boolean newstate){

            }
            @Override
            public void onRotate(int percentage){
                if(erasureDisplay != null) {
                    erasureDisplay.setText(Integer.toString(percentage));
                }
            }
        });

        mapView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    //start event
                    mode = ONE_FINGER_DRAG;
                    startX = motionEvent.getX() - mapView.getWidth() / 2;
                    startY = mapView.getHeight() / 2 - motionEvent.getY();
                } else if((motionEvent.getActionMasked() == MotionEvent.ACTION_UP) ||
                (motionEvent.getActionMasked() == MotionEvent.ACTION_POINTER_UP)){
                    endX = motionEvent.getX() - mapView.getWidth() / 2;
                    endY = mapView.getHeight() / 2 - motionEvent.getY();
                    //if ACTION_POINTER_DOWN was not called this is a one finger event.
                    if(mode == ONE_FINGER_DRAG){
                        generateAtom(startX, startY, endX, endY);
                    }
                    maxDiameterButton.setIsUpdating(false);
                    serverProxy.sendMessage("d"+maxDiameterButton.getText());
                } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                    //swipe event
                    mode = TWO_FINGERS_DRAG;
                    maxDiameterButton.setIsUpdating(true);
                } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE){
                    if(mode == TWO_FINGERS_DRAG){
                        scaleGestureDetector.onTouchEvent(motionEvent);
                    }
                }

                return true;
            }
        });

        swipeChangeView.setOnTouchListener(new OnSwipeListener(this,null)
        {
            @Override
            public void onSwipeLeft()
            {
                loadProbabilityActivity();
            }
        });

        //bind ServerProxy bound service.
        spIntent = new Intent(this,ServerProxy.class);
        bindService(spIntent, serverConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy()
    {
        //unbind ServerProxy bound service.
        if (this.serverBound)
        {
            unbindService(serverConnection);
            serverBound = false;
        }
        super.onDestroy();
    }

    /**
     * @brief Generate hex atom of selected type.
     *
     * Generate hex atom of selected type (this.selectorGroup) at location startX,startY, in the
     * direction of slope between start and end points, and with the rotation and quantum number
     * set by this.rotation RoundKnobButtton and this.quantum RoundKnobButton, respectively.
     *
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     */
    private void generateAtom(float startX, float startY, float endX, float endY)
    {
        String direction = "x";
        if((Math.abs(endX - startX) < 50) && (Math.abs(endY - startY) < 50))
            direction = "x";
        else
        {
            float slope = determineSlope(startX, startY, endX, endY);
            if(slope < -2.5 || slope > 2.5)
            {
                if(endY > startY)
                    direction = "n";
                else
                    direction = "s";
            }
            else if(slope > 0 && slope < 2.5)
            {
                if(endY > startY)
                    direction = "ne";
                else
                    direction = "sw";
            }
            else if(slope < 0 && slope > -2.5)
            {
                if(endY > startY)
                    direction = "nw";
                else
                    direction = "se";
            }

        }

        for(int i=0;i<this.selectorGroup.getChildCount();i++) {
            if(((ToggleButton)this.selectorGroup.getChildAt(i)).isChecked()) {
                String radioSelection = atomArray[i];
                String message = radioSelection + direction + "0";
                //Log.e("",message);
                serverProxy.sendMessage(message);
            }
        }
    }

    /**
     * Given two points, start (x,y) and end (x,y), return the slope.
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @return slope of points
     */
    private float determineSlope(float startX, float startY, float endX, float endY)
    {
        return ((endY - startY)/(endX - startX));
    }

    /**
     * Load and start HexAtomProbabilityActivity
     */
    private void loadProbabilityActivity()
    {
        Intent genActivityIntent = new Intent(this, HexAtomProbabilityActivity.class);
        startActivity(genActivityIntent);
    }

    /**
     * For two finger pinching/zooming of MapView, update the sendDiameterButton.
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            scale *= detector.getScaleFactor();
            scale = Math.max(3, Math.min(scale, 250));
            int rounded = Math.round(scale);
            if(rounded % 2 == 0)
                rounded += 1;
            maxDiameterButton.setText(String.valueOf(rounded));
            return true;
        }
    }

    /**
     * Defines callbacks for server binding, passed to bindService()
     */
    private ServiceConnection serverConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            // We've bound to ServerBinder, cast the IBinder and get ServerBinder instance
            ServerProxy.ServerBinder binder = (ServerProxy.ServerBinder) service;
            HexAtomGenerateActivity.serverProxy = binder.getService();
            HexAtomGenerateActivity.serverBound = true;

            if (HexAtomGenerateActivity.serverProxy == null)
            {
                Log.i("ConnectToServerActivity", "Bind to service ServerProxy was unsuccessful.");
            }else{
                //Generate Callbacks
                //HexAtomGenerateActivity.serverProxy.tempoRegister(tempoBar);
                HexAtomGenerateActivity.serverProxy.maxDiameterRegister(maxDiameterButton);
                HexAtomGenerateActivity.serverProxy.currentDiameterRegister(currentDiameterButton);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className)
        {
            HexAtomGenerateActivity.serverBound = false;
        }
    };
}