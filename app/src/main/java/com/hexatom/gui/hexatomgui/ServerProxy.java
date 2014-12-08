package com.hexatom.gui.hexatomgui;

/**
 * @author  Ken Zyma
 * @version 1.0
 * @since   2014-12-1
 */

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.content.SharedPreferences;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCListener;
import android.os.Handler;
import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import android.widget.RadioGroup;
import java.util.Vector;

/**
 * @brief ServerProxy handles all messaging to and from the server.
 *
 * ServerProxy handles all messaging to and from the server. This is a long-standing thread
 * which runs as a binded-service. After binding to this service an Activity (or other) should
 * use setProxyCredentials(String IP, String Port) to first set ip and port for OSC/UDP communcation
 * channel. Then the binded item may use sendMessage(String message) to send a message to the
 * server, or may override the class GuiUpdateCallback for remote updating from the server
 * by registering an element (which implements GuiUpdateCallback).
 */
public class ServerProxy extends Service {
    //members associated with establishing a connection with the server
    private String InIP;
    private String InPort;
    private String OutIP;
    private String OutPort;
    private OSCPortIn receiver;
    private Handler handler;
    private Timer requestInformationTimer;
    private Integer seqNum;

    //callback's
    private GuiUpdateCallback tempoCallback,messageReceivedCallback,maxDiameterCallback;
    private GuiUpdateCallback currentDiameterCallback,erasureCallback;
    private Vector<AtomSeekBar> probabilityCallback;
    private RadioGroup probRadioGroup;

    private final IBinder binder = new ServerBinder();

    /**
     * Initialize ServerProxy with empty credentials for connecting.
     */
    public ServerProxy() {
        this.InIP = "";
        this.InPort = "";
        this.seqNum = 1;
        this.OutIP = "";
        this.OutPort = "";
        requestInformationTimer = null;
        handler = new Handler();
    }

    /**
     * Set Port and IP of server for sending/receiving messages.
     * @param IP
     * @param Port
     */
    public void setProxyCredentials(String IP, String Port) {
        this.OutIP = IP;
        this.OutPort = Port;
        this.InIP = getLocalIpAddress();
        this.InPort = "5000";

        try {
            //Initialize variables for use in establishing the OSC connection
            if (receiver != null) {
                receiver.close();
            }
            receiver = new OSCPortIn(Integer.parseInt(this.InPort));
            OSCListener listener = new ServerListener(this);

            //Register the receiver to listen for incoming packets
            receiver.addListener("/interpret", listener);
            receiver.startListening();
        } catch (final java.net.SocketException sx) {
            Log.e("ServerProxy","Socket Exception on OSC Listener.");
        } catch (final Exception e) {
            Log.e("ServerProxy","Exception starting OSC Listener");
        }

        if(requestInformationTimer == null)
        requestInformationTimer = new Timer();
        try {
            requestInformationTimer.schedule(doAsynchronousTask,0,2000);
        }catch(IllegalStateException e){
            Log.e("RequestTimer","Task was already scheduled or cancelled, timer was " +
                    "cancelled, or timer thread terminated");
        }
    }

    /**
     * Must implement GuiUpdateCallback to register with a ServerProxy update.
     */
    interface GuiUpdateCallback{
        public void update(String value);
    }

    /**
     * Register callback for updating HexAtom tempo information.
     * @param callback
     */
    public void tempoRegister(GuiUpdateCallback callback){
        tempoCallback = callback;
    }

    /**
     * Register callback for updating HexAtom probability information.
     * @param callback seekbar's to be updated
     * @param probRadioGroup a copy of atom selection group, so that only the
     *                       atom currently displayed on screen is updated.
     */
    public void probabilityRegister(Vector<AtomSeekBar> callback,
                                    RadioGroup probRadioGroup){
        this.probabilityCallback = callback;
        this.probRadioGroup = probRadioGroup;
    }

    /**
     * Register callback for updating HexAtom max diameter information.
     * @param callback
     */
    public void maxDiameterRegister(GuiUpdateCallback callback){
        this.maxDiameterCallback = callback;
    }

    /**
     * Register callback for updating HexAtom current diameter information.
     * @param callback
     */
    public void currentDiameterRegister(GuiUpdateCallback callback){
        this.currentDiameterCallback = callback;
    }

    /**
     * Register callback for updating HexAtom max erasure information.
     * @param callback
     */
    public void erasureRegister(GuiUpdateCallback callback){
        this.erasureCallback = callback;
    }

    /**
     * Register callback to notify when a message was successfully received by the server.
     * @param callback
     */
    public void messageReceivedRegister(GuiUpdateCallback callback){
        messageReceivedCallback = callback;
    }

