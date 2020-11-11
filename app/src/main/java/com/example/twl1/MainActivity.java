package com.example.twl1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.twl.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,SensorEventListener{
    private static final String TAG = "MainActivity";

    BluetoothAdapter mBluetoothAdapter;
    BluetoothConnectionService mBluetoothConnection;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private ViewFlipper viewFlipper;
    private LineChart mChart;

    File txtfile = new File("/storage/emulated/0/", "TWLData.txt");
    File txtlocation = new File("/storage/emulated/0/", "TWLLocation.txt");

    //Interface
    Button btnSend, BtDeviceName, SplitBit;
    ImageButton btnONOFF, BtnDiscover, btnPlayPause;
    TextView incomingMessages, Data1, Data2,Data3,Data4,Data5,Data6;
    StringBuilder messages;
    EditText etSend;
    ImageView DoorOpen;
    ListView lvNewDevices;
    ListView lvReceivedMessage;

    //Data
    String WLConnectTarget, SplitStr, StartStr = "a";
    int[] DoorOpenimage = {R.drawable.transparent, R.drawable.car_open_door};
    String[] stringTokens = {"0", "0", "0", "0", "0"};
    String stringTokensbuffer = "0";
    String[] stringTokens1;
    int[] Data = {1, 1, 1};
    int ChartX = 0, delayflag = 0, playpauseflag = 0, currentpage = 1, DeleteCount = 0;
    private float x1, x2;
    long Startmilli = 0;
    List<Entry> yVals1 = new ArrayList<>();
    List<Entry> yVals2 = new ArrayList<>();
    List<String> ReceivedVals = new ArrayList<String>();

    //Location
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    Location location;
    //google API for location service
    FusedLocationProviderClient fusedLocationProviderClient;

    //Compass
    private SensorManager mSensorManager;
    private Sensor mRotationV;

    float[] rMat = new float[9];
    float[] rMatrmp = new float[9];
    float[] orientation = new float[3];
    int mAzimuth;

    private Sensor mAccelerometer, mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    double mLatitude,mLongtitude,mLocationAccuracy;

    float[] mAcceleration = new float[3];

    private long mSensorLastMilli;



    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothDevice mBTDevice;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ArrayAdapter<String> mMessageReceived;

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {                //Copied
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        btnONOFF.setImageResource(R.drawable.ic_bluetooth_disabled_black_50dp);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        btnONOFF.setImageResource(R.drawable.ic_bluetooth_disabled_black_50dp);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        btnONOFF.setImageResource(R.drawable.ic_bluetooth_connected_blue_50dp);
                        startDiscovery();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        btnONOFF.setImageResource(R.drawable.ic_bluetooth_settings_blue_50dp);
                        break;
                }
            }
        }
    };

    /**
     * Broadcast Receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire.
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };

    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());

                String deviceName = device.getName();

                if (deviceName != null) {
                    if (deviceName.equals(WLConnectTarget)) {
                        mBluetoothAdapter.cancelDiscovery();

                        int i = mBTDevices.size() - 1;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            mBTDevices.get(i).createBond();
                            mBTDevice = mBTDevices.get(i);
                            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
                        }
                        startConnection(i);
                    }
                }

                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);


            }

        }
    };

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    mBTDevice = mDevice;            //NEW
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        stopCompass();
        Log.d(TAG, "onPause: Activated");
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCompass();
        Log.d(TAG, "onResume: Activated");
        getLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Activated");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: Activated");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        try {
            unregisterReceiver(mBroadcastReceiver1);
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: " + e);
        }
        try {
            unregisterReceiver(mBroadcastReceiver2);
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: " + e);
        }
        try {
            unregisterReceiver(mBroadcastReceiver3);
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: " + e);
        }
        try {
            unregisterReceiver(mBroadcastReceiver4);
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: " + e);
        }
        mBluetoothAdapter.cancelDiscovery();
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();

        viewFlipper = findViewById(R.id.ViewFlipper);
        View BtPage = findViewById(R.id.bt_page);
        View GraphPage = findViewById(R.id.graph_page);
        View MainPage = findViewById(R.id.main_page);
        mBTDevices = new ArrayList<>();


        BtnDiscover = BtPage.findViewById(R.id.btnFindUnpairedDevices);
        btnONOFF = BtPage.findViewById(R.id.btnONOFF);
        lvNewDevices = BtPage.findViewById(R.id.lvNewDevices);
        BtDeviceName = BtPage.findViewById(R.id.BtDeviceName);
        SplitBit = BtPage.findViewById(R.id.SplitBit);

        mChart = GraphPage.findViewById(R.id.Linechart);
        btnPlayPause = GraphPage.findViewById(R.id.GraphPause);


        etSend = MainPage.findViewById(R.id.editText);
        btnSend = MainPage.findViewById(R.id.btnSend);
        Data1 = MainPage.findViewById(R.id.Data1);
        Data2 = MainPage.findViewById(R.id.Data2);
        Data3 = MainPage.findViewById(R.id.Data3);
        Data4 = MainPage.findViewById(R.id.Data4);
        Data5 = MainPage.findViewById(R.id.Data5);
        Data6 = MainPage.findViewById(R.id.Data6);
        DoorOpen = MainPage.findViewById(R.id.DoorOpen);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000*30);           //millisecond
        locationRequest.setFastestInterval(1000*5);     //millisecond
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                location = locationResult.getLastLocation();
            }
        };

        SensorManager mgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = mgr.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensors) {
            Log.d("Sensors", "" + sensor.getName());
        }


        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));
        //NEW

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        lvNewDevices.setOnItemClickListener(MainActivity.this);
        lvReceivedMessage = MainPage.findViewById(R.id.lvReceivedMessage);

        runTimePermission();

        saveData(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));

        enableBT();
        startDiscovery();
        btImageInit();
        viewFlipper.showNext();
        mChart.setScaleEnabled(false);

        WLConnectTarget = mPreferences.getString("BtName", "HC-06");
        SplitStr = mPreferences.getString("SplitBit", ",");
        BtDeviceName.setText("Target Device: " + WLConnectTarget);
        SplitBit.setText("\" " + SplitStr + " \"");


        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                enableDisableBT();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
                etSend.setText("");
                for (byte b:bytes){
                    Log.d(TAG, "onClick: Text Sending: " + b);
                }

            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playpauseflag == 0) {
                    btnPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    playpauseflag = 1;
                    mChart.setScaleEnabled(true);
                    Toast.makeText(getApplicationContext(), "Paused", Toast.LENGTH_SHORT).show();
                } else {
                    btnPlayPause.setImageResource(R.drawable.ic_pause_black_24dp);
                    playpauseflag = 0;
                    mChart.fitScreen();
                    mChart.setScaleEnabled(false);
                }
            }
        });
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");

            //           Log.d(TAG, "Received: "+ text);
            if (text != "Failed") {
                mMessageReceived = new ArrayAdapter<String>(context, R.layout.message_adapter_view, ReceivedVals);
                lvReceivedMessage.setAdapter(mMessageReceived);
                stringTokensbuffer = stringTokensbuffer.concat(text);
                //             Log.d(TAG, "onReceive: Tokensbuffer: " + stringTokensbuffer);
                stringTokens1 = stringTokensbuffer.split(StartStr, 0);
                if (stringTokens1.length > 1) {
                    //                      Log.d(TAG, "Received Tokens1: " + stringTokens1[0] + "; Tokens2: " + stringTokens1[1]);
                    if(stringTokensbuffer.endsWith(StartStr)) {
                        stringTokensbuffer = (stringTokens1[stringTokens1.length - 2]);
                        stringTokensbuffer = stringTokensbuffer.concat(StartStr);
                        stringTokensbuffer = stringTokensbuffer.concat(stringTokens1[stringTokens1.length - 1]);
                        stringTokensbuffer = stringTokensbuffer.concat(StartStr);
                    }
                    else{
                        stringTokensbuffer = (stringTokens1[stringTokens1.length - 2]);
                        stringTokensbuffer = stringTokensbuffer.concat(StartStr);
                        stringTokensbuffer = stringTokensbuffer.concat(stringTokens1[stringTokens1.length - 1]);
                    }
                }
                for (int i = 0; i < (stringTokens1.length - 2); i++) {
                    stringTokens = stringTokens1[i].split(SplitStr, 4);
                    Date date = Calendar.getInstance().getTime();
                    DateFormat dateFormat = new SimpleDateFormat("mm:ss.SSS");
                    String strdate = dateFormat.format(date);
                    if(ReceivedVals.size()>50000){
                        ReceivedVals.remove(0);
                    }
                    ReceivedVals.add(strdate + "   \t" + stringTokens1[i]);
                    //                      Log.d(TAG, "Received: Tokens1: " + stringTokens1[i]);
                    //                      Log.d(TAG, "Received: Tokens : " + stringTokens[0] + "," + stringTokens[1] );
                    if (stringTokens.length > 2) {

                        saveData(strdate + "\t" + stringTokens[0] + "\t" + stringTokens[1] + "\t" + stringTokens[2]);
                        for (int a = 0; a < 3; a++) {
                            try {
                                Data[a] = Integer.parseInt(stringTokens[a]);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "onReceive: Non Integer Received:" + stringTokens[a]);
                            }
                        }
                        float mdata2 = (float) Data[1] / 100;
                        //              Log.d(TAG, "WLReceived: " + Data[0] + "," + Data[1] + "," + Data[2]);
                             Data1.setText(Data[0] + "km/h");
                             Data2.setText((int)(mdata2*1000)  + "rpm");

                    }
                }
            }
            else {
                BtnDiscover.setImageResource(R.drawable.ic_refresh_black_50dp);
                BtDeviceName.setText("Target Device: "+ WLConnectTarget);
                Data1.setText("");
                Data2.setText("");
                //               Data1.setHint("Speed");
                //               Data2.setHint("RPM");
                Toast.makeText(getApplicationContext(),"Disconnected from Target Device", Toast.LENGTH_SHORT).show();
                startDiscovery();
            }
            getLocation();
 //           long now = System.currentTimeMillis();
 //           float interval = (float)(now - mSensorLastMilli);
 //           mSensorLastMilli = now;
 //           String mDataSend = String.format("E%f,%f,%f,%f,%f,%f,%f,%f\n",interval,mLongtitude,mLatitude,mLocationAccuracy,(float)mAzimuth,mAcceleration[0],mAcceleration[1],mAcceleration[2]);
 //           byte[] bytes = mDataSend.getBytes(Charset.defaultCharset());
 //           mBluetoothConnection.write(bytes);
        }
    };

    public void startConnection(int i){              //Connection will fail and App will crash if haven't paired yet
        startBTConnection(mBTDevice,MY_UUID_INSECURE,i);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid, int i){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

        mBluetoothConnection.startClient(device,uuid);
        BtnDiscover.setImageResource(R.drawable.ic_done_black_50dp);
        BtDeviceName.setText("Connected to " + mBTDevices.get(i).getName());
        Toast.makeText(getApplicationContext(),"Connecting to " + mBTDevices.get(i).getName() , Toast.LENGTH_SHORT).show();
        if(currentpage==2){
            swapNext();
            swapNext();
            swapPrevious();
        }
        else if(currentpage==1){
            swapNext();
            swapPrevious();
        }
    }

    public void enableBT(){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }

    public void enableDisableBT(){
        enableBT();
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }

    public void startDiscovery(){
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");//WL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkBTPermissions();
        }

        mBluetoothAdapter.startDiscovery();
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
    }


 /*   public void btnEnableDisable_Discoverable(View view) {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);

    }*/

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){
            //check BT permissions in manifest
            checkBTPermissions();
            enableBT();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    private void runTimePermission(){
        Dexter.withActivity(this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {

            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();

            }
        }).check();
        Dexter.withActivity(this).withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {

            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();

            }
        }).check();
        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {

            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();

            }
        }).check();
    }

    private void saveData(String Data){

        try {
            FileWriter writer = new FileWriter(txtfile,true);
            writer.append(Data);
            writer.append("\n");
            writer.flush();
            writer.close();
            //           Log.d(TAG, "SaveData: Saving Data to "+ txtfile);
        } catch (IOException e) {
            Log.e(TAG, "SaveData: " + e );
        }
    }

    private void saveLocation(String location){

        try {
            FileWriter writer = new FileWriter(txtlocation,true);
            writer.append(location);
            writer.append("\n");
            writer.flush();
            writer.close();
            //           Log.d(TAG, "SaveData: Saving Data to "+ txtfile);
        } catch (IOException e) {
            Log.e(TAG, "SaveData: " + e );
        }
    }

    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location newlocation) {
                    location = newlocation;
                    saveLocation(location.getLatitude() +";" + location.getLongitude());
                    mLatitude = location.getLatitude();
                    mLongtitude = location.getLongitude();
                    mLocationAccuracy=location.getAccuracy();
                    Data1.setText("Latitiude:" + location.getLatitude());
                    Data2.setText("Longitude:" + location.getLongitude());
                    Log.d(TAG, "onSuccess: Location=" + location.getLatitude() +";" + location.getLongitude());      //Data available at https://developer.android.com/reference/android/location/Location
                }
            });
        }
    }

    private void startCompass() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorManager.registerListener(this,mRotationV,SensorManager.SENSOR_DELAY_UI);
        Log.d(TAG, "Compassstart: Using Vector Sensor");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
    }
    public void stopCompass(){
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
/*        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            SensorManager.getRotationMatrixFromVector(rMat,event.values);
            mAzimuth = (int)(Math.toDegrees(SensorManager.getOrientation(rMat,orientation)[0]+360)%360);        //value[0,1,2] = Yaw,Pitch,Roll
            Log.d(TAG, "onSensorChanged: " + mAzimuth);
        }*/
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
            mAcceleration[0] = event.values[SensorManager.DATA_X];
            Data4.setText("AcceX: "+ mAcceleration[0]);
            mAcceleration[1] = event.values[SensorManager.DATA_Y];
            Data5.setText("AcceY: "+ mAcceleration[1]);
            mAcceleration[2] = event.values[SensorManager.DATA_Z];
            Data6.setText("AcceZ: "+ mAcceleration[2]);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.remapCoordinateSystem(rMat,SensorManager.AXIS_X,SensorManager.AXIS_Z,rMatrmp);
//            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
            mAzimuth = (int)(Math.toDegrees(SensorManager.getOrientation(rMatrmp, orientation)[0]) + 360) % 360;
            int mVAzimuth = (int)(Math.toDegrees(SensorManager.getOrientation(rMatrmp, orientation)[0]) + 360) % 360;
            Data3.setText("Azimuth:"+mAzimuth);

            //          Log.d(TAG, "onSensorChanged: " + mAzimuth +" Pitch: "+ mVAzimuth);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    private void swapPrevious(){
        if(currentpage > 0) {
            viewFlipper.setInAnimation(this, R.anim.slide_in_right);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
            viewFlipper.showPrevious();
            currentpage = currentpage -1;
            Log.d(TAG, "CurrentPage: " + currentpage);
        }
    }

    private void swapNext(){
        if(currentpage < 2) {
            viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
            viewFlipper.showNext();
            currentpage = currentpage + 1;
            Log.d(TAG, "CurrentPage: " + currentpage);
        }
    }

    private int countingDelay(long delayms){
        int x=0;
        if(delayflag==1){
            Startmilli=System.currentTimeMillis();
            delayflag=0;
            x=1;
        }
        else if(System.currentTimeMillis()-Startmilli >= delayms && delayflag==0){
            delayflag=1;
            x=2;
        }
        return x;
    }

    public void swapMainPage(View view) {
        if(currentpage==0){
            swapNext();
        }
        else{
        swapPrevious();
        }
    }

    public void swapGraphPage(View view){
        swapPrevious();
    }

    public void swapBtPage(View view) {
        swapNext();
    }

    public void updateBtName(View view){
        Dialog_Fragment dialog = new Dialog_Fragment();
        dialog.show(getSupportFragmentManager(), "DialogFragment");
        dialog.Hint(0);
    }

    public void updateSplitter(View view) {
        Dialog_Fragment dialog = new Dialog_Fragment();
        dialog.show(getSupportFragmentManager(), "DialogFragment");
        dialog.Hint(1);
    }

    public void shareData(View view) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        if(txtfile.exists()){
            sendIntent.setType("text/*");
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(txtfile.getAbsolutePath()));
            startActivity(Intent.createChooser(sendIntent,"SHARE"));
        }

    }

    public void deleteData(View view){
        if(System.currentTimeMillis()-Startmilli >= 2000 || DeleteCount==0){
            Toast.makeText(MainActivity.this,"Press Again to Delete", Toast.LENGTH_SHORT).show();
            //           delayflag=0;
            Startmilli=System.currentTimeMillis();
            DeleteCount=1;
            Log.d(TAG, "DeleteData: Press Again to Delete");
        }
        else if(DeleteCount==1 && System.currentTimeMillis()-Startmilli < 2000){
            Toast.makeText(MainActivity.this,"File Deleted", Toast.LENGTH_SHORT).show();
            DeleteCount=0;
            txtfile.delete();
            Log.d(TAG, "DeleteData: File Deleted");
        }
    }

    private void btImageInit(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            btnONOFF.setImageResource(R.drawable.ic_bluetooth_disabled_black_50dp);
        } else if (!mBluetoothAdapter.isEnabled()) {
            btnONOFF.setImageResource(R.drawable.ic_bluetooth_disabled_black_50dp);
        } else {
            btnONOFF.setImageResource(R.drawable.ic_bluetooth_connected_blue_50dp);
        }
    }

    public void preferenceEdit(String Name, int a){

        String Tag[]={"BtName","StartBit"};
        mEditor.putString(Tag[a],Name);
        mEditor.commit();
        if(a==0){
            Log.d(TAG, "UpdateBtName: Updating to ." + Name + ".");
            WLConnectTarget = Name;
            BtDeviceName.setText("Target Device: "+ Name);
        }
        else if(a==1){
            Log.d(TAG, "UpdateSplitBit: Updating to ." + Name + ".");
            SplitStr = Name;
            SplitBit.setText("\" " + Name + " \"");
        }

    }

    @Override
    public void onBackPressed() {
        if(currentpage==2){
            swapPrevious();
        }
        else if(currentpage ==0){
            swapNext();
        }
        else{
            this.finish();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            x1=event.getX();
        }
        else if(event.getAction()==MotionEvent.ACTION_UP){
            x2=event.getX();
            float deltaX = x2-x1;
            if(Math.abs(deltaX)>150){
                if(x2>x1){
                    Log.d(TAG, "onTouchEvent: Swipe Left");
                    swapNext();
                }
                else{
                    Log.d(TAG, "onTouchEvent: Swipe Right");
                    swapPrevious();
                }
            }
        }
        return super.onTouchEvent(event);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress + "i =" + i);

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + mBTDevices.get(i));
            mBTDevices.get(i).createBond();

            mBTDevice = mBTDevices.get(i);
            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
        }
        if(deviceName!=null){
            startConnection(i);
        }
    }                                                                                                       //Copied



}
