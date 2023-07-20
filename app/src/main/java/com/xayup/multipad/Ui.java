package com.xayup.multipad;

import android.content.Context;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;

public class Ui {
    public static class Touch {
        public static void touch(View view) {
            view.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0));
        }

        public static void release(View view) {
            view.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0));
        }

        public static void touchAndRelease(View view) {
            touch(view);
            release(view);
        }
    }
    public static float getSettingsAnimationScale(Context context){
        return Settings.Global.getFloat(context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f);
    }
}
