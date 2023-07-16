package com.xayup.multipad;

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
}
