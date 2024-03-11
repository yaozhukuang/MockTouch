package com.zw.yzk.mock.touch.utils;

import android.os.SystemClock;
import android.view.View;

public abstract class DebounceClickListener implements View.OnClickListener {

    private long lastClickTime = 0;
    private final long duration;

    public DebounceClickListener() {
        this(1000);
    }

    public DebounceClickListener(long duration) {
        this.duration = duration;
    }

    @Override
    public void onClick(View v) {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime < duration) {
            return;
        }
        onDebounceClick(v);
        lastClickTime = currentTime;
    }

    public abstract void onDebounceClick(View view);
}
