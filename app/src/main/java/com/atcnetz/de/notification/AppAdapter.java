package com.atcnetz.de.notification;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.atcnetz.de.notification.util.Apps;

import java.util.List;

public class AppAdapter extends ArrayAdapter<Apps> {
    private List<Apps> mList;
    private LayoutInflater mInflater;
    private int mResId;

    public AppAdapter(Context context, int resId, List<Apps> objects) {
        super(context, resId, objects);
        mResId = resId;
        mList = objects;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Apps item = (Apps) getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(mResId, null);
        }
        TextView name = (TextView) convertView.findViewById(R.id.device_name);
        assert item != null;
        name.setText(item.getAppName());
        TextView address = (TextView) convertView.findViewById(R.id.device_address);
        address.setText(item.getDisplayName());
        ImageView iconview = (ImageView) convertView.findViewById(R.id.app_icon);
        iconview.setImageDrawable(item.getIcon());
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
        checkBox.setChecked(item.getIsChecked());
        return convertView;
    }

    public void setChecked(View convertView, boolean checked){
        if (convertView == null) {
            convertView = mInflater.inflate(mResId, null);
        }
        CheckBox checkBox = convertView.findViewById(R.id.checkBox);
        checkBox.setChecked(checked);
    }
    /** add or update BluetoothDevice */
    public void update(String packageName, Drawable appIcon,String appName,boolean isChecked) {
        if (packageName == null) {
            return;
        }
        mList.add(new Apps(packageName, appIcon,appName,isChecked));
    }
    /** add or update BluetoothDevice */
    public void updateReal() {
        notifyDataSetChanged();
    }
}
