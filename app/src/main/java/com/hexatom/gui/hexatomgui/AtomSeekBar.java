package com.hexatom.gui.hexatomgui;

/**
 * @author  Ken Zyma
 * @version 1.0
 * @since   2014-12-6
 */

import android.widget.SeekBar;
import android.content.Context;
import android.util.AttributeSet;

/**
 * @brief AtomSeekBar extends SeekBar for remote updating.
 *
 * AtomSeekBar extends Button for remote updating with ServerProxy using the
 * ServerProxy.GuiUpdateCallback interface.
 */
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

    /**
     * Set if AtomSeekBar is currently being updated. This may be used to stop the function
     * update() from updating and overriding the value when it is in the process of being  changed
     * (possibly by the slider or other means).
     * @param value True if the SeekBar is currently being updated, false otherwise.
     */
    public void setIsUpdating(boolean value){
        isUpdating = value;
    }

    /**
     * Return the state of AtomSeekBar
     * @return True if AtomSeekBar is being updated, false otherwise.
     */
    private boolean isUpdating(){
        return isUpdating;
    }

    /**
     * Update AtomSeekBar to display the value being passed in if it's not in a state
     * of already being updated. This function is overridden from GuiUpdateCallback and is
     * used as the callback.
     * @param value
     */
    @Override
    public void update(String value){
        if(!this.isUpdating()){
            this.setProgress(((int) Float.parseFloat(value)));
        }
    }

}