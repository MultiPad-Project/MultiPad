package com.xayup.multipad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.icu.math.BigDecimal;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.net.*;
import android.os.*;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VerticalSeekBar;
import android.widget.ViewFlipper;

import com.xayup.midi.Controllers;
import com.xayup.midi.types.Devices;
import com.xayup.multipad.configs.GlobalConfigs;
import com.xayup.multipad.midi.MidiStaticVars;
import com.xayup.multipad.midi.controller.ControllerManager;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.pads.Render.skin.SkinManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayPads extends Activity {

    public Context context;

    public static int currentChainMC = 1;
    public static String getCurrentPath;

    public static Map<String, Object> project;
    public static Map<String, Integer> ledrpt;
    public static Map<String, List<List<String>>> ledFiles;
    public static Map<String, ThreadLed> threadMap;
    public static MakePads.Pads grids;

    public static int glowPadRadius, glowChainRadius;

    public static int project_chains;
    public static int otherChain;
    public static int oldPad;
    public static int currentChainId;
    public static int padWH;
    public static int
            display_height; // MainActivity.height;//Default MainActivity.height, ou ponha um valor
    // estatico, quanto menor que a altura de sua tela, menor os botoes

    private final int PICK_PHANTOM_IMG = 0;
    private final int PICK_PHANTOMC_IMG = 1;
    private final int PICK_CHAIN_IMG = 2;
    private final int PICK_LOGO_IMG = 3;
    private final int PICK_BACKGROUND_IMG = 4;
    private final int PICK_BTN_IMG = 5;
    private final int PICK_LOGO_BG_IMG = 6;

    private final int EXIT_PAGE_LISTSKIN = 0;
    private final int EXIT_PAGE_CONFIGS = 1;
    private final int EXIT_PAGE_UI_IMG_SELECTOR = 2;
    private final int EXIT_PAGE_LIST_COLOR_TABLE = 3;
    private final int EXIT_PAGE_PAD_GRIDS = 4;

    public static float watermark, padPressAlpha;
    public static int glowPadIntensity, glowChainIntensity;

    public static boolean autoPlayCheck;
    public static boolean spamSounds;
    public static boolean spamLeds;
    public static boolean slideMode;
    public static boolean oldColors;
    public static boolean changeChainGlows;
    public static boolean pressLed;
    public static boolean custom_color_table;
    public static boolean have_sounds;

    public static List<String> autoPlay;
    public static List<String> invalid_formats;

    public com.xayup.multipad.pads.Render.MakePads makepad;
    public com.xayup.multipad.pads.Render.MakePads.Pads mPads;
    public static AutoPlayFunc autoPlayThread;
    public static KeyLedColors ledFunc;
    public static SoundLoader mSoundLoader;


    private static Button stopRecAutoplay;

    private boolean hide_buttons_b;
    private boolean layer_decoration;
    private boolean ifglow_cfg_show;
    public static boolean stopAll;
    public static VerticalSeekBar progressAutoplay;
    public static boolean glowEf;
    public static boolean recAutoplay;
    public static boolean useSoundPool;

    public ImageView playBgimg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playpads);

        this.context = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        XayUpFunctions.hideSystemBars(getWindow());

        varInstance();
        SkinTheme.varInstance();

        project = (Map<String, Object>) getIntent().getSerializableExtra("project");
        if (project != null) {
            getCurrentPath = (String) project.get(ProjectListAdapter.KEY_PATH);
            project_chains = (int) project.get(ProjectListAdapter.KEY_CHAINS);

            new GetFilesTask(this) {
                @Override
                protected void onPostExecute() {
                    Handler handler = new Handler(context.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            end(context, time);
                            handler.removeCallbacks(this);
                        }
                    });
                    super.onPostExecute();
                }
            }.getFiles();
        } else {
            // Controller mode
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
                MidiManager manager = (MidiManager) getSystemService(Context.MIDI_SERVICE);
                for (MidiDeviceInfo midi : manager.getDevices()) {
                    Log.v("Scanning midi: Manufacturer", midi.getProperties().getString(MidiDeviceInfo.PROPERTY_MANUFACTURER));
                    if (
                        //midi.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME).equals(getString(android.os.)) &&
                        //midi.getProperties().getString(MidiDeviceInfo.PROPERTY_PRODUCT).equals(getString(android.R.string.usb_midi_peripheral_product_name)) &&
                            midi.getProperties().getString(MidiDeviceInfo.PROPERTY_MANUFACTURER).equalsIgnoreCase("Android")
                    ) {
                        manager.openDevice(midi, new MidiManager.OnDeviceOpenedListener() {
                            @Override
                            public void onDeviceOpened(MidiDevice midiDevice) {
                                MidiDeviceInfo.PortInfo input = null, output = null;
                                MidiDeviceInfo.PortInfo[] ports = midi.getPorts();
                                for (int i = 0; i < ports.length; i++) {
                                    for (int o = i; o < ports.length; o++) {
                                        if (ports[i].getName().equals(ports[o].getName())) {
                                            if (ports[i].getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT && ports[o].getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
                                                input = ports[i];
                                                output = ports[o];
                                                i = ports.length;
                                                o = ports.length;
                                            } else if (ports[i].getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT && ports[o].getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
                                                input = ports[o];
                                                output = ports[i];
                                                i = ports.length;
                                                o = ports.length;
                                            }
                                        }
                                    }
                                }
                                if (input != null || output != null) {
                                    MidiStaticVars.controllerManager = new ControllerManager(context, midiDevice, input, output) {
                                        @Override
                                        public void received(int row, int colum, int velocity) {
                                            if (row > -1 & colum > -1)
                                                runOnUiThread(() -> mPads.setLedColor(row, colum, VariaveisStaticas.colorInt(currentChainMC, velocity, custom_color_table, oldColors)));
                                        }
                                    };
                                    Log.v("Controller mode", "Midi opened");
                                    end(context, 0);
                                } else {
                                    finish();
                                    Toast.makeText(getApplicationContext(), getString(R.string.controller_mode_usb_error), Toast.LENGTH_LONG).show();
                                }
                            }
                        }, new Handler(context.getMainLooper()));
                        return;
                    }
                }
                finish();
                Toast.makeText(getApplicationContext(), getString(R.string.controller_mode_usb_error), Toast.LENGTH_LONG).show();
            } else {
                finish();
                Toast.makeText(this, getString(R.string.controller_mode_not_supported), Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void varInstance() {
        ledrpt = new HashMap<>();
        ledFiles = null;
        invalid_formats = new ArrayList<>();

        // Get app data
        SharedPreferences app_config = getSharedPreferences("app_configs", MODE_PRIVATE);

        // configs
        spamSounds = false;
        spamLeds = false;
        layer_decoration = false;
        hide_buttons_b = false;
        glowEf = app_config.getBoolean("glowEf", false);
        ifglow_cfg_show = false;
        slideMode = app_config.getBoolean("slideMode", false);
        recAutoplay = false;
        oldColors = app_config.getBoolean("oldColors", false);
        changeChainGlows = false;
        custom_color_table = app_config.getBoolean("custom_color_table", false);
        useSoundPool = app_config.getBoolean("use_soundpool", false);
        have_sounds = false;

        stopRecAutoplay = findViewById(R.id.stopAutoplayRec);

        otherChain = 19;
        display_height = MainActivity.heightCustom;
        oldPad = 0;
        watermark = 1.0f;
        padPressAlpha = 0.0f;
        currentChainId = 19;
        padWH = display_height / 10;
        try {
            glowChainIntensity = app_config.getInt("glowChainIntensity", 60);
            glowPadIntensity = app_config.getInt("glowPadIntensity", 90);
        } catch (RuntimeException ignored) {
            glowChainIntensity = 10 * Integer.parseInt(String.valueOf(app_config.getFloat("glowChainIntensity", 0.6f)).replace(".", ""));
            glowPadIntensity = 10 * Integer.parseInt(String.valueOf(app_config.getFloat("glowPadIntensity", 0.9f)).replace(".", ""));
            app_config.edit().putInt("glowChainIntensity", glowChainIntensity).putInt("glowPadIntensity", glowPadIntensity).apply();
        }
        glowPadRadius = app_config.getInt("glowPadRadius", 180);
        glowChainRadius = app_config.getInt("glowChainRadius", 160);

        autoPlayCheck = false;
        stopAll = false;
        pressLed = false;

        playBgimg = findViewById(R.id.playbgimg);
        playBgimg.setOnLongClickListener(view -> {
            mPads.switchLayout();
            return true;
        });
    }

    public void exitPads() {
        stopAll = true;
        if ((autoPlayThread != null) && autoPlayThread.isRunning()) {
            autoPlayCheck = false;
            autoPlayThread.exit();
        }
        if (have_sounds) mSoundLoader.release();
        if (ledFiles != null) {
            ledFiles.clear();
            ledFiles = null;
        }
    }

    @SuppressLint("SetTextI18n")
    public void end(Context context, long time_duration) {
        int min = (int) TimeUnit.MILLISECONDS.toMinutes(time_duration);
        int sec = (int) TimeUnit.MILLISECONDS.toSeconds(time_duration);
        AlertDialog.Builder alertInvalidFiles = new AlertDialog.Builder(context);
        if (!invalid_formats.isEmpty()) {
            View alertDiagView = LayoutInflater.from(context).inflate(R.layout.project_warnings, null);
            ((ListView) alertDiagView.findViewById(R.id.warning_list))
                    .setAdapter(
                            new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, invalid_formats));
            ((TextView) alertDiagView.findViewById(R.id.warning_time))
                    .setText("Time: " + min + "m " + sec + "s");
            alertInvalidFiles.setView(alertDiagView);
        } else {
            alertInvalidFiles.setMessage("Time: " + min + "m " + sec + "s");
        }

        if (ledFiles != null) {
            PlayPads.ledFunc = new KeyLedColors();
        }

        makepad = new com.xayup.multipad.pads.Render.MakePads(context);
        grids = (mPads = makepad.make((byte) 10, (byte) 10, (RelativeLayout pad) -> {
            View layer = pad.findViewById(MakePads.PadInfo.PadLayerType.BTN_);
            layer.setAlpha(0f);
            if (pad.getTag() instanceof MakePads.ChainInfo) layer.setBackgroundColor(Color.WHITE);
        }));

        new ConfigurePads(context).configure(mPads);
        updateSkin(GlobalConfigs.skin_package);
        //Render Glows
        mPads.getGlows().changeCfg(glowPadRadius, glowPadIntensity, false);
        mPads.getGlows().changeCfg(glowChainRadius, glowChainIntensity, true);
        if (glowEf) {
            mPads.getGlows().setOnGlows();
        }

        if (progressAutoplay != null) { // nao remova se nao quiser problemas%
            progressAutoplay.setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar arg0) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar arg0) {
                            XayUpFunctions.clearLeds(context, mPads);
                        }
                    });
        }
        stopRecAutoplay.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        recAutoplay = false;
                        Toast.makeText(context, context.getString(R.string.rec_ap_saving), Toast.LENGTH_SHORT)
                                .show();
                        AutoplayRecFunc.saveAutoplay();
                        Toast.makeText(context, context.getString(R.string.done), Toast.LENGTH_SHORT).show();
                        stopRecAutoplay.setVisibility(View.GONE);
                    }
                });
        autoPlayThread = new AutoPlayFunc((Activity) context) {
            @Override
            public void onStopAutoPlay() {
                /*
                 * Remove as visualizaces de controle do autoPlay
                 */
                stop();
                mPads.getPadView(0, 3).findViewById(MakePads.PadInfo.PadLayerType.BTN_).setAlpha(0f);
                ViewGroup prev = (ViewGroup) mPads.getPadView(0, 4);
                ViewGroup state = (ViewGroup) mPads.getPadView(0, 5);
                ViewGroup next = (ViewGroup) mPads.getPadView(0, 6);
                prev.removeView(prev.findViewById(AutoPlayFunc.ICON_ID_PREV));
                state.removeView(state.findViewById(AutoPlayFunc.ICON_ID_STATE));
                next.removeView(next.findViewById(AutoPlayFunc.ICON_ID_NEXT));
                PlayPads.autoPlayCheck = false;
                padWaiting = -1;
                PlayPads.progressAutoplay.setVisibility(View.GONE);
            }
        };

        //Initialize color pallet
        VariaveisStaticas.customColorMap = new int[PlayPads.project_chains][];

        //Update size variables;
        View main_layout = super.findViewById(R.id.layoutbackground);

        //Show dialog
        XayUpFunctions.showDiagInFullscreen(alertInvalidFiles.create());

        //Render Grid
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(MainActivity.height, MainActivity.height);
        param.addRule(RelativeLayout.CENTER_IN_PARENT);
        ((RelativeLayout) ((Activity) context).findViewById(R.id.layoutbackground)).addView(mPads.getRoot(), param);

        //USB device thread
        //if(MidiStaticVars.midiDeviceController != null) MidiStaticVars.midiDeviceController.dataReceiverThread.start();
        Log.v("Connect MIDI", String.valueOf(MidiStaticVars.midiDeviceController != null));
        invalid_formats.clear();
        invalid_formats = null;
    }

    /**
     * @param pad_info_type       < 0 for all pad
     * @param pad_info_layer_type layer type identifier
     * @param imgdata             Image type Bitmap
     */
    public void setPadLayerSkin(int pad_info_type, int pad_info_layer_type, Bitmap imgdata) {
        for (int i = mPads.getGrid().getChildCount() - 1; !(i < 0); i--) {
            View view = mPads.getGrid().getChildAt(i);
            if (view instanceof ViewGroup) {
                ViewGroup pad = (ViewGroup) view;
                if (pad_info_type == -1 || pad.getTag().equals(pad_info_layer_type)) {
                    for (int p = pad.getChildCount() - 1; !(p < 0); p--) {
                        View layer = pad.getChildAt(p);
                        if (layer.getTag().equals(pad_info_layer_type)) {
                            ((ImageView) layer).setImageBitmap(imgdata);
                            p = -1; // Exit this loop
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        View main_layout = super.findViewById(R.id.layoutbackground);
        main_layout.post(new Runnable() {
            @Override
            public void run() {
                MainActivity.height = main_layout.getMeasuredHeight();
                MainActivity.width = main_layout.getMeasuredWidth();
                if (mPads != null) {
                    mPads.getRoot().post(new Runnable() {
                        @Override
                        public void run() {
                            mPads.changeLayout(mPads.layout);
                            mPads.getRoot().removeCallbacks(this);
                        }
                    });
                }
                main_layout.removeCallbacks(this);
            }
        });
    }

    /**
     * Após usar .loadSkin(), será necessário usar isto para aplicar a skin.
     */
    public void updateSkin(String package_or_path) {
        Log.v("Try apply skin", package_or_path);
        try {
            if(!SkinManager.loadSkinResources(context, package_or_path, mPads, new SkinManager.OnSkinResourceLoaded() {
                @Override
                public void resourceLoadedBasic(View pad, MakePads.ChildInfo padInfo, SkinManager.SkinResources skinResources) {
                    //Log.v("updateSkin", padInfo.getRow() + " " + padInfo.getColum());
                    if(padInfo == null) return;
                    if (padInfo.getType() == MakePads.PadType.CHAIN) {
                        ImageView btn_ = ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.BTN_));
                        if (skinResources.CHAIN != null) {
                            ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.BTN)).setImageDrawable(skinResources.CHAIN);
                            btn_.setImageDrawable(skinResources.CHAIN_);
                            btn_.setBackground(null);
                            ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.CHAIN_LED)).setImageDrawable(null);
                            pad.findViewById(MakePads.PadInfo.PadLayerType.LED).setVisibility(View.INVISIBLE);
                        } else {
                            ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.BTN)).setImageDrawable(skinResources.BTN);
                            btn_.setImageDrawable(skinResources.BTN_);
                            ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.CHAIN_LED)).setImageDrawable(skinResources.CHAINLED);
                            pad.findViewById(MakePads.PadInfo.PadLayerType.LED).setVisibility(View.VISIBLE);
                        }
                    } else if (padInfo.getType() != MakePads.PadType.NONE) {
                        ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.BTN)).setImageDrawable(skinResources.BTN);
                        ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.BTN_)).setImageDrawable(skinResources.BTN_);
                        if (padInfo.getType() == MakePads.PadType.PAD) {
                            try {
                                ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.PHANTOM)).setImageDrawable(skinResources.PHANTOM);
                            } catch (NullPointerException n) {
                                ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.PHANTOM_)).setImageDrawable(skinResources.PHANTOM_);
                            }
                        } else if (padInfo.getType() == MakePads.PadType.PAD_LOGO) {
                            ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.LOGO)).setImageDrawable(skinResources.CUSTOMLOGO);
                        }
                    }
                    pad.requestLayout();
                    mPads.defaultRotationSetting();
                }

                @Override
                public void resourceLoadedAdvanced(View pad, MakePads.ChildInfo padInfo, Drawable resource, SkinManager.SkinResources skinResources) {
                    if(padInfo == null) return;
                    byte viewId = 0;
                    if(resource != null)  {pad.setRotation(0); pad.setScaleX(1); pad.setScaleY(1);}
                    if (padInfo.getType() == MakePads.PadType.CHAIN){
                        viewId = MakePads.PadInfo.PadLayerType.CHAIN_LED;
                        if(resource == null)
                            if((resource = skinResources.CHAINLED) == null)
                                resource = context.getDrawable(R.drawable.chainled);
                    }  else if (padInfo.getType() != MakePads.PadType.NONE)
                        if (padInfo.getType() == MakePads.PadType.PAD)
                            if(pad.findViewById(MakePads.PadInfo.PadLayerType.PHANTOM) != null) {
                                viewId = MakePads.PadInfo.PadLayerType.PHANTOM;
                                if(resource == null)
                                    if((resource = skinResources.PHANTOM) == null)
                                        resource = context.getDrawable(R.drawable.phantom);
                            } else {
                                viewId = MakePads.PadInfo.PadLayerType.PHANTOM_;
                                if(resource == null)
                                    if((resource = skinResources.PHANTOM_) == null)
                                        resource = context.getDrawable(R.drawable.phantom_);
                            }
                        else if (padInfo.getType() == MakePads.PadType.PAD_LOGO) {
                            viewId = MakePads.PadInfo.PadLayerType.LOGO;
                            if (resource == null)
                                if ((resource = skinResources.CUSTOMLOGO) == null)
                                    resource = context.getDrawable(R.drawable.customlogo);
                        }
                    else return;
                    ((ImageView) pad.findViewById(viewId)).setImageDrawable(resource);
                    ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.BTN)).setImageDrawable(skinResources.BTN);
                    ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.BTN_)).setImageDrawable(skinResources.BTN_);
                    pad.requestLayout();
                }

                @Override
                public void finishResourceLoaded(SkinManager.SkinResources skinResources){
                    if(skinResources != null) playBgimg.setImageDrawable(skinResources.PLAYBG);
                    mPads.getRoot().requestLayout();
                }
            })){Toast.makeText(context, context.getString(R.string.skin_failed_get_resources_skin), Toast.LENGTH_SHORT).show();}
        } catch (JSONException | IOException je){
            Toast.makeText(context, context.getString(R.string.skin_failed_get_resources_from_storage), Toast.LENGTH_SHORT).show();
            Log.e("JSON or IO exception", je.toString());
        } catch (PackageManager.NameNotFoundException nnfe){
            Toast.makeText(context, context.getString(R.string.skin_failed_get_resources_from_app), Toast.LENGTH_SHORT).show();
            Log.e("NameNotFoundException", nnfe.toString());

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
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
                    setPadLayerSkin(
                            MakePads.PadType.PAD,
                            MakePads.PadInfo.PadLayerType.PHANTOM, imgdata);
                    break;
                case PICK_PHANTOMC_IMG:
                    setPadLayerSkin(
                            MakePads.PadType.PAD,
                            MakePads.PadInfo.PadLayerType.PHANTOM_, imgdata);
                    break;
                case PICK_LOGO_IMG:
                    setPadLayerSkin(
                            MakePads.PadType.PAD_LOGO,
                            MakePads.PadInfo.PadLayerType.LOGO, imgdata);
                    break;
                case PICK_CHAIN_IMG:
                    setPadLayerSkin(
                            MakePads.PadType.CHAIN,
                            MakePads.PadInfo.PadLayerType.CHAIN_LED, imgdata);
                    break;
                case PICK_LOGO_BG_IMG:
                    setPadLayerSkin(
                            MakePads.PadType.PAD_LOGO,
                            MakePads.PadInfo.PadLayerType.BTN, imgdata);
                    break;
                case PICK_BTN_IMG:
                    setPadLayerSkin(-1, MakePads.PadInfo.PadLayerType.BTN, imgdata);
                    break;
                case PICK_BACKGROUND_IMG:
                    playBgimg.setImageBitmap(imgdata);
                    break;

            }
        }
    }

    public void switShowNext(ViewFlipper swit) {
        swit.setInAnimation(PlayPads.this, R.anim.move_in_to_left);
        swit.setOutAnimation(PlayPads.this, R.anim.move_out_to_left);
        swit.showNext();
    }

    public void switShowByIndex(ViewFlipper swit, boolean Previous, int page) {
        if (Previous) {
            swit.setInAnimation(PlayPads.this, R.anim.move_in_to_right);
            swit.setOutAnimation(PlayPads.this, R.anim.move_out_to_right);
        } else {
            swit.setInAnimation(PlayPads.this, R.anim.move_in_to_left);
            swit.setOutAnimation(PlayPads.this, R.anim.move_out_to_left);
        }
        swit.setDisplayedChild(page);
    }

    @Override
    public void onDestroy() {
        Log.v("onDestroy", "Called");
        super.onDestroy();
        if (MidiStaticVars.controllerManager != null) {
            MidiStaticVars.controllerManager.close();
            MidiStaticVars.controllerManager = null;
        }
    }

    @Override
    public void finish() {
        Log.v("Finish", "Called");
        super.finish();
        onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Layout
        View onExitDialog = getLayoutInflater().inflate(R.layout.alertexit_dialog, null);
        // Botões principais
        RelativeLayout onExitButton = onExitDialog.findViewById(R.id.alertExitButtonExit);
        Button exit_config = onExitDialog.findViewById(R.id.alert_configs_buttom);
        Button prev = onExitDialog.findViewById(R.id.alertExit_prev);
        Button color_table = onExitDialog.findViewById(R.id.alert_color_table_buttom);
        Button pad_grids = onExitDialog.findViewById(R.id.alert_exit_pad_grids);
        // Select Skin page
        ListView listSkins = onExitDialog.findViewById(R.id.skins_list_view);
        Switch skinSwitch = onExitDialog.findViewById(R.id.skins_list_switch);
        TextView barTitle = onExitDialog.findViewById(R.id.alertExitTitle);
        ViewFlipper flipper = onExitDialog.findViewById(R.id.exitMenuSwitcher);

        // Color table page
        ListView color_table_files = onExitDialog.findViewById(R.id.alertExit_list_color_table);
        Button default_color_table = onExitDialog.findViewById(R.id.alert_default_color_table_buttom);

        // Grid pads page
        ListView pad_grids_list = onExitDialog.findViewById(R.id.alert_exit_pad_grids_list);

        // Option page
        View sound_spam_item = onExitDialog.findViewById(R.id.alertExit_spam_sound_item);
        CheckBox sound_spam = onExitDialog.findViewById(R.id.soundSpam);

        View hide_buttoms = onExitDialog.findViewById(R.id.alertExit_hide_buttoms);
        CheckBox hide_check = onExitDialog.findViewById(R.id.hide_buttoms_check);

        View alertExit_layout_decoration_item =
                onExitDialog.findViewById(R.id.alertExit_layer_decoration_item);
        CheckBox decoration_show = onExitDialog.findViewById(R.id.layer_decoration_check);

        View ui_img_select = onExitDialog.findViewById(R.id.alertExit_ui_img_select_item);

        View glowShow = onExitDialog.findViewById(R.id.alertExit_glow_item);
        CheckBox glow_check = onExitDialog.findViewById(R.id.glow_check);

        View glow_cfg_show = onExitDialog.findViewById(R.id.alertExit_glow_cfg_item);
        CheckBox glow_cfg_check = onExitDialog.findViewById(R.id.glow_cfg_check);

        View led_spam_item = onExitDialog.findViewById(R.id.alertExit_spam_led_item);
        CheckBox led_spam_check = onExitDialog.findViewById(R.id.ledSpam);

        View slide_mode = onExitDialog.findViewById(R.id.alertExit_slide_mode);
        CheckBox slide_check = onExitDialog.findViewById(R.id.slide_mode_check);

        View rec_autoplay = onExitDialog.findViewById(R.id.alertExit_rec_autoplay);
        CheckBox rec_ap_check = onExitDialog.findViewById(R.id.rec_autoplay_check);

        View old_colors = onExitDialog.findViewById(R.id.alertExit_old_colors);
        CheckBox old_colors_check = onExitDialog.findViewById(R.id.old_colors_check);

        View item_customHeight = onExitDialog.findViewById(R.id.alertExit_item_layer_cfg);

        // Restore saved from app configs
        if (glowEf) glow_cfg_show.setAlpha(1.0f);
        glow_cfg_check.setChecked(ifglow_cfg_show);
        glow_check.setChecked(glowEf);
        sound_spam.setChecked(spamSounds);
        hide_check.setChecked(hide_buttons_b);
        decoration_show.setChecked(layer_decoration);
        rec_ap_check.setChecked(recAutoplay);
        old_colors_check.setChecked(oldColors);
        slide_check.setChecked(slideMode);

        if (layer_decoration) alertExit_layout_decoration_item.setAlpha(1.0f);
        item_customHeight.setAlpha(1.0f);
        SharedPreferences.Editor save_cfg = getSharedPreferences("app_configs", MODE_PRIVATE).edit();
        //List skin
        skinSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) listSkins.setAdapter(SkinManager.getAdapterSkinsFromApps(context));
                else listSkins.setAdapter(SkinManager.getAdapterSkinsFromStorage(context));
            }
        });
        listSkins.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String package_or_path;
                if(skinSwitch.isChecked()){
                    package_or_path = ((String[]) adapterView.getItemAtPosition(i))[SkinManager.SkinInfo.package_name]; }
                else { try {
                    package_or_path = ((JSONObject) adapterView.getItemAtPosition(i)).getString(SkinManager.JSON_SKIN_PATH);
                } catch (JSONException e) { Log.e("Load skin path", "Failed to get path"); return; }}

                updateSkin(package_or_path);
                GlobalConfigs.saveSkin(package_or_path);
            }
        });
        onExitDialog.findViewById(R.id.skins_list_button_default_skin).setOnClickListener((button)->{
            updateSkin(BuildConfig.APPLICATION_ID);
            GlobalConfigs.saveSkin(BuildConfig.APPLICATION_ID);
        });
        skinSwitch.setChecked(true);

        // Ir para a pagina de configuraçoes
        exit_config.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        barTitle.setText(getString(R.string.alert_exit_options));
                        exit_config.setVisibility(View.GONE);
                        switShowByIndex(flipper, false, EXIT_PAGE_CONFIGS);
                    }
                });
        // botao voltar
        prev.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        switch (flipper.getDisplayedChild()) {
                            case EXIT_PAGE_LISTSKIN:
                                break;
                            case EXIT_PAGE_CONFIGS:
                                switShowByIndex(flipper, true, EXIT_PAGE_LISTSKIN);
                                barTitle.setText(getString(R.string.alert_exit_title));
                                exit_config.setVisibility(View.VISIBLE);
                                break;
                            case EXIT_PAGE_UI_IMG_SELECTOR:
                                switShowByIndex(flipper, true, EXIT_PAGE_CONFIGS);
                                barTitle.setText(getString(R.string.alert_exit_options));
                                break;
                            case EXIT_PAGE_LIST_COLOR_TABLE:
                                switShowByIndex(flipper, true, EXIT_PAGE_LISTSKIN);
                                barTitle.setText(getString(R.string.alert_exit_title));
                                color_table.setVisibility(View.VISIBLE);
                                default_color_table.setVisibility(View.GONE);
                                exit_config.setVisibility(View.VISIBLE);
                                break;
                            case EXIT_PAGE_PAD_GRIDS:
                                switShowByIndex(flipper, true, EXIT_PAGE_LISTSKIN);
                                barTitle.setText(getString(R.string.alert_exit_title));
                                color_table.setVisibility(View.VISIBLE);
                                exit_config.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                });
        // old colors
        old_colors.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (oldColors) {
                            oldColors = false;
                            old_colors_check.setChecked(false);
                        } else {
                            oldColors = true;
                            old_colors_check.setChecked(true);
                        }
                    }
                });

        // Ver a lista de pad grids
        if (MidiStaticVars.controllerManager != null) {
            pad_grids.setOnClickListener(
                    new Button.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //exit_config.setVisibility(View.GONE);
                            //color_table.setVisibility(View.GONE);
                            AlertDialog.Builder devices = new AlertDialog.Builder(context);
                            ListView listView = new ListView(context);
                            listView.setAdapter(new Controllers(context));
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    MidiStaticVars.controllerManager.deviceCfg.setLaunchpadCfg((Devices.GridDeviceConfig) adapterView.getItemAtPosition(i));
                                    Toast.makeText(context, context.getString(R.string.device_selected).replace("%d", MidiStaticVars.controllerManager.deviceCfg.getLaunchpadCfg().name), Toast.LENGTH_SHORT).show();
                                }
                            });
                            devices.setView(listView);
                            devices.create().show();
                        }
                    });
        } else pad_grids.setVisibility(View.GONE);

        // Botao color_table
        color_table.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        barTitle.setText(getString(R.string.color_table_title));
                        color_table.setVisibility(View.GONE);
                        default_color_table.setVisibility(View.VISIBLE);
                        switShowByIndex(flipper, false, EXIT_PAGE_LIST_COLOR_TABLE);
                        color_table_files.setAdapter(Readers.listColorTable(getApplicationContext()));
                        color_table_files.setOnItemClickListener(
                                new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
                                        AlertDialog.Builder select_chain_dialog = new AlertDialog.Builder(context);
                                        View select_chain_view = LayoutInflater.from(context).inflate(R.layout.dialog_with_edittext, null);
                                        select_chain_dialog.setView(select_chain_view);
                                        TextView text = select_chain_view.findViewById(R.id.dwe_title);
                                        text.setText(context.getString(R.string.select_chain_color_table));
                                        text.setVisibility(View.VISIBLE);
                                        EditText edit = select_chain_view.findViewById(R.id.dwe_editText);
                                        edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                                        edit.setVisibility(View.VISIBLE);
                                        Button button = select_chain_view.findViewById(R.id.dwe_btn1);
                                        button.setVisibility(View.VISIBLE);
                                        button.setText(context.getString(android.R.string.ok));
                                        button.setOnClickListener((button_view) -> {
                                            int chain = Integer.parseInt(edit.getText().toString());
                                            if (chain >= 0 && chain <= project_chains) {
                                                Readers.getColorTableForCTFile((File) adapter.getItemAtPosition(pos), pos, !oldColors, (chain == 0) ? chain : chain - 1);
                                                if (chain == 0) {
                                                    VariaveisStaticas.defaultColorMap = VariaveisStaticas.customColorMap[chain];
                                                    Arrays.fill(VariaveisStaticas.customColorMap, null);
                                                }
                                                custom_color_table = true;
                                            } else {
                                                Toast.makeText(context, context.getString(R.string.invalid_chain_code), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        select_chain_dialog.create().show();
                                    }
                                });
                        default_color_table.setOnClickListener(
                                new Button.OnClickListener() {
                                    @Override
                                    public void onClick(View arg0) {
                                        custom_color_table = false;
                                        Toast.makeText(
                                                        getApplicationContext(),
                                                        getApplicationContext().getString(R.string.default_color_table),
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                });
                    }
                });

        // Option page functions
        // Pad/Chain/Background/Logo select
        ui_img_select.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        //	View ui_img_select_page =
                        // getLayoutInflater().inflate(R.layout.ui_img_select, null);
                        /*Phatom*/
                        onExitDialog
                                .findViewById(R.id.alertExit_phantom_select)
                                .setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                Intent get_image_from_gallery =
                                                        new Intent(
                                                                Intent.ACTION_PICK,
                                                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                                startActivityForResult(get_image_from_gallery, PICK_PHANTOM_IMG);
                                            }
                                        });
                        /*Phatom Center*/
                        onExitDialog
                                .findViewById(R.id.alertExit_phantomC_select)
                                .setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                Intent get_image_from_gallery =
                                                        new Intent(
                                                                Intent.ACTION_PICK,
                                                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                                startActivityForResult(get_image_from_gallery, PICK_PHANTOMC_IMG);
                                            }
                                        });
                        /*Logo*/
                        onExitDialog
                                .findViewById(R.id.alertExit_logo_select)
                                .setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                Intent get_image_from_gallery =
                                                        new Intent(
                                                                Intent.ACTION_PICK,
                                                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                                startActivityForResult(get_image_from_gallery, PICK_LOGO_IMG);
                                            }
                                        });
                        /*Logo Color*/
                        onExitDialog
                                .findViewById(R.id.alertExit_logo_bg_select)
                                .setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                Intent get_image_from_gallery =
                                                        new Intent(
                                                                Intent.ACTION_PICK,
                                                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                                startActivityForResult(get_image_from_gallery, PICK_LOGO_BG_IMG);
                                            }
                                        });

                        /*Background*/
                        onExitDialog
                                .findViewById(R.id.alertExit_background_select)
                                .setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                Intent get_image_from_gallery =
                                                        new Intent(
                                                                Intent.ACTION_PICK,
                                                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                                startActivityForResult(get_image_from_gallery, PICK_BACKGROUND_IMG);
                                            }
                                        });
                        /*Chain*/
                        onExitDialog
                                .findViewById(R.id.alertExit_chain_select)
                                .setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                Intent get_image_from_gallery =
                                                        new Intent(
                                                                Intent.ACTION_PICK,
                                                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                                startActivityForResult(get_image_from_gallery, PICK_CHAIN_IMG);
                                            }
                                        });
                        /*Buttom color*/
                        onExitDialog
                                .findViewById(R.id.alertExit_btn_select)
                                .setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                Intent get_image_from_gallery =
                                                        new Intent(
                                                                Intent.ACTION_PICK,
                                                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                                startActivityForResult(get_image_from_gallery, PICK_BTN_IMG);
                                            }
                                        });
                        switShowByIndex(flipper, false, EXIT_PAGE_UI_IMG_SELECTOR);
                        barTitle.setText(getString(R.string.ui_img_select_title));
                    }
                });

        // sound spam
        sound_spam_item.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (spamSounds) {
                            sound_spam.setChecked(spamSounds = false);
                        } else {
                            AlertDialog.Builder spam_alert = new AlertDialog.Builder(PlayPads.this);
                            spam_alert.setCancelable(false);
                            spam_alert.setMessage(R.string.sound_spam_dialog);
                            spam_alert.setPositiveButton(
                                    R.string.yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            sound_spam.setChecked(spamSounds = true);
                                        }
                                    });
                            spam_alert.setNegativeButton(R.string.no, null);
                            spam_alert.create().show();
                        }
                    }
                });
        // hide buttons
        hide_buttoms.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (hide_check.isChecked()) {
                            hide_check.setChecked(false);
                            XayUpFunctions.changePadsPhantomLayerVisibility(mPads.getGrid(), View.VISIBLE);
                            hide_buttons_b = false;
                        } else {
                            hide_check.setChecked(true);
                            hide_buttons_b = true;
                            XayUpFunctions.changePadsPhantomLayerVisibility(mPads.getGrid(), View.INVISIBLE);
                        }
                    }
                });
        // Slide mode
        slide_mode.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (slideMode) {
                            slide_check.setChecked(false);
                            slideMode = false;
                        } else {
                            slide_check.setChecked(true);
                            slideMode = true;
                        }
                        save_cfg.putBoolean("slideMode", slideMode);
                        save_cfg.apply();
                    }
                });
        // record to autoplay
        rec_autoplay.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (recAutoplay) {
                            recAutoplay = false;
                            rec_ap_check.setChecked(false);
                            stopRecAutoplay.setVisibility(View.GONE);
                        } else {
                            recAutoplay = true;
                            rec_ap_check.setChecked(true);
                            stopRecAutoplay.setVisibility(View.VISIBLE);
                            new AutoplayRecFunc(PlayPads.this);
                        }
                    }
                });
        // Show glow effect
        glowShow.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (mPads.getGlows().isEnabled()) {
                            glow_check.setChecked(false);
                            glow_cfg_show.setAlpha(0.6f);
                            mPads.getGlows().setOffGlows();
                            glowEf = false;
                        } else {
                            glow_check.setChecked(true);
                            glow_cfg_show.setAlpha(1.0f);
                            mPads.getGlows().setOnGlows();
                            glowEf = true;
                        }
                        save_cfg.putBoolean("glowEf", glowEf);
                        save_cfg.apply();
                    }
                });
        // Show glow configs (if Show Glows Effect is enabled)
        glow_cfg_show.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        final View glow_cfg_window = PlayPads.this.findViewById(R.id.glow_cfg_window);
                        if (mPads.getGlows().isEnabled())
                            if (ifglow_cfg_show) {
                                glow_cfg_window.setVisibility(View.GONE);
                                glow_cfg_check.setChecked(false);
                                ifglow_cfg_show = false;
                            } else {
                                // change window position
                                RelativeLayout.LayoutParams params =
                                        new RelativeLayout.LayoutParams(
                                                glow_cfg_window.getLayoutParams().width,
                                                glow_cfg_window.getLayoutParams().height);
                                params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                                glow_cfg_window.setLayoutParams(params);
                                // set data and functions

                                EditText radius = glow_cfg_window.findViewById(R.id.glow_config_edit_radius);
                                radius.setText(String.valueOf(changeChainGlows ? mPads.getGlows().chainRadius() : mPads.getGlows().padRadius()));
                                glow_cfg_window
                                        .findViewById(R.id.glow_cfg_r_minus)
                                        .setOnClickListener(
                                                new Button.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if (changeChainGlows) {
                                                            mPads.getGlows().changeCfg(mPads.getGlows().chainRadius() - 1, mPads.getGlows().chainIntensity(), changeChainGlows);
                                                            radius.setText(String.valueOf(glowChainRadius = mPads.getGlows().chainRadius()));
                                                        } else {
                                                            mPads.getGlows().changeCfg(mPads.getGlows().padRadius() - 1, mPads.getGlows().padIntensity(), changeChainGlows);
                                                            radius.setText(String.valueOf(glowPadRadius = mPads.getGlows().padRadius()));
                                                        }
                                                    }
                                                });
                                glow_cfg_window
                                        .findViewById(R.id.glow_cfg_r_plus)
                                        .setOnClickListener(
                                                new Button.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if (changeChainGlows) {
                                                            mPads.getGlows().changeCfg(mPads.getGlows().chainRadius() + 1, mPads.getGlows().chainIntensity(), changeChainGlows);
                                                            radius.setText(String.valueOf(glowChainRadius = mPads.getGlows().chainRadius()));
                                                        } else {
                                                            mPads.getGlows().changeCfg(mPads.getGlows().padRadius() + 1, mPads.getGlows().padIntensity(), changeChainGlows);
                                                            radius.setText(String.valueOf(glowPadRadius = mPads.getGlows().padRadius()));
                                                        }
                                                    }
                                                });

                                EditText intensity =
                                        glow_cfg_window.findViewById(R.id.glow_config_edit_intensidade);
                                intensity.setText(String.valueOf(glowPadIntensity));
                                glow_cfg_window
                                        .findViewById(R.id.glow_cfg_i_minus)
                                        .setOnClickListener(
                                                new Button.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if (changeChainGlows) {
                                                            mPads.getGlows().changeCfg(mPads.getGlows().chainRadius(), mPads.getGlows().chainIntensity() - 1, changeChainGlows);
                                                            intensity.setText(String.valueOf(glowChainIntensity = mPads.getGlows().chainIntensity()));
                                                        } else {
                                                            mPads.getGlows().changeCfg(mPads.getGlows().padRadius(), mPads.getGlows().padIntensity() - 1, changeChainGlows);
                                                            intensity.setText(String.valueOf(glowPadIntensity = mPads.getGlows().padIntensity()));
                                                        }
                                                    }
                                                });
                                glow_cfg_window
                                        .findViewById(R.id.glow_cfg_i_plus)
                                        .setOnClickListener(
                                                new Button.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if (changeChainGlows) {
                                                            mPads.getGlows().changeCfg(mPads.getGlows().chainRadius(), mPads.getGlows().chainIntensity() + 1, changeChainGlows);
                                                            intensity.setText(String.valueOf(glowChainIntensity = mPads.getGlows().chainIntensity()));
                                                        } else {
                                                            mPads.getGlows().changeCfg(mPads.getGlows().padRadius(), mPads.getGlows().padIntensity() + 1, changeChainGlows);
                                                            intensity.setText(String.valueOf(glowPadIntensity = mPads.getGlows().padIntensity()));
                                                        }
                                                    }
                                                });
                                // widgets
                                Switch padOrChain = glow_cfg_window.findViewById(R.id.glow_cfg_switch);
                                changeChainGlows = padOrChain.isChecked();
                                Button hide = glow_cfg_window.findViewById(R.id.glow_cfg_exit);
                                Button change = glow_cfg_window.findViewById(R.id.glow_cfg_changecfgs);
                                hide.setOnClickListener(
                                        new Button.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                glow_cfg_window.setVisibility(View.GONE);
                                                ifglow_cfg_show = false;
                                            }
                                        });
                                padOrChain.setChecked(false);
                                changeChainGlows = false;
                                padOrChain.setOnCheckedChangeListener(
                                        new Switch.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                                                changeChainGlows = arg1;
                                                if (arg1) {
                                                    glow_cfg_window.findViewById(R.id.textPad).setAlpha(0.5f);
                                                    glow_cfg_window.findViewById(R.id.textChain).setAlpha(1.0f);
                                                    intensity.setText(String.valueOf(glowChainIntensity));
                                                    radius.setText(String.valueOf(mPads.getGlows().chainRadius()));
                                                } else {
                                                    glow_cfg_window.findViewById(R.id.textChain).setAlpha(0.5f);
                                                    glow_cfg_window.findViewById(R.id.textPad).setAlpha(1.0f);
                                                    intensity.setText(String.valueOf(glowPadIntensity));
                                                    radius.setText(String.valueOf(mPads.getGlows().padRadius()));
                                                }
                                            }
                                        });
                                change.setOnClickListener(
                                        new Button.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                if (radius.getText().toString().matches("([1-9]|[1-9][0-9]{0,3})")) {
                                                    float radiusSize = Float.parseFloat(radius.getText().toString());
                                                    if (intensity.getText().toString().matches("\\d*")) {
                                                        int intensityAlpha = Integer.parseInt(intensity.getText().toString());
                                                        mPads.getGlows().changeCfg((int) radiusSize, intensityAlpha, changeChainGlows);
                                                        if (changeChainGlows) {
                                                            glowChainRadius = Integer.parseInt(radius.getText().toString());
                                                        } else {
                                                            glowPadRadius = Integer.parseInt(radius.getText().toString());
                                                        }
                                                        save_cfg.putInt("glowChainRadius", glowChainRadius);
                                                        save_cfg.putInt("glowPadRadius", glowPadRadius);
                                                        save_cfg.putInt("glowChainIntensity", glowChainIntensity);
                                                        save_cfg.putInt("glowPadIntensity", glowPadIntensity);
                                                        save_cfg.apply();
                                                    } else {
                                                        Toast.makeText(
                                                                        PlayPads.this,
                                                                        getString(R.string.invalid_intensity),
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                                    PlayPads.this,
                                                                    getString(R.string.invalid_radius),
                                                                    Toast.LENGTH_LONG)
                                                            .show();
                                                }
                                            }
                                        });
                                glow_cfg_window.setVisibility(View.VISIBLE);
                                glow_cfg_check.setChecked(true);
                                ifglow_cfg_show = true;
                            }
                    }
                });
        // Buscar a imagem overlay
        alertExit_layout_decoration_item.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (decoration_show.isChecked()) {
                            int PICK_IMG = 12;
                            Intent get_image_from_gallery =
                                    new Intent(
                                            Intent.ACTION_PICK,
                                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(get_image_from_gallery, PICK_IMG);
                        }
                    }
                });
        // Permitir a camada PNG de launchpad ou sei la..
        final ImageView layer = findViewById(R.id.launchpadOverride);
        final View window = findViewById(R.id.layer_cfg_window);
        decoration_show.setOnCheckedChangeListener(
                new CheckBox.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        if (!arg1) {
                            decoration_show.setChecked(false);
                            layer_decoration = false;
                            ((ImageView) findViewById(R.id.launchpadOverride)).setVisibility(View.GONE);
                            window.setVisibility(View.GONE);
                            alertExit_layout_decoration_item.setAlpha(0.6f);
                            item_customHeight.setAlpha(0.6f);
                            ((LinearLayout) findViewById(R.id.layer_decoration_scale_background))
                                    .setVisibility(View.GONE);
                        } else {
                            decoration_show.setChecked(true);
                            layer_decoration = true;
                            item_customHeight.setAlpha(1.0f);
                            layer.getLayoutParams().height = MainActivity.height;
                            layer.getLayoutParams().width = MainActivity.width;
                            layer.setVisibility(View.VISIBLE);
                            //	window.setVisibility(View.VISIBLE);
                            alertExit_layout_decoration_item.setAlpha(1.0f);
                        }
                    }
                });
        // Tamanho das pads
        item_customHeight.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (layer_decoration) {
                            // Busque as View e Obtenha as configuracoes atuais
                            EditText layer_size_w = findViewById(R.id.layer_cfg_edit_size_w);
                            EditText layer_size_h = findViewById(R.id.layer_cfg_edit_size_h);
                            EditText pads_size = findViewById(R.id.pads_cfg_edit_size);
                            Button layer_size_plus = findViewById(R.id.layer_cfg_s_plus);
                            Button layer_size_minus = findViewById(R.id.layer_cfg_s_minus);
                            Button pads_size_plus = findViewById(R.id.pads_cfg_s_plus);
                            Button pads_size_minus = findViewById(R.id.pads_cfg_s_minus);
                            Button close = findViewById(R.id.layer_cfg_exit);
                            Button change = findViewById(R.id.layer_cfg_changecfgs);

                            View window = findViewById(R.id.layer_cfg_window);

                            float layer_h = layer.getScaleX();
                            int pads_hw = mPads.getGrid().getLayoutParams().height;

                            layer_size_h.setText(String.valueOf(layer_h));
                            pads_size.setText(String.valueOf(pads_hw));

                            layer_size_w.setVisibility(View.GONE);

                            layer_size_plus.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View arg0) {
                                            layer_size_h.setText(resizeLayer(layer, 0.01));
                                        }
                                    });
                            layer_size_minus.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View arg0) {
                                            layer_size_h.setText(resizeLayer(layer, -0.01));
                                        }
                                    });

                            pads_size_plus.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View arg0) {
                                            int pads_hw = mPads.getGrid().getLayoutParams().height + 10;
                                            mPads.getGrid().getLayoutParams().height = pads_hw;
                                            mPads.getGrid().getLayoutParams().width = pads_hw;
                                            pads_size.setText(String.valueOf(pads_hw));
                                            mPads.getGrid().setVisibility(View.GONE);
                                            mPads.getGrid().setVisibility(View.VISIBLE);
                                            display_height = pads_hw;
                                        }
                                    });
                            pads_size_minus.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View arg0) {
                                            int pads_hw = mPads.getGrid().getLayoutParams().height - 10;
                                            mPads.getGrid().getLayoutParams().height = pads_hw;
                                            mPads.getGrid().getLayoutParams().width = pads_hw;
                                            pads_size.setText(String.valueOf(pads_hw));
                                            mPads.getGrid().setVisibility(View.GONE);
                                            mPads.getGrid().setVisibility(View.VISIBLE);
                                            display_height = pads_hw;
                                        }
                                    });
                            change.setOnClickListener(
                                    new View.OnClickListener() {

                                        @Override
                                        public void onClick(View arg0) {
                                            String lh = layer_size_h.getText().toString();
                                            String lw = layer_size_w.getText().toString();
                                            String phw = pads_size.getText().toString();
                                            if (!lw.isEmpty()) {
                                                if (!lh.isEmpty()) {
                                                    if (!phw.isEmpty()) {
                                                    } else {
                                                        lh = "0";
                                                        Toast.makeText(PlayPads.this, "Pads size error", Toast.LENGTH_SHORT)
                                                                .show();
                                                    }
                                                } else {
                                                    lh = "0";
                                                    Toast.makeText(PlayPads.this, "Layer height error", Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                            } else {
                                                lw = "0";
                                                Toast.makeText(PlayPads.this, "Layer width error", Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                            int layer_h = Integer.parseInt(lh);
                                            int layer_w = Integer.parseInt(lw);
                                            int pads_hw = Integer.parseInt(phw);
                                            layer.setScaleX(layer_h);
                                            layer.setScaleY(layer_w);
                                            mPads.getGrid().getLayoutParams().height = pads_hw;
                                            mPads.getGrid().getLayoutParams().width = pads_hw;
                                            pads_size.setText(String.valueOf(pads_hw));

                                            layer.setVisibility(View.GONE);
                                            layer.setVisibility(View.VISIBLE);
                                            mPads.getGrid().setVisibility(View.GONE);
                                            mPads.getGrid().setVisibility(View.VISIBLE);
                                            display_height = pads_hw;
                                        }
                                    });
                            close.setOnClickListener(
                                    new View.OnClickListener() {

                                        @Override
                                        public void onClick(View arg0) {
                                            window.setVisibility(View.GONE);
                                        }
                                    });
                            window.setVisibility(View.VISIBLE);
                        }
                    }
                });
        // Permitir spam de leds..
        led_spam_item.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (spamLeds) {
                            spamLeds = false;
                            led_spam_check.setChecked(false);
                        } else {
                            AlertDialog.Builder spam_alert = new AlertDialog.Builder(PlayPads.this);
                            spam_alert.setCancelable(false);
                            spam_alert.setMessage(R.string.led_spam_dialog);
                            spam_alert.setPositiveButton(
                                    R.string.yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            spamLeds = true;
                                            led_spam_check.setChecked(true);
                                        }
                                    });
                            spam_alert.setNegativeButton(R.string.no, null);
                            spam_alert.create().show();
                        }
                    }
                });

        // Finalmente criar e mostrar a Janela
        AlertDialog.Builder alertExit = new AlertDialog.Builder(this);
        alertExit.setView(onExitDialog);
        AlertDialog alertDialog = alertExit.create();
        XayUpFunctions.showDiagInFullscreen(alertDialog);
        alertDialog.getWindow().setLayout(MainActivity.height, WindowManager.LayoutParams.MATCH_PARENT);
        alertDialog.getWindow().setGravity(Gravity.RIGHT);

        // Voltar para a lista de Unipacks
        onExitButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        exitPads();
                        alertDialog.dismiss();
                        PlayPads.super.onBackPressed();
                    }
                });
    }

    private String resizeLayer(final View layer, Double increment) {
        float new_size = BigDecimal.valueOf(layer.getScaleY()).add(BigDecimal.valueOf(increment)).floatValue();
        layer.setScaleY(new_size);
        layer.setScaleX(new_size);
        layer.setVisibility(View.GONE);
        layer.setVisibility(View.VISIBLE);
        return String.valueOf(new_size);
    }
}
