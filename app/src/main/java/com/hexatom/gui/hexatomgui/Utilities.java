package com.hexatom.gui.hexatomgui;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Utilities
{
	private static final String TAG = "Utilities";
	
	public static void makeToast(Context callingContext, String message)
	{
		Log.d(TAG, "In makeToast()");
		Toast.makeText(callingContext, message, Toast.LENGTH_SHORT).show();
	}
}
