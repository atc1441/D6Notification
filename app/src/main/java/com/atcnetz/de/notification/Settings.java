package com.atcnetz.de.notification;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class Settings extends Activity {

    private LocalBroadcastManager localBroadcastManager;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    //EditText CustomBLEcmd;
    int VibrationIntens = 0;
    int NotificationMode = 0;
    //int DisplayMode = 0;
    //int MovementDisplay = 0;
    int ContrastDisplay = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        /*CustomBLEcmd = findViewById(R.id.editText);
        Button button = findViewById(R.id.sendCMDbuttonID);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBLEcmd(CustomBLEcmd.getText().toString());
            }
        });

         */

        Button set_filter_button = findViewById(R.id.set_filter);
        set_filter_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    Intent intent = new Intent(Settings.this, app_filter.class);
                    startActivity(intent);
            }
        });

        Button alarm_button = findViewById(R.id.alarmSettingsButtonID);
        alarm_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                    Intent intent = new Intent(Settings.this, D6SettingsActivity.class);
                    startActivityForResult(intent, 34);

            }
        });

        Button http_settings_button = findViewById(R.id.logAPISettingsButtonID);
        http_settings_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.this, com.atcnetz.de.notification.httplogset.class);
                startActivityForResult(intent, 2);
            }
        });

        Button select_apps_button = findViewById(R.id.selectAppsButtonID);
        select_apps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.this, NofiticationPicker.class);
                startActivityForResult(intent, 2);
            }
        });








        initBattandFW();
        initVibration();
        initContrast();
        //initMovementDisplay();
        //initDisplayMode();
        initNotifiContent();

        initDoNotDisturb();

    }


    void initBattandFW() {
        String BatteryPercent = prefs.getString("BatteryPercent", "xxx");
        String FirmwareVersion = prefs.getString("FirmwareVersion", "not loaded");

        TextView textView = findViewById(R.id.Battery);
        textView.setText("Battery of D6: " + BatteryPercent + "%");
        TextView textView1 = findViewById(R.id.Version);
        textView1.setText("Firmware on D6: " + FirmwareVersion);
    }

    void initVibration() {

        VibrationIntens = prefs.getInt("VibrationIntens", 11);

        RadioButton Radiobutton1 = findViewById(R.id.vibration1);
        Radiobutton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBLEcmd("AT+MOTOR=11");
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putInt("VibrationIntens", 11);
                editor.apply();
            }
        });
        RadioButton Radiobutton2 = findViewById(R.id.vibration2);
        Radiobutton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBLEcmd("AT+MOTOR=12");
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putInt("VibrationIntens", 12);
                editor.apply();
            }
        });
        RadioButton Radiobutton3 = findViewById(R.id.vibration3);
        Radiobutton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBLEcmd("AT+MOTOR=13");
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putInt("VibrationIntens", 13);
                editor.apply();
            }
        });

        if (VibrationIntens == 0)
            Radiobutton1.setChecked(true);
        else if (VibrationIntens == 1)
            Radiobutton2.setChecked(true);
        else Radiobutton3.setChecked(true);

    }

    void initContrast() {

        ContrastDisplay = prefs.getInt("ContrastDisplay", 0);

        RadioButton Radiobutton1 = findViewById(R.id.contrast1);
        Radiobutton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBLEcmd("AT+CONTRAST=100");
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putInt("ContrastDisplay", 0);
                editor.apply();
            }
        });
        RadioButton Radiobutton2 = findViewById(R.id.contrast2);
        Radiobutton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBLEcmd("AT+CONTRAST=175");
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putInt("ContrastDisplay", 1);
                editor.apply();
            }
        });
        RadioButton Radiobutton3 = findViewById(R.id.contrast3);
        Radiobutton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBLEcmd("AT+CONTRAST=255");
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putInt("ContrastDisplay", 2);
                editor.apply();
            }
        });

        if (ContrastDisplay == 0)
            Radiobutton1.setChecked(true);
        else if (ContrastDisplay == 1)
            Radiobutton2.setChecked(true);
        else Radiobutton3.setChecked(true);

    }

