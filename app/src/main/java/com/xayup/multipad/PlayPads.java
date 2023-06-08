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
import android.view.View;
import android.view.View.OnClickListener;
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
import com.xayup.multipad.pads.PadPressCallInterface;
import com.xayup.multipad.pads.Render.MakePads;
import java.io.*;
import java.util.*;

public class PlayPads extends Activity implements PlayPadsOptionsInterface {

    public Activity context = this;

    Color cor;
    LoadScreen mLoadScreen;
    PadPress mPadPress;

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

    class CreateUi extends Project {
        private Pad.Pads last_pads_grid;

        private RelativeLayout root_pads;

        protected boolean show_project_erros = false;
        protected boolean hide_load_screen = false;
        protected Pad pad;

        public CreateUi(Activity context) {
            this.root_pads = context.findViewById(R.id.contAllPads);
            loadProject(
                    context,
                    new LoadProject.LoadingProject() {

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
                            context.runOnUiThread(
                                    () ->
                                            mLoadScreen.updatedText(
                                                    context.getString(R.string.reading)
                                                            + ": "
                                                            + file_name));
                        }

                        @Override
                        public void onFileError(String file_name, int line, String cause) {
                            project_loaded_problems.add(line + ":" + file_name + ": " + cause);
                        }

                        @Override
                        public void onFinishLoadProject() {
                            /* Press Pads */
                            mPadPress = new PadPress();
                            if(mKeyLED != null) mPadPress.calls.add(mKeyLED);
                            if(mKeySounds != null) mPadPress.calls.add(mKeySounds);
                            /* Hide Load Screen */
                            hide_load_screen = true;
                            show_project_erros = (project_loaded_problems.size() > 0);
                            if (show_project_erros) {
                                mLoadScreen.showErrorsList(
                                        project_loaded_problems,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                mLoadScreen.hide(300);
                                                mLoadScreen
                                                        .getCurrentAnimation()
                                                        .setAnimationListener(
                                                                new Animation.AnimationListener() {

                                                                    @Override
                                                                    public void onAnimationStart(
                                                                            Animation arg0) {}

                                                                    @Override
                                                                    public void onAnimationEnd(
                                                                            Animation arg0) {
                                                                        mLoadScreen.remove();
                                                                        mLoadScreen = null;
                                                                    }

                                                                    @Override
                                                                    public void onAnimationRepeat(
                                                                            Animation arg0) {}
                                                                });
                                                view.setOnClickListener(null);
                                            }
                                        });
                            } else {
                                context.runOnUiThread(
                                        ()->mLoadScreen.hide(500));

                            }
                        }
                    });
            pad = new Pad(context, padInteraction());
            root_pads.post(
                    () -> {
                        int h = root_pads.getMeasuredHeight();
                        int w = root_pads.getMeasuredWidth();
                        last_pads_grid =
                                pad.newPads(GlobalConfigs.PlayPadsConfigs.skin_package, 10, 10);
                        ViewGroup virtual_launchpad = last_pads_grid.getRootPads();
                        GridLayout launchpad_grid = last_pads_grid.getGridPads();
                        root_pads.addView(virtual_launchpad, new RelativeLayout.LayoutParams(w, h));
                        ViewGroup.LayoutParams rLayout = launchpad_grid.getLayoutParams();
                        rLayout.height = h;
                        rLayout.width = h;

                        //if (!show_project_erros && hide_load_screen) mLoadScreen.hide(500);
                    });
        }

        protected PadInteraction padInteraction() {
            return new PadInteraction() {
                @Override
                public OnClickListener onPadClick(View view) {
                    MakePads.PadInfo mPadInfo = (MakePads.PadInfo) view.getTag();
                    if (mPadInfo.row == 0) {
                        switch (mPadInfo.colum) {
                            case 1:
                                {
                                    return new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            XLog.v("Top Chain", String.valueOf(mPadInfo.colum));
                                            pad.watermark_press = !pad.watermark_press;
                                        }
                                    };
                                }
                            case 2:
                                {
                                    return new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            XLog.v("Top Chain", String.valueOf(mPadInfo.colum));
                                            if(mKeyLED != null) {
                                                if (mPadPress.calls.contains(mKeyLED)) {
                                                    mPadPress.calls.remove(mKeyLED);
                                                    if (mKeySounds != null)
                                                        mKeySounds.stopSound(pad.current_chain, mPadInfo.row, mPadInfo.colum);
                                                } else {
                                                    mPadPress.calls.add(mKeyLED);
                                                }
                                            }
                                        }
                                    };
                                }
                            case 3:
                                {
                                    return new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            XLog.v("Top Chain", String.valueOf(mPadInfo.colum));
                                            if (mAutoPlay != null) {
                                                if(mAutoPlay.isRunning()){
                                                    mAutoPlay.stopAutoPlay();
                                                } else {
                                                    mAutoPlay.startAutoPlay();
                                                }
                                            }
                                        }
                                    };
                                }
                            case 4:
                                {
                                    return new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            XLog.v("Top Chain", String.valueOf(mPadInfo.colum));
                                            if (mAutoPlay != null && mAutoPlay.isRunning()){
                                                mAutoPlay.regressAutoPlay();
                                            }
                                        }
                                    };
                                }
                            case 5:
                                {
                                    return new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            XLog.v("Top Chain", String.valueOf(mPadInfo.colum));
                                            if (mAutoPlay != null){
                                                if (mAutoPlay.inPaused()) {
                                                    mAutoPlay.pauseAutoPlay();
                                                } else {
                                                    mAutoPlay.resumeAutoPlay();
                                                }
                                            }
                                        }
                                    };
                                }
                            case 6:
                                {
                                    return new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            XLog.v("Top Chain", String.valueOf(mPadInfo.colum));
                                            if (mAutoPlay != null && mAutoPlay.isRunning()){
                                                mAutoPlay.advanceAutoPlay();
                                            }
                                        }
                                    };
                                }
                            case 7:
                            {
                                return new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        XLog.v("Top Chain", String.valueOf(mPadInfo.colum));
                                    }
                                };
                            }
                            case 8:
                                {
                                    return new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            XLog.v("Top Chain", String.valueOf(mPadInfo.colum));
                                            pad.watermark = !pad.watermark;
                                        }
                                    };
                                }
                        }
                    } else if (mPadInfo.row > 0 && mPadInfo.row < 9 && mPadInfo.colum > 0 && mPadInfo.colum < 9){
                        return new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            XLog.v("Pad press", "X: " + mPadInfo.row + ",Y: " + mPadInfo.colum);
                                            mPadPress.call(pad.current_chain, mPadInfo.row, mPadInfo.colum);
                                        }
                                    };
                    }
                    return null;
                }
            };
        }
    }
    
    public class PadPress implements PadPressCallInterface {
        public List<PadPressCallInterface> calls;
        public PadPress() {
            calls = new ArrayList<>();
        }
        @Override
        public void call(int chain, int x, int y) {
            for (PadPressCallInterface mCall : calls) mCall.call(chain, x, y);
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
        new PlayPadsOptions(context) {
            @Override
            public void onExit() {
                context.finish();
            }
        };
    }
}
