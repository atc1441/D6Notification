package com.atcnetz.de.notification;

import android.text.InputFilter;
import android.text.Spanned;

public class FilterTextH implements InputFilter {

    private int min, max;

    public FilterTextH(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            String newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend);
            newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart);
            int input = Integer.parseInt(newVal);
            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException nfe) { }
        return "";
    }
    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}