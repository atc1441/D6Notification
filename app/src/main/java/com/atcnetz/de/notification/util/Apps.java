/*
 * Copyright (C) 2013 youten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atcnetz.de.notification.util;


import android.graphics.drawable.Drawable;

/** LeScanned Bluetooth Device */
public class Apps {
    private String packageNameHere;
    private Drawable packageIconHere;
    private String appNameHere;
    private boolean isCheckedHere;

    public Apps(String packageName, Drawable appIcon,String appName,boolean isChecked) {
        packageNameHere = packageName;
        packageIconHere = appIcon;
        appNameHere = appName;
        isCheckedHere = isChecked;
    }

    public String getDevice() {
        return packageNameHere;
    }


    public String getDisplayName() {
        return packageNameHere;
    }
    public String getAppName() {
        return appNameHere;
    }
    public Drawable getIcon() {
        return packageIconHere;
    }
    public boolean getIsChecked() {
        return isCheckedHere;
    }
}
