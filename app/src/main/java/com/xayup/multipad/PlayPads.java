package com.xayup.multipad;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xayup.debug.XLog;
import com.xayup.multipad.configs.GlobalConfigs;
import com.xayup.multipad.layouts.options.PlayPadsOptionsInterface;

import com.xayup.multipad.pads.*;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.pads.Render.PadSkinData;
import com.xayup.multipad.projects.project.autoplay.AutoPlay;

public class PlayPads implements PlayPadsOptionsInterface {

    public Activity context;
    protected GridPadsReceptor gridPadsReceptor;

    public PlayPads(Activity context, ViewGroup pads_to_add) {
        this.context = context;
        loadConfigs();
        createUi(context, pads_to_add);
    }

    public void loadConfigs() {
        // configs
        GlobalConfigs.PlayPadsConfigs.skin_package = GlobalConfigs.app_configs.getString("skin", context.getPackageName());
        GlobalConfigs.PlayPadsConfigs.glow_enabled = GlobalConfigs.app_configs.getBoolean("glowEf", false);
        GlobalConfigs.PlayPadsConfigs.slideMode = GlobalConfigs.app_configs.getBoolean("slideMode", false);
        GlobalConfigs.PlayPadsConfigs.glowChainIntensity = GlobalConfigs.app_configs.getFloat("glowChainIntensity", 0.6f);
        GlobalConfigs.PlayPadsConfigs.glowIntensity = GlobalConfigs.app_configs.getFloat("glowPadIntensity", 0.9f);
        GlobalConfigs.PlayPadsConfigs.glowPadRadius = GlobalConfigs.app_configs.getInt("glowPadRadius", 180);
        GlobalConfigs.PlayPadsConfigs.glowChainRadius = GlobalConfigs.app_configs.getInt("glowChainRadius", 160);
        GlobalConfigs.PlayPadsConfigs.stopRecAutoplay = context.findViewById(R.id.stopAutoplayRec);
    }

