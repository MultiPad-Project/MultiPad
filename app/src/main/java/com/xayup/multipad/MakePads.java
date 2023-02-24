package com.xayup.multipad;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.text.Layout;
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.upstream.PlaceholderDataSource;
import com.xayup.multipad.PlayPads;
import java.lang.reflect.Parameter;
import java.util.*;
import android.app.*;

import android.media.*;
import java.io.*;

public class MakePads {
    private Activity context;
    public static ImageView phantom;
    public static ImageView btn;
    // private ImageView cantos;
    private String[] btnsIDs = {
        "0 0", "0 1", "0 2", "0 3", "0 4", "0 5", "0 6", "0 7", "0 8", "0 9", "1 0", "1 9", "2 0",
        "2 9", "3 0", "3 9", "4 0", "4 9", "5 0", "5 9", "6 0", "6 9", "7 0", "7 9", "8 0", "8 9",
        "9 0", "9 1", "9 2", "9 3", "9 4", "9 5", "9 6", "9 7", "9 8", "9 9"
    };
    // private String[] chainIDs = { "19", "29", "39", "49", "59", "69", "79", "89" };
    List<String> chainsID;
    private int[] mk2Chains = {
        100, 10, 20, 30, 40, 50, 60, 70, 80, 190, 91, 92, 93, 94, 95, 96, 97, 98, 99
    };

    Readers read = new Readers();
    public static GridLayout layoutpads;
    private String currentProj;
    private KeyLedColors ledLight;
    private int ViewID;
    private int largura;
    private int multiplicadorX = 1; // slide
    private int multiplicadorY = 1; // slide
    private int slideMoved = 0;
    private int padIDAtual;

    public static ViewGroup.LayoutParams param;
    public static ViewGroup.LayoutParams parammk2;
    public static ViewGroup.LayoutParams rowParam;
    // public static ViewGroup.LayoutParams tableParam;

    private Map<Integer, Map<Integer, Integer>> slidePad =
            new HashMap<Integer, Map<Integer, Integer>>();
    private final int SLIDE_PAD_ATUAL = 1;
    private final int SLIDE_LIMIT_X = 2;
    private final int SLIDE_LIMIT_Y = 3;

    public MakePads(String currentPath, int ViewID, final int largura, Activity activity) {
        this.context = activity;
        this.currentProj = currentPath;
        this.ViewID = ViewID;
        this.largura = largura;
    }