/*
    void initMovementDisplay() {

        MovementDisplay = prefs.getInt("DisplayMovement", 0);

        RadioButton Radiobutton1 = findViewById(R.id.MovementOff);
        Radiobutton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBLEcmd("AT+HANDSUP=0");
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putInt("DisplayMovement", 0);
                editor.apply();
            }
        });
        RadioButton Radiobutton2 = findViewById(R.id.MovementOn);
        Radiobutton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBLEcmd("AT+HANDSUP=2");
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putInt("DisplayMovement", 2);
                editor.apply();
            }
        });

        if (MovementDisplay == 0)
            Radiobutton1.setChecked(true);
        else Radiobutton2.setChecked(true);

    }

    void initDisplayMode() {

        DisplayMode = prefs.getInt("DisplayMode", 0);

        RadioButton Radiobutton1 = findViewById(R.id.vibration11);
        Radiobutton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBLEcmd("AT+DISMOD=1");
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putInt("DisplayMode", 0);
                editor.apply();
            }
        });
        RadioButton Radiobutton2 = findViewById(R.id.vibration12);
        Radiobutton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBLEcmd("AT+DISMOD=2");
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putInt("DisplayMode", 1);
                editor.apply();
            }
        });
        RadioButton Radiobutton3 = findViewById(R.id.vibration13);
        Radiobutton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBLEcmd("AT+DISMOD=3");
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putInt("DisplayMode", 2);
                editor.apply();
            }
        });

        if (DisplayMode == 0)
            Radiobutton1.setChecked(true);
        else if (DisplayMode == 1)
            Radiobutton2.setChecked(true);
        else Radiobutton3.setChecked(true);

    }
*/
    void initNotifiContent() {

        NotificationMode = prefs.getInt("NotificationMode", 0);

        RadioButton Radiobutton1 = findViewById(R.id.notification1);
        Radiobutton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putInt("NotificationMode", 0);
                editor.apply();
            }
        });
        RadioButton Radiobutton2 = findViewById(R.id.notification2);
        Radiobutton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putInt("NotificationMode", 1);
                editor.apply();
            }
        });
        RadioButton Radiobutton3 = findViewById(R.id.notification3);
        Radiobutton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putInt("NotificationMode", 2);
                editor.apply();
            }
        });
        RadioButton Radiobutton4 = findViewById(R.id.notification4);
        Radiobutton4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putInt("NotificationMode", 3);
                editor.apply();
            }
        });

        if (NotificationMode == 0)
            Radiobutton1.setChecked(true);
        else if (NotificationMode == 1)
            Radiobutton2.setChecked(true);
        else if (NotificationMode == 2)
            Radiobutton3.setChecked(true);
        else Radiobutton4.setChecked(true);

    }

    void initDoNotDisturb() {
        final EditText notDistStartH = findViewById(R.id.notDistStartH);
        notDistStartH.setFilters(new InputFilter[]{new FilterTextH(0, 23)});
        final EditText notDistStartM = findViewById(R.id.notDistStartM);
        notDistStartM.setFilters(new InputFilter[]{new FilterTextH(0, 59)});
        final EditText notDistStopH = findViewById(R.id.notDistStopH);
        notDistStopH.setFilters(new InputFilter[]{new FilterTextH(0, 23)});
        final EditText notDistStopM = findViewById(R.id.notDistStopM);
        notDistStopM.setFilters(new InputFilter[]{new FilterTextH(0, 59)});
        final CheckBox enableNotDisturb = findViewById(R.id.enableNotDisturb);
        Button saveDisturb = findViewById(R.id.saveDisturb);

        enableNotDisturb.setChecked(prefs.getBoolean("DoNotDisturb", false));
        notDistStartH.setText(prefs.getString("notDistStartH","22"));
        notDistStartM.setText(prefs.getString("notDistStartM","00"));
        notDistStopH.setText(prefs.getString("notDistStopH","05"));
        notDistStopM.setText(prefs.getString("notDistStopM","00"));

        saveDisturb.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
            editor.putString("notDistStartH", notDistStartH.getText().toString());
            editor.putString("notDistStartM", notDistStartM.getText().toString());
            editor.putString("notDistStopH", notDistStopH.getText().toString());
            editor.putString("notDistStopM", notDistStopM.getText().toString());
            editor.putBoolean("DoNotDisturb", enableNotDisturb.isChecked());
            editor.apply();
        }});
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    public void sendBLEcmd(String message) {
        Intent intent = new Intent("MSGtoServiceIntentBLEcmd");
        if (message != null)
            intent.putExtra("MSGtoService", message);
        localBroadcastManager.sendBroadcast(intent);
    }
}
