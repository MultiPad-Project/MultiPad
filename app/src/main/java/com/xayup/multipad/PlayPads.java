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

public class PlayPads extends Activity implements PlayPadsOptionsInterface {

    public Activity context;

    //LoadScreen mLoadScreen;
    protected RelativeLayout pads_to_add;
    protected GridPads gridPads;

    protected OnTouchListener onPadTouch,
            onChainTouch,
            onPressWatermarkTouch,
            onLedSwitchTouch,
            onAutoplaySwitchTouch,
            onAutoplayPrevTouch,
            onAutoplayPauseTouch,
            onAutoplayNextTouch,
            onLayoutSwitchTouch,
            onWatermarkTouch;

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
        /*
        // Set pads functions
        onPadTouch = onPadTouch();
        onChainTouch = onChainTouch();
        onPressWatermarkTouch = onPressWatermarkTouch();
        onLedSwitchTouch = onLedSwitchTouch();
        onAutoplaySwitchTouch = onAutoplaySwitchTouch();
        onAutoplayPrevTouch = onAutoplayPrevTouch();
        onAutoplayPauseTouch = onAutoplayPauseTouch();
        onAutoplayNextTouch = onAutoplayNextTouch();
        onLayoutSwitchTouch = onLayoutSwitchTouch();
        onWatermarkTouch = onWatermarkTouch();
         */
        // Create Pads Layout
        gridPads = new GridPads(context);
        // Make new Pads object
        gridPads.newPads(GlobalConfigs.PlayPadsConfigs.skin_package, 10, 10);
        gridPads.getActivePads().setForAllPadInteraction(padInteraction());
        (this.pads_to_add = (RelativeLayout) pads_to_add).post(
                () -> {
                    // Get display size from MATCH_PARENT view
                    int h = this.pads_to_add.getMeasuredHeight();
                    int w = this.pads_to_add.getMeasuredWidth();


                    // Get View from Pads
                    ViewGroup virtual_launchpad = gridPads.getActivePads().getContainer(); //Background

                    // Prepare Params after add Pads to scene
                    RelativeLayout.LayoutParams bParams = new RelativeLayout.LayoutParams(w/2, w/2);
                    /// TEST //// bParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                    bParams.addRule(RelativeLayout.CENTER_VERTICAL);
                    bParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

                    // Add Pads to scene
                    this.pads_to_add.addView(virtual_launchpad, bParams);

                    /*/ Get Pads grid params and set new values (Size)
                    ViewGroup.LayoutParams rLayout = gridPads.getActivePads().getGridPads().getLayoutParams();
                    rLayout.height = h;
                    rLayout.width = h;
                    */
                    /*Setup actives*/
                    /*Current Chain*/
                    gridPads.getActivePads().getGridPads().findViewById(
                            MakePads.PadID.getId(gridPads.current_chain.getRow(), gridPads.current_chain.getColum())).findViewById(R.id.press).setAlpha(1f);

                    /*LEDs*/
                    gridPads.getActivePads().getGridPads().findViewById(
                            MakePads.PadID.getId(0, 2)).findViewById(R.id.press).setAlpha(1f);

                    /*Actives Watermark*/
                    gridPads.getActivePads().getGridPads().findViewById(
                            MakePads.PadID.getId(0, 8)).findViewById(R.id.press).setAlpha(1f);
                });
    }

    protected PadInteraction padInteraction() {
        XLog.v("PAD INTERACTION", "");
        return (view, padGrid) -> {
            MakePads.PadInfo mPadInfo = (MakePads.PadInfo) view.getTag();
            if (mPadInfo.getRow() == 0) {
                switch (mPadInfo.getColum()) {
                    case 1: return (View v, MotionEvent event) -> {
                        v.performClick();
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                            {
                                gridPads.watermark_press = !gridPads.watermark_press;
                                if (gridPads.watermark_press) v.findViewById(R.id.press).setAlpha(1f);
                                else v.findViewById(R.id.press).setAlpha(0f);
                                return true;
                            }
                        }

                        return false;
                    };
                    case 2: return (View v, MotionEvent event) -> {
                        v.performClick();
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                            {
                                if(padGrid.getProject().getPadPress() != null && padGrid.getProject().mKeyLED != null) {
                                    if (padGrid.getProject().getPadPress().calls.contains(padGrid.getProject().mKeyLED)) {
                                        padGrid.getProject().getPadPress().calls.remove(padGrid.getProject().mKeyLED);
                                        if (padGrid.getProject().mKeySounds != null) {
                                            padGrid.getProject().getPadPress().calls.remove(padGrid.getProject().mKeySounds);
                                            padGrid.getProject().mKeySounds.stopAll();
                                        }
                                        v.findViewById(R.id.press).setAlpha(0f);
                                    } else {
                                        padGrid.getProject().getPadPress().calls.add(padGrid.getProject().mKeyLED);
                                        if (padGrid.getProject().mKeySounds != null)
                                            padGrid.getProject().getPadPress().calls.add(padGrid.getProject().mKeySounds);
                                        v.findViewById(R.id.press).setAlpha(1f);
                                    }
                                }
                                return true;
                            }
                        }
                        return false;
                    };
                    case 3: return (View v, MotionEvent event) -> {
                        v.performClick();
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                            {
                                if (padGrid.getProject().mAutoPlay != null) {
                                    if (padGrid.getProject().mAutoPlay.isRunning()) {
                                        padGrid.getProject().mAutoPlay.stopAutoPlay();
                                        v.findViewById(R.id.press).setAlpha(0f);
                                        // Disable control
                                        /////// TEMPORARY ///////
                                        ViewGroup a = gridPads.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(0, 4)); a.removeView(a.getChildAt(a.getChildCount()-1));
                                        ViewGroup b = gridPads.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(0, 5)); b.removeView(b.getChildAt(b.getChildCount()-1));
                                        ViewGroup c = gridPads.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(0, 6)); c.removeView(c.getChildAt(c.getChildCount()-1));

                                    } else {
                                        padGrid.getProject().mAutoPlay.startAutoPlay(new AutoPlay.AutoPlayChanges() {
                                            @Override
                                            public View getViewShowPracticalMark(int r, int c) {
                                                return gridPads.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(r, c)).findViewById(R.id.press);
                                            }

                                            @Override
                                            public View getPadToTouch(int r, int c) {
                                                return gridPads.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(r, c));
                                            }

                                            @Override
                                            public MakePads.ChainInfo getCurrentChainProperties() {
                                                return gridPads.current_chain;
                                            }

                                            @Override
                                            public PadSkinData getSkinData() {
                                                return gridPads.getActivePads().getSkinData();
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
                                        v.findViewById(R.id.press).setAlpha(1f);
                                        // Enable control
                                        /////// TEMPORARY ///////
                                        ImageView a = new ImageView(context); a.setImageDrawable(context.getDrawable(R.drawable.play_prev)); a.setRotation(90f);
                                        ImageView b = new ImageView(context); b.setImageDrawable(context.getDrawable(R.drawable.play_pause)); b.setRotation(90f);
                                        ImageView c = new ImageView(context); c.setImageDrawable(context.getDrawable(R.drawable.play_prev)); c.setScaleX(-1f); c.setRotation(90f);
                                        ((ViewGroup) gridPads.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(0, 4))).addView(a, new ViewGroup.LayoutParams(-1, -1));
                                        ((ViewGroup) gridPads.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(0, 5))).addView(b, new ViewGroup.LayoutParams(-1, -1));
                                        ((ViewGroup) gridPads.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(0, 6))).addView(c, new ViewGroup.LayoutParams(-1, -1));
                                    }
                                }
                                return true;
                            }
                        }
                        return false;
                    };
                    case 4: return (View v, MotionEvent event) -> {
                        v.performClick();
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                            {
                                if (padGrid.getProject().mAutoPlay != null && padGrid.getProject().mAutoPlay.isRunning()) {
                                    padGrid.getProject().mAutoPlay.regressAutoPlay();
                                    v.findViewById(R.id.press).setAlpha(1f);
                                }
                                return true;
                            }
                            case MotionEvent.ACTION_UP: {
                                v.findViewById(R.id.press).setAlpha(0f);
                                return true;
                            }
                        }
                        return false;
                    };
                    case 5: return (View v, MotionEvent event) -> {
                        v.performClick();
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                            {
                                if (padGrid.getProject().mAutoPlay != null && padGrid.getProject().mAutoPlay.isRunning()) {
                                    v.findViewById(R.id.press).setAlpha(1f);
                                    if (padGrid.getProject().mAutoPlay.isPaused()) {
                                        padGrid.getProject().mAutoPlay.resumeAutoPlay();
                                    } else {
                                        padGrid.getProject().mAutoPlay.pauseAutoPlay(2);
                                    }
                                }
                                return true;
                            }
                            case MotionEvent.ACTION_UP: {
                                v.findViewById(R.id.press).setAlpha(0f);
                                return true;
                            }
                        }
                        return false;
                    };
                    case 6: return (View v, MotionEvent event) -> {
                        v.performClick();
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                            {
                                if (padGrid.getProject().mAutoPlay != null && padGrid.getProject().mAutoPlay.isRunning()) {
                                    v.findViewById(R.id.press).setAlpha(1f);
                                    padGrid.getProject().mAutoPlay.advanceAutoPlay();
                                }
                                return true;
                            }
                            case MotionEvent.ACTION_UP: {
                                v.findViewById(R.id.press).setAlpha(0f);
                                return true;
                            }
                        }
                        return false;
                    };
                    case 7: return (View v, MotionEvent event) -> {
                        v.performClick();
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                            {
                                v.findViewById(R.id.press).setAlpha(1f);
                                return true;
                            }
                            case MotionEvent.ACTION_UP: {
                                v.findViewById(R.id.press).setAlpha(0f);
                                return true;
                            }
                        }
                        return false;
                    };
                    case 8: return (View v, MotionEvent event) -> {
                        v.performClick();
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                            {
                                gridPads.watermark = !gridPads.watermark;
                                if (gridPads.watermark) {
                                    v.findViewById(R.id.press).setAlpha(1f);
                                } else {
                                    v.findViewById(R.id.press).setAlpha(0f);
                                }
                            }
                        }
                        return false;
                    };
                }
            } else if (mPadInfo.getType() == MakePads.PadInfo.PadInfoIdentifier.CHAIN) {
                return (View v, MotionEvent event) -> {
                    MakePads.ChainInfo mChainInfo = (MakePads.ChainInfo) v.getTag();
                    XLog.v("Chain press", "X: " + mChainInfo.getRow() + ",Y: " + mChainInfo.getColum());
                    v.performClick();
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            gridPads.getActivePads().getGridPads().findViewById(gridPads.current_chain.getId()).findViewById(R.id.press).setAlpha(0f);
                            boolean chain_changed = gridPads.current_chain.getMc() != mChainInfo.getMc();
                            if(padGrid.getProject().mKeySounds != null && chain_changed) padGrid.getProject().mKeySounds.resetSequencer();
                            if(padGrid.getProject().mKeyLED != null && chain_changed) padGrid.getProject().mKeyLED.resetSequence();

                            padGrid.getProject().callPress(gridPads.current_chain, mChainInfo);

                            gridPads.current_chain.setCurrentChain(mChainInfo.getRow(), mChainInfo.getColum());
                            // After set this chain to current chain
                            gridPads.getActivePads().getGridPads().findViewById(gridPads.current_chain.getId()).findViewById(R.id.press).setAlpha(1f);
                            return true;
                        }
                        case MotionEvent.ACTION_UP: {
                            //padGrid.getProject().mPadRelease.call(gridPads.current_chain, mChainInfo);
                            return true;
                        }
                    }
                    return false;
                };
            } else if (mPadInfo.getType() == MakePads.PadInfo.PadInfoIdentifier.PAD) {
                return (View v, MotionEvent event) -> {
                    XLog.v("Pad press", "X: " + mPadInfo.getRow() + ",Y: " + mPadInfo.getColum());
                    v.performClick();
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:{
                            padGrid.getProject().callPress(gridPads.current_chain, mPadInfo);
                            if(gridPads.watermark_press) v.findViewById(R.id.press).setAlpha(1f);
                            return true;
                        }
                        case MotionEvent.ACTION_UP: {
                            //mPadRelease.call(gridPads.current_chain, mPadInfo);
                            if(gridPads.watermark_press) v.findViewById(R.id.press).setAlpha(0f);
                            return true;
                        }
                    }

                    return false;
                };
            }
            return null;
        };
    }

    public GridPads getPads(){
        return gridPads;
    }

    public void addNewGrid(){
        gridPads.newPads(GlobalConfigs.PlayPadsConfigs.skin_package, 10, 10);
        gridPads.getActivePads().setForAllPadInteraction(padInteraction());
        RelativeLayout.LayoutParams bParams = new RelativeLayout.LayoutParams(GlobalConfigs.display_height, GlobalConfigs.display_height);
        bParams.addRule(RelativeLayout.CENTER_VERTICAL);
        bParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        pads_to_add.addView(gridPads.getActivePads().getContainer(), bParams);
    }

    /*
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public OnTouchListener onPadTouch() {
        return (View v, MotionEvent event) -> {
            MakePads.PadInfo mPadInfo = (MakePads.PadInfo) v.getTag();
            XLog.v("Pad press", "X: " + mPadInfo.getRow() + ",Y: " + mPadInfo.getColum());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:{
                    mPadPress.call(pad.current_chain, mPadInfo);
                    if(pad.watermark_press) v.findViewById(R.id.press).setAlpha(1f);
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    mPadRelease.call(pad.current_chain, mPadInfo);
                    if(pad.watermark_press) v.findViewById(R.id.press).setAlpha(0f);
                    return true;
                }
            }

            return false;
        };
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public OnTouchListener onChainTouch() {
        return (View v, MotionEvent event) -> {
            MakePads.ChainInfo mChainInfo = (MakePads.ChainInfo) v.getTag();
            XLog.v("Chain press", "X: " + mChainInfo.getRow() + ",Y: " + mChainInfo.getColum());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    pad.getActivePads().getGridPads().findViewById(pad.current_chain.getId()).findViewById(R.id.press).setAlpha(0f);
                    boolean chain_changed = pad.current_chain.getMc() != mChainInfo.getMc();
                    if(mKeySounds != null && chain_changed) mKeySounds.resetSequencer();
                    if(mKeyLED != null && chain_changed) mKeyLED.resetSequence();

                    mPadPress.call(pad.current_chain, mChainInfo);

                    pad.current_chain.setCurrentChain(mChainInfo.getRow(), mChainInfo.getColum());
                    // After set this chain to current chain
                    pad.getActivePads().getGridPads().findViewById(pad.current_chain.getId()).findViewById(R.id.press).setAlpha(1f);
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    mPadRelease.call(pad.current_chain, mChainInfo);
                    return true;
                }
            }
            return false;
        };
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public OnTouchListener onPressWatermarkTouch() {
        return (View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    {
                        pad.watermark_press = !pad.watermark_press;
                        if (pad.watermark_press) v.findViewById(R.id.press).setAlpha(1f);
                        else v.findViewById(R.id.press).setAlpha(0f);
                        return true;
                    }
            }

            return false;
        };
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public OnTouchListener onLedSwitchTouch() {
        return (View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    {
                        if (mKeyLED != null) {
                            if (mPadPress.calls.contains(mKeyLED)) {
                                mPadPress.calls.remove(mKeyLED);
                                if (mKeySounds != null){
                                    mPadPress.calls.remove(mKeySounds);
                                    mKeySounds.stopAll();
                                }
                                v.findViewById(R.id.press).setAlpha(0f);
                            } else {
                                mPadPress.calls.add(mKeyLED);
                                if (mKeySounds != null) mPadPress.calls.add(mKeySounds);
                                v.findViewById(R.id.press).setAlpha(1f);
                            }
                        }
                        return true;
                    }
            }
            return false;
        };
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public OnTouchListener onAutoplaySwitchTouch() {
        return (View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    {
                        if (mAutoPlay != null) {
                            if (mAutoPlay.isRunning()) {
                                mAutoPlay.stopAutoPlay();
                                v.findViewById(R.id.press).setAlpha(0f);
                                // Disable control
                                    /////// TEMPORARY ///////
                                ViewGroup a = pad.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(0, 4)); a.removeView(a.getChildAt(a.getChildCount()-1));
                                ViewGroup b = pad.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(0, 5)); b.removeView(b.getChildAt(b.getChildCount()-1));
                                ViewGroup c = pad.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(0, 6)); c.removeView(c.getChildAt(c.getChildCount()-1));

                            } else {
                                mAutoPlay.startAutoPlay(new AutoPlay.AutoPlayChanges() {
                                    @Override
                                    public View getViewShowPracticalMark(int r, int c) {
                                        return pad.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(r, c)).findViewById(R.id.press);
                                    }

                                    @Override
                                    public View getPadToTouch(int r, int c) {
                                        return pad.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(r, c));
                                    }

                                    @Override
                                    public MakePads.ChainInfo getCurrentChainProperties() {
                                        return pad.current_chain;
                                    }

                                    @Override
                                    public PadSkinData getSkinData() {
                                        return pad.getActivePads().getSkinData();
                                    }

                                    @Override
                                    public void onStopped(PadPressCallInterface call) {
                                        mPadPress.calls.remove(call);
                                    }

                                    @Override
                                    public void onStarted(PadPressCallInterface call) {
                                        mPadPress.calls.add(call);
                                    }
                                });
                                v.findViewById(R.id.press).setAlpha(1f);
                                // Enable control
                                    /////// TEMPORARY ///////
                                ImageView a = new ImageView(context); a.setImageDrawable(context.getDrawable(R.drawable.play_prev)); a.setRotation(90f);
                                ImageView b = new ImageView(context); b.setImageDrawable(context.getDrawable(R.drawable.play_pause)); b.setRotation(90f);
                                ImageView c = new ImageView(context); c.setImageDrawable(context.getDrawable(R.drawable.play_prev)); c.setScaleX(-1f); c.setRotation(90f);
                                ((ViewGroup) pad.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(0, 4))).addView(a, new ViewGroup.LayoutParams(-1, -1));
                                ((ViewGroup) pad.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(0, 5))).addView(b, new ViewGroup.LayoutParams(-1, -1));
                                ((ViewGroup) pad.getActivePads().getGridPads().findViewById(MakePads.PadID.getId(0, 6))).addView(c, new ViewGroup.LayoutParams(-1, -1));
                            }
                        }
                        return true;
                    }
            }
            return false;
        };
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public OnTouchListener onAutoplayPrevTouch() {
        return (View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    {
                        if (mAutoPlay != null && mAutoPlay.isRunning()) {
                            mAutoPlay.regressAutoPlay();
                            v.findViewById(R.id.press).setAlpha(1f);
                        }
                        return true;
                    }
                case MotionEvent.ACTION_UP: {
                    v.findViewById(R.id.press).setAlpha(0f);
                    return true;
                }
            }
            return false;
        };
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public OnTouchListener onAutoplayPauseTouch() {
        return (View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    {
                        if (mAutoPlay != null && mAutoPlay.isRunning()) {
                            v.findViewById(R.id.press).setAlpha(1f);
                            if (mAutoPlay.isPaused()) {
                                mAutoPlay.resumeAutoPlay();
                            } else {
                                mAutoPlay.pauseAutoPlay(2);
                            }
                        }
                        return true;
                    }
                case MotionEvent.ACTION_UP: {
                    v.findViewById(R.id.press).setAlpha(0f);
                    return true;
                }
            }
            return false;
        };
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public OnTouchListener onAutoplayNextTouch() {
        return (View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    {
                        if (mAutoPlay != null && mAutoPlay.isRunning()) {
                            v.findViewById(R.id.press).setAlpha(1f);
                            mAutoPlay.advanceAutoPlay();
                        }
                        return true;
                    }
                case MotionEvent.ACTION_UP: {
                    v.findViewById(R.id.press).setAlpha(0f);
                    return true;
                }
            }
            return false;
        };
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public OnTouchListener onLayoutSwitchTouch() {
        return (View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    {
                        v.findViewById(R.id.press).setAlpha(1f);
                        return true;
                    }
                case MotionEvent.ACTION_UP: {
                    v.findViewById(R.id.press).setAlpha(0f);
                    return true;
                }
            }
            return false;
        };
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public OnTouchListener onWatermarkTouch() {
        return (View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    {
                        pad.watermark = !pad.watermark;
                        if (pad.watermark) {
                            v.findViewById(R.id.press).setAlpha(1f);
                        } else {
                            v.findViewById(R.id.press).setAlpha(0f);
                        }
                    }
            }
            return false;
        };
    }
*/
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor =
                    getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap imgdata = BitmapFactory.decodeFile(picturePath);
            switch (requestCode) {
                case 12:
                    ImageView overlay = findViewById(R.id.launchpadOverride);
                    overlay.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                    overlay.setVisibility(View.VISIBLE);
                    break;
                case PICK_PHANTOM_IMG:
                    for (int i = 0; i < 1; i++) {
                        // SkinTheme.pads.get(i).setImageBitmap(imgdata);
                    }
                    break;
                case PICK_BTN_IMG:
                    for (int i = 0; i < 1 ; i++) {
                        // SkinTheme.btnlist.get(i).setImageBitmap(imgdata);
                    }
                    break;
                case PICK_PHANTOMC_IMG:
                    for (int i = 0; i < 1 ; i++) {
                        // SkinTheme.padsCenter.get(i).setImageBitmap(imgdata);
                    }
                    break;
                case PICK_LOGO_IMG:
                    ((ImageView) PlayPads.this.findViewById((int) 9).findViewById(R.id.phantom))
                            .setImageBitmap(imgdata);
                    break;
                case PICK_BACKGROUND_IMG:
                    ((ImageView) PlayPads.this.findViewById(R.id.playbgimg))
                            .setImageBitmap(imgdata);
                    break;
                case PICK_CHAIN_IMG:
                    for (int i = 0; i < 1; i++) {
                        // SkinTheme.chainsled.get(i).setImageBitmap(imgdata);
                    }
                    break;
                case PICK_LOGO_BG_IMG:
                    ((ImageView) PlayPads.this.findViewById((int) 9).findViewById(R.id.pad))
                            .setImageBitmap(imgdata);
                    break;
            }
        }
    }
    */
}