    public void createUi(Activity context, ViewGroup pads_to_add) {
        // Create Pads Layout
        gridPadsReceptor = new GridPadsReceptor(context){
            @Override
            public boolean onTheButtonSign(PadGrid padGrid, MakePads.PadInfo padInfo, MotionEvent event) {
                View v = padGrid.getPads().getPadView(padInfo.getRow(), padInfo.getColum());
                if (padInfo.getRow() == 0) {
                    switch (padInfo.getColum()) {
                        case 1:
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                {
                                    gridPadsReceptor.watermark_press = !gridPadsReceptor.watermark_press;
                                    if (gridPadsReceptor.watermark_press) v.findViewById(R.id.btn_).setAlpha(1f);
                                    else v.findViewById(R.id.btn_).setAlpha(0f);
                                    return true;
                                }
                            }
                            return false;
                        case 2:
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                {
                                    if(padGrid.getProject().getPadPress() != null && padGrid.getProject().getKeyLED() != null) {
                                        if (padGrid.getProject().getPadPress().calls.contains(padGrid.getProject().getKeyLED())) {
                                            padGrid.getProject().getPadPress().calls.remove(padGrid.getProject().getKeyLED());
                                            if (padGrid.getProject().getKeySounds() != null) {
                                                padGrid.getProject().getPadPress().calls.remove(padGrid.getProject().getKeySounds());
                                                padGrid.getProject().getKeySounds().stopAll();
                                            }
                                            v.findViewById(R.id.btn_).setAlpha(0f);
                                        } else {
                                            padGrid.getProject().getPadPress().calls.add(padGrid.getProject().getKeyLED());
                                            if (padGrid.getProject().getKeySounds() != null)
                                                padGrid.getProject().getPadPress().calls.add(padGrid.getProject().getKeySounds());
                                            v.findViewById(R.id.btn_).setAlpha(1f);
                                        }
                                    }
                                    return true;
                                }
                            }
                            return false;
                        case 3:
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                {
                                    if (padGrid.getProject().getAutoPlay() != null) {
                                        if (padGrid.getProject().getAutoPlay().isRunning()) {
                                            padGrid.getProject().getAutoPlay().stopAutoPlay();
                                            v.findViewById(R.id.btn_).setAlpha(0f);
                                            // Disable control
                                            /////// TEMPORARY ///////
                                            ViewGroup a = padGrid.getGridPads().findViewById(MakePads.PadID.getId(0, 4)); a.removeView(a.getChildAt(a.getChildCount()-1));
                                            ViewGroup b = padGrid.getGridPads().findViewById(MakePads.PadID.getId(0, 5)); b.removeView(b.getChildAt(b.getChildCount()-1));
                                            ViewGroup c = padGrid.getGridPads().findViewById(MakePads.PadID.getId(0, 6)); c.removeView(c.getChildAt(c.getChildCount()-1));

                                        } else {
                                            padGrid.getProject().getAutoPlay().startAutoPlay(new AutoPlay.AutoPlayChanges() {
                                                @Override
                                                public View getViewShowPracticalMark(int r, int c) {
                                                    return padGrid.getGridPads().findViewById(MakePads.PadID.getId(r, c)).findViewById(R.id.btn_);
                                                }

                                                @Override
                                                public View getPadToTouch(int r, int c) {
                                                    return padGrid.getGridPads().findViewById(MakePads.PadID.getId(r, c));
                                                }

                                                @Override
                                                public MakePads.ChainInfo getCurrentChainProperties() {
                                                    return gridPadsReceptor.current_chain;
                                                }

                                                @Override
                                                public PadSkinData getSkinData() {
                                                    return gridPadsReceptor.getActivePads().getSkinData();
                                                }

                                                @Override
                                                public void onStopped(PadPressCallInterface call) {
                                                    if(padGrid.getProject().getPadPress() != null) {
                                                        padGrid.getProject().getPadPress().calls.remove(call);
                                                    }
                                                }

                                                @Override
                                                public void onStarted(PadPressCallInterface call) {
                                                    if(padGrid.getProject().getPadPress() != null) {
                                                        padGrid.getProject().getPadPress().calls.add(call);
                                                    }
                                                }
                                            });
                                            v.findViewById(R.id.btn_).setAlpha(1f);
                                            // Enable control
                                            /////// TEMPORARY ///////
                                            ImageView a = new ImageView(context); a.setImageDrawable(context.getDrawable(R.drawable.play_prev)); a.setRotation(90f);
                                            ImageView b = new ImageView(context); b.setImageDrawable(context.getDrawable(R.drawable.play_pause)); b.setRotation(90f);
                                            ImageView c = new ImageView(context); c.setImageDrawable(context.getDrawable(R.drawable.play_prev)); c.setScaleX(-1f); c.setRotation(90f);
                                            ((ViewGroup) padGrid.getGridPads().findViewById(MakePads.PadID.getId(0, 4))).addView(a, new ViewGroup.LayoutParams(-1, -1));
                                            ((ViewGroup) padGrid.getGridPads().findViewById(MakePads.PadID.getId(0, 5))).addView(b, new ViewGroup.LayoutParams(-1, -1));
                                            ((ViewGroup) padGrid.getGridPads().findViewById(MakePads.PadID.getId(0, 6))).addView(c, new ViewGroup.LayoutParams(-1, -1));
                                        }
                                    }
                                    return true;
                                }
                            }
                            return false;
                        case 4:
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                {
                                    if (padGrid.getProject().getAutoPlay() != null && padGrid.getProject().getAutoPlay().isRunning()) {
                                        padGrid.getProject().getAutoPlay().regressAutoPlay();
                                        v.findViewById(R.id.btn_).setAlpha(1f);
                                    }
                                    return true;
                                }
                                case MotionEvent.ACTION_UP: {
                                    v.findViewById(R.id.btn_).setAlpha(0f);
                                    return true;
                                }
                            }
                            return false;
                        case 5:
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN: {
                                    if (padGrid.getProject().getAutoPlay() != null && padGrid.getProject().getAutoPlay().isRunning()) {
                                        v.findViewById(R.id.btn_).setAlpha(1f);
                                        if (padGrid.getProject().getAutoPlay().isPaused()) {
                                            padGrid.getProject().getAutoPlay().resumeAutoPlay();
                                        } else {
                                            padGrid.getProject().getAutoPlay().pauseAutoPlay(2);
                                        }
                                    }
                                    return true;
                                }
                                case MotionEvent.ACTION_UP: {
                                    v.findViewById(R.id.btn_).setAlpha(0f);
                                    return true;
                                }
                            }
                            return false;
                        case 6:
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN: {
                                    if (padGrid.getProject().getAutoPlay() != null && padGrid.getProject().getAutoPlay().isRunning()) {
                                        v.findViewById(R.id.btn_).setAlpha(1f);
                                        padGrid.getProject().getAutoPlay().advanceAutoPlay();
                                    }
                                    return true;
                                }
                                case MotionEvent.ACTION_UP: {
                                    v.findViewById(R.id.btn_).setAlpha(0f);
                                    return true;
                                }
                            }
                            return false;
                        case 7:
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN: {
                                    v.findViewById(R.id.btn_).setAlpha(1f);
                                    return true;
                                }
                                case MotionEvent.ACTION_UP: {
                                    v.findViewById(R.id.btn_).setAlpha(0f);
                                    return true;
                                }
                            }
                            return false;
                        case 8:
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN: {
                                    gridPadsReceptor.watermark = !gridPadsReceptor.watermark;
                                    if (gridPadsReceptor.watermark) {
                                        v.findViewById(R.id.btn_).setAlpha(1f);
                                    } else {
                                        v.findViewById(R.id.btn_).setAlpha(0f);
                                    }
                                }
                            }
                            return false;
                    }
                } else if (padInfo.getType() == MakePads.PadInfo.PadInfoIdentifier.CHAIN) {
                        XLog.v("Chain press", "X: " + padInfo.getRow() + ",Y: " + padInfo.getColum());
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN: {
                                boolean chain_changed = padGrid.getCurrentChain().getMc() != ((MakePads.ChainInfo) padInfo).getMc();
                                if(chain_changed) {
                                    boolean synchronized_chains_project = true;
                                    if (synchronized_chains_project) {
                                        padGrid.getProject().setCurrentChain(padInfo.getRow(), padInfo.getColum());
                                    } else {
                                        padGrid.setCurrentChain(padInfo.getRow(), padInfo.getColum());
                                    }
                                    if (padGrid.getProject().getKeySounds() != null)
                                        padGrid.getProject().getKeySounds().resetSequencer();
                                    if (padGrid.getProject().getKeyLED() != null)
                                        padGrid.getProject().getKeyLED().resetSequence();

                                    padGrid.getProject().callPress(padGrid, padInfo);
                                }
                                gridPadsReceptor.current_chain.setCurrentChain(padInfo.getRow(), padInfo.getColum());
                                // After set this chain to current chain
                                gridPadsReceptor.getActivePads().getGridPads().findViewById(gridPadsReceptor.current_chain.getId()).findViewById(R.id.btn_).setAlpha(1f);
                                return true;
                            }
                            case MotionEvent.ACTION_UP: {
                                //padGrid.getProject().mPadRelease.call(gridPads.current_chain, mChainInfo);
                                return true;
                            }
                        }
                        return false;
                } else if (padInfo.getType() == MakePads.PadInfo.PadInfoIdentifier.PAD) {
                        XLog.v("Pad press", "X: " + padInfo.getRow() + ",Y: " + padInfo.getColum());
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:{
                                padGrid.getProject().callPress(padGrid, padInfo);
                                if(gridPadsReceptor.watermark_press) {
                                    if (padGrid.getSkinData().draw_btn_ != null) v.findViewById(R.id.btn_).setAlpha(1f);
                                    else {
                                        padGrid.getPads().setLedColor(padInfo.getRow(), padInfo.getColum(), padGrid.getSkinData().draw_btn__color);
                                        padInfo.markAsActivated(true); }
                                }
                                return true;
                            }
                            case MotionEvent.ACTION_UP: {
                                if(gridPadsReceptor.watermark_press) {
                                    if (padGrid.getSkinData().draw_btn_ != null) v.findViewById(R.id.btn_).setAlpha(0f);
                                    else {
                                        padInfo.markAsActivated(false);
                                        padGrid.getPads().setLedColor(padInfo.getRow(), padInfo.getColum(), 0); }
                                }
                                return true;
                            }
                        }

                        return false;
                }
                return false;
            }

            @Override
            public void onGridCreated(PadGrid padGrid) {
                //// Setup actives ////
                // Current Chain
                padGrid.getPads().getPadView(
                        gridPadsReceptor.current_chain.getRow(), gridPadsReceptor.current_chain.getColum()).findViewById(R.id.btn_).setAlpha(1f);
                // LEDs
                padGrid.getPads().getPadView(0, 2).findViewById(R.id.btn_).setAlpha(1f);
                // Actives Watermark
                padGrid.getPads().getPadView(0, 8).findViewById(R.id.btn_).setAlpha(1f);
            }

            @Override
            public void onGridDeleted() {

            }
        };
    }

    public GridPadsReceptor getPads(){
        return gridPadsReceptor;
    }

}
