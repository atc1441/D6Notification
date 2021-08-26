package com.atcnetz.de.notification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.atcnetz.de.notification.util.http_request;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class httplogset extends Activity {
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 160;

    private LocalBroadcastManager localBroadcastManager;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_httplogset);
        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        init_custom_cmd();
        init_http();
        init_log();
    }

    RadioButton http_enable, http_disable, http_answer_enable, http_answer_disable;
    EditText http_url_edit, http_test_edit;
    Button http_test, http_save;

    void init_http() {
        http_enable = findViewById(R.id.http_enable);
        http_disable = findViewById(R.id.http_disable);
        http_answer_enable = findViewById(R.id.http_answer_enable);
        http_answer_disable = findViewById(R.id.http_answer_disable);
        http_url_edit = findViewById(R.id.http_url_edit);
        http_test_edit = findViewById(R.id.http_test_edit);
        http_test = findViewById(R.id.http_test);
        http_save = findViewById(R.id.http_save);

        http_enable.setChecked(prefs.getBoolean("http_enable", false));
        http_answer_enable.setChecked(prefs.getBoolean("http_answer_enable", false));
        http_url_edit.setText(prefs.getString("http_url_edit", "https://google.com?testcmd="));
        http_test_edit.setText(prefs.getString("http_test_edit", "12345"));

        http_test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String req_cmd = http_url_edit.getText().toString() + http_test_edit.getText().toString();
                String req_answer = http_request.get_http_request(req_cmd);
                if(req_answer.length()>300)req_answer=req_answer.substring(0,300);
                Toast.makeText(getApplicationContext(), "Answer: " + req_answer, Toast.LENGTH_LONG).show();
            }
        });
        http_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putBoolean("http_enable", http_enable.isChecked());
                editor.putBoolean("http_answer_enable", http_answer_enable.isChecked());
                editor.putString("http_url_edit", http_url_edit.getText().toString());
                editor.putString("http_test_edit", http_test_edit.getText().toString());
                editor.apply();
            }
        });

    }

    RadioButton savelog_enable, savelog_disable;
    EditText logname_edit, log_test_edit;
    Button log_test, log_save;

    void init_log() {
        savelog_enable = findViewById(R.id.savelog_enable);
        savelog_disable = findViewById(R.id.savelog_disable);
        logname_edit = findViewById(R.id.logname_edit);
        log_test_edit = findViewById(R.id.log_test_edit);
        log_test = findViewById(R.id.log_test);
        log_save = findViewById(R.id.log_save);

        savelog_enable.setChecked(prefs.getBoolean("savelog_enable", false));
        logname_edit.setText(prefs.getString("logname_edit", "d6_log.txt"));
        log_test_edit.setText(prefs.getString("log_test_edit", "12345"));

        log_test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                append_log(logname_edit.getText().toString(), log_test_edit.getText().toString());
            }
        });
        log_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putBoolean("savelog_enable", savelog_enable.isChecked());
                editor.putString("logname_edit", logname_edit.getText().toString());
                editor.putString("log_test_edit", log_test_edit.getText().toString());
                editor.apply();
                request_perms();
            }
        });
    }

    EditText CustomBLEcmd;

    void init_custom_cmd() {
        CustomBLEcmd = findViewById(R.id.editText);
        Button button = findViewById(R.id.sendCMDbuttonID);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBLEcmd(CustomBLEcmd.getText().toString());
            }
        });
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

    public void request_perms() {
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(httplogset.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(httplogset.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    public void append_log(String filename, String log_text) {
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(httplogset.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(httplogset.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
        } else {
            File publicDir = Environment.getExternalStorageDirectory();

            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            String time = sdf.format(new Date());
            String new_line = time + " : " + log_text + "\r\n";

            File newFile = new File(publicDir, filename);
            FileWriter fw;
            try {
                fw = new FileWriter(newFile, true);
                fw.append(new_line);
                fw.flush();
                fw.close();
                Toast.makeText(getApplicationContext(), "Successful: " + new_line, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            int grantResultsLength = grantResults.length;
            if (grantResultsLength > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "External storage permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Please allow external storage permission to enable SD_Card Logging", Toast.LENGTH_LONG).show();
            }
        }
    }
}
