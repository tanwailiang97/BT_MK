package com.example.twl1;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ"; //name for chat service

    private static final String appName = "TWL";

    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    //run while listening for incoming connection ; server side client ; run until a connection is accepted
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket; //Local Server Socket

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using" + MY_UUID_INSECURE);
            }catch(IOException e){
                Log.e(TAG, "AcceptThread: IOException:" +e.getMessage() );
            }

            mmServerSocket = tmp;
        }
        public void run(){
            Log.d(TAG,"run: AcceptThread Running.");

            BluetoothSocket socket = null;
            try {
                Log.d(TAG, "run: RFCOM server socket start....."); //Blocking call and only return when successful connection
                socket = mmServerSocket.accept();
                Log.d(TAG, "run: RFCOM server socket accepted connection");

            }catch (IOException e){
                Log.e(TAG, "Accept Thread: IOException:" + e.getMessage());
            }

            if(socket !=null){
                connected(socket,mmDevice);         //3rd Video
            }

            Log.i(TAG, "END mAccept Thread");
        }

 /*       public void cancel(){
            Log.d(TAG,"cancel: Canceling AcceptThread");
            try{
                mmServerSocket.close();
            }catch(IOException e){
                Log.e(TAG,"cancel: Close of AcceptThread ServerSocket failed." + e.getMessage());
            }
        }
 */   }
    private class ConnectThread extends Thread{         //run when attempting to make an outgoing connection; either success or fail
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid){
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }
        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG,"RUN mConnectThread ");
            try{
                Log.d(TAG,"ConnectThread: Trying to create InsecureRfcommSocket using UUID: " + MY_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);           //get a bluetoothsocket for connection with the given Bluetoothdevice
            }catch(IOException e){
                Log.e(TAG,"ConnectThread: Could not create InsecureRfcommSocket "+ e.getMessage());
            }

            mmSocket = tmp;
            mBluetoothAdapter.cancelDiscovery();        //cancel discovery cause will slow down connection

            try {
                mmSocket.connect();

                Log.d(TAG,"run: ConnectThread connected. ");
            } catch (IOException e){
                try{
                    mmSocket.close();               //Close Socket
                    Log.d(TAG,"run: Closed Socket. ");
                }catch (IOException e1){
                    Log.e(TAG,"mConnectionThread: run: Unable to close connection in socket" + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: could not connect to UUID: " + MY_UUID_INSECURE );
            }

            connected(mmSocket,mmDevice);    //3rd video
        }
        public void cancel(){
            try{
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            }catch (IOException e){
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }



    public synchronized void start(){               //start chat service : start accept thread to begin in listening (server) mode. Called by the Activity OnResume()
        Log.d(TAG, "start");

        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }
    public void startClient(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startClient: Started.");

        //initprogress dialog
        mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth","Please Wait...",true);
        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }
    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;                 //Modified
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG, "ConnectedThread: Starting. ");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                mProgressDialog.dismiss();                 //dismiss the progress dialog when connection is entablished
            }catch(NullPointerException e){
                e.printStackTrace();
            }

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            }catch(IOException e){
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run(){
            byte[] buffer = new byte[50];         //buffer store for stream
            int bytes;                              //bytes returned from read()

            while(true){
                try{
                    bytes = mmInStream.read(buffer);    //read from input stream
                    String incomingMessage = new String(buffer, 0, bytes);

//                    Log.d(TAG, "InputStream: "+ incomingMessage);


                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("theMessage",incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);

                }catch (IOException e){
                    Log.e(TAG, "InputStream: Error reading input. " + e.getMessage() );
                    String incomingMessage = "Failed";
                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("theMessage",incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);
                    break;                              //come out from while
                }
            }
        }
        public void write(byte[] bytes){                //Call from main to send data
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to output stream: " + text);
            try{
                mmOutStream.write(bytes);
            }catch(IOException e){
                Log.e(TAG, "write: Error writing to OutputStream. " + e.getMessage());
            }
        }
        public void cancel(){                           //Call from main to shutdown connection
            try{
                mmSocket.close();
            }catch(IOException e){
                Log.e(TAG, "cancel: Fail to cancel" + e.getMessage());
            }
        }

    }
    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting");
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public void write(byte[] out){
        ConnectThread r;                //Temporary Object
        Log.d(TAG, "write: Write Called.");        //synchronize a copy of connected thread
        mConnectedThread.write(out);                    //perform the write

    }
}
