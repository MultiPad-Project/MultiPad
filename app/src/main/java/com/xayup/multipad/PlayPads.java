package com.xayup.multipad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xayup.debug.XLog;
import com.xayup.multipad.configs.GlobalConfigs;
import com.xayup.multipad.layouts.loadscreen.LoadScreen;
import com.xayup.multipad.layouts.options.PlayPadsOptions;
import com.xayup.multipad.layouts.options.PlayPadsOptionsInterface;
import com.xayup.multipad.load.Project;

import com.xayup.multipad.load.thread.LoadProject;
import com.xayup.multipad.pads.*;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.pads.Render.PadSkinData;
import com.xayup.multipad.project.autoplay.AutoPlay;
import com.xayup.multipad.project.keyled.KeyLED;

import java.util.*;

public class PlayPads extends Activity implements PlayPadsOptionsInterface {

    public Activity context = this;

    LoadScreen mLoadScreen;
    PadPressCall mPadPress;
    PadPressCall mPadRelease;
    PlayPadsOptions mPlayPadsOptions;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playpads);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        XayUpFunctions.hideSystemBars(getWindow());
        ViewGroup rootView = findViewById(R.id.layoutbackground);
        mLoadScreen = new LoadScreen(this, rootView);
        mLoadScreen.show(0);
        rootView.post(
                () -> {
                    GlobalConfigs.display_height = rootView.getMeasuredHeight();
                    GlobalConfigs.display_width = rootView.getMeasuredWidth();
                });
        new LoadConfigs();
        new CreateUi(this);
    }

    class LoadConfigs extends GlobalConfigs.PlayPadsConfigs {
        public LoadConfigs() {
            // configs
            skin_package = GlobalConfigs.app_configs.getString("skin", context.getPackageName());
            glow_enabled = GlobalConfigs.app_configs.getBoolean("glowEf", false);
            slideMode = GlobalConfigs.app_configs.getBoolean("slideMode", false);
            glowChainIntensity = GlobalConfigs.app_configs.getFloat("glowChainIntensity", 0.6f);
            glowIntensity = GlobalConfigs.app_configs.getFloat("glowPadIntensity", 0.9f);
            glowPadRadius = GlobalConfigs.app_configs.getInt("glowPadRadius", 180);
            glowChainRadius = GlobalConfigs.app_configs.getInt("glowChainRadius", 160);
            stopRecAutoplay = context.findViewById(R.id.stopAutoplayRec);
        }
    }

    class CreateUi extends Project implements PadInterface {
        private final RelativeLayout root_pads;
        protected Pad pad;

        public CreateUi(Activity context) {
            /*Set pads functions*/
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
            /*Load Project*/
            loadProject(context, getLoadingProject());
            /*Create Pads Layout*/
            pad = new Pad(context, padInteraction());
            (this.root_pads = context.findViewById(R.id.contAllPads)).post(
                    () -> {
                        /*Get display size from MATCH_PARENT view*/
                        int h = root_pads.getMeasuredHeight();
                        int w = root_pads.getMeasuredWidth();

                        /*Make new Pads object*/
                        pad.newPads(GlobalConfigs.PlayPadsConfigs.skin_package, 10, 10);

                        /*Get View from Pads*/
                        ViewGroup virtual_launchpad = pad.getActivePads().getRootPads(); //Background

                        /*Prepare Params after add Pads to scene*/
                        RelativeLayout.LayoutParams bParams = new RelativeLayout.LayoutParams(h, h);
                        bParams.addRule(RelativeLayout.CENTER_IN_PARENT);

                        /*Add Pads to scene*/
                        root_pads.addView(virtual_launchpad, bParams);

                        /*Get Pads grid params and set new values (Size)*/
                        ViewGroup.LayoutParams rLayout = pad.getActivePads().getGridPads().getLayoutParams();
                        rLayout.height = h;
                        rLayout.width = h;

                        /*Setup actives*/
                        /*Current Chain*/
                        pad.getActivePads().getGridPads().findViewById(
                                MakePads.PadID.getId(pad.current_chain.getRow(), pad.current_chain.getColum())).findViewById(R.id.press).setAlpha(1f);

                        /*LEDs*/
                        pad.getActivePads().getGridPads().findViewById(
                                MakePads.PadID.getId(0, 2)).findViewById(R.id.press).setAlpha(1f);

                        /*Actives Watermark*/
                        pad.getActivePads().getGridPads().findViewById(
                                MakePads.PadID.getId(0, 8)).findViewById(R.id.press).setAlpha(1f);
                    });
        }
        public LoadProject.LoadingProject getLoadingProject() {
            return new LoadProject.LoadingProject() {
                @Override
                public void onStartLoadProject() {
                    /* GET FILES DIRs */
                    path = context.getIntent().getExtras().getString("project_path");
                    XLog.e("Path", path);
                    boolean[] flags = new boolean[FLAG_SIZE];
                    flags[TYPE_SAMPLE_FOLDER] = true;
                    flags[TYPE_KEYLED_FOLDERS] = true;
                    flags[TYPE_AUTOPLAY_FILE] = true;
                    readInProject(flags);
                }
                @Override
                public void onStartReadFile(String file_name) {
                    context.runOnUiThread(() -> mLoadScreen.updatedText(context.getString(R.string.reading) + ": " + file_name));
                }
                @Override
                public void onFileError(String file_name, int line, String cause) {
                    project_loaded_problems.add(line + " : " + file_name + " : " + cause);
                }
                @Override
                public void onFinishLoadProject() {
                    /* Press Pads */
                    mPadPress = new PadPressCall();
                    mPadRelease = new PadPressCall();

                    if (mKeyLED != null) {
                        mKeyLED.setToShowLed((x, y, real_color, lp_index) -> {
                            List<Pad.Pads> mPadsList = pad.getPadsWithIndex(lp_index);
                            //XLog.v("Show led with lp index", String.valueOf(lp_index));
                            if(mPadsList != null) {
                                for(Pad.Pads mPads : mPadsList) {
                                    View pad_view = mPads.getGridPads().getChildAt(MakePads.PadID.getGridIndexFromXY(mPads.getGridPads().getColumnCount(), x, y));
                                    if(pad_view == null){
                                        XLog.e("Led view error", "Touched: Row: " + "Colum: " + "Chain: " + pad.current_chain + " Data: Row: " + x + ", Colum: " + y + ", Child index: " + MakePads.PadID.getGridIndexFromXY(mPads.getGridPads().getColumnCount(), x, y));
                                    } else {
                                        context.runOnUiThread(() -> pad_view.findViewById(R.id.led).setBackgroundColor(real_color));
                                    }
                                }
                            }
                        });
                        mPadPress.calls.add(mKeyLED);
                    }
                    if (mKeySounds != null) mPadPress.calls.add(mKeySounds);

                    /* Hide Load Screen */
                    if (project_loaded_problems.size() > 0) {
                        mLoadScreen.showErrorsList(
                                project_loaded_problems,
                                (view) -> {
                                    mLoadScreen.OnEndAnimation(
                                            () -> {
                                                mLoadScreen.remove();
                                                mLoadScreen = null;
                                            });
                                    mLoadScreen.hide(300);
                                    view.setOnClickListener(null);
                                });
                    } else {
                        context.runOnUiThread(() -> mLoadScreen.hide(500));
                    }
                    context.runOnUiThread(
                            () -> mPlayPadsOptions =
                                    new PlayPadsOptions(context) {
                                        @Override
                                        public void onExit() {
                                            /* Clear */
                                            if (mKeyLED != null) {
                                                mKeyLED.clear();
                                                mKeyLED = null;
                                            }
                                            if (mAutoPlay != null) {
                                                mAutoPlay.clear();
                                                mAutoPlay = null;
                                            }
                                            if (mKeySounds != null) {
                                                mKeySounds.clear();
                                                mKeySounds = null;
                                            }
                                            context.finish();
                                        }
                                        @Override
                                        public KeyLED getKeyLEDInstance() {return mKeyLED;}
                                        @Override
                                        public Pad getPadInstance() {return pad;}
                                    });
                }
            };
        }

        protected PadInteraction padInteraction() {
            XLog.v("PAD INTERACTION", "");
            return (View view) -> {
                MakePads.PadInfo mPadInfo = (MakePads.PadInfo) view.getTag();
                if (mPadInfo.getRow() == 0) {
                    switch (mPadInfo.getColum()) {
                        case 1: return onPressWatermarkTouch;
                        case 2: return onLedSwitchTouch;
                        case 3: return onAutoplaySwitchTouch;
                        case 4: return onAutoplayPrevTouch;
                        case 5: return onAutoplayPauseTouch;
                        case 6: return onAutoplayNextTouch;
                        case 7: return onLayoutSwitchTouch;
                        case 8: return onWatermarkTouch;
                    }
                } else if (mPadInfo.getType() == MakePads.PadInfo.PadInfoIdentifier.CHAIN) {
                    return onChainTouch;
                } else if (mPadInfo.getType() == MakePads.PadInfo.PadInfoIdentifier.PAD) {
                    return onPadTouch;
                }
                return null;
            };
        }

        protected void calculateSlide(View button_clicked, int x, int y){

        }

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
                        /* After set this chain to current chain */
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
                                    /*Disable control*/
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
                                    /*Enable control*/
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
    }

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
                    for (int i = 0; i < 1 /*MUDE ISSO*/; i++) {
                        // SkinTheme.pads.get(i).setImageBitmap(imgdata);
                    }
                    break;
                case PICK_BTN_IMG:
                    for (int i = 0; i < 1 /*MUDE ISSO*/; i++) {
                        // SkinTheme.btnlist.get(i).setImageBitmap(imgdata);
                    }
                    break;
                case PICK_PHANTOMC_IMG:
                    for (int i = 0; i < 1 /*MUDE ISSO*/; i++) {
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
                    for (int i = 0; i < 1 /*MUDE ISSO*/; i++) {
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

    @Override
    public void onBackPressed() {
        if (mPlayPadsOptions != null) {
            mPlayPadsOptions.show();
        }
    }
}
