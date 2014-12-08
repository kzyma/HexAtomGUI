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
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Vector;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.view.View;
import android.content.SharedPreferences;
import android.widget.ToggleButton;

/**
 * @brief Activity for altering atom probabilities.
 *
 * HexAtomProbabilityActivity is used for setting atom probabilities. This is bound to service
 * ServerProxy for 'talking' with the Hex Atom server, which is used to send messages using
 * ServerProxy.sendMessage(message) and updated from the server using callback routines which
 * may be registered.
 */

public class HexAtomProbabilityActivity extends Activity
{
    private static final String TAG = "HexAtomProbabilityActivity";
    LinearLayout hexAtomProbabilityLayout;
    Vector<AtomSeekBar> probabilityBars;
    Vector<TextView> probabilityTexts;
    Vector<String> messageStrings;
    Vector<Vector<Integer>> probabilityValues;
    Vector<ToggleButton> atomSelectors;

    private static ServerProxy serverProxy;
    private Intent spIntent;
    private static boolean serverBound = false;
    RadioGroup atomSelectorGroup;
    int atomSelected;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_hex_atom_probablity);

        atomSelectorGroup = (RadioGroup)findViewById(R.id.SelectorGroup);
        this.atomSelected = 0;

        this.atomSelectors = new Vector();
        atomSelectors.add((ToggleButton)findViewById(R.id.a0));
        atomSelectors.add((ToggleButton)findViewById(R.id.a1));
        atomSelectors.add((ToggleButton)findViewById(R.id.a2));
        atomSelectors.add((ToggleButton)findViewById(R.id.a3));
        atomSelectors.add((ToggleButton)findViewById(R.id.a4));
        atomSelectors.add((ToggleButton)findViewById(R.id.a5));
        atomSelectors.add((ToggleButton)findViewById(R.id.a6));
        atomSelectors.add((ToggleButton)findViewById(R.id.a7));
        atomSelectors.add((ToggleButton)findViewById(R.id.a8));
        atomSelectors.add((ToggleButton)findViewById(R.id.a9));
        atomSelectors.add((ToggleButton)findViewById(R.id.a10));
        atomSelectors.add((ToggleButton)findViewById(R.id.a11));

        atomSelectorGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            public void onCheckedChanged(RadioGroup group, int checkedId){
                int index = group.indexOfChild
                        (group.findViewById(group.getCheckedRadioButtonId()));
                for (int j = 0; j < atomSelectorGroup.getChildCount(); j++) {
                    final ToggleButton view = (ToggleButton) atomSelectorGroup.getChildAt(j);
                    if(index == j){
                        view.setChecked(true);
                        atomSelected = j;
                        view.setAlpha(new Float(1));
                        updateProbabilities(j);
                    }else{
                        view.setChecked(false);
                        view.setAlpha(new Float(0.3));
                    }
                }
            }
        });

        probabilityBars = new Vector();
        probabilityTexts = new Vector();
        messageStrings = new Vector();

        hexAtomProbabilityLayout = (LinearLayout)findViewById(R.id.HexAtomProbabilityLayout);

        probabilityBars.add((AtomSeekBar)findViewById(R.id.pcd));
        probabilityBars.add((AtomSeekBar)findViewById(R.id.pmt));
        probabilityBars.add((AtomSeekBar)findViewById(R.id.pde));
        probabilityBars.add((AtomSeekBar)findViewById(R.id.pst));
        probabilityBars.add((AtomSeekBar)findViewById(R.id.pdf));
        probabilityBars.add((AtomSeekBar)findViewById(R.id.pfi));
        probabilityBars.add((AtomSeekBar)findViewById(R.id.pft));
        probabilityBars.add((AtomSeekBar)findViewById(R.id.pfu));
        probabilityBars.add((AtomSeekBar)findViewById(R.id.pvf));
        probabilityBars.add((AtomSeekBar)findViewById(R.id.pxu));

        messageStrings.add("pcd");
        messageStrings.add("pmt");
        messageStrings.add("pde");
        messageStrings.add("pst");
        messageStrings.add("pdf");
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
        probabilityTexts.add((TextView)findViewById(R.id.PFIProgress));
        probabilityTexts.add((TextView)findViewById(R.id.PFTProgress));
        probabilityTexts.add((TextView)findViewById(R.id.PFUProgress));
        probabilityTexts.add((TextView)findViewById(R.id.VF1Progress));
        probabilityTexts.add((TextView)findViewById(R.id.PXUProgress));

        hexAtomProbabilityLayout.setOnTouchListener(new OnSwipeListener(this,null)
        {
            @Override
            public void onSwipeRight()
            {
                onBackPressed();
            }
        });

        this.probabilityValues = new Vector<Vector<Integer>>();

        for(int i=0;i<12;i++){
            Vector<Integer> temp = new Vector();
            for(int j=0;j<this.probabilityBars.size();j++){
                temp.add(new Integer(0));
            }
            this.probabilityValues.add(temp);
        }

        //bind ServerProxy bound service.
        spIntent = new Intent(this,ServerProxy.class);
        bindService(spIntent, serverConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart(){
        super.onStart();
        onToggle(findViewById(R.id.a0));
    }

    @Override
    protected void onRestart(){super.onRestart();}
    @Override
    protected void onResume(){
        super.onResume();
        SharedPreferences prefs = this.getSharedPreferences(
                "HexAtomTemp", Context.MODE_PRIVATE);
        for(int i=0;i<this.probabilityValues.size();i++) {
            for(int j=0;j<this.probabilityValues.get(i).size();j++) {
                int temp = prefs.getInt(Integer.toString(i)+Integer.toString(j),0);
                this.probabilityValues.get(i).set(j,temp);
            }
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
        SharedPreferences prefs = this.getSharedPreferences(
                "HexAtomTemp", Context.MODE_PRIVATE);
        SharedPreferences.Editor hexAtomConfigEditor = prefs.edit();
        for(int i=0;i<this.probabilityValues.size();i++) {
            for(int j=0;j<this.probabilityValues.get(i).size();j++) {
                hexAtomConfigEditor.putInt(Integer.toString(i) + Integer.toString(j)
                        , this.probabilityValues.get(i).get(j));
            }
        }
        hexAtomConfigEditor.apply();
    }
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

    /**
     * Save the state of probability data for atoms.
     * @param savedInstanceState
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        for(int i=0;i<this.probabilityValues.size();i++) {
            for(int j=0;j<this.probabilityValues.get(i).size();j++) {
                savedInstanceState.putInt(Integer.toString(i)+Integer.toString(j)
                        , this.probabilityValues.get(i).get(j));
            }
        }
        super.onSaveInstanceState(savedInstanceState);

    }

    /**
     * Get the saved state of probability data of atoms.
     * @param savedInstanceState
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        for(int i=0;i<this.probabilityValues.size();i++) {
            for(int j=0;j<this.probabilityValues.get(i).size();j++) {
                int temp = savedInstanceState.getInt(Integer.toString(i)+Integer.toString(j));
                this.probabilityValues.get(i).set(j,temp);
            }
        }
    }

    /**
     * Toggle a button on, and all others off. This is a helper function so that atomSelectorGroup
     * can act like a radio button group, where if one is toggled 'on' all others should be off.
     * @param view
     */
    public void onToggle(View view) {
        atomSelectorGroup.check(view.getId());
        ((ToggleButton)view).setChecked(true);
    }

    /**
     * Listener for updating probability SeekBar's. Handles sending a message when tracking stops
     * to the server to update probability for selected (this.atomSelectorGroup) atom. Also
     * handles updating the display (this.probabilityText) with current probability value.
     * Values may range from 0 to 1 inclusive.
     */
    SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener()
    {
        int progress = 0;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            this.progress = progress;

            for (int i = 0; i < probabilityBars.size(); i++) {
                if (seekBar == probabilityBars.elementAt(i)) {
                    probabilityTexts.elementAt(i).setText(Integer.toString(progress) + "%");
                    probabilityValues.get(atomSelected).set(i, this.progress);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar){
            if (seekBar instanceof AtomSeekBar){
                ((AtomSeekBar)seekBar).setIsUpdating(true);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar){
            for(int i=0;i<probabilityBars.size();i++){
                if(seekBar == probabilityBars.elementAt(i)){
                    float prog = (((float)this.progress) / (float)100.0);
                    String prob = messageStrings.elementAt(i);
                    probabilityValues.get(atomSelected).set(i,this.progress);
                    serverProxy.sendMessage(prob+Integer.toString(atomSelected)+"=" +
                            Float.toString(prog));
                }
            }
            if (seekBar instanceof AtomSeekBar){
                ((AtomSeekBar)seekBar).setIsUpdating(false);
            }
        }
    };

    /**
     * Helper function for setting probability sliders (this.probabilityBars) to the correct
     * saved value. Used when an atom is toggled.
     * @param atomNumber
     */
    public void updateProbabilities(int atomNumber){
        for(int i=0;i<this.probabilityValues.elementAt(atomNumber).size();i++){
            int num =  this.probabilityValues.elementAt(atomNumber).elementAt(i);
            this.probabilityBars.elementAt(i).setProgress(num);
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
            HexAtomProbabilityActivity.serverProxy = binder.getService();
            HexAtomProbabilityActivity.serverBound = true;

            if (HexAtomProbabilityActivity.serverProxy == null)
            {
                Log.i("ConnectToServerActivity", "Bind to service ServerProxy was unsuccessful.");
            }else{
                //register callback's for remote listening
                HexAtomProbabilityActivity.serverProxy.probabilityRegister
                        (probabilityBars,atomSelectorGroup);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className)
        {
            HexAtomProbabilityActivity.serverBound = false;
        }
    };
}
