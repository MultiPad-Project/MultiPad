package com.xayup.multipad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.*;
import android.icu.math.BigDecimal;
import android.media.*;
import android.net.*;
import android.os.*;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VerticalSeekBar;
import android.widget.ViewFlipper;
import android.view.View.OnClickListener;

import com.xayup.debug.XLog;
import com.xayup.multipad.configs.GlobalConfigs;
import com.xayup.multipad.layouts.loadscreen.LoadScreen;
import com.xayup.multipad.layouts.options.PlayPadsOptions;
import com.xayup.multipad.layouts.options.PlayPadsOptionsInterface;
import com.xayup.multipad.load.Project;

import com.xayup.multipad.load.thread.LoadProject;
import com.xayup.multipad.pads.PadInteraction;
import com.xayup.multipad.pads.Pad;
import com.xayup.multipad.pads.PadInterface;
import com.xayup.multipad.pads.PadPressCallInterface;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.project.keyled.Colors;
import com.xayup.multipad.project.keyled.KeyLED;

import java.io.*;
import java.util.*;

public class PlayPads extends Activity implements PlayPadsOptionsInterface {

    public Activity context = this;

    LoadScreen mLoadScreen;
    PadPress mPadPress;
    PadRelease mPadRelease;
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
            onPadTouch = onChainTouch();
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
                    });
        }
        public LoadProject.LoadingProject getLoadingProject() {
            return new LoadProject.LoadingProject() {
                @Override
                public void onStartLoadProject() {
                    /* GET FILES DIRs */
                    path = context.getIntent().getExtras().getString("project_path");
                    XLog.e("PATHHH", path + "");
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
                    project_loaded_problems.add(line + ":" + file_name + ": " + cause);
                }
                @Override
                public void onFinishLoadProject() {
                    /* Press Pads */
                    mPadPress = new PadPress();
                    mPadRelease = new PadRelease();
                    if (mKeyLED != null) {
                        mKeyLED.setToShowLed((x, y, real_color, lp_index) -> {
                            List<Pad.Pads> mPadsList = pad.getPadsWithIndex(lp_index);
                            XLog.v("Show led with lp index", String.valueOf(lp_index));
                            if(mPadsList != null) {
                                for(Pad.Pads mPads : mPadsList) {
                                    View led = mPads.getGridPads().getChildAt(MakePads.PadID.getGridIndexFromXY(mPads.getGridPads().getColumnCount(), x, y)).findViewById(R.id.led);
                                    context.runOnUiThread(() -> led.setBackgroundColor(real_color));
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
                            () -> {
                                mPlayPadsOptions =
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
                                        };
                            });
                }
            };
        }
        protected PadInteraction padInteraction() {
            XLog.v("PAD INTERACTION", "");
            return (View view) -> {
                MakePads.PadInfo mPadInfo = (MakePads.PadInfo) view.getTag();
                if (mPadInfo.row == 0) {
                    switch (mPadInfo.colum) {
                        case 1:
                            {
                                return onPressWatermarkTouch;
                            }
                        case 2:
                            {
                                return onLedSwitchTouch;
                            }
                        case 3:
                            {
                                return onAutoplaySwitchTouch;
                            }
                        case 4:
                            {
                                return onAutoplayPrevTouch;
                            }
                        case 5:
                            {
                                return onAutoplayPauseTouch;
                            }
                        case 6:
                            {
                                return onAutoplayNextTouch;
                            }
                        case 7:
                            {
                                return onLayoutSwitchTouch;
                            }
                        case 8:
                            {
                                return onWatermarkTouch;
                            }
                    }
                } else if (mPadInfo.row > 0
                        && mPadInfo.row < 9
                        && mPadInfo.colum > 0
                        && mPadInfo.colum < 9) {
                    return onPadTouch;
                } else if (mPadInfo.colum == 0 || mPadInfo.colum == 9 || mPadInfo.row == 9) {
                    return onChainTouch;
                }
                return null;
            };
        }

        @Override
        public OnTouchListener onPadTouch() {
            return (View v, MotionEvent event) -> {
                MakePads.PadInfo mPadInfo = (MakePads.PadInfo) v.getTag();
                XLog.v("Pad press", "X: " + mPadInfo.row + ",Y: " + mPadInfo.colum);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        {
                            mPadPress.call(pad.current_chain, mPadInfo.row, mPadInfo.colum);
                            return true;
                        }
                }

                return false;
            };
        }

        @Override
        public OnTouchListener onChainTouch() {
            return (View v, MotionEvent event) -> {
                MakePads.PadInfo mPadInfo = (MakePads.PadInfo) v.getTag();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        {
                            mPadPress.call(pad.current_chain, mPadInfo.row, mPadInfo.colum);
                            if (mPadPress.getLastSucessCount() == 0) {
                                int[] current_chain = pad.getCurrentChain();
                                v.getRootView()
                                        .findViewById(
                                                MakePads.PadID.getId(
                                                        current_chain[0], current_chain[1]))
                                        .findViewById(R.id.press);
                                pad.setCurrentChain(mPadInfo.row, mPadInfo.colum);
                            }
                            return true;
                        }
                }

                return false;
            };
        }

        @Override
        public OnTouchListener onPressWatermarkTouch() {
            return (View v, MotionEvent event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        {
                            pad.watermark_press = !pad.watermark_press;
                            if (pad.watermark_press) {
                                v.findViewById(R.id.press).setAlpha(1f);
                            } else {
                                v.findViewById(R.id.press).setAlpha(0f);
                            }
                            return true;
                        }
                }

                return false;
            };
        }

        @Override
        public OnTouchListener onLedSwitchTouch() {
            return (View v, MotionEvent event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        {
                            if (mKeyLED != null) {
                                if (mPadPress.calls.contains(mKeyLED)) {
                                    mPadPress.calls.remove(mKeyLED);
                                } else {
                                    mPadPress.calls.add(mKeyLED);
                                }
                                if (mKeySounds != null) {
                                } else {
                                    mPadPress.calls.add(mKeyLED);
                                }
                            }
                            return true;
                        }
                }
                return false;
            };
        }

        @Override
        public OnTouchListener onAutoplaySwitchTouch() {
            return (View v, MotionEvent event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        {
                            if (mAutoPlay != null) {
                                if (mAutoPlay.isRunning()) {
                                    mAutoPlay.stopAutoPlay();
                                } else {
                                    mAutoPlay.startAutoPlay(pad.getActivePads().getGridPads());
                                }
                            }
                            return true;
                        }
                }
                return false;
            };
        }

        @Override
        public OnTouchListener onAutoplayPrevTouch() {
            return (View v, MotionEvent event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        {
                            if (mAutoPlay != null && mAutoPlay.isRunning()) {
                                mAutoPlay.regressAutoPlay();
                            }
                            return true;
                        }
                }
                return false;
            };
        }

        @Override
        public OnTouchListener onAutoplayPauseTouch() {
            return (View v, MotionEvent event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        {
                            if (mAutoPlay != null) {
                                if (mAutoPlay.inPaused()) {
                                    mAutoPlay.pauseAutoPlay();
                                } else {
                                    mAutoPlay.resumeAutoPlay();
                                }
                            }
                            return true;
                        }
                }
                return false;
            };
        }

        @Override
        public OnTouchListener onAutoplayNextTouch() {
            return (View v, MotionEvent event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        {
                            if (mAutoPlay != null && mAutoPlay.isRunning()) {
                                mAutoPlay.advanceAutoPlay();
                            }
                            return true;
                        }
                }
                return false;
            };
        }

        @Override
        public OnTouchListener onLayoutSwitchTouch() {
            return (View v, MotionEvent event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        {
                            return true;
                        }
                }
                return false;
            };
        }

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

    public class PadRelease implements PadPressCallInterface {
        public List<PadPressCallInterface> calls;
        protected int sucess = 0;

        public PadRelease() {
            calls = new ArrayList<>();
        }

        public int getLastSucessCount() {
            return sucess;
        }

        @Override
        public boolean call(int chain, int x, int y) {
            sucess = 0;
            for (PadPressCallInterface mCall : calls) sucess += (mCall.call(chain, x, y)) ? 1 : 0;
            return true;
        }
    }

    public class PadPress implements PadPressCallInterface {
        public List<PadPressCallInterface> calls;
        protected int sucess = 0;

        public PadPress() {
            calls = new ArrayList<>();
        }

        public int getLastSucessCount() {
            return sucess;
        }

        @Override
        public boolean call(int chain, int x, int y) {
            sucess = 0;
            for (PadPressCallInterface mCall : calls) sucess += (mCall.call(chain, x, y)) ? 1 : 0;
            return true;
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
        if (mPlayPadsOptions != null) mPlayPadsOptions.show();
    }
}
