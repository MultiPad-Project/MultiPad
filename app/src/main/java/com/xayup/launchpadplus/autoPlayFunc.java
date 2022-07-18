package com.xayup.launchpadplus;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class autoPlayFunc {
    Activity context;
    autoPlayFunc(Activity context){
        this.context = context;
    }
    public void play(){
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();
                int delay = 0;
                int touchViewId;
                int chain = 1;
                View touchInView;
                for(String line : playPads.autoPlay) {
                    if (playPads.autoPlayCheck) {
                        line = line.replace(" ", "").toLowerCase();
                        if (!line.isEmpty()) {
                            switch (line.substring(0, 1)) {
                                case "c":
                                    touchViewId = Integer.parseInt(line.substring(line.length() - 1) + 9);
                                    chain = touchViewId;
                                    context.runOnUiThread(run(touchViewId));
                                    break;
                                case "d":
                                    delay = Integer.parseInt(line.replace("d", ""));
                                    break;
                                case "f":
                                    break;
                                default:
                                    touchViewId = Integer.parseInt(line.substring(line.length() - 2));
                                    context.runOnUiThread(run(touchViewId));
                                    break;
                            }
                        }
                    } else {
                        break;
                    }
                    time += delay;
                    delay = 0;
                    while (System.currentTimeMillis() < time) {
                    }
                    if (playPads.chainSl + "9" != chain + "") {
                        context.runOnUiThread(run(chain));
                    }
                }
                ImageView autoimg = context.findViewById(3).findViewById(R.id.press);
                autoimg.setAlpha(0.0f);
                playPads.autoPlayCheck = false;
            }
            private Runnable run(final int ViewId){
                return new Runnable() {
                    @Override
                    public void run() {
                        View touchInView = context.findViewById(ViewId);
                        touchInView.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0));
                    }
                };
            }
        });
        thread.start();

    }

}
