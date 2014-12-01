package com.hexatom.gui.hexatomgui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Vector;

//onSaveInstanceState Bundle research

public class HexAtomProbabilityActivity extends Activity
{
    private static final String TAG = "HexAtomProbabilityActivity";
    LinearLayout hexAtomProbabilityLayout;
    Vector<SeekBar> probabilityBars;
    Vector<TextView> probabilityTexts;
    Vector<String> messageStrings;

    private static ServerProxy serverProxy;
    private Intent spIntent;
    private static boolean serverBound = false;
    RadioGroup atomSelector;
    int atomSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_hex_atom_probablity);

        atomSelector = (RadioGroup)findViewById(R.id.SelectorGroup);
        this.atomSelected = 0;
        ((RadioButton)findViewById(R.id.defaultButton)).setChecked(true);

        probabilityBars = new Vector();
        probabilityTexts = new Vector();
        messageStrings = new Vector();

        hexAtomProbabilityLayout = (LinearLayout)findViewById(R.id.HexAtomProbabilityLayout);

        probabilityBars.add((SeekBar)findViewById(R.id.pcd));
        probabilityBars.add((SeekBar)findViewById(R.id.pmt));
        probabilityBars.add((SeekBar)findViewById(R.id.pde));
        probabilityBars.add((SeekBar)findViewById(R.id.pst));
        probabilityBars.add((SeekBar)findViewById(R.id.pdf));
        probabilityBars.add((SeekBar)findViewById(R.id.pmu));
        probabilityBars.add((SeekBar)findViewById(R.id.pfi));
        probabilityBars.add((SeekBar)findViewById(R.id.pft));
        probabilityBars.add((SeekBar)findViewById(R.id.pfu));
        probabilityBars.add((SeekBar)findViewById(R.id.pvf));
        probabilityBars.add((SeekBar)findViewById(R.id.pxu));

        messageStrings.add("pcd");
        messageStrings.add("pmt");
        messageStrings.add("pde");
        messageStrings.add("pst");
        messageStrings.add("pdf");
        messageStrings.add("pmu");
        messageStrings.add("pfi");
        messageStrings.add("pft");
        messageStrings.add("pfu");
        messageStrings.add("pvf");
        messageStrings.add("pxu");

        for(int i=0;i<probabilityBars.size();i++){
            probabilityBars.elementAt(i).setOnSeekBarChangeListener(seekBarListener);
        }

        probabilityTexts.add((TextView)findViewById(R.id.PCDProgress));
        probabilityTexts.add((TextView)findViewById(R.id.PMTProgress));
        probabilityTexts.add((TextView)findViewById(R.id.PDEProgress));
        probabilityTexts.add((TextView)findViewById(R.id.PSTProgress));
        probabilityTexts.add((TextView)findViewById(R.id.PDFProgress));
        probabilityTexts.add((TextView)findViewById(R.id.PMUProgress));
        probabilityTexts.add((TextView)findViewById(R.id.PFIProgress));
        probabilityTexts.add((TextView)findViewById(R.id.PFTProgress));
        probabilityTexts.add((TextView)findViewById(R.id.PFUProgress));
        probabilityTexts.add((TextView)findViewById(R.id.VF1Progress));
        probabilityTexts.add((TextView)findViewById(R.id.PXUProgress));

        hexAtomProbabilityLayout.setOnTouchListener(new OnSwipeListener(this)
        {
            @Override
            public void onSwipeRight()
            {
                onBackPressed();
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

    SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener()
    {
        int progress = 0;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean b)
        {
            this.progress = progress;

            for(int i=0;i<probabilityBars.size();i++){
                if(seekBar == probabilityBars.elementAt(i)){
                    probabilityTexts.elementAt(i).setText(Integer.toString(progress) + "%");
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
            for(int i=0;i<probabilityBars.size();i++){
                if(seekBar == probabilityBars.elementAt(i)){
                    float prog = (((float)this.progress) / (float)100.0);
                    String prob = messageStrings.elementAt(i);
                    Log.e("",prob+Integer.toString(atomSelected)+"=" +
                            Float.toString(prog));
                    serverProxy.sendMessage(prob+Integer.toString(atomSelected)+"=" +
                            Float.toString(prog));
                }
            }
        }
    };

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
            HexAtomProbabilityActivity.serverProxy = binder.getService();
            HexAtomProbabilityActivity.serverBound = true;

            if (HexAtomProbabilityActivity.serverProxy == null)
            {
                Log.i("ConnectToServerActivity", "Bind to service ServerProxy was unsuccessful.");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className)
        {
            HexAtomProbabilityActivity.serverBound = false;
        }
    };
}
