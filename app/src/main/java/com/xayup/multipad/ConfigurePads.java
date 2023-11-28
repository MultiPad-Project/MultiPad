package com.xayup.multipad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import java.util.*;

import com.xayup.multipad.midi.MidiStaticVars;
import com.xayup.multipad.pads.Render.MakePads;

public class ConfigurePads {
    private final Context context;
    private Map<Integer, Map<Integer, Object>> slidePad =
            new HashMap<>();
    private final int SLIDE_PAD_ATUAL = 1;
    private final int SLIDE_LIMIT_X = 2;
    private final int SLIDE_LIMIT_Y = 3;

    public ConfigurePads(Context context) {
        this.context = context;}

    public void configure(MakePads.Pads mPads) {
        for(int i = mPads.getGrid().getChildCount()-1; !(i < 0); i--){
            View view;
            Object tag = (view = mPads.getGrid().getChildAt(i)).getTag();
            if(tag instanceof MakePads.PadInfo){
                ViewGroup pad = (ViewGroup) view;
                if(tag instanceof MakePads.ChainInfo){
                    MakePads.ChainInfo chainInfo = (MakePads.ChainInfo) tag;
                    if(chainInfo.getRow() == 0){
                        if (chainInfo.getColum() == 1) { // led de botão precionado
                            pad.setOnTouchListener(new OnTouchListener() {
                                @Override
                                public boolean onTouch(View arg0, MotionEvent arg1) {
                                    if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                                        ImageView state = arg0.findViewById(MakePads.PadInfo.PadLayerType.BTN_);
                                        if (PlayPads.pressLed) {
                                            PlayPads.pressLed = false;
                                            state.setAlpha(0.0f);
                                        } else {
                                            PlayPads.pressLed = true;
                                            state.setAlpha(PlayPads.watermark);
                                        }
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }
                            });
                        } else if (chainInfo.getColum() == 2) { // leds on/off and stop sounds
                            pad.findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(PlayPads.watermark);
                            pad.setOnTouchListener(new OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                        ImageView press = view.findViewById(MakePads.PadInfo.PadLayerType.BTN_);
                                        if (!PlayPads.stopAll) {
                                            press.setAlpha(0.0f);
                                            PlayPads.stopAll = true;
                                            XayUpFunctions.clearLeds(context, mPads);
                                            if(PlayPads.mSoundLoader != null) PlayPads.mSoundLoader.stopAll();
                                        } else {
                                            press.setAlpha(PlayPads.watermark);
                                            PlayPads.stopAll = false;
                                        }
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }
                            });
                        } else if (chainInfo.getColum() == 3) { // autoplay
                            pad.setOnTouchListener(new OnTouchListener() {
                                @SuppressLint("ResourceType")
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    if ((PlayPads.autoPlay != null)
                                            && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                        int viewId = view.getId();
                                        ImageView press = view.findViewById(MakePads.PadInfo.PadLayerType.BTN_);
                                        RelativeLayout prev = view.getRootView().findViewById(4);
                                        RelativeLayout pause = view.getRootView().findViewById(5);
                                        RelativeLayout next = view.getRootView().findViewById(6);
                                        if (PlayPads.autoPlayCheck) {
                                            prev.removeView(mPads.getGrid().findViewById(AutoPlayFunc.ICON_ID_PREV));
                                            pause.removeView(mPads.getGrid().findViewById(AutoPlayFunc.ICON_ID_STATE));
                                            next.removeView(mPads.getGrid().findViewById(AutoPlayFunc.ICON_ID_NEXT));
                                            press.setAlpha(0.0f);
                                            PlayPads.autoPlayThread.stop();
                                            PlayPads.autoPlayCheck = false;
                                            PlayPads.progressAutoplay.setVisibility(View.GONE);
                                            //		PlayPads.stopAll = true;
                                        } else {
                                            if(PlayPads.autoPlay != null) {
                                                PlayPads.progressAutoplay.setAlpha(PlayPads.watermark);
                                                PlayPads.progressAutoplay.setVisibility(View.VISIBLE);
                                                PlayPads.progressAutoplay.setProgress(0);
                                                ImageView prev_img = new ImageView(context);
                                                prev_img.setId(AutoPlayFunc.ICON_ID_PREV);
                                                prev_img.setImageDrawable(context.getDrawable(R.drawable.play_prev));
                                                prev_img.setAlpha(PlayPads.watermark);
                                                prev_img.setRotation(90f);
                                                ImageView pause_img = new ImageView(context);
                                                pause_img.setId(AutoPlayFunc.ICON_ID_STATE);
                                                pause_img.setImageDrawable(context.getDrawable(R.drawable.play_pause));
                                                pause_img.setAlpha(PlayPads.watermark);
                                                pause_img.setRotation(90f);
                                                ImageView next_img = new ImageView(context);
                                                next_img.setId(AutoPlayFunc.ICON_ID_NEXT);
                                                next_img.setImageDrawable(context.getDrawable(R.drawable.play_prev));
                                                next_img.setRotation(90f);
                                                next_img.setScaleX(-1f);
                                                next_img.setAlpha(PlayPads.watermark);
                                                prev.addView(prev_img);
                                                pause.addView(pause_img);
                                                next.addView(next_img);
                                                press.setAlpha(PlayPads.watermark);
                                                PlayPads.autoPlayCheck = true;
                                                PlayPads.stopAll = false;
                                                //	System.out.println("btn play");
                                                PlayPads.autoPlayThread.play();
                                            }
                                        }
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }
                            });
                        } else if (chainInfo.getColum() == 4) { // prev
                            pad.setOnTouchListener(new OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    if ((PlayPads.autoPlayCheck)
                                            && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                        view.findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(PlayPads.watermark);
                                        PlayPads.autoPlayThread.prev();
                                        // Toast.makeText(context, "prev", Toast.LENGTH_SHORT).show();
                                        return true;
                                    } else {
                                        if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                                            view.findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(0.0f);
                                        return false;
                                    }
                                }
                            });
                        } else if (chainInfo.getColum() == 5) { // pause/release
                            pad.setOnTouchListener(new OnTouchListener() {
                                @SuppressLint("ResourceType")
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    if ((PlayPads.autoPlayCheck)
                                            && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                        view.findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(PlayPads.watermark);
                                        if (PlayPads.autoPlayThread.isPaused()) {
                                            PlayPads.autoPlayThread.start();
                                            ((ImageView) view.findViewById(3005))
                                                    .setImageDrawable(context.getDrawable(R.drawable.play_pause));
                                        } else {
                                            PlayPads.autoPlayThread.pause();
                                            ((ImageView) view.findViewById(3005))
                                                    .setImageDrawable(context.getDrawable(R.drawable.play_play));
                                        }
                                        // Toast.makeText(context, "pause", Toast.LENGTH_SHORT).show();
                                        return true;
                                    } else {
                                        if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                                            view.findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(0.0f);
                                        return false;
                                    }
                                }
                            });
                        } else if (chainInfo.getColum() == 6) { // next
                            pad.setOnTouchListener(new OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    if ((PlayPads.autoPlayCheck)
                                            && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                        view.findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(PlayPads.watermark);
                                        PlayPads.autoPlayThread.next();
                                        // Toast.makeText(context, "next", Toast.LENGTH_SHORT).show();
                                        return true;
                                    } else {
                                        if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                                            view.findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(0.0f);
                                        return false;
                                    }
                                }
                            });
                        } else if (chainInfo.getColum() == 7) { // layout switch
                            pad.setOnTouchListener(new OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                        view.findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(1f);
                                        mPads.switchLayout();
                                        return true;
                                    } else {
                                        view.findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(0f);
                                        return false;
                                    }
                                }
                            });
                        } else if (chainInfo.getColum() == 8) { // Watermark on/off
                            pad.setOnTouchListener(new OnTouchListener() {
                                @SuppressLint("ResourceType")
                                @Override
                                public boolean onTouch(View arg0, MotionEvent arg1) {
                                    if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                                        PlayPads.watermark = (PlayPads.watermark == 0f) ? 1f: 0f;
                                        if (PlayPads.autoPlayCheck)
                                            arg0.getRootView()
                                                    .findViewById(3)
                                                    .findViewById(MakePads.PadInfo.PadLayerType.BTN_)
                                                    .setAlpha(PlayPads.watermark);

                                        if (!PlayPads.stopAll)
                                            arg0.getRootView()
                                                    .findViewById(2)
                                                    .findViewById(MakePads.PadInfo.PadLayerType.BTN_)
                                                    .setAlpha(PlayPads.watermark);
                                        if (PlayPads.pressLed)
                                            arg0.getRootView()
                                                    .findViewById(1)
                                                    .findViewById(MakePads.PadInfo.PadLayerType.BTN_)
                                                    .setAlpha(PlayPads.watermark);
                                        if (PlayPads.progressAutoplay != null)
                                            PlayPads.progressAutoplay.setAlpha(PlayPads.watermark);
                                        arg0.getRootView()
                                                .findViewById(7)
                                                .findViewById(MakePads.PadInfo.PadLayerType.BTN_)
                                                .setAlpha(PlayPads.watermark);
                                        arg0.getRootView()
                                                .findViewById(PlayPads.currentChainId)
                                                .findViewById(MakePads.PadInfo.PadLayerType.BTN_)
                                                .setAlpha(PlayPads.watermark);
                                        if (arg0.getRootView().findViewById(4).findViewById(3004) != null) {
                                            arg0.getRootView()
                                                    .findViewById(4)
                                                    .findViewById(3004)
                                                    .setAlpha(PlayPads.watermark);
                                            arg0.getRootView()
                                                    .findViewById(5)
                                                    .findViewById(3005)
                                                    .setAlpha(PlayPads.watermark);
                                            arg0.getRootView()
                                                    .findViewById(6)
                                                    .findViewById(3006)
                                                    .setAlpha(PlayPads.watermark);
                                        }
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }
                            });
                        }
                    } else { // CC chains
                        if(chainInfo.getColum() == 9 && chainInfo.getRow() == 1)
                            pad.findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(PlayPads.watermark);
                        pad.setOnTouchListener(new OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                    PlayPads.currentChainId = chainInfo.getId();
                                    if ((PlayPads.autoPlayThread != null)
                                            && PlayPads.autoPlayThread.isPaused()) {
                                        PlayPads.autoPlayThread.chainChanged();
                                        PlayPads.autoPlayThread.touch(
                                                Integer.parseInt(String.valueOf(PlayPads.currentChainId) + chainInfo.getId()));
                                    }
                                    if (PlayPads.currentChainId != PlayPads.otherChain) {
                                        PlayPads.currentChainMC = chainInfo.getMc();

                                        ImageView img =
                                                mPads.getGrid().findViewById(PlayPads.otherChain)
                                                        .findViewById(MakePads.PadInfo.PadLayerType.BTN_);
                                        img.setAlpha(0.0f);
                                        img = view.findViewById(MakePads.PadInfo.PadLayerType.BTN_);
                                        img.setAlpha(PlayPads.watermark);
                                        PlayPads.otherChain = PlayPads.currentChainId;


                                        for (String k : PlayPads.ledrpt.keySet()) {
                                            PlayPads.ledrpt.put(k, 0);
                                        }
                                        if(PlayPads.mSoundLoader != null) PlayPads.mSoundLoader.resetSequencer();

                                        if (PlayPads.recAutoplay) {
                                            AutoplayRecFunc.addChain(String.valueOf(PlayPads.currentChainMC));
                                        }
                                    }
                                    if(MidiStaticVars.controllerManager != null){
                                        MidiStaticVars.controllerManager.send(chainInfo.getRow(), chainInfo.getColum(), (int)(motionEvent.getPressure() * 127));
                                    }
                                    return true;
                                } else {
                                    if(MidiStaticVars.controllerManager != null){
                                        MidiStaticVars.controllerManager.send(chainInfo.getRow(), chainInfo.getColum(), 0);
                                    }
                                    return false;
                                }
                            }
                        });
                    }
                } else { //Pads
                    MakePads.PadInfo padInfo = (MakePads.PadInfo) tag;
                    pad.setOnTouchListener(new OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {

                            switch (motionEvent.getAction()) {
                                case MotionEvent.ACTION_DOWN:

                                    if (PlayPads.recAutoplay) {
                                        AutoplayRecFunc.autoPlayRecord(padInfo.getId());
                                    }
                                    if (PlayPads.pressLed) {
                                        PlayPads.padPressAlpha = 1.0f;
                                        view.findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(PlayPads.padPressAlpha);
                                    }
                                    if ((PlayPads.autoPlayThread != null)
                                            && PlayPads.autoPlayThread.isPaused()) {
                                        PlayPads.autoPlayThread.touch(
                                                Integer.parseInt(String.valueOf(PlayPads.currentChainId) + padInfo.getId()));
                                    }

                                    if (PlayPads.slideMode && motionEvent.getDeviceId() != 100) {
                                        slidePad.put(padInfo.getId(), new HashMap<>());
                                        slidePad.get(padInfo.getId()).put(SLIDE_LIMIT_X, 0);
                                        slidePad.get(padInfo.getId()).put(SLIDE_LIMIT_Y, 0);
                                        slidePad.get(padInfo.getId()).put(SLIDE_PAD_ATUAL, new int[]{padInfo.getRow(), padInfo.getColum()});
                                    }
                                    if(MidiStaticVars.controllerManager != null){
                                        MidiStaticVars.controllerManager.send(padInfo.getRow(), padInfo.getColum(), (int)(motionEvent.getPressure() * 127));
                                    } else {
                                        playSound(padInfo);
                                    }
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    if (PlayPads.slideMode && motionEvent.getDeviceId() != 100) {
                                        int[] current_pad = (int[]) slidePad.get(padInfo.getId()).get(SLIDE_PAD_ATUAL);
                                        view = mPads.getPadView(current_pad[0], current_pad[1]);
                                    }
                                    if(view != null) {
                                        if (PlayPads.pressLed) {
                                            PlayPads.padPressAlpha = 0.0f;
                                            view.findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(PlayPads.padPressAlpha);
                                        }

                                        if ((PlayPads.autoPlayThread == null)
                                                || !(PlayPads.currentChainMC + "9" + padInfo.getId())
                                                .equals("" + PlayPads.autoPlayThread.padWaiting))
                                            view.findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(0.0f);
                                        // Stop led 0 looper
                                        try {
                                            PlayPads.threadMap
                                                    .get(PlayPads.currentChainMC + padInfo.getId())
                                                    .stopZeroLooper();
                                        } catch (NullPointerException n) {
                                            Log.e("Stop zero looper", n.getStackTrace()[0].toString());
                                        }
                                        if(MidiStaticVars.controllerManager != null){
                                            MidiStaticVars.controllerManager.send(padInfo.getRow(), padInfo.getColum(), 0);
                                        }
                                    }
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    if (PlayPads.slideMode) {
                                        try {
                                            float x = motionEvent.getX();
                                            float y = motionEvent.getY();
                                            int slidelimit_x = (int) slidePad.get(padInfo.getId()).get(SLIDE_LIMIT_X);
                                            int slidelimit_y = (int) slidePad.get(padInfo.getId()).get(SLIDE_LIMIT_Y);
                                            int[] current_pad = (int[]) slidePad.get(padInfo.getId()).get(SLIDE_PAD_ATUAL);
                                            int[] old_pad = new int[]{current_pad[0], current_pad[1]};
                                            int pad_height = view.getMeasuredHeight();
                                            int pad_width = view.getMeasuredWidth();
                                            if ((x > slidelimit_x + pad_width || x < slidelimit_x)
                                                    || (y > slidelimit_y + pad_height || y < slidelimit_y)) {

                                                View old_view = mPads.getPadView(old_pad[0], old_pad[1]);

                                                if (old_view != null)
                                                    old_view.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0, 1, 0, 1, 1, 100, 0));
                                                if (x > slidelimit_x + pad_width) {
                                                    current_pad[1] += 1;
                                                    slidelimit_x += pad_width;
                                                } else if (x < slidelimit_x) {
                                                    current_pad[1] -= 1;
                                                    slidelimit_x -= pad_width;
                                                }
                                                if (y > slidelimit_y + pad_height) {
                                                    current_pad[0] += 1;
                                                    slidelimit_y += pad_height;
                                                } else if (y < slidelimit_y) {
                                                    current_pad[0] -= 1;
                                                    slidelimit_y -= pad_height;
                                                }

                                                current_pad[0] *= (int) view.getScaleX();
                                                current_pad[1] *= (int) view.getScaleY();

                                                Log.v("Current pad", "X: " + current_pad[0] + ", Y: " + current_pad[1]);

                                                slidePad.get(padInfo.getId()).put(SLIDE_PAD_ATUAL, current_pad);
                                                slidePad.get(padInfo.getId()).put(SLIDE_LIMIT_X, slidelimit_x);
                                                slidePad.get(padInfo.getId()).put(SLIDE_LIMIT_Y, slidelimit_y);

                                                int w = mPads.getRoot().getMeasuredWidth();
                                                int ww = (MainActivity.width / 2) - (w / 2);
                                                if (!(motionEvent.getRawX() < ww
                                                        || motionEvent.getRawX() > ww + w)
                                                        && !((current_pad[0] == 0 || current_pad[0] == 9)
                                                        && (current_pad[1] == 9 || current_pad[1] == 0))) {
                                                    Log.e("Try touch pad on slide mode", Arrays.toString(current_pad));
                                                    view = mPads.getPadView(current_pad[0], current_pad[1]);
                                                    view.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0, 1, 0, 1, 1, 100, 0));
                                                }
                                            }
                                        } catch (NullPointerException ignore){
                                            ignore.printStackTrace(System.out);
                                        }
                                    }
                                    break;
                            }
                            return true;
                        }
                    });
                }
            }
        }
    }

    /*
    public void changeChainPlayable() {
        for (String chain : PlayPads.chainClickable.keySet()) {
            if (VariaveisStaticas.chainsIDlist.contains(chain)) {
                int chainId = Integer.parseInt(chain);
                context.findViewById(chainId)
                        .setOnTouchListener(
                                new Button.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View arg0, MotionEvent arg1) {
                                        switch (arg1.getAction()) {
                                            case MotionEvent.ACTION_DOWN:
                                                if (PlayPads.recAutoplay) {
                                                    AutoplayRecFunc.autoPlayRecord(arg0.getId());
                                                }
                                                playSound(arg0);
                                                return true;
                                            case MotionEvent.ACTION_UP:
                                                if ((PlayPads.autoPlayThread == null)
                                                        || !((String) PlayPads.chainSl
                                                                        + "9"
                                                                        + arg0.getId())
                                                                .equals(
                                                                        ""
                                                                                + PlayPads
                                                                                        .autoPlayThread
                                                                                        .padWaiting))
                                                    arg0.findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(0.0f);
                                                return true;
                                        }
                                        return false;
                                    }
                                });
            }
        }
    }

     */

    private void playSound(MakePads.PadInfo padInfo) {
        int viewId = padInfo.getId();
        String pad = String.valueOf(PlayPads.currentChainMC) + padInfo.getId();
        String toChain = null;
       
        // Show leds
        if (((!PlayPads.stopAll) && PlayPads.ledFiles != null)
                && PlayPads.ledFiles.get(pad) != null) {
            if ((PlayPads.ledrpt.get(String.valueOf(padInfo.getId())) == null)
                    || PlayPads.ledFiles.get(pad).size()
                            == PlayPads.ledrpt.get(String.valueOf(padInfo.getId()))) {
                PlayPads.ledrpt.put(String.valueOf(padInfo.getId()), 0);
            }
            PlayPads.ledFunc.readKeyLed(
                    PlayPads.ledrpt.get(String.valueOf(padInfo.getId())), PlayPads.currentChainMC, padInfo.getId(), context, padInfo.getPads());
            PlayPads.ledrpt.put(String.valueOf(padInfo.getId()), PlayPads.ledrpt.get(String.valueOf(padInfo.getId())) + 1);
        }

        // Play sound sample
        if(PlayPads.have_sounds) PlayPads.mSoundLoader.playSound(pad);
    }
}
