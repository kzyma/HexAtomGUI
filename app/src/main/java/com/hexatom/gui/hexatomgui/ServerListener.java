package com.hexatom.gui.hexatomgui;

/**
 * Created by kaz002 on 11/26/14.
 */

import android.content.Context;
import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;

public class ServerListener implements OSCListener {
    private static ServerProxy parent;

    //This constructor gets the context from the previous activity, so that the previous
    //activitie's context may be used within this activity.
    private static Context context;
    ServerListener(Context c){
        context = c;
        parent = (ServerProxy) context;
    }

    //Receive an incoming OSC message from the HexAtom server and print in to the user in
    //the main user interface thread. This is accomplished using the Android runOnUiThread
    //method.
    public void acceptMessage(java.util.Date time, OSCMessage message) {

        class OSCRunnable implements Runnable{
            final OSCMessage msg;
            final java.util.Date time;

            OSCRunnable(java.util.Date time,OSCMessage temp){
                msg = temp;
                this.time = time;
            }

            //This is the code which actually runs on the main User Interface thread. This simply
            //bundles up the data from the message and displays it to the user via an Android toast.
            public void run(){
                parent.updateViews(time,msg);
            }
        }

        //The code below allows each OSC message to be displayed on the user interface.
        //The runOnUiThread() method runs the code in the run() method, within the runnable
        //object passed to it, on the main thread controlling the user interface. Without
        //this code, the app will crash since any code that attempts to access the UI
        //must access the UI through the main thread of operation.
        parent.runOnUiThread(new OSCRunnable(time,message));

    }
}
