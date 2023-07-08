package com.xayup.multipad;

import android.app.*;
import android.content.*;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.midi.MidiDeviceInfo;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.xayup.debug.Debug;
import com.xayup.filesexplorer.FileExplorerDialog;
import com.xayup.multipad.configs.GlobalConfigs;
import com.xayup.multipad.layouts.PlayProject;
import com.xayup.multipad.layouts.ProjectsBase;

import com.xayup.multipad.layouts.loadscreen.LoadScreen;
import com.xayup.storage.FileManagerPermission;
import com.xayup.storage.ManagePermission;

import java.io.*;

public class MainActivity extends Activity {
    private final Activity context = this;

    protected final byte INTENT_PLAY_PADS = 0;

    protected LoadScreen mLoadScreen;
    protected File info;

    protected PendingIntent permissionIntent;
    protected FileManagerPermission mFMP;

    protected static String skinConfig;
    protected static boolean useUnipadFolderConfig;

    protected static int height;
    protected static int width;

    protected File rootFolder;
    protected String ACTION_USB_PERMISSION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ACTION_USB_PERMISSION = this.getPackageName()+".USB_PERMISSION";
        this.mFMP = new FileManagerPermission(context);
        mFMP.checkPermission(new ManagePermission() {
            @Override
            public void onStorageGranted() {
                makeActivity(true);
            }

            @Override
            public void onStorageDenied() {
                mFMP.showSimpleAlertDialog("Title", "Msg", "cancel", "ok");
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (mFMP.STORAGE_PERMISSION == requestCode) {
            makeActivity(mFMP.permissionGranted());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == mFMP.ANDROID_11_REQUEST_PERMISSION_AMF) {
            makeActivity(mFMP.permissionGranted());
        }
        else if (requestCode == INTENT_PLAY_PADS){
            mLoadScreen.OnEndAnimation(null);
            mLoadScreen.hide(0);
            mLoadScreen.remove();
            mLoadScreen = null;
        }
    }

    public void makeActivity(boolean granted) {
        XayUpFunctions.hideSystemBars(getWindow());
        GlobalConfigs.loadSharedPreferences(context);

        if (granted) {
            skinConfig = GlobalConfigs.app_configs.getString("skin", context.getPackageName());
            useUnipadFolderConfig = GlobalConfigs.app_configs.getBoolean("useUnipadFolder", false);
            GlobalConfigs.use_unipad_colors = false;

            if (useUnipadFolderConfig) rootFolder = new File(GlobalConfigs.DefaultConfigs.UNIPAD_PATH);
            else rootFolder = new File(GlobalConfigs.DefaultConfigs.PROJECTS_PATH);

            if (!rootFolder.exists()) rootFolder.mkdirs();

            registerReceiver(usbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
        }

        ViewGroup rootView = findViewById(R.id.main_activity);
        new ProjectsBase(
                this,
                rootFolder,
                rootView,
                getPackageName(),
                "main",
                new PlayProject() {
                    @Override
                    public void onPreLoadProject() {
                        mLoadScreen = new LoadScreen(context, rootView);
                    }

                    @Override
                    public void loadProject(String path) {
                        mLoadScreen.OnEndAnimation(
                                () -> {
                                    Intent intent = new Intent(context, PlayPads.class);;
                                    context.startActivityForResult(
                                            intent.putExtra("project_path", path), INTENT_PLAY_PADS);
                                    context.overridePendingTransition(0, 0);
                                });
                        mLoadScreen.show(500);
                    }
                });
        rootView.findViewById(R.id.main_floating_menu_button).setOnClickListener((v)->setMenuFunctions());
        rootView.post(this::hideSplash);
        rootView.post(()->{
            GlobalConfigs.display_height = rootView.getMeasuredHeight();
            GlobalConfigs.display_width = rootView.getMeasuredWidth();
        });
    }

    protected void hideSplash() {
        View splash_screen = findViewById(R.id.splash);
        splash_screen.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_splash));
        splash_screen.setVisibility(View.GONE);
    }

    private void setMenuFunctions() {
        final int MAIN = 0;
        final int SKINS = 1;
        final int USB_MIDI = 2;

        AlertDialog.Builder floating_menu = new AlertDialog.Builder(MainActivity.this);
        View menu = getLayoutInflater().inflate(R.layout.main_float_menu, null);
        // Funcoes principais
        TextView barTitle = menu.findViewById(R.id.main_floating_menu_bar_title);
        ViewFlipper flipper = menu.findViewById(R.id.main_floating_menu_background);
        Button prev = menu.findViewById(R.id.main_floating_menu_bar_button_prev);
        Button import_project = menu.findViewById(R.id.main_floating_menu_button_import_project);
        // View..
        View item_skins = menu.findViewById(R.id.main_floating_item_skins);
        View item_useUnipadFolder = menu.findViewById(R.id.main_floating_item_useunipadfolder);
        // View item_customHeight =
        // menu.findViewById(R.id.main_floating_item_customHeight);
        View item_sourceCode = menu.findViewById(R.id.main_floating_item_sourcecode);
        View item_myChannel = menu.findViewById(R.id.main_floating_item_mychannel);
        View item_manual = menu.findViewById(R.id.main_floating_item_manual);
        View item_crash = menu.findViewById(R.id.main_floating_item_crash);
        Button list_usb_midi = menu.findViewById(R.id.main_floating_menu_button_midi_devices);

        CheckBox unipad_folder = menu.findViewById(R.id.main_floating_menu_useunipadfolder_check);
        unipad_folder.setChecked(useUnipadFolderConfig);

        floating_menu.setView(menu);
        Button floating_button_exit =
                (Button) menu.findViewById(R.id.main_floating_menu_button_exit);
        AlertDialog show = floating_menu.create();
        XayUpFunctions.showDiagInFullscreen(show);

        // Botoes principais
        floating_button_exit.setOnClickListener((v)-> show.dismiss());

        list_usb_midi.setOnClickListener((v) -> {
                        list_usb_midi.setVisibility(View.GONE);
                        barTitle.setText(getString(R.string.usb_midi));
                        flipper.setInAnimation(MainActivity.this, R.anim.move_in_to_left);
                        flipper.setOutAnimation(MainActivity.this, R.anim.move_out_to_left);
                        flipper.setDisplayedChild(USB_MIDI);
                        ListView list_mids = ((ListView) flipper.getChildAt(USB_MIDI));
                        list_mids.setAdapter(new UsbMidiAdapter(getApplicationContext(), true));
                        list_mids.setOnItemClickListener(
                                new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(
                                            AdapterView<?> adapter, View v, int pos, long id) {
                                        MidiDeviceInfo usb_midi =
                                                (MidiDeviceInfo) adapter.getItemAtPosition(pos);
                                        if (MidiStaticVars.midiDevice == usb_midi) {
                                            Toast.makeText(
                                                            context,
                                                            context.getString(
                                                                    R.string.midi_aready_connected),
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                            return;
                                        }
                                        MidiStaticVars.device =
                                                (UsbDevice)
                                                        usb_midi.getProperties()
                                                                .getParcelable(
                                                                        MidiDeviceInfo
                                                                                .PROPERTY_USB_DEVICE);
                                        if (MidiStaticVars.device != null) {
                                            MidiStaticVars.midiDevice = usb_midi;
                                            permissionIntent = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                                                    ? PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE)
                                                    : PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
                                            MidiStaticVars.manager.requestPermission(MidiStaticVars.device, permissionIntent);
                                        } else new UsbDeviceActivity().openMidiDevice(context, usb_midi);
                                    }
                                });
                    });

        import_project.setOnClickListener((v) -> new FileExplorerDialog(context).getExplorerDialog());

        prev.setOnClickListener(
                (v) -> {
                    switch (flipper.getDisplayedChild()) {
                        case SKINS:
                            flipper.setInAnimation(getApplicationContext(), R.anim.move_in_to_right);
                            flipper.setOutAnimation(getApplicationContext(), R.anim.move_out_to_right);
                            flipper.setDisplayedChild(MAIN);
                            barTitle.setText(getString(R.string.main_floating_title));
                            break;
                        case USB_MIDI:
                            flipper.setInAnimation(getApplicationContext(), R.anim.move_in_to_right);
                            flipper.setOutAnimation(getApplicationContext(), R.anim.move_out_to_right);
                            flipper.setDisplayedChild(MAIN);
                            list_usb_midi.setVisibility(View.VISIBLE);
                            barTitle.setText(getString(R.string.usb_midi));
                            break;
                        default:
                        }
                    });

        // items clicked
        item_skins.setOnClickListener((v) -> {
            barTitle.setText(getString(R.string.skins));
            flipper.setInAnimation(MainActivity.this, R.anim.move_in_to_left);
            flipper.setOutAnimation(MainActivity.this, R.anim.move_out_to_left);
            flipper.setDisplayedChild(SKINS);
        });
        item_useUnipadFolder.setOnClickListener((v) -> {
            SharedPreferences app_configs =
                    getSharedPreferences("app_configs", MODE_PRIVATE);
            SharedPreferences.Editor editConfigs = app_configs.edit();
            if (app_configs.getBoolean("useUnipadFolder", false)) {
                unipad_folder.setChecked(false);
                editConfigs.putBoolean("useUnipadFolder", false);
            } else {
                unipad_folder.setChecked(true);
                editConfigs.putBoolean("useUnipadFolder", true);
            }
            editConfigs.apply();
            MainActivity.this.recreate();
        });

        item_sourceCode.setOnClickListener((v) -> {
            Intent source_code_page = new Intent(Intent.ACTION_VIEW);
            source_code_page.setData(Uri.parse("https://github.com/XayUp/MultiPad"));
            startActivity(source_code_page);
        });
        item_myChannel.setOnClickListener((v) -> {
            Intent my_channel_page = new Intent(Intent.ACTION_VIEW);
            my_channel_page.setData(
                    Uri.parse("https://youtube.com/channel/UCQUG1PVbnmIIYRDbC-qYTqA"));
            startActivity(my_channel_page);
        });
        item_manual.setOnClickListener((v) -> {
            AlertDialog.Builder manual =
                    new AlertDialog.Builder(
                            MainActivity.this, R.style.alertdialog_transparent);
            ImageView manualImg = new ImageView(MainActivity.this);
            manualImg.setImageDrawable(getDrawable(R.drawable.manual));
            manual.setView(manualImg);
            Dialog show_manual = manual.create();
            XayUpFunctions.showDiagInFullscreen(show_manual);
            manualImg.setOnClickListener((view) -> show_manual.dismiss());
        });

        // DEBUG //
        item_crash.setOnClickListener(v -> { throw new RuntimeException("Crash app (TEST)"); });

        show.getWindow().setLayout(GlobalConfigs.display_width/2, WindowManager.LayoutParams.MATCH_PARENT);
        show.getWindow().setGravity(Gravity.END);
        show.getWindow().setBackgroundDrawable(getDrawable(R.drawable.inset_floating_menu));
    }

    private final BroadcastReceiver usbReceiver =
            new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (ACTION_USB_PERMISSION.equals(action)) {
                        synchronized (this) {
                            if (intent.getBooleanExtra(
                                    UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                new UsbDeviceActivity()
                                        .openMidiDevice(context, MidiStaticVars.midiDevice);
                            } else {
                                MidiStaticVars.midiDevice = null;
                            }
                        }
                    }
                }
            };

    @Override
    public void onWindowFocusChanged(boolean bool) {
        super.onWindowFocusChanged(bool);
        if (bool) {
            XayUpFunctions.hideSystemBars(getWindow());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(usbReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(usbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
    }
}
