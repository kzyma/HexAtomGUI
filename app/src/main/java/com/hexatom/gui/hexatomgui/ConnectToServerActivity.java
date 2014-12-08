package com.hexatom.gui.hexatomgui;

/**
 * @author  Cory Ma & Ken Zyma
 * @version 1.0
 * @since   2014-12-1
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.StrictMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @brief Activity for Connect to Server view.
 *
 * ConnectToServerActivity is the home screen for HexAtomGUI, where a user may connect to a
 * hex atom server. IP and Port are verified and a service, ServerProxy, is started which
 * handles all message i/o for the application.
 */
public class ConnectToServerActivity extends Activity
{
    private static final String TAG = "ConnectToServerActivity";
    SharedPreferences hexAtomConfig;
    String defaultIP, defaultPort;
    EditText ipEntry, portEntry;
    Button connectButton;

    private static ServerProxy serverProxy;
    private Intent spIntent;
    private static boolean serverBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_connect_to_server);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        hexAtomConfig = getSharedPreferences("HexAtomConfig", MODE_PRIVATE);
        defaultIP = hexAtomConfig.getString("defaultIP", "");
        defaultPort = hexAtomConfig.getString("defaultPort", "");

        ipEntry = (EditText)findViewById(R.id.IPEntry);
        portEntry = (EditText)findViewById(R.id.PortEntry);
        connectButton = (Button)findViewById(R.id.ConnectButton);

        ipEntry.setText(defaultIP);
        portEntry.setText(defaultPort);

        connectButton.setOnClickListener(connectButtonListener);

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
     * Listener for connect button. Checks for Valid IP address and Port number and starts
     * HexAtomGenerateActivity if successful. If either IP or Port are invalid, then an error
     * message is displayed.
     */
    View.OnClickListener connectButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            String ip = ipEntry.getText().toString();
            String port = portEntry.getText().toString();
            if(validateIP(ip) && validatePort(port))
            {
                writePreferences();
                finish();
                connectToServer(ip, port);
                loadGenActivity();
            }
            else
            {
                buildErrorDialog();
            }
        }
    };

    /**
     * Load HexAtomGenerateActivity and start the Activity.
     */
    private void loadGenActivity()
    {
        Intent genActivityIntent = new Intent(this, HexAtomGenerateActivity.class);
        startActivity(genActivityIntent);
    }

    /**
     * Validate ip address.
     * @param ip ip address for validation
     * @return true valid ip address, false invalid ip address
     */
    private boolean validateIP(final String ip)
    {
        Pattern pattern = Pattern.compile(
                "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                        + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                        + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                        + "|[1-9][0-9]|[0-9]))");
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    /**
     * Validate port number.
     * @param port port number for validation
     * @return true valid port number, false invalid port number
     */
    private boolean validatePort(final String port) throws UnsupportedOperationException
    {
        return !port.isEmpty();
    }

    /**
     * Display an error dialog to the user that states there is an error connecting to
     * the server.
     */
    private void buildErrorDialog()
    {
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
        errorDialog.setTitle("Error")
                .setMessage("Sorry, there was an error connecting to server.")
                .setPositiveButton("Okay", null)
                .show();
    }

    /**
     * Write IP address and Port number to preferences file.
     */
    private void writePreferences()
    {
        SharedPreferences.Editor hexAtomConfigEditor = hexAtomConfig.edit();
        hexAtomConfigEditor.putString("defaultIP", ipEntry.getText().toString());
        hexAtomConfigEditor.putString("defaultPort", portEntry.getText().toString());
        hexAtomConfigEditor.apply();
    }

    /**
     * Set the credentials for the service ServerProxy to connect to the server over OSC/UDP.
     * @param IP the Ip Address.
     * @param Port the Port number.
     */
    public void connectToServer(String IP, String Port)
    {
        if(this.serverBound)
        {
            this.serverProxy.setProxyCredentials(IP, Port);
        }
        else
        {
            //display toast to let client know the connect was not successful
            CharSequence text = "Oops. Something went wrong. Err: ServerProxy bind unsuccessful.";
            (Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)).show();
        }
    }

    /**
     * Close the connection with the server. Note packets are currently being sent over UDP, there
     * is no actual "connection" thus nothing has to be done to close. However, this is included
     * so the necessary calls within source code in this module are already done if this protocol
     * is ever changed.
     */
    public void closeConnectionToServer(){}

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
            ConnectToServerActivity.serverProxy = binder.getService();
            ConnectToServerActivity.serverBound = true;

            if(ConnectToServerActivity.serverProxy == null)
            {
                Log.i("ConnectToServerActivity", "Bind to service ServerProxy was unsuccessful.");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className)
        {
            ConnectToServerActivity.serverBound = false;
        }
    };
}
