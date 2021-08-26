package com.atcnetz.de.notification;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.atcnetz.de.notification.util.Apps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NofiticationPicker extends Activity {
    private AppAdapter mAppAdapter;

    private LinearLayout HeaderProgress;
    private LinearLayout MainProgress;
    TextView statusText;
    String AllApps = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nofitication_picker);
        HeaderProgress = findViewById(R.id.HeaderProgress);
        MainProgress = findViewById(R.id.MainProgress);
        statusText = findViewById(R.id.textView4);
        listviewinit();
        new LoadAppsViaTask().execute();
        Button Button = findViewById(R.id.sendCMDbuttonID);
        Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("enabledApps", AllApps);
                editor.apply();
                listviewinit();
                new LoadAppsViaTask().execute();
            }
        });
        Button Button1 = findViewById(R.id.selectDevButtonID);
        Button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("enabledApps", "");
                editor.apply();
                listviewinit();
                new LoadAppsViaTask().execute();
            }
        });
    }


    void listviewinit() {
        ListView appListView = findViewById(R.id.lv);
        mAppAdapter = new AppAdapter(this, R.layout.listitem_app, new ArrayList<Apps>());
        appListView.setAdapter(mAppAdapter);
        appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterview, View view, int position, long id) {
                Apps item = mAppAdapter.getItem(position);
                if (item != null) {
                    SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
                    String disableApps = prefs.getString("enabledApps", "");
                    String[] items = disableApps.split(";");
                    boolean itmesContain = Arrays.asList(items).contains(item.getDisplayName());
                    if (itmesContain) {
                        disableApps = disableApps.replace(item.getDisplayName() + ";", "");
                        mAppAdapter.setChecked(view, false);
                    } else {
                        disableApps += item.getDisplayName() + ";";
                        mAppAdapter.setChecked(view, true);
                    }
                    SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                    editor.putString("enabledApps", disableApps);
                    editor.apply();
                }

            }
        });
    }


    class LoadAppsViaTask extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params) {
            final PackageManager pm = getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(pm));
            SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
            String enabledApps = prefs.getString("enabledApps", "");
            String[] items = enabledApps.split(";");
            Integer counts = 1;
            Integer max = packages.size();
            for (ApplicationInfo packageInfo : packages) {
                publishProgress(counts++, max);
                boolean isChecked = Arrays.asList(items).contains(packageInfo.packageName);
                mAppAdapter.update(packageInfo.packageName, packageInfo.loadIcon(pm), packageInfo.loadLabel(pm).toString(), isChecked);
                AllApps += packageInfo.packageName + ";";
            }
            return "Task Completed.";
        }

        @Override
        protected void onPostExecute(String result) {
            HeaderProgress.setVisibility(View.GONE);
            mAppAdapter.updateReal();
        }

        @Override
        protected void onPreExecute() {
            statusText.setText("");
            HeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Integer counts = values[0];
            Integer max = values[1];
            statusText.setText("App " + counts + " from " + max + " is loaded");
        }
    }

}
