package com.hexatom.gui.hexatomgui;

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
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;


public class HexAtomGenerateActivity extends Activity
{
    private static final String TAG = "HexAtomGenerateActivity";
    private View swipeGestureView, mapView;
    private Button sendDiameterButton, resetButton;
    private ToggleButton randomToggleButton;
    private AtomSeekBar tempoBar;
    private TextView tempoProgress;
    private RadioGroup selectorGroup;

    ScaleGestureDetector scaleGestureDetector;

    private static ServerProxy serverProxy;
    private Intent spIntent;
    private static boolean serverBound = false;

    private final static String[] atomArray = {"a0", "a1", "a2", "a3", "a4", "a5",
            "a6", "a7", "a8", "a9", "a10", "a11"};
    private String radioSelection, delay = "0";
    private float scale;
    private boolean atomGenMode;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Log.d(TAG, "inOnCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_hex_atom_generate);

        selectorGroup = (RadioGroup)findViewById(R.id.SelectorGroup);
        mapView = findViewById(R.id.MapView);
        sendDiameterButton = (Button)findViewById(R.id.SendDiameterButton);
        tempoBar = (AtomSeekBar)findViewById(R.id.TempoBar);
        tempoProgress = (TextView)findViewById(R.id.TempoProgress);
        swipeGestureView = findViewById(R.id.SwipeGestureView);

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        selectorGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                radioSelection = ((RadioButton)findViewById(checkedId)).getText().toString();
                if(checkedId == R.id.genDiameter)
                {
                    atomGenMode = false;
                    sendDiameterButton.setVisibility(View.VISIBLE);
                }
                else
                {
                    atomGenMode = true;
                    if(sendDiameterButton.getVisibility() == View.VISIBLE)
                        sendDiameterButton.setVisibility(View.GONE);
                    radioSelection = atomArray[selectorGroup.indexOfChild(findViewById(checkedId))];
                }
            }
        });

        mapView.setOnTouchListener(new View.OnTouchListener()
        {
            float startX, startY, endX, endY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                if (atomGenMode)
                {
                    if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN)
                    {
                        startX = motionEvent.getX() - mapView.getWidth() / 2;
                        startY = mapView.getHeight() / 2 - motionEvent.getY();
                    }
                    else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP)
                    {
                        endX = motionEvent.getX() - mapView.getWidth() / 2;
                        endY = mapView.getHeight() / 2 - motionEvent.getY();
                        generateAtom(startX, startY, endX, endY);
                    }
                }
                else
                    scaleGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        sendDiameterButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                serverProxy.sendMessage("d" + sendDiameterButton.getText().toString());
            }
        });

        tempoBar.setMax(120);

        tempoBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            int progress = 1;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                progress = i;
                tempoProgress.setText(Integer.toString(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tempoBar.setIsUpdating(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                int min = 1;
                if(this.progress >= 1) {
                    serverProxy.sendMessage("t" + Integer.toString(seekBar.getProgress()));
                }else{
                    serverProxy.sendMessage("t1");
                }
                tempoBar.setIsUpdating(false);
            }
        });

        swipeGestureView.setOnTouchListener(new OnSwipeListener(this)
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
    protected void onStart(){super.onStart();}
    @Override
    protected void onRestart(){super.onRestart();}
    @Override
    protected void onResume(){super.onResume();}
    @Override
    protected void onPause(){super.onPause();}
    @Override
    protected void onStop(){super.onStop();}

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

    private void generateAtom(float startX, float startY, float endX, float endY)
    {
        String direction = "x";
        if((Math.abs(endX - startX) < 50) && (Math.abs(endY - startY) < 50))
            direction = "x";
        else
        {
            float slope = determineSlope(startX, startY, endX, endY);
            if(slope < -2 || slope > 2)
            {
                if(endY > startY)
                    direction = "n";
                else
                    direction = "s";
            }
            else if(slope > .5 && slope < 2)
            {
                if(endY > startY)
                    direction = "ne";
                else
                    direction = "sw";
            }
            else if(slope < .5 && slope > -.5)
            {
                if(endX > startX)
                    direction = "ne";
                else
                    direction = "nw";
            }
            else if(slope < -.5 && slope > -2)
            {
                if(endY > startY)
                    direction = "nw";
                else
                    direction = "se";
            }

        }
        serverProxy.sendMessage(radioSelection + direction + delay);
    }

    private float determineSlope(float startX, float startY, float endX, float endY)
    {
        return ((endY - startY)/(endX - startX));
    }

    private void loadProbabilityActivity()
    {
        Intent genActivityIntent = new Intent(this, HexAtomProbabilityActivity.class);
        startActivity(genActivityIntent);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            scale *= detector.getScaleFactor();
            scale = Math.max(3, Math.min(scale, 101));
            int rounded = Math.round(scale);
            if(rounded % 2 == 0)
                rounded += 1;
            sendDiameterButton.setText(String.valueOf(rounded));
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

            //Generate Callbacks
            HexAtomGenerateActivity.serverProxy.tempoRegister(tempoBar);

            if (HexAtomGenerateActivity.serverProxy == null)
            {
                Log.i("ConnectToServerActivity", "Bind to service ServerProxy was unsuccessful.");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className)
        {
            HexAtomGenerateActivity.serverBound = false;
        }
    };
}