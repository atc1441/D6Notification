package com.atcnetz.de.notification;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.SyncStateContract;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.atcnetz.de.notification.util.BleUtil;
import com.atcnetz.de.notification.util.http_request;
import com.atcnetz.de.notification.util.ExternalStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static java.lang.Thread.sleep;

public class BLEservice extends Service {
    public static final String CHANNEL_ID = "D6NotificationForegroundService";

    Vibrator v;
    Context Context;
    private boolean recoverState;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private BluetoothAdapter mBTAdapter;
    private BluetoothGatt mConnGatt;
    private int mStatus;
    boolean isRunning = false;
    String BleDevice = "00:00:00:00:00:00";
    int tryBle = 0;
    String bleBigCMD = "";
    SharedPreferences prefs;
    boolean isConnected = false;
    String savedBleText = "";
    long lastTime;
    String lastReceiveTime = "";
    boolean hasSend = false;

    public static class StockDFUUUIDs {
        static final UUID Main_Characteristic_Write = UUID.fromString("00000001-0000-1000-8000-00805f9b34fb");
        static final UUID Main_Characteristic_Notify = UUID.fromString("00000002-0000-1000-8000-00805f9b34fb");
        static final UUID Notify_Config = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        static final UUID Main_Service = UUID.fromString("0000190a-0000-1000-8000-00805f9b34fb");
    }

    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver broadcastReceiver;
    private BroadcastReceiver broadcastReceiverMain;
    private BroadcastReceiver broadcastReceiverBLEcmd;


    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            assert action != null;
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        postToastMessageLog("Bluetooth now Off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        postToastMessageLog("Bluetooth now On");
                        reconnectBLE();
                        break;
                }

            }
        }
    };
    private final BluetoothGattCallback mGattcallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            postToastMessageLog("Connection Changed: " + status + " " + newState);
            if (recoverState) {
                recoverState = false;
                BleDevice = prefs.getString("MacId", "00:00:00:00:00:00");
                reconnectBLE();
            } else {

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    isConnected = true;
                    tryBle = 0;
                    mStatus = newState;
                    mConnGatt.discoverServices();
                } else {
                    isConnected = false;
                }
                if (status == 133 && newState == 0) {
                    isConnected = false;
                    tryBle++;
                } else tryBle = 0;
                if (tryBle >= 10) {
                    tryBle = 0;
                    postToastMessage("Could Not Connect to D6 tracker, please reselect your tracker, or is it off? maybe not in range?");
                    recoverBLE();
                } else {
                    if (!isConnected) {
                        reconnectBLE();
                    }
                }
                postToastMessage((isConnected ? "Connected" : "Disconnected"));
                setNotify();
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                postToastMessageLog("CharsReaded: " + status);
            }
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] receiverData = characteristic.getValue();
            postToastMessageLog("Got BLE Msg: " + new String(receiverData, StandardCharsets.UTF_8));
            filterResponse(new String(receiverData, StandardCharsets.UTF_8));
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            postToastMessageLog("Got BLE Services: " + status);
            for (BluetoothGattService service : gatt.getServices()) {
                if ((service == null) || (service.getUuid() == null)) {
                    continue;
                }
                if (StockDFUUUIDs.Main_Service.toString().equalsIgnoreCase(service.getUuid().toString())) {
                    setNotifyCharacteristic(true);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            try {
                                sleep(300);
                                addCMD("AT+VER");
                                sleep(30);
                                addCMD("AT+BATT");
                                sleep(30);
                                addCMD("AT+PACE");
                                sleep(30);
                                addCMD("AT+FINDPHONE=1");
                                sleep(30);
                                addCMD("AT+HANDSUP=" + prefs.getInt("DisplayMovement", 0));
                                sleep(30);
                                String date = getCurrentDateTime();
                                postToastMessageLog("Sending Date: " + date);
                                addCMD("AT+DT=" + date);
                                if (!savedBleText.equals("")) {
                                    sleep(300);
                                    addCMD("AT+PUSH=0," + savedBleText + ",0");
                                    savedBleText = "";
                                }
                                readRssi();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            postToastMessageLog("Wrote BLE Char: " + status);
            sendNextPart();
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            postToastMessageLog("RemoteRssi: " + rssi);
            super.onReadRemoteRssi(gatt, rssi, status);
        }

    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        postToastMessageLog("service on start command");
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                int IDnr = intent.getIntExtra("IDnr", 0);
                String PackageName = intent.getStringExtra("PackageName1");
                String tickerText = intent.getStringExtra("tickerText1");
                String Text = intent.getStringExtra("Text1");
                String Title = intent.getStringExtra("Title1");
                String appName = "Unknown";
                PackageManager packageManager = getApplicationContext().getPackageManager();
                try {
                    assert PackageName != null;
                    appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(PackageName, PackageManager.GET_META_DATA));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                String bleText = "";
                if(appName == null) appName = "MissingNo.";
                if(Title == null) Title = "Empty";
                if(Text == null) Text = "Empty";
                if(tickerText == null) tickerText = "Empty";

                switch (prefs.getInt("NotificationMode", 0)) {
                    case 0:
                    default:
                        if (Title != null) bleText = Title;
                        break;
                    case 1:
                        if (tickerText != null) bleText = tickerText;
                        break;
                    case 2:
                        if (Text != null) bleText = Text;
                        break;
                    case 3:
                        if (appName != null) bleText = appName;
                        break;
                }

                if (bleText.equals("")) bleText = "Notification";
                if (bleText.length() >= 200) bleText = bleText.substring(0, 200);

                String disableApps = prefs.getString("enabledApps", "");
                String[] items = disableApps.split(";");
                System.out.println(bleText);
                //bleText = bleText.replaceAll("[^a-zA-Z0-9ÄÜÖäöü :;_%&()!?ß+*/]", ".");
                bleText = bleText.replaceAll(",", ".");
                System.out.println(bleText);
                if (Arrays.asList(items).contains(PackageName) && prefs.getBoolean("isNotificationEnabled", true)) {
                    postToastMessage("NotifyService:\r\nTitle: " + Title + "\r\nTickertext: " + tickerText + "\r\nText: " + Text + "\r\nAppname: " + appName + "\r\nID:" + IDnr);

                    if (isDisturbing()) {
                        lastTime = System.currentTimeMillis();
                        postToastMessage("Do not disturb is Active");
                    } else {
                        if (isConnected) {
                            if (System.currentTimeMillis() - lastTime > 1000) {
                                lastTime = System.currentTimeMillis();
                                try {
                                    addCMD("AT+PUSH=0," + bleText + ",0");

                                    sleep(30);
                                    addCMD("AT+NAME=" + appName);
                                    sleep(30);
                                    addCMD("AT+TITL=" + Title);
                                    sleep(30);
                                    addCMD("AT+TICK=" + tickerText);
                                    sleep(30);
                                    addCMD("AT+BODY=" + Text);
                                }catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                postToastMessageLog("will not send now because timout not reached for new message");
                            }
                        } else {
                            savedBleText = bleText;
                            postToastMessageLog("Not Connected will send notifi later");
                            //reconnectBLE();
                        }
                    }
                }
            }
        };
        broadcastReceiverMain = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                String MsgFrMain = intent.getStringExtra("MSGtoService");
                postToastMessage("MSG From Main: " + MsgFrMain);
                assert MsgFrMain != null;
                if (MsgFrMain.equals("GetBatt")) addCMD("AT+BATT");
                if (MsgFrMain.equals("SendVibrate")) addCMD("AT+MOTOR=13");

            }
        };
        broadcastReceiverBLEcmd = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                String MsgFrMain = intent.getStringExtra("MSGtoService");
                if (MsgFrMain != null) addCMD(MsgFrMain);
            }
        };
        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        BleDevice = prefs.getString("MacId", "00:00:00:00:00:00");
        if (!isRunning & prefs.getBoolean("isNotificationEnabled", true)) {
            isRunning = true;
            reconnectBLE();
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    if (!hasSend) {
                        postToastMessageLog("Error getting periodic Battery");
                        //recoverBLE();
                        //setNotify();
                        final Handler handler = new Handler();
                        TimerTask timertask = new TimerTask() {
                            @Override
                            public void run() {
                                handler.post(new Runnable() {
                                    public void run() {
                                        startService(new Intent(BLEservice.this, BLEservice.class));
                                    }
                                });
                            }
                        };
                        Timer timer = new Timer();
                        timer.schedule(timertask, 1000);

                        stopForeground(true);
                        BLEservice.this.stopSelf();
                    }
                    hasSend = false;
                    try {
                        addCMD("AT+PACE");
                        sleep(30);
                        addCMD("AT+BATT");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    //reconnectBLE();
                }
                handler.postDelayed(this, 60 * 1000);
            }
        }, 60 * 1000);

        createNotificationChannel();
        startForeground(1337, getNotifi("Started please connect to Watch"));

        registerReceiver((bluetoothStateReceiver), new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver), new IntentFilter("com.service.result"));
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiverMain), new IntentFilter("MSGtoServiceIntent"));
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiverBLEcmd), new IntentFilter("MSGtoServiceIntentBLEcmd"));
        return START_STICKY;
    }

    private void setNotify() {
        updateNitify((isConnected ? "Connected" : "Disconnected") + " - Steps: " + prefs.getString("Steps", "0") + " - Battery: " + prefs.getString("BatteryPercent", "xxx") + "% " + (prefs.getBoolean("isDebugEnabled", false) ? "- Last Answer: " + lastReceiveTime : ""));
    } //TODO look here for step data

    private void setLastReceiveTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getDefault());
        lastReceiveTime = sdf.format(Calendar.getInstance().getTime());
    }

    private void updateNitify(String text) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert mNotificationManager != null;
        mNotificationManager.notify(1337, getNotifi(text));
    }

    private Notification getNotifi(String text) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification;
        if (isConnected)
            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(text)
                    .setVibrate(new long[0])
                    .setNotificationSilent()
                    .setOnlyAlertOnce(true)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .build();
        else
            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(text)
                    .setVibrate(new long[0])
                    .setNotificationSilent()
                    .setOnlyAlertOnce(true)
                    .setSmallIcon(R.drawable.ic_launcher_nc)
                    .setContentIntent(pendingIntent)
                    .build();
        return notification;
    }

    public void filterResponse(String response1) {
        String response = response1.replaceAll("[^\\x20-\\x7E]", "");
        if (response.length() >= 5) {
            setLastReceiveTime();
            setNotify();
            if (isInCMD(response, "AT+VER")) {
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("FirmwareVersion", response.substring(7));
                editor.apply();
                postToastMessage("D6 Firmware Version: " + response.substring(7));
            } else if (isInCMD(response, "AB+VER")) {
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("BTFirmwareVersion", response.substring(7));
                editor.apply();
                postToastMessage("D6 Bootloader Version: " + response.substring(7));
            } else if (isInCMD(response, "AT+BATT")) {
                hasSend = true;
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("BatteryPercent", response.substring(8));
                editor.apply();
                postToastMessage("Battery: " + response.substring(8) + "%");
            } else if (isInCMD(response, "AT+PACE")) {
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("Steps", response.substring(8));
                editor.apply();
                postToastMessage("Steps: " + response.substring(8));
            } else if (isInCMD(response, "AT+BEEP")) {
                v.vibrate(400);
                postToastMessage("You Make me go Bzzzzz");
            } else if (isInCMD(response, "AT+DT")) {
                postToastMessage("Setting Date successful");
            } else if (isInCMD(response, "AT+HTTP")) {
                postToastMessage("Got HTTP cmd...");
                if (prefs.getBoolean("http_enable", false)) {
                    String req_cmd = prefs.getString("http_url_edit", "https://google.com?testcmd=") + response.substring(8);
                    postToastMessage("Requesting: " + req_cmd);
                    String req_answer = http_request.get_http_request(req_cmd);
                    if(req_answer.length()>300)req_answer=req_answer.substring(0,300);
                    postToastMessage("Req Answer: "+req_answer);
                    if(prefs.getBoolean("http_answer_enable", false)){
                        addCMD("AT+HTTP="+req_answer);
                    }
                } else {
                    postToastMessage("HTTP requests not enabled");
                }
            } else if (isInCMD(response, "AT+LOG")) {
                postToastMessage("Got LOG cmd...");
                if (prefs.getBoolean("savelog_enable", false)) {
                    String log_cmd = response.substring(7);
                    postToastMessage("Logging: " + log_cmd+ " to file: "+prefs.getString("logname_edit", "d6_log.txt"));
                    postToastMessage(ExternalStorage.append_log(prefs.getString("logname_edit", "d6_log.txt"),log_cmd,this));
                } else {
                    postToastMessage("Logging not enabled");
                }
            }
        }
    }

    public boolean isInCMD(String fullCmd, String searchedCmd) {
        int searchedLength = searchedCmd.length();
        return fullCmd.substring(0, searchedLength).equals(searchedCmd);
    }

    public static String getCurrentDateTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }

    @Override
    public void onCreate() {
        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        postToastMessage("Service Started");
    }

    void reconnectBLE() {
        disconnectBLE();
        init();
    }

    void disconnectBLE() {
        if (mConnGatt != null) {
            Method refresh = null;
            try {
                refresh = mConnGatt.getClass().getMethod("refresh");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            try {
                assert refresh != null;
                refresh.invoke(mConnGatt);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        if (mBTAdapter != null)
            mBTAdapter = null;
        bleBigCMD = "";
        isConnected = false;
        hasSend = false;
        if (mConnGatt != null) {
            mConnGatt.disconnect();
            mConnGatt.close();
            mConnGatt = null;
            mStatus = BluetoothProfile.STATE_DISCONNECTED;
        }
    }


    @Override
    public void onDestroy() {
        postToastMessage("serviceOnDestroy");
        unregisterReceiver(bluetoothStateReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverMain);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverBLEcmd);
        isRunning = false;
        disconnectBLE();

        super.onDestroy();
    }

    public boolean isDisturbing() {
        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        if (!prefs.getBoolean("DoNotDisturb", false))
            return false;
        String startHM = prefs.getString("notDistStartH", "22") + ":" + prefs.getString("notDistStartM", "00");
        String stopHM = prefs.getString("notDistStopH", "05") + ":" + prefs.getString("notDistStopM", "00");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getDefault());
        String timeNow = sdf.format(Calendar.getInstance().getTime());
        boolean comp = isTimeBetweenTwoTime(startHM, stopHM, timeNow);
        postToastMessageLog(String.format("Start: %s Stop: %s time: %s comp: %s", startHM, stopHM, timeNow, isTimeBetweenTwoTime(startHM, stopHM, timeNow)));
        return comp;

    }

    public static boolean isTimeBetweenTwoTime(String startHM, String stopHM, String currentTime) {
        int startHi = Integer.parseInt(startHM.split(":")[0]);
        int startMi = Integer.parseInt(startHM.split(":")[1]);
        int stopHi = Integer.parseInt(stopHM.split(":")[0]);
        int stopMi = Integer.parseInt(stopHM.split(":")[1]);
        int timeNowHi = Integer.parseInt(currentTime.split(":")[0]);
        int timeNowMi = Integer.parseInt(currentTime.split(":")[1]);
        int start = (startHi * 60) + startMi;
        int stop = (stopHi * 60) + stopMi;
        int currT = (timeNowHi * 60) + timeNowMi;

        if (currT > start && currT < stop) {
            return true;
        }
        if (stop < start && !(currT < start && currT > stop))
            return true;
        return false;
    }

    private void recoverBLE() {
        postToastMessageLog("Trying to recover now");
        recoverState = true;
        BleDevice = "FF:FF:FF:FF:FF:FF";
        reconnectBLE();
    }

    private void init() {

        if (BleDevice.equals("00:00:00:00:00:00")) {
            postToastMessage("No Fitness Tracker selected, please Click on SELECT DEVICE");
            return;
        }
        if (!BleUtil.isBLESupported(this)) {
            postToastMessage("BLE not Supported");
        }
        BluetoothManager manager = BleUtil.getManager(this);
        if (manager != null) {
            mBTAdapter = manager.getAdapter();
        }
        if (mBTAdapter == null) {
            postToastMessage("BT unavailable");
            return;
        }
        if (!mBTAdapter.isEnabled()) {
            postToastMessage("Bluetooth not Enabled!");
            return;
        }
        if (!recoverState) postToastMessage("BLEdevice: " + BleDevice);
        BluetoothDevice mDevice = mBTAdapter.getRemoteDevice(BleDevice);
        if ((mConnGatt == null) && (mStatus == BluetoothProfile.STATE_DISCONNECTED)) {
            mConnGatt = mDevice.connectGatt(this, false, mGattcallback);
            mStatus = BluetoothProfile.STATE_CONNECTING;
        } else {
            if (mConnGatt != null) {
                Method refresh = null;
                try {
                    refresh = mConnGatt.getClass().getMethod("refresh");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                try {
                    assert refresh != null;
                    refresh.invoke(mConnGatt);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                mConnGatt.connect();
                mConnGatt.discoverServices();
            }
        }
        postToastMessage("BLEconnect");
    }

    byte[] bleBigCMDbyte = {};

    public void addCMD(String cmdCode) {
        if (cmdCode != null) {
            cmdCode = cmdCode + "\r\n";
            byte[] cmdByteArray = {};
            try {
                cmdByteArray = cmdCode.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (bleBigCMDbyte.length == 0) {
                bleBigCMDbyte = cmdByteArray;
                sendNextPart();
            } else {
                bleBigCMDbyte = concat(bleBigCMDbyte, cmdByteArray);
            }
        }
    }

    public static byte[] concat(byte[] a, byte[] b) {
        int lenA = a.length;
        int lenB = b.length;
        byte[] c = Arrays.copyOf(a, lenA + lenB);
        System.arraycopy(b, 0, c, lenA, lenB);
        return c;
    }

    void sendNextPart() {
        if (isConnected) {

            if (!(bleBigCMDbyte.length == 0)) {

                int strLength = bleBigCMDbyte.length;
                int end = strLength;
                int DATA_LENGTH = 20;
                if (DATA_LENGTH < strLength) {
                    end = DATA_LENGTH;
                }
                byte[] writeByte = Arrays.copyOfRange(bleBigCMDbyte, 0, end);
                if (DATA_LENGTH < strLength) {
                    bleBigCMDbyte = Arrays.copyOfRange(bleBigCMDbyte, end, strLength);
                } else {
                    bleBigCMDbyte = new byte[]{};
                }
                try {
                    postToastMessageLog("SendBLE: " + new String(writeByte, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    postToastMessageLog("SendBLE: ERROR");
                }

                writeCharacteristic(writeByte);

            } else
                postToastMessageLog("Sending BLE done");

        } else {
            postToastMessage("Not Connected, please check that.");
            //reconnectBLE();
        }
    }

    public void writeCharacteristic(byte[] data) {
        if (isRunning) {
            if (data == null) {
                return;
            } else {
                BluetoothGatt gatt = mConnGatt;
                if (gatt == null) {
                    return;
                }
                BluetoothGattService service = gatt.getService(StockDFUUUIDs.Main_Service);
                if (service == null) {
                    return;
                }
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(StockDFUUUIDs.Main_Characteristic_Write);
                if (characteristic == null || (characteristic.getProperties() & 12) == 0) {
                    return;
                } else if (!characteristic.setValue(data)) {
                    return;
                } else if (!gatt.writeCharacteristic(characteristic)) {
                    return;
                } else {
                    return;
                }
            }
        } else {
            return;
        }
    }

    public void setNotifyCharacteristic(boolean enabled) {
        BluetoothGatt gatt = mConnGatt;
        if (gatt != null) {
            BluetoothGattService service = gatt.getService(BLEservice.StockDFUUUIDs.Main_Service);
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(BLEservice.StockDFUUUIDs.Main_Characteristic_Notify);
                if (characteristic != null) {
                    boolean success = gatt.setCharacteristicNotification(characteristic, enabled);
                    if (success) {
                        postToastMessageLog("Got an answer");
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BLEservice.StockDFUUUIDs.Notify_Config);
                        if (descriptor == null) {
                            postToastMessageLog("descriptor is null");
                            return;
                        }
                        int properties = characteristic.getProperties();
                        if ((properties & 32) != 0) {
                            descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : new byte[]{(byte) 0, (byte) 0});
                        } else if ((properties & 16) != 0) {
                            descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{(byte) 0, (byte) 0});
                        } else {
                            descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{(byte) 0, (byte) 0});
                        }
                        gatt.writeDescriptor(descriptor);
                    }
                }
            }
        }
    }

    public void readRssi() {
        if (this.mConnGatt != null) {
            this.mConnGatt.readRemoteRssi();
        }
    }

    public void postToastMessageLog(final String message) {
        if (prefs.getBoolean("isDebugEnabled", false)) postToastMessage(message);
    }

    public void postToastMessage(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                appendLog(message);
                sendResult(message);
            }
        });
    }

    private void sendResult(String message) {
        Intent intent = new Intent("ToActivity1");
        if (message != null)
            intent.putExtra("ToActivity", message);
        localBroadcastManager.sendBroadcast(intent);
    }

    public void appendLog(String text) {
        text += "\r\n";
        long length = new File(getFilesDir().getAbsolutePath() + "/D6notfifier.log").length();
        if (length >= 13000) clearLog();
        FileOutputStream fos = null;
        try {
            fos = openFileOutput("D6notfifier.log", Context.MODE_APPEND + MODE_PRIVATE);
            fos.write(text.getBytes());
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