    private void updateTempo(String val){
        if(tempoCallback != null){
            val = Integer.toString((int)((Float.parseFloat(val) / (float)1000)*100));
            tempoCallback.update(val);
        }
    }

    private void updateProbability(int atom,int prob,int value){
        if(probabilityCallback != null){
            probabilityCallback.elementAt(prob).update(Integer.toString(value));
        }
    }

    private void updateMaxDiameter(String val){
        if(maxDiameterCallback != null){
            maxDiameterCallback.update(val);
        }
    }

    private void updateCurrentDiameter(String val){
        if(currentDiameterCallback != null){
            currentDiameterCallback.update(val);
        }
    }

    private void updateErasure(String val){
        if(erasureCallback != null){
            val = Integer.toString((int)(((float)Integer.parseInt(val) / (float)255.0)*100));
            erasureCallback.update(val);
        }
    }

    private void notifyMessageReceivedByServer(){
        if(messageReceivedCallback != null){
            messageReceivedCallback.update("");
        }
        //Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        //v.vibrate(100);
    }

    /**
     * Send a query message to the server to send back tempo, probability, diameter and erasure
     * information.
     */
    public void queryGameStateFromServer(){
        if(this.tempoCallback != null){
            this.sendMessage("qt");
        }
        if(this.probRadioGroup != null & this.probabilityCallback != null){
            //get current selected index
            int index = this.probRadioGroup.indexOfChild
                    (probRadioGroup.findViewById(this.probRadioGroup.getCheckedRadioButtonId()));
            this.sendMessage("qp"+Integer.toString(index));
        }
        if((this.maxDiameterCallback != null) || (this.currentDiameterCallback != null)){
            this.sendMessage("qd");
        }
        if(this.erasureCallback != null){
            this.sendMessage("qe");
        }
    }

    /**
     * Update views using the callback's which are currently registerd. An OSCMessage is parsed
     * and appropriate function called to handle the message.
     * @param time
     * @param message
     */
    public synchronized void updateViews(java.util.Date time,OSCMessage message){

        Object [] args = message.getArguments();

        //check for oscmessage error
        if((args[1].toString()).equals("false")) {
            Log.e("OSCMessage:", args[2].toString());
            return;
        }

        //message is for updating
        for (int i = 2; i < args.length ; i++) {

            try {
                String arg = args[i].toString();
                String msg = arg.substring(1, arg.length() - 1);
                String delims = ",";
                String[] delimArgs = msg.split(delims);

                for(int j=0;j<delimArgs.length;j++) {
                    delims = "=";
                    String[] KeyVal = delimArgs[j].split(delims);
                    String key = KeyVal[0].trim();
                    String value = KeyVal[1].trim();
                    if (key.equals("tempo")) {
                        updateTempo(value);
                    }else if(key.substring(0,Math.min(key.length(),3)).equals("pcd")){
                        int val = (int)((new Float(value) * 100));
                        int atom = Integer.parseInt(key.substring(3));
                        updateProbability(atom,0,val);
                    }else if(key.substring(0,Math.min(key.length(),3)).equals("pmt")){
                        int val = (int)((new Float(value) * 100));
                        int atom = Integer.parseInt(key.substring(3));
                        updateProbability(atom,1,val);
                    }else if(key.substring(0,Math.min(key.length(),3)).equals("pde")){
                        int val = (int)((new Float(value) * 100));
                        int atom = Integer.parseInt(key.substring(3));
                        updateProbability(atom,2,val);
                    }else if(key.substring(0,Math.min(key.length(),3)).equals("pst")){
                        int val = (int)((new Float(value) * 100));
                        int atom = Integer.parseInt(key.substring(3));
                        updateProbability(atom,3,val);
                    }else if(key.substring(0,Math.min(key.length(),3)).equals("pdf")){
                        int val = (int)((new Float(value) * 100));
                        int atom = Integer.parseInt(key.substring(3));
                        updateProbability(atom,4,val);
                    }else if(key.substring(0,Math.min(key.length(),3)).equals("pfi")){
                        int val = (int)((new Float(value) * 100));
                        int atom = Integer.parseInt(key.substring(3));
                        updateProbability(atom,6,val);
                    }else if(key.substring(0,Math.min(key.length(),3)).equals("pft")){
                        int val = (int)((new Float(value) * 100));
                        int atom = Integer.parseInt(key.substring(3));
                        updateProbability(atom,7,val);
                    }else if(key.substring(0,Math.min(key.length(),3)).equals("pfu")){
                        int val = (int)((new Float(value) * 100));
                        int atom = Integer.parseInt(key.substring(3));
                        updateProbability(atom,8,val);
                    }else if(key.substring(0,Math.min(key.length(),3)).equals("pvf")){
                        int val = (int)((new Float(value) * 100));
                        int atom = Integer.parseInt(key.substring(3));
                        updateProbability(atom,9,val);
                    }else if(key.substring(0,Math.min(key.length(),3)).equals("pxu")) {
                        int val = (int) ((new Float(value) * 100));
                        int atom = Integer.parseInt(key.substring(3));
                        updateProbability(atom, 10, val);
                    }else if(key.substring(0,Math.min(key.length(),11)).equals("diameterMax")) {
                        updateMaxDiameter(value);
                    }else if(key.substring(0,Math.min(key.length(),8)).equals("diameter")) {
                        String intDelim = " ";
                        String[] temp = value.split(intDelim);
                        updateCurrentDiameter(temp[0]);
                    }else if(key.substring(0,Math.min(key.length(),9)).equals("eraseRate")) {
                        updateErasure(value);
                    }else{
                        Log.d("Message Unknown", key+" "+value);
                    }
                }
            }catch(Exception ex){
                //Log.e("Serverproxy.updateViews","Error.");
                //ex.printStackTrace();
                notifyMessageReceivedByServer();
            }
        }
    }

    public void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    /**
     * Check if all credentials for connection have been met.
     * @return True if all connection credentials are met, false otherwise.
     */
    public boolean isConnected(){
        if((this.InIP.equals("")) || (this.InPort.equals("")) || (this.OutIP.equals(""))
                || (this.OutPort.equals("")) || (this.receiver == null)){
            return false;
        }
        return true;
    }

    /**
     * Send a message to the server. Will return true if successful and false otherwise.
     *
     * @param message message to send to the server via OSC
     * @return true if message was successfully send, false otherwise. Note this does not
     * guarantee the message was received by the server.
     */
    public synchronized boolean sendMessage(String message) {
        //first if no connection is established, try to regain a connection.
        //this may occur if the service is unbound and restarts
        if(!this.isConnected()){
            SharedPreferences hexAtomConfig = getSharedPreferences("HexAtomConfig", MODE_PRIVATE);
            String IP = hexAtomConfig.getString("defaultIP", "");
            String Port = hexAtomConfig.getString("defaultPort", "");
            setProxyCredentials(IP,Port);
        }

        try {
            Object[] oscargs = new Object[4];

            //Bundle up the incoming IP address, incoming port number, sequence number, and command
            oscargs[0] = InIP;
            oscargs[1] = Integer.parseInt(InPort);
            oscargs[2] = seqNum;
            oscargs[3] = message;

            //Initialize the OSC object that will actually send the OSC packet to HexAtom
            OSCPortOut sender = null;

            //Set the IP address and port of the server to which packets will be sent
            if (OutIP != "" && OutPort != "") {
                InetAddress otherIP = InetAddress.getByName(OutIP);
                try {
                    sender = new OSCPortOut(otherIP, Integer.parseInt(OutPort));
                } catch (SocketException e) {
                    e.printStackTrace();
                    Log.e("ServerProxy", "Failed to create new OSCPortOut");
                    return false;
                }
            } else {
                //out ip and port were not yet set!
                Log.e("ServerProxy", "Null OutIP/Port");
                return false;
            }
            //Send the bundled information to HexAtom
            try {
                sender.send(new OSCMessage("/interpret", oscargs));
            } catch (final IOException e) {
                e.printStackTrace();
                Log.e("ServerProxy", "Interpret failed for OSCMessage");
                return false;
            }

        } catch (final UnknownHostException ux) {
            ux.printStackTrace();
            Log.e("ServerProxy", "UnknownHostException was thrown.");
            return false;
        }
        this.seqNum++;
        return true;
    }

    /**
     * Timer for querying the server for information at regular intervals.
     */
    TimerTask doAsynchronousTask = new TimerTask() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @SuppressWarnings("unchecked")
                public void run() {
                    try {
                        queryGameStateFromServer();
                    }
                    catch (Exception e) {
                        // TODO Auto-generated catch block
                    }
                }
            });
        }
    };

    public class ServerBinder extends Binder {
        ServerProxy getService() {
            //return an instance of server proxy so clients can
            //call the public methods
            return ServerProxy.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private String getLocalIpAddress() {
        try {
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            String ip = intToIp(ipAddress);
            return ip;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("ServerProxy","Error getting local IP.");
        }
        return null;
    }

    private String intToIp(int i) {

        return ((i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 24) & 0xFF));
    }
}
