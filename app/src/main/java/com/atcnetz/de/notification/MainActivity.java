package com.atcnetz.de.notification;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private AlertDialog enableNotificationListenerAlertDialog;
    public static final String EXTRA_BLUETOOTH_DEVICE = "BT_DEVICE";
    private BluetoothDevice mDevice;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;
    //Textviews
    TextView tvContent;
    TextView tvHeartRate;
    TextView tvSteps;
    TextView tvTemp;
    TextView tvDeviceType;
    TextView tvPolicy;
    TextView tvMacAddress;
    TextView tvDeviceName;
    //Buttons
    ImageButton DeviceImageButton;
    ImageButton SettingsButton;
    Button ClearLogButton;
    CheckBox NotificationsCheckBox;
    CheckBox LogCheckBox;
    //Other
    ScrollView scrollview;
    EditText CustomBLEcmd;
    boolean isConnected;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.SettingsButtonID) {
            Intent intent = new Intent(this, com.atcnetz.de.notification.Settings.class);
            startActivityForResult(intent, 34);
        } else if (v.getId() == R.id.clearLogButtonID) {
            clearLog();
            load();
            scrollDown();
        } else if (v.getId() == R.id.notificationCheckBoxID) {
            editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
            editor.putBoolean("isNotificationEnabled", NotificationsCheckBox.isChecked());
            editor.apply();
            stopService(new Intent(this, BLEservice.class));
            if (NotificationsCheckBox.isChecked() && !isMyServiceRunning()) startService(new Intent(this, BLEservice.class));
        } else if (v.getId() == R.id.logCheckBoxID) {
            editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
            editor.putBoolean("isDebugEnabled", LogCheckBox.isChecked());
            editor.apply();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Textviews
        tvContent = (TextView) findViewById(R.id.tv_content);
        scrollview = (ScrollView) findViewById(R.id.scroll);
        tvPolicy = findViewById(R.id.privacyPolicytextViewID);
        tvPolicy.setMovementMethod(LinkMovementMethod.getInstance()); //setup hyperlink
        //Buttons
        SettingsButton = findViewById(R.id.SettingsButtonID);
        SettingsButton.setOnClickListener(this);
        ClearLogButton = findViewById(R.id.clearLogButtonID);
        ClearLogButton.setOnClickListener(this);
        NotificationsCheckBox = findViewById(R.id.notificationCheckBoxID);
        NotificationsCheckBox.setOnClickListener(this);
        LogCheckBox = findViewById(R.id.logCheckBoxID);
        LogCheckBox.setOnClickListener(this);
        //Other
        prefs = getSharedPreferences("Settings", MODE_PRIVATE);

        //Set checkbox
        NotificationsCheckBox.setChecked(prefs.getBoolean("isNotificationEnabled", true));
        LogCheckBox.setChecked(prefs.getBoolean("isDebugEnabled", false));

        //check for DnDisturb
        if (!isNotificationServiceEnabled()) {
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }


        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //String s = intent.getStringExtra("ToActivity");
                load();
            }
        };
        //if(!isMyServiceRunning() && prefs.getBoolean("isNotificationEnabled", true))startService(new Intent(this, BLEservice.class));

        CustomBLEcmd = findViewById(R.id.editText);
        Button BLEcmdButton = findViewById(R.id.sendCMDbuttonID);
        BLEcmdButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBLEcmd(CustomBLEcmd.getText().toString());
            }
        });


        initWatchInfo();
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private AlertDialog buildNotificationServiceAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Welcome to The D6 Notification App");
        alertDialogBuilder.setMessage("Please allow Notification for This App in the Upcoming window and press the back button");
        alertDialogBuilder.setPositiveButton("Open settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton("No Abort",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        return (alertDialogBuilder.create());
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),
                new IntentFilter("ToActivity1"));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == 4) {
                if (data.getExtras() != null) {
                    mDevice = data.getExtras().getParcelable(EXTRA_BLUETOOTH_DEVICE);
                }
                KLog("Got Device:" + mDevice.getName() + " " + mDevice.getAddress());
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("MacId", mDevice.getAddress());
                editor.putString("BleName" , mDevice.getName());
                editor.apply();
                stopService(new Intent(this, BLEservice.class));
                if (NotificationsCheckBox.isChecked() && !isMyServiceRunning()) startService(new Intent(this, BLEservice.class));
            }
        } else if (requestCode == 34) {

        }
    }


    public void KLog(String TEXT) {
        tvContent.append("\n" + TEXT + "\n");
        scrollDown();
    }

    void scrollDown() {
        Thread scrollThread = new Thread() {
            public void run() {
                try {
                    sleep(200);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            scrollview.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        scrollThread.start();
    }

    public void sendResult(String message) {
        Intent intent = new Intent("MSGtoServiceIntent");
        if (message != null)
            intent.putExtra("MSGtoService", message);
        localBroadcastManager.sendBroadcast(intent);
    }

    public void load() {
        tvContent.setText("Please Long click on Device Image to select your device, you need to accept Location services to enable Bluetooth acces to this app.\r\n" +
                "\r\nYou can Enable or Disable the notification for certain app's via the Select App's Button in settings (Click cog icon for settings).\r\n" +
                "\r\nHave fun with this App and give me feedback if you found a bug :)\r\n\r\nSpecial thanks the following people for contributions:\r\nNeil O'Brien\r\n");
        FileInputStream fis = null;
        updateWatchInfo();
        try {
            fis = openFileInput("D6notfifier.log");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }
            KLog(sb.toString());
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void clearLog() {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput("D6notfifier.log", MODE_PRIVATE);
            fos.write("".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void initWatchInfo() {
        String BatteryPercent = prefs.getString("BatteryPercent", "xxx");
        String DeviceType = prefs.getString("FirmwareVersion", "not loaded");
        String StepCount =  prefs.getString("Steps", "0");
        String bleName = prefs.getString("BleName", "not loaded");
        String macId = prefs.getString("MacId", "00:00:00:00:00:00");
        String HeartRate = prefs.getString("HeartRate", "0");
        isConnected = prefs.getBoolean("isConnected", false);

        //Device Image Button
        DeviceImageButton = findViewById(R.id.imageButtonDevicePicID);
        DeviceImageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivityForResult(intent, 2);
                return true;
            }
        });




        //check connection status and set image
        if (isConnected){
            setDeviceImage();
            //Toast.makeText(MainActivity.this, "connection", Toast.LENGTH_SHORT).show();
        } else { DeviceImageButton.setImageResource(R.drawable.nodeviceimage);
            //Toast.makeText(MainActivity.this, "no connection", Toast.LENGTH_SHORT).show();
            }



        // BLE name
        tvDeviceName = findViewById(R.id.textViewDeviceNameID);
        if (bleName != null){
            tvDeviceName.setText("Name: " + bleName);
        }



        // MAC
        tvMacAddress = findViewById(R.id.textViewMacID);
        tvMacAddress.setText("MAC: " + macId);

        //Firmware Version
        tvDeviceType = findViewById(R.id.textViewDevTypeID);
        tvDeviceType.setText("Device: " + DeviceType);

        //Heart Rate
        tvHeartRate = findViewById(R.id.textViewHeartRateID);
        tvHeartRate.setText(HeartRate);

        tvSteps = (TextView) findViewById(R.id.textViewStepcountID);
        tvSteps.setText("Steps: " + StepCount);

        // Battery
        tvTemp = (TextView) findViewById(R.id.textViewBatteryID);
        tvTemp.setText("Battery: " + BatteryPercent + "% ");

        //Connected?
        //TODO add bluetooth icon.. blue if connected else red

    }

    void updateWatchInfo() {
        String BatteryPercent = prefs.getString("BatteryPercent", "xxx");
        String StepCount =  prefs.getString("Steps", "0");
        String HeartRate = prefs.getString("HeartRate", "0");
        String DeviceType = prefs.getString("FirmwareVersion", "not loaded");
        String bleName = prefs.getString("BleName", "not loaded");
        String macId = prefs.getString("MacId", "00:00:00:00:00:00");
        isConnected = prefs.getBoolean("isConnected", false);

        //check connection status and set image
        if (isConnected){
            setDeviceImage();
        } else {
            DeviceImageButton.setImageResource(R.drawable.nodeviceimage);
        }

        //update steps
        tvSteps.setText(StepCount);
        //update battery
        tvTemp.setText("Battery: " + BatteryPercent + "% ");
        //update heartrate //
        tvHeartRate.setText(HeartRate);

        // update MAC
        tvMacAddress.setText("MAC: " + macId);

        // update Device Type
        tvDeviceType.setText("Device: " + DeviceType);

        //update bleName
        if (bleName != null){
            tvDeviceName.setText("Name: " + bleName);
        }



    }

    void setDeviceImage() {
        String DeviceType = prefs.getString("FirmwareVersion", "not loaded");

        //Image
        if (DeviceType != null){
            if (DeviceType.equals("PineTime")){
                DeviceImageButton.setImageResource(R.drawable.imagepinetime);
            } else if (DeviceType.equals("P8")){
                DeviceImageButton.setImageResource(R.drawable.imagep8);
            } else if (DeviceType.equals("P22")){
                DeviceImageButton.setImageResource(R.drawable.imagep22);
            } else {
                DeviceImageButton.setImageResource(R.drawable.noimage);
            }
        }
    }

    public void sendBLEcmd(String message) {
        Intent intent = new Intent("MSGtoServiceIntentBLEcmd");
        if (message != null)
            intent.putExtra("MSGtoService", message);
        localBroadcastManager.sendBroadcast(intent);
    }


    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (BLEservice.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}