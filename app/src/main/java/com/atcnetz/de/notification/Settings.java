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
    EditText CustomBLEcmd;
    int VibrationIntens = 0;
    int DisplayMode = 0;
    int NotificationMode = 0;
    int MovementDisplay = 0;
    int ContrastDisplay = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        CustomBLEcmd = findViewById(R.id.editText);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBLEcmd(CustomBLEcmd.getText().toString());
            }
        });
        Button set_filter_button = findViewById(R.id.set_filter);
        set_filter_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    Intent intent = new Intent(Settings.this, app_filter.class);
                    startActivity(intent);
            }
        });
        initBattandFW();
        initVibration();
        initContrast();
        initMovementDisplay();
        initDisplayMode();
        initNotifiContent();
        initAlarm0();
        initAlarm1();
        initAlarm2();
        initAlarm3();
        initAlarm4();
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

    Integer AlarmDaysM1 = 0;
    EditText Alarm1h;
    EditText Alarm1m;
    CheckBox enableAlarm1;
    CheckBox Alarm1Mo;
    CheckBox Alarm1Di;
    CheckBox Alarm1Mi;
    CheckBox Alarm1Do;
    CheckBox Alarm1Fr;
    CheckBox Alarm1Sa;
    CheckBox Alarm1So;
    void initAlarm0() {
        Alarm1h = findViewById(R.id.Alarm1h);
        Alarm1h.setFilters(new InputFilter[]{new FilterTextH(0, 23)});
        Alarm1m = findViewById(R.id.Alarm1m);
        Alarm1m.setFilters(new InputFilter[]{new FilterTextH(0, 59)});
        enableAlarm1 = findViewById(R.id.enableAlarm1);
        Alarm1Mo = findViewById(R.id.Alarm1Mo);
        Alarm1Di = findViewById(R.id.Alarm1Di);
        Alarm1Mi = findViewById(R.id.Alarm1Mi);
        Alarm1Do = findViewById(R.id.Alarm1Do);
        Alarm1Fr = findViewById(R.id.Alarm1Fr);
        Alarm1Sa = findViewById(R.id.Alarm1Sa);
        Alarm1So = findViewById(R.id.Alarm1So);
        Alarm1h.setText(prefs.getString("Alarm1h", "07"));
        Alarm1m.setText(prefs.getString("Alarm1m", "00"));
        enableAlarm1.setChecked(prefs.getBoolean("enableAlarm1", false));
        Alarm1Mo.setChecked(prefs.getBoolean("Alarm1Mo", true));
        Alarm1Di.setChecked(prefs.getBoolean("Alarm1Di", true));
        Alarm1Mi.setChecked(prefs.getBoolean("Alarm1Mi", true));
        Alarm1Do.setChecked(prefs.getBoolean("Alarm1Do", true));
        Alarm1Fr.setChecked(prefs.getBoolean("Alarm1Fr", true));
        Alarm1Sa.setChecked(prefs.getBoolean("Alarm1Sa", true));
        Alarm1So.setChecked(prefs.getBoolean("Alarm1So", true));

        Button SaveButton = findViewById(R.id.saveAlarm1);
        SaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("Alarm1h", Alarm1h.getText().toString());
                editor.putString("Alarm1m", Alarm1m.getText().toString());
                editor.putBoolean("enableAlarm1", enableAlarm1.isChecked());
                editor.putBoolean("Alarm1Mo", Alarm1Mo.isChecked());
                editor.putBoolean("Alarm1Di", Alarm1Di.isChecked());
                editor.putBoolean("Alarm1Mi", Alarm1Mi.isChecked());
                editor.putBoolean("Alarm1Do", Alarm1Do.isChecked());
                editor.putBoolean("Alarm1Fr", Alarm1Fr.isChecked());
                editor.putBoolean("Alarm1Sa", Alarm1Sa.isChecked());
                editor.putBoolean("Alarm1So", Alarm1So.isChecked());
                editor.apply();
                AlarmDaysM1=0;
                AlarmDaysM1 += (enableAlarm1.isChecked() ? 1 : 0) + (Alarm1Sa.isChecked() ? 2 : 0) + (Alarm1Fr.isChecked() ? 4 : 0) + (Alarm1Do.isChecked() ? 8 : 0) + (Alarm1Mi.isChecked() ? 16 : 0) + (Alarm1Di.isChecked() ? 32 : 0) + (Alarm1Mo.isChecked() ? 64 : 0) + (Alarm1So.isChecked() ? 128 : 0);
                sendBLEcmd("AT+ALARM=00" + String.format("%02d", Integer.parseInt(Alarm1h.getText().toString())) + String.format("%02d", Integer.parseInt(Alarm1m.getText().toString())) + String.format("%02X", AlarmDaysM1));
            }
        });

    }

    Integer AlarmDaysM2 = 0;
    EditText Alarm2h;
    EditText Alarm2m;
    CheckBox enableAlarm2;
    CheckBox Alarm2Mo;
    CheckBox Alarm2Di;
    CheckBox Alarm2Mi;
    CheckBox Alarm2Do;
    CheckBox Alarm2Fr;
    CheckBox Alarm2Sa;
    CheckBox Alarm2So;
    void initAlarm1() {
        Alarm2h = findViewById(R.id.Alarm2h);
        Alarm2h.setFilters(new InputFilter[]{new FilterTextH(0, 23)});
        Alarm2m = findViewById(R.id.Alarm2m);
        Alarm2m.setFilters(new InputFilter[]{new FilterTextH(0, 59)});
        enableAlarm2 = findViewById(R.id.enableAlarm2);
        Alarm2Mo = findViewById(R.id.Alarm2Mo);
        Alarm2Di = findViewById(R.id.Alarm2Di);
        Alarm2Mi = findViewById(R.id.Alarm2Mi);
        Alarm2Do = findViewById(R.id.Alarm2Do);
        Alarm2Fr = findViewById(R.id.Alarm2Fr);
        Alarm2Sa = findViewById(R.id.Alarm2Sa);
        Alarm2So = findViewById(R.id.Alarm2So);
        Alarm2h.setText(prefs.getString("Alarm2h", "07"));
        Alarm2m.setText(prefs.getString("Alarm2m", "00"));
        enableAlarm2.setChecked(prefs.getBoolean("enableAlarm2", false));
        Alarm2Mo.setChecked(prefs.getBoolean("Alarm2Mo", true));
        Alarm2Di.setChecked(prefs.getBoolean("Alarm2Di", true));
        Alarm2Mi.setChecked(prefs.getBoolean("Alarm2Mi", true));
        Alarm2Do.setChecked(prefs.getBoolean("Alarm2Do", true));
        Alarm2Fr.setChecked(prefs.getBoolean("Alarm2Fr", true));
        Alarm2Sa.setChecked(prefs.getBoolean("Alarm2Sa", true));
        Alarm2So.setChecked(prefs.getBoolean("Alarm2So", true));

        Button SaveButton = findViewById(R.id.saveAlarm2);
        SaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("Alarm2h", Alarm2h.getText().toString());
                editor.putString("Alarm2m", Alarm2m.getText().toString());
                editor.putBoolean("enableAlarm2", enableAlarm2.isChecked());
                editor.putBoolean("Alarm2Mo", Alarm2Mo.isChecked());
                editor.putBoolean("Alarm2Di", Alarm2Di.isChecked());
                editor.putBoolean("Alarm2Mi", Alarm2Mi.isChecked());
                editor.putBoolean("Alarm2Do", Alarm2Do.isChecked());
                editor.putBoolean("Alarm2Fr", Alarm2Fr.isChecked());
                editor.putBoolean("Alarm2Sa", Alarm2Sa.isChecked());
                editor.putBoolean("Alarm2So", Alarm2So.isChecked());
                editor.apply();
                AlarmDaysM2=0;
                AlarmDaysM2 += (enableAlarm2.isChecked() ? 1 : 0) + (Alarm2Sa.isChecked() ? 2 : 0) + (Alarm2Fr.isChecked() ? 4 : 0) + (Alarm2Do.isChecked() ? 8 : 0) + (Alarm2Mi.isChecked() ? 16 : 0) + (Alarm2Di.isChecked() ? 32 : 0) + (Alarm2Mo.isChecked() ? 64 : 0) + (Alarm2So.isChecked() ? 128 : 0);
                sendBLEcmd("AT+ALARM=01" + String.format("%02d", Integer.parseInt(Alarm2h.getText().toString())) + String.format("%02d", Integer.parseInt(Alarm2m.getText().toString())) + String.format("%02X", AlarmDaysM2));
            }
        });

    }

    Integer AlarmDaysM3 = 0;
    EditText Alarm3h;
    EditText Alarm3m;
    CheckBox enableAlarm3;
    CheckBox Alarm3Mo;
    CheckBox Alarm3Di;
    CheckBox Alarm3Mi;
    CheckBox Alarm3Do;
    CheckBox Alarm3Fr;
    CheckBox Alarm3Sa;
    CheckBox Alarm3So;
    void initAlarm2() {
        Alarm3h = findViewById(R.id.Alarm3h);
        Alarm3h.setFilters(new InputFilter[]{new FilterTextH(0, 23)});
        Alarm3m = findViewById(R.id.Alarm3m);
        Alarm3m.setFilters(new InputFilter[]{new FilterTextH(0, 59)});
        enableAlarm3 = findViewById(R.id.enableAlarm3);
        Alarm3Mo = findViewById(R.id.Alarm3Mo);
        Alarm3Di = findViewById(R.id.Alarm3Di);
        Alarm3Mi = findViewById(R.id.Alarm3Mi);
        Alarm3Do = findViewById(R.id.Alarm3Do);
        Alarm3Fr = findViewById(R.id.Alarm3Fr);
        Alarm3Sa = findViewById(R.id.Alarm3Sa);
        Alarm3So = findViewById(R.id.Alarm3So);
        Alarm3h.setText(prefs.getString("Alarm3h", "07"));
        Alarm3m.setText(prefs.getString("Alarm3m", "00"));
        enableAlarm3.setChecked(prefs.getBoolean("enableAlarm3", false));
        Alarm3Mo.setChecked(prefs.getBoolean("Alarm3Mo", true));
        Alarm3Di.setChecked(prefs.getBoolean("Alarm3Di", true));
        Alarm3Mi.setChecked(prefs.getBoolean("Alarm3Mi", true));
        Alarm3Do.setChecked(prefs.getBoolean("Alarm3Do", true));
        Alarm3Fr.setChecked(prefs.getBoolean("Alarm3Fr", true));
        Alarm3Sa.setChecked(prefs.getBoolean("Alarm3Sa", true));
        Alarm3So.setChecked(prefs.getBoolean("Alarm3So", true));

        Button SaveButton = findViewById(R.id.saveAlarm3);
        SaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("Alarm3h", Alarm3h.getText().toString());
                editor.putString("Alarm3m", Alarm3m.getText().toString());
                editor.putBoolean("enableAlarm3", enableAlarm3.isChecked());
                editor.putBoolean("Alarm3Mo", Alarm3Mo.isChecked());
                editor.putBoolean("Alarm3Di", Alarm3Di.isChecked());
                editor.putBoolean("Alarm3Mi", Alarm3Mi.isChecked());
                editor.putBoolean("Alarm3Do", Alarm3Do.isChecked());
                editor.putBoolean("Alarm3Fr", Alarm3Fr.isChecked());
                editor.putBoolean("Alarm3Sa", Alarm3Sa.isChecked());
                editor.putBoolean("Alarm3So", Alarm3So.isChecked());
                editor.apply();
                AlarmDaysM3=0;
                AlarmDaysM3 += (enableAlarm3.isChecked() ? 1 : 0) + (Alarm3Sa.isChecked() ? 2 : 0) + (Alarm3Fr.isChecked() ? 4 : 0) + (Alarm3Do.isChecked() ? 8 : 0) + (Alarm3Mi.isChecked() ? 16 : 0) + (Alarm3Di.isChecked() ? 32 : 0) + (Alarm3Mo.isChecked() ? 64 : 0) + (Alarm3So.isChecked() ? 128 : 0);
                sendBLEcmd("AT+ALARM=02" + String.format("%02d", Integer.parseInt(Alarm3h.getText().toString())) + String.format("%02d", Integer.parseInt(Alarm3m.getText().toString())) + String.format("%02X", AlarmDaysM3));
            }
        });

    }

    Integer AlarmDaysM4 = 0;
    EditText Alarm4h;
    EditText Alarm4m;
    CheckBox enableAlarm4;
    CheckBox Alarm4Mo;
    CheckBox Alarm4Di;
    CheckBox Alarm4Mi;
    CheckBox Alarm4Do;
    CheckBox Alarm4Fr;
    CheckBox Alarm4Sa;
    CheckBox Alarm4So;
    void initAlarm3() {
        Alarm4h = findViewById(R.id.Alarm4h);
        Alarm4h.setFilters(new InputFilter[]{new FilterTextH(0, 23)});
        Alarm4m = findViewById(R.id.Alarm4m);
        Alarm4m.setFilters(new InputFilter[]{new FilterTextH(0, 59)});
        enableAlarm4 = findViewById(R.id.enableAlarm4);
        Alarm4Mo = findViewById(R.id.Alarm4Mo);
        Alarm4Di = findViewById(R.id.Alarm4Di);
        Alarm4Mi = findViewById(R.id.Alarm4Mi);
        Alarm4Do = findViewById(R.id.Alarm4Do);
        Alarm4Fr = findViewById(R.id.Alarm4Fr);
        Alarm4Sa = findViewById(R.id.Alarm4Sa);
        Alarm4So = findViewById(R.id.Alarm4So);
        Alarm4h.setText(prefs.getString("Alarm4h", "07"));
        Alarm4m.setText(prefs.getString("Alarm4m", "00"));
        enableAlarm4.setChecked(prefs.getBoolean("enableAlarm4", false));
        Alarm4Mo.setChecked(prefs.getBoolean("Alarm4Mo", true));
        Alarm4Di.setChecked(prefs.getBoolean("Alarm4Di", true));
        Alarm4Mi.setChecked(prefs.getBoolean("Alarm4Mi", true));
        Alarm4Do.setChecked(prefs.getBoolean("Alarm4Do", true));
        Alarm4Fr.setChecked(prefs.getBoolean("Alarm4Fr", true));
        Alarm4Sa.setChecked(prefs.getBoolean("Alarm4Sa", true));
        Alarm4So.setChecked(prefs.getBoolean("Alarm4So", true));

        Button SaveButton = findViewById(R.id.saveAlarm4);
        SaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("Alarm4h", Alarm4h.getText().toString());
                editor.putString("Alarm4m", Alarm4m.getText().toString());
                editor.putBoolean("enableAlarm4", enableAlarm4.isChecked());
                editor.putBoolean("Alarm4Mo", Alarm4Mo.isChecked());
                editor.putBoolean("Alarm4Di", Alarm4Di.isChecked());
                editor.putBoolean("Alarm4Mi", Alarm4Mi.isChecked());
                editor.putBoolean("Alarm4Do", Alarm4Do.isChecked());
                editor.putBoolean("Alarm4Fr", Alarm4Fr.isChecked());
                editor.putBoolean("Alarm4Sa", Alarm4Sa.isChecked());
                editor.putBoolean("Alarm4So", Alarm4So.isChecked());
                editor.apply();
                AlarmDaysM4=0;
                AlarmDaysM4 += (enableAlarm4.isChecked() ? 1 : 0) + (Alarm4Sa.isChecked() ? 2 : 0) + (Alarm4Fr.isChecked() ? 4 : 0) + (Alarm4Do.isChecked() ? 8 : 0) + (Alarm4Mi.isChecked() ? 16 : 0) + (Alarm4Di.isChecked() ? 32 : 0) + (Alarm4Mo.isChecked() ? 64 : 0) + (Alarm4So.isChecked() ? 128 : 0);
                sendBLEcmd("AT+ALARM=03" + String.format("%02d", Integer.parseInt(Alarm4h.getText().toString())) + String.format("%02d", Integer.parseInt(Alarm4m.getText().toString())) + String.format("%02X", AlarmDaysM4));
            }
        });

    }

    Integer AlarmDaysM5 = 0;
    EditText Alarm5h;
    EditText Alarm5m;
    CheckBox enableAlarm5;
    CheckBox Alarm5Mo;
    CheckBox Alarm5Di;
    CheckBox Alarm5Mi;
    CheckBox Alarm5Do;
    CheckBox Alarm5Fr;
    CheckBox Alarm5Sa;
    CheckBox Alarm5So;
    void initAlarm4() {
        Alarm5h = findViewById(R.id.Alarm5h);
        Alarm5h.setFilters(new InputFilter[]{new FilterTextH(0, 23)});
        Alarm5m = findViewById(R.id.Alarm5m);
        Alarm5m.setFilters(new InputFilter[]{new FilterTextH(0, 59)});
        enableAlarm5 = findViewById(R.id.enableAlarm5);
        Alarm5Mo = findViewById(R.id.Alarm5Mo);
        Alarm5Di = findViewById(R.id.Alarm5Di);
        Alarm5Mi = findViewById(R.id.Alarm5Mi);
        Alarm5Do = findViewById(R.id.Alarm5Do);
        Alarm5Fr = findViewById(R.id.Alarm5Fr);
        Alarm5Sa = findViewById(R.id.Alarm5Sa);
        Alarm5So = findViewById(R.id.Alarm5So);
        Alarm5h.setText(prefs.getString("Alarm5h", "07"));
        Alarm5m.setText(prefs.getString("Alarm5m", "00"));
        enableAlarm5.setChecked(prefs.getBoolean("enableAlarm5", false));
        Alarm5Mo.setChecked(prefs.getBoolean("Alarm5Mo", true));
        Alarm5Di.setChecked(prefs.getBoolean("Alarm5Di", true));
        Alarm5Mi.setChecked(prefs.getBoolean("Alarm5Mi", true));
        Alarm5Do.setChecked(prefs.getBoolean("Alarm5Do", true));
        Alarm5Fr.setChecked(prefs.getBoolean("Alarm5Fr", true));
        Alarm5Sa.setChecked(prefs.getBoolean("Alarm5Sa", true));
        Alarm5So.setChecked(prefs.getBoolean("Alarm5So", true));

        Button SaveButton = findViewById(R.id.saveAlarm5);
        SaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("Alarm5h", Alarm5h.getText().toString());
                editor.putString("Alarm5m", Alarm5m.getText().toString());
                editor.putBoolean("enableAlarm5", enableAlarm5.isChecked());
                editor.putBoolean("Alarm5Mo", Alarm5Mo.isChecked());
                editor.putBoolean("Alarm5Di", Alarm5Di.isChecked());
                editor.putBoolean("Alarm5Mi", Alarm5Mi.isChecked());
                editor.putBoolean("Alarm5Do", Alarm5Do.isChecked());
                editor.putBoolean("Alarm5Fr", Alarm5Fr.isChecked());
                editor.putBoolean("Alarm5Sa", Alarm5Sa.isChecked());
                editor.putBoolean("Alarm5So", Alarm5So.isChecked());
                editor.apply();
                AlarmDaysM5=0;
                AlarmDaysM5 += (enableAlarm5.isChecked() ? 1 : 0) + (Alarm5Sa.isChecked() ? 2 : 0) + (Alarm5Fr.isChecked() ? 4 : 0) + (Alarm5Do.isChecked() ? 8 : 0) + (Alarm5Mi.isChecked() ? 16 : 0) + (Alarm5Di.isChecked() ? 32 : 0) + (Alarm5Mo.isChecked() ? 64 : 0) + (Alarm5So.isChecked() ? 128 : 0);
                sendBLEcmd("AT+ALARM=04" + String.format("%02d", Integer.parseInt(Alarm5h.getText().toString())) + String.format("%02d", Integer.parseInt(Alarm5m.getText().toString())) + String.format("%02X", AlarmDaysM5));
            }
        });

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