    public void makePadInLayout() {
        PlayPads.chainSl = "1";
        chainsID = Arrays.asList(VariaveisStaticas.chainsID);
        int padWH = largura / 10;
        layoutpads = (GridLayout) context.findViewById(ViewID);
        layoutpads.getLayoutParams().height = largura;
        layoutpads.getLayoutParams().width = largura;
        for (int l = 0; l <= 9; l++) {
            for (int c = 0; c <= 9; c++) {
                GridLayout.LayoutParams vparam =
                        new GridLayout.LayoutParams(
                                GridLayout.spec(l, GridLayout.FILL, 1.0f),
                                GridLayout.spec(c, GridLayout.FILL, 1.0f));
                vparam.height = 0;
                vparam.width = 0;

                RelativeLayout layoutpad =
                        (RelativeLayout) context.getLayoutInflater().inflate(R.layout.pad, null);
                // PlayPads.pad = LayoutInflater.from(context).inflate(R.layout.pad, null);
                RelativeLayout cantov = null; // new RelativeLayout(context);
                float alpha = 0;
                final ImageView chainCurrent = layoutpad.findViewById(R.id.press);
                if (Arrays.asList(btnsIDs).contains(l + " " + c)) {
                    if ((l == 0 || l == 9) && (c == 0 || c == 9)) {
                        cantov =
                                new RelativeLayout(
                                        context); // context.getLayoutInflater().inflate(R.layout.cantos, null);
                        // cantov.setLayoutParams();
                        if (l == 0 && c == 9) {
                            cantov =
                                    (RelativeLayout)
                                            context.getLayoutInflater().inflate(R.layout.pad, null);
                            cantov.findViewById(R.id.press).setAlpha(0.0f);
                            SkinTheme.logo = cantov.findViewById(R.id.phantom);
                            SkinTheme.logo.setImageDrawable(SkinTheme.customLogo);
                        } else {
                            // cantov = new RelativeLayout(context);//(RelativeLayout)
                            // context.getLayoutInflater().inflate(R.layout.cantos, null);
                            cantov.setVisibility(View.INVISIBLE);
                            // cantos.setImageDrawable(context.getDrawable(R.drawable.cantos));
                        }
                    } else {
                        phantom = layoutpad.findViewById(R.id.phantom);

                        phantom.setImageDrawable(SkinTheme.chains);
                        chainCurrent.setImageDrawable(context.getDrawable(R.drawable.currentchain));
                        if ((l == 0 && c == 2) || (l == 1 && c == 9)) {
                            alpha = 1.0f;
                        } else {
                            alpha = 0.0f;
                        }

                        if ((l != 0 || l != 9) && (c == 0)) {
                            phantom.setRotationY(180);
                        } else if ((c != 0 || c != 9) && (l == 0)) {
                            phantom.setRotation(-90);
                        } else if ((c != 0 || c != 9) && (l == 9)) {
                            phantom.setRotation(90);
                        }
                        SkinTheme.chainsled.add(phantom);
                    }
                } else {
                    // PlayPads.padPlayer.put(l + "" + c, new MediaPlayer());
                    //	PlayPads.exoplayers.put(l+""+c, null);
                    PlayPads.ledrpt.put(l + "" + c, 0);
                    //		PlayPads.soundrpt.put(l + "" + c, 0);
                    boolean center = true;
                    phantom = layoutpad.findViewById(R.id.phantom);
                    switch (l + "" + c) {
                        case "44":

                            // phantom = layoutpad.findViewById(R.id.phantom);
                            phantom.setImageDrawable(SkinTheme.phantomCenter);
                            phantom.setRotation(0);
                            break;
                        case "45":

                            // phantom = layoutpad.findViewById(R.id.phantom);
                            phantom.setImageDrawable(SkinTheme.phantomCenter);
                            phantom.setRotation(90);
                            break;
                        case "54":

                            // phantom = layoutpad.findViewById(R.id.phantom);
                            phantom.setImageDrawable(SkinTheme.phantomCenter);
                            phantom.setRotation(-90);
                            break;
                        case "55":

                            // phantom = layoutpad.findViewById(R.id.phantom);
                            phantom.setImageDrawable(SkinTheme.phantomCenter);
                            phantom.setRotation(180);
                            break;
                        default:
                            phantom.setImageDrawable(SkinTheme.phantons);
                            center = false;
                            break;
                    }
                    if (center) {
                        SkinTheme.padsCenter.add(phantom);
                    } else {
                        SkinTheme.pads.add(phantom);
                    }
                }
                if ((l == 0 || l == 9) && (c == 0 || c == 9)) {
                    if ((l == 0 || l == 9) && c == 0) {
                        cantov.setId(Integer.parseInt("1" + l + c));
                    } else if (l == 0 && c == 9) {
                        btn = cantov.findViewById(R.id.pad);
                        btn.setImageDrawable(new ColorDrawable(Color.WHITE));
                        SkinTheme.btnlist.add(btn);
                        cantov.setId(9);
                    } else {
                        cantov.setId(Integer.parseInt("" + l + c));
                    }

                    layoutpads.addView(cantov, vparam);

                } else {
                    btn = layoutpad.findViewById(R.id.pad);
                    btn.setImageDrawable(SkinTheme.btn);
                    SkinTheme.btnlist.add(btn);

                    layoutpad.setId(Integer.parseInt(l + "" + c));
                    chainCurrent.setAlpha(alpha);

                    layoutpads.addView(layoutpad, vparam);

                    layoutpad.setOnTouchListener(onTouch(Integer.parseInt(l + "" + c)));
                }
            }
        }
    }

