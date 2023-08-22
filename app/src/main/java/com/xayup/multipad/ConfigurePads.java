package com.xayup.multipad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import java.util.*;

import com.xayup.multipad.pads.Render.MakePads;

public class ConfigurePads {
    private final Context context;
    private Map<Integer, Map<Integer, Integer>> slidePad =
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
                                        PlayPads.currentChainMC = String.valueOf(chainInfo.getMc());

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
                                            AutoplayRecFunc.addChain(PlayPads.currentChainMC);
                                        }
                                    }
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        });
                    }
                } else {
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
                                        slidePad.put(padInfo.getId(), new HashMap<Integer, Integer>());
                                        slidePad.get(padInfo.getId()).put(SLIDE_LIMIT_X, 0);
                                        slidePad.get(padInfo.getId()).put(SLIDE_LIMIT_Y, 0);
                                        slidePad.get(padInfo.getId()).put(SLIDE_PAD_ATUAL, padInfo.getId());
                                    }
                                    playSound(padInfo);
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    if (PlayPads.slideMode && motionEvent.getDeviceId() != 100)
                                        view =
                                                mPads.getGrid().findViewById(
                                                        slidePad.get(padInfo.getId()).get(SLIDE_PAD_ATUAL));
                                    if(view != null) {
                                        if (PlayPads.pressLed) {
                                            PlayPads.padPressAlpha = 0.0f;
                                            view.findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(PlayPads.padPressAlpha);
                                        }

                                        if ((PlayPads.autoPlayThread == null)
                                                || !((String) PlayPads.currentChainMC + "9" + padInfo.getId())
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
                                    }
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    if (PlayPads.slideMode) {
                                        try {
                                            float x = motionEvent.getX();
                                            float y = motionEvent.getY();
                                            int slidelimit_x = slidePad.get(padInfo.getId()).get(SLIDE_LIMIT_X);
                                            int slidelimit_y = slidePad.get(padInfo.getId()).get(SLIDE_LIMIT_Y);
                                            int padAtual = slidePad.get(padInfo.getId()).get(SLIDE_PAD_ATUAL);
                                            int oldPad = padAtual;
                                            int padWH = PlayPads.padWH;
                                            if ((x > slidelimit_x + padWH || x < slidelimit_x)
                                                    || (y > slidelimit_y + padWH || y < slidelimit_y)) {
                                                mPads.getGrid().findViewById(oldPad).dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0, 1, 0, 1, 1, 100, 0));
                                                if (x > slidelimit_x + padWH) {
                                                    padAtual += 1;
                                                    slidelimit_x += padWH;
                                                } else if (x < slidelimit_x) {
                                                    padAtual -= 1;
                                                    slidelimit_x -= padWH;
                                                }
                                                if (y > slidelimit_y + padWH) {
                                                    padAtual += 10;
                                                    slidelimit_y += padWH;
                                                } else if (y < slidelimit_y) {
                                                    padAtual -= 10;
                                                    slidelimit_y -= padWH;
                                                }

                                                slidePad.get(padInfo.getId()).put(SLIDE_PAD_ATUAL, padAtual);
                                                slidePad.get(padInfo.getId()).put(SLIDE_LIMIT_X, slidelimit_x);
                                                slidePad.get(padInfo.getId()).put(SLIDE_LIMIT_Y, slidelimit_y);

                                                int w = mPads.getGrid().getLayoutParams().width;
                                                int ww = (MainActivity.width / 2) - (w / 2);
                                                if (!(motionEvent.getRawX() < ww
                                                        || motionEvent.getRawX() > ww + w)
                                                        && !(padAtual == 90
                                                        || padAtual == 99
                                                        || padAtual == 9
                                                        || padAtual == 0)) {
                                                    view = mPads.getGrid().findViewById(padAtual);
                                                    view.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0, 1, 0, 1, 1, 100, 0));
                                                }
                                            }
                                        } catch (NullPointerException ignore){}
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
        String pad = PlayPads.currentChainMC + padInfo.getId();
        String toChain = null;

        // Play sound sample
        if(PlayPads.have_sounds) PlayPads.mSoundLoader.playSound(pad);
       
        // Show leds
        if (((!PlayPads.stopAll) && PlayPads.ledFiles != null)
                && PlayPads.ledFiles.get(pad) != null) {
            if ((PlayPads.ledrpt.get(String.valueOf(padInfo.getId())) == null)
                    || PlayPads.ledFiles.get(pad).size()
                            == PlayPads.ledrpt.get(String.valueOf(padInfo.getId()))) {
                PlayPads.ledrpt.put(String.valueOf(padInfo.getId()), 0);
            }
            PlayPads.ledFunc.readKeyLed(
                    PlayPads.ledrpt.get(String.valueOf(padInfo.getId())), pad, context, padInfo.getPads());
            PlayPads.ledrpt.put(String.valueOf(padInfo.getId()), PlayPads.ledrpt.get(String.valueOf(padInfo.getId())) + 1);
        }
    }
}
