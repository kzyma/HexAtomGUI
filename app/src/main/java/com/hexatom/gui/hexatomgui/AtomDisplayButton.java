package com.hexatom.gui.hexatomgui;

/**
 * @author  Ken Zyma
 * @version 1.0
 * @since   2014-12-6
 */

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.util.Log;

/**
 * @brief AtomDisplayButton extends Button for remote updating.
 *
 * AtomDisplayButton extends Button for remote updating with ServerProxy using the
 * ServerProxy.GuiUpdateCallback interface.
 */
public class AtomDisplayButton extends Button implements ServerProxy.GuiUpdateCallback {
    private boolean isUpdating = false;

    public AtomDisplayButton(Context context) {
        super(context);
    }

    public AtomDisplayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AtomDisplayButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Set if AtomDisplayButton is currently being updated. This may be used to stop the function
     * update() from updating and overriding the value when it is in the process of being  changed
     * (possibly by a slider or other means).
     * @param value True if the button is currently being updated, false otherwise.
     */
    public void setIsUpdating(boolean value){
        isUpdating = value;
    }

    /**
     * Return the state of AtomDisplayButton
     * @return True if AtomDisplayButton is being updated, false otherwise.
     */
    private boolean isUpdating(){
        return isUpdating;
    }

    /**
     * Update AtomDisplayButton to display the value being passed in if it's not in a state
     * of already being updated. This function is overridden from GuiUpdateCallback and is
     * used as the callback.
     * @param value
     */
    @Override
    public void update(String value){
        if(!this.isUpdating()){
            this.setText(value);
        }
    }
}