    public OnTouchListener onTouch(int viewId) {
        if (viewId == 1) { // led de botão precionado
            return new OnTouchListener() {
                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                        ImageView state = arg0.findViewById(R.id.press);
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
            };
        } else if (viewId == 2) { // leds on/off and stop sounds
            return new OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        int viewId = view.getId();
                        ImageView press = view.findViewById(R.id.press);
                        if (!PlayPads.stopAll) {
                            press.setAlpha(0.0f);
                            PlayPads.stopAll = true;
                            XayUpFunctions.stopSounds();
                            XayUpFunctions.clearLeds(context, view.getRootView());
                        } else {
                            press.setImageDrawable(context.getDrawable(R.drawable.currentchain));
                            press.setAlpha(PlayPads.watermark);
                            PlayPads.stopAll = false;
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
            };
        } else if (viewId == 3) { // autoplay
            return new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if ((PlayPads.autoPlay != null)
                            && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        int viewId = view.getId();
                        ImageView press = view.findViewById(R.id.press);
                        RelativeLayout prev = view.getRootView().findViewById(4);
                        RelativeLayout pause = view.getRootView().findViewById(5);
                        RelativeLayout next = view.getRootView().findViewById(6);
                        if (PlayPads.autoPlayCheck) {
                            prev.removeView(context.findViewById(3004));
                            pause.removeView(context.findViewById(3005));
                            next.removeView(context.findViewById(3006));
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
                            prev_img.setId(3004);
                            prev_img.setImageDrawable(context.getDrawable(R.drawable.play_prev));
                            prev_img.setAlpha(PlayPads.watermark);
                            ImageView pause_img = new ImageView(context);
                            pause_img.setId(3005);
                            pause_img.setImageDrawable(context.getDrawable(R.drawable.play_pause));
                            pause_img.setAlpha(PlayPads.watermark);
                            ImageView next_img = new ImageView(context);
                            next_img.setId(3006);
                            next_img.setImageDrawable(context.getDrawable(R.drawable.play_prev));
                            next_img.setRotationY(180.0f);
                            next_img.setAlpha(PlayPads.watermark);
                            prev.addView(prev_img);
                            pause.addView(pause_img);
                            next.addView(next_img);
                            press.setImageDrawable(context.getDrawable(R.drawable.currentchain));
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
            };
        } else if (viewId == 4) { // prev

            return new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if ((PlayPads.autoPlayCheck)
                            && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        view.findViewById(R.id.press).setAlpha(PlayPads.watermark);
                        PlayPads.autoPlayThread.prev();
                        // Toast.makeText(context, "prev", Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                            view.findViewById(R.id.press).setAlpha(0.0f);
                        return false;
                    }
                }
            };
        } else if (viewId == 5) { // pause/release
            return new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if ((PlayPads.autoPlayCheck)
                            && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        view.findViewById(R.id.press).setAlpha(PlayPads.watermark);
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
                            view.findViewById(R.id.press).setAlpha(0.0f);
                        return false;
                    }
                }
            };
        } else if (viewId == 6) { // next
            return new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if ((PlayPads.autoPlayCheck)
                            && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        view.findViewById(R.id.press).setAlpha(PlayPads.watermark);
                        PlayPads.autoPlayThread.next();
                        // Toast.makeText(context, "next", Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                            view.findViewById(R.id.press).setAlpha(0.0f);
                        return false;
                    }
                }
            };
        } else if (viewId == 7) { // mk2/pro mode
            return new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        int viewId = view.getId();
                        ImageView press = view.findViewById(R.id.press);
                        int visibility;
                        // make MK2 layout
                        if (!PlayPads.mk2) {
                            PlayPads.padWH = layoutpads.getLayoutParams().height / 9;
                            visibility = View.GONE;
                            //	layoutpads.setColumnShrinkable(0, true);// = PlayPads.padWH;
                            PlayPads.mk2 = true;
                            press.setImageDrawable(context.getDrawable(R.drawable.currentchain));
                            press.setAlpha(PlayPads.watermark);
                        } else {
                            PlayPads.padWH = layoutpads.getLayoutParams().height / 10;
                            visibility = View.VISIBLE;
                            //	layoutpads.setColumnShrinkable(0, false);// = PlayPads.padWH;
                            PlayPads.mk2 = false;
                            press.setAlpha(0.0f);
                        }
                        for (int i = 0; i < layoutpads.getRowCount(); i++) {
                            if (i == 9) {
                                for (int c = 0; c < layoutpads.getColumnCount(); c++) {
                                    layoutpads.getChildAt((i * 10) + c).setVisibility(visibility);
                                }
                            }
                            layoutpads.getChildAt(i * 10).setVisibility(visibility);
                        }
                        layoutpads.setVisibility(View.GONE);
                        layoutpads.setVisibility(View.VISIBLE);
                        if (PlayPads.glows != null)
                            PlayPads.glows.mk2Glows(0, PlayPads.padWH, PlayPads.mk2);

                        return true;
                    } else {
                        return false;
                    }
                }
            };
        } else if (viewId == 8) { // Watermark on/off
            return new OnTouchListener() {
                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                        if (PlayPads.watermark == 1.0f) {
                            PlayPads.watermark = 0.0f;
                        } else {
                            PlayPads.watermark = 1.0f;
                        }
                        if (PlayPads.autoPlayCheck)
                            arg0.getRootView()
                                    .findViewById(3)
                                    .findViewById(R.id.press)
                                    .setAlpha(PlayPads.watermark);
                        if (PlayPads.mk2)
                            arg0.getRootView()
                                    .findViewById(7)
                                    .findViewById(R.id.press)
                                    .setAlpha(PlayPads.watermark);
                        if (!PlayPads.stopAll)
                            arg0.getRootView()
                                    .findViewById(2)
                                    .findViewById(R.id.press)
                                    .setAlpha(PlayPads.watermark);
                        if (PlayPads.pressLed)
                            arg0.getRootView()
                                    .findViewById(1)
                                    .findViewById(R.id.press)
                                    .setAlpha(PlayPads.watermark);
                        if (PlayPads.progressAutoplay != null)
                            PlayPads.progressAutoplay.setAlpha(PlayPads.watermark);
                        arg0.getRootView()
                                .findViewById(PlayPads.chainId)
                                .findViewById(R.id.press)
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
            };

        } else if (VariaveisStaticas.chainsIDlist.contains(viewId + "")) { // chains
            return new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        int viewId = view.getId();
                        PlayPads.chainId = viewId;
                        if ((PlayPads.autoPlayThread != null)
                                && PlayPads.autoPlayThread.isPaused()) {
                            PlayPads.autoPlayThread.chainChanged();
                            PlayPads.autoPlayThread.touch(
                                    Integer.parseInt(PlayPads.chainId + "" + view.getId()));
                        }
                        if (viewId != PlayPads.otherChain) {
                            PlayPads.chainSl = (String) "" + chainsID.indexOf(viewId + "");
                            //	Toast.makeText(context, PlayPads.chainSl, Toast.LENGTH_SHORT).show();
                            //	PlayPads.chainSl = ((viewId - 9) / 10) + "";

                            ImageView img =
                                    context.findViewById(PlayPads.otherChain)
                                            .findViewById(R.id.press);
                            img.setAlpha(0.0f);
                            img = view.findViewById(R.id.press);
                            img.setImageDrawable(context.getDrawable(R.drawable.currentchain));
                            img.setAlpha(PlayPads.watermark);
                            PlayPads.otherChain = view.getId();

                            //	if ((PlayPads.autoPlayThread != null) &&
                            // !PlayPads.autoPlayThread.isPaused()) {
                            for (String k : PlayPads.ledrpt.keySet()) {
                                PlayPads.ledrpt.put(k, 0);
                            }
                            PlayPads.mSoundLoader.resetRpt();
                            //	}
                            if (PlayPads.recAutoplay) {
                                AutoplayRecFunc.addChain(PlayPads.chainSl);
                            }
                        }

                        return true;
                    } else {
                        return false;
                    }
                }
            };
        } else if (!Arrays.asList(btnsIDs).contains(viewId)) { // Pads
            return new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (PlayPads.recAutoplay) {
                                AutoplayRecFunc.autoPlayRecord(view.getId());
                            }
                            if (PlayPads.pressLed) {
                                PlayPads.padPressAlpha = 1.0f;
                                view.findViewById(R.id.press).setAlpha(PlayPads.padPressAlpha);
                            }
                            if ((PlayPads.autoPlayThread != null)
                                    && PlayPads.autoPlayThread.isPaused()) {
                                PlayPads.autoPlayThread.touch(
                                        Integer.parseInt(PlayPads.chainId + "" + view.getId()));
                            }

                            if (PlayPads.slideMode) {
                                slidePad.put(view.getId(), new HashMap<Integer, Integer>());
                                slidePad.get(view.getId()).put(SLIDE_LIMIT_X, 0);
                                slidePad.get(view.getId()).put(SLIDE_LIMIT_Y, 0);
                                slidePad.get(view.getId()).put(SLIDE_PAD_ATUAL, view.getId());
                            }
                            playSound(view);
                            return true;
                        case MotionEvent.ACTION_UP:
                            if (PlayPads.pressLed) {
                                PlayPads.padPressAlpha = 0.0f;
                                view.findViewById(R.id.press).setAlpha(PlayPads.padPressAlpha);
                            }
                            if (PlayPads.slideMode)
                                view =
                                        context.findViewById(
                                                slidePad.get(view.getId()).get(SLIDE_PAD_ATUAL));
                            if ((PlayPads.autoPlayThread == null)
                                    || !((String) PlayPads.chainSl + "9" + view.getId())
                                            .equals("" + PlayPads.autoPlayThread.padWaiting))
                                view.findViewById(R.id.press).setAlpha(0.0f);
                            // Stop led 0 looper
                            try {
                                PlayPads.threadMap
                                        .get(PlayPads.chainSl + view.getId())
                                        .stopZeroLooper();
                            } catch (NullPointerException n) {
                                Log.e("Stop zero looper", n.getStackTrace()[0].toString());
                            }
                            if (PlayPads.keySound == null) {

                            } else {

                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (PlayPads.slideMode) {
                                float x = motionEvent.getX();
                                float y = motionEvent.getY();
                                int slidelimit_x = slidePad.get(view.getId()).get(SLIDE_LIMIT_X);
                                int slidelimit_y = slidePad.get(view.getId()).get(SLIDE_LIMIT_Y);
                                int padAtual = slidePad.get(view.getId()).get(SLIDE_PAD_ATUAL);
                                int olderPadAtual = padAtual;
                                int padWH = PlayPads.padWH;
                                if ((x > slidelimit_x + padWH || x < slidelimit_x)
                                        || (y > slidelimit_y + padWH || y < slidelimit_y)) {
                                    if ((PlayPads.autoPlayThread == null)
                                            || !((String) PlayPads.chainSl + "9" + view.getId())
                                                    .equals(
                                                            ""
                                                                    + PlayPads.autoPlayThread
                                                                            .padWaiting))
                                        context.findViewById(padAtual)
                                                .findViewById(R.id.press)
                                                .setAlpha(0.0f);
                                    // if(x > slidelimit_x+padWH || x < slidelimit_x)
                                    if (x > slidelimit_x + padWH) {
                                        padAtual += 1;
                                        slidelimit_x += padWH;
                                    } else if (x < slidelimit_x) {
                                        padAtual -= 1;
                                        slidelimit_x -= padWH;
                                    }
                                    // if(y > slidelimit_y+padWH || y < slidelimit_y)
                                    if (y > slidelimit_y + padWH) {
                                        padAtual += 10;
                                        slidelimit_y += padWH;
                                    } else if (y < slidelimit_y) {
                                        padAtual -= 10;
                                        slidelimit_y -= padWH;
                                    }

                                    slidePad.get(view.getId()).put(SLIDE_PAD_ATUAL, padAtual);
                                    slidePad.get(view.getId()).put(SLIDE_LIMIT_X, slidelimit_x);
                                    slidePad.get(view.getId()).put(SLIDE_LIMIT_Y, slidelimit_y);

                                    int w = layoutpads.getLayoutParams().width;
                                    int ww = (MainActivity.width / 2) - (w / 2);
                                    if (!(motionEvent.getRawX() < ww
                                                    || motionEvent.getRawX() > ww + w)
                                            && !(padAtual == 90
                                                    || padAtual == 99
                                                    || padAtual == 9
                                                    || padAtual == 0)) {
                                        view = context.findViewById(padAtual);
                                        playSound(view);
                                    }
                                }
                            }
                            break;
                    }
                    return true;
                }
            };
        }
        // Toast.makeText(context, ""+view.getId(), Toast.LENGTH_SHORT).show();
        return null;
    }

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
                                                    arg0.findViewById(R.id.press).setAlpha(0.0f);
                                                return true;
                                        }
                                        return false;
                                    }
                                });
            }
        }
    }

    private void playSound(View view) {
        int viewId = view.getId();
        String pad = PlayPads.chainSl + view.getId();
        String toChain = null;

        // Play sound sample
        if(PlayPads.have_sounds) PlayPads.mSoundLoader.playSound(pad);
       
        // Show leds
        if (((!PlayPads.stopAll) && PlayPads.ledFiles != null)
                && PlayPads.ledFiles.get(pad) != null) {
            if ((PlayPads.ledrpt.get(view.getId() + "") == null)
                    || PlayPads.ledFiles.get(pad).size()
                            == PlayPads.ledrpt.get(view.getId() + "")) {
                PlayPads.ledrpt.put(view.getId() + "", 0);
            }
            PlayPads.ledFunc.readKeyLed(
                    PlayPads.ledrpt.get(view.getId() + ""), pad, context, view.getRootView());
            PlayPads.ledrpt.put(view.getId() + "", PlayPads.ledrpt.get(view.getId() + "") + 1);
        }
    }
}
