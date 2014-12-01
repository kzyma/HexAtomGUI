package com.hexatom.gui.hexatomgui;

/**
 * Created by kaz002 on 11/26/14.
 */

import android.widget.SeekBar;
import android.content.Context;
import android.util.AttributeSet;

public class AtomSeekBar extends SeekBar implements ServerProxy.GuiUpdateCallback {

    private boolean isUpdating = false;

    public AtomSeekBar(Context context) {
        super(context);
    }

    public AtomSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AtomSeekBar(Context context, AttributeSet attrs, int defStyle) {
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