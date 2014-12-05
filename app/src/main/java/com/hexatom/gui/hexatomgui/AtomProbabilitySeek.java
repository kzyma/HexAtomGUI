package com.hexatom.gui.hexatomgui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.SeekBar;

import java.util.Vector;

/**
 * Created by kaz002 on 12/2/14.
 */
public class AtomProbabilitySeek extends SeekBar implements ServerProxy.GuiUpdateCallback{

    private boolean isUpdating = false;

    public AtomProbabilitySeek(Context context) {
        super(context);
    }

    public AtomProbabilitySeek(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AtomProbabilitySeek(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setIsUpdating(boolean value){
        isUpdating = value;
    }

    private boolean isUpdating(){
        return isUpdating;
    }

    @Override
    public void update(String value){
        if(!this.isUpdating()){
            this.setProgress(((int) Float.parseFloat(value)));
        }
    }

}


