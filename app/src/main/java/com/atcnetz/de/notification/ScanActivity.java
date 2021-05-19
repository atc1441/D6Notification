package com.atcnetz.de.notification;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.atcnetz.de.notification.util.BleUtil;
import com.atcnetz.de.notification.util.ScannedDevice;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class ScanActivity extends Activity implements BluetoothAdapter.LeScanCallback {
    private BluetoothAdapter mBTAdapter;
    private DeviceAdapter mDeviceAdapter;
    private boolean mIsScanning;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDeviceAdapter.update(result.getDevice(), result.getRssi());
                }
            });
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_scan);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBTAdapter.isEnabled())stopScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDeviceAdapter.clear();
        if (mBTAdapter.isEnabled())startScan();
    }

    @Override
    public void onLeScan(final BluetoothDevice newDeivce, final int newRssi,
                         final byte[] newScanRecord) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceAdapter.update(newDeivce, newRssi);
            }
        });
    }

    private void init() {
        if (!BleUtil.isBLESupported(this)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        BluetoothManager manager = BleUtil.getManager(this);
        if (manager != null) {
            mBTAdapter = manager.getAdapter();
        }
        if (mBTAdapter == null) {
            Toast.makeText(this, R.string.bt_unavailable, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!mBTAdapter.isEnabled()) {
            Toast.makeText(this, R.string.bt_disabled, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ListView deviceListView = findViewById(R.id.list);
        mDeviceAdapter = new DeviceAdapter(this, R.layout.listitem_device,
                new ArrayList<ScannedDevice>());
        deviceListView.setAdapter(mDeviceAdapter);
        deviceListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterview, View view, int position, long id) {
                ScannedDevice item = mDeviceAdapter.getItem(position);
                if (item != null) {
                    // stop before change Activity
                    stopScan();

                    Intent intent = new Intent();
                    BluetoothDevice selectedDevice = item.getDevice();
                    intent.putExtra(MainActivity.EXTRA_BLUETOOTH_DEVICE, selectedDevice);
                    setResult(4, intent);
                    finish();
                }
            }
        });
        stopScan();
        startScan();
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void startScan() {
        if ((mBTAdapter != null) && (!mIsScanning)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    mBTAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
                    mIsScanning = true;
                    setProgressBarIndeterminateVisibility(true);
                    invalidateOptionsMenu();
                }
            } else {
                mBTAdapter.startLeScan(this);
                mIsScanning = true;
                setProgressBarIndeterminateVisibility(true);
                invalidateOptionsMenu();
            }
        }
    }

    void stopScan() {
        if (mBTAdapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mBTAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
            } else {
                mBTAdapter.stopLeScan(this);
            }
        }
        mIsScanning = false;
        setProgressBarIndeterminateVisibility(false);
        invalidateOptionsMenu();
    }

}
