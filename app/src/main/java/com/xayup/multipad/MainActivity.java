package com.xayup.multipad;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.hardware.usb.UsbDeviceConnection;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.xayup.filesexplorer.FileExplorerDialog;
import com.xayup.midi.manager.DevicesManager;
import com.xayup.midi.types.Devices;
import com.xayup.multipad.configs.GlobalConfigs;
import com.xayup.multipad.midi.MidiDeviceController;
import com.xayup.multipad.midi.MidiStaticVars;
import com.xayup.multipad.pads.Render.skin.SkinManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class MainActivity extends Activity {
    ListView listaprojetos;
    Button button_floating_menu;

    public PendingIntent permissionIntent;

    public static int height;
    public static int width;
    public static int heightCustom;

    private Context context;

    File rootFolder = new File(Environment.getExternalStorageDirectory() + "/MultiPad/Projects");
    final String[] per =
            new String[] {
                "android.permission.MANAGER_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE"
            };

    private final String ACTION_USB_PERMISSION = "com.xayup.multipad.USB_PERMISSION";
    final int STORAGE_PERMISSION = 1000;
    private final int ANDROID_11_REQUEST_PERMISSION_AMF = 1001;
    int android11per = 1;
    String traceLog;

    protected View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //	decorView = getWindow().getDecorView();
        if (logRastreador()) {
            setContentView(R.layout.crash);

            TextView textLog = findViewById(R.id.logText);
            textLog.setText(traceLog);

            Button copyToClipboard = findViewById(R.id.copyLog);
            Button finishApp = findViewById(R.id.exitcrash);
            Button restartApp = findViewById(R.id.restartApp);

            copyToClipboard.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ClipboardManager clipboard =
                                    (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboard.setText(traceLog);
                            Toast.makeText(
                                            getApplicationContext(),
                                            R.string.cop,
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
            finishApp.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finishAffinity();
                        }
                    });
            restartApp.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            recreate();
                        }
                    });
        } else {
            Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
            getWindow()
                    .setFlags(
                            WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.main);
            this.context = this;
            getWindow()
                    .setFlags(
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            checarPermissao();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState); // Salva Activity
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState); // Restaura o Activity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ANDROID_11_REQUEST_PERMISSION_AMF:
                makeActivity(
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ? Environment.isExternalStorageManager()
                                : super.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                );
                break;
        }
    }

    public boolean logRastreador() {
        if (this.getFileStreamPath("stack.trace").exists()) {
            traceLog = null;
            try {
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(this.openFileInput("stack.trace")));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    traceLog += line + "\n";
                }

            } catch (FileNotFoundException fnfe) {
                // ...
            } catch (IOException ioe) {
                // ...
            }
            this.deleteFile("stack.trace");
            return true;
        }
        return false;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(context != null) {
            View root = ((Activity) context).findViewById(R.id.ActivityLinearLayout);
            if (root != null) {
                GlobalConfigs.display_width = root.getMeasuredWidth();
                GlobalConfigs.display_height = root.getMeasuredHeight();
                height = GlobalConfigs.display_height;
                width = GlobalConfigs.display_width;
            }
        }
    }

    public void makeActivity(boolean granted) {
        XayUpFunctions.hideSystemBars(getWindow());
        SkinTheme.cachedSkinSet(context);
        GlobalConfigs.loadSharedPreferences(context);
        //registerReceiver(usbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
        View root = ((Activity) context).findViewById(R.id.ActivityLinearLayout);
        root.post(new Runnable() {
            @Override
            public void run() {
                GlobalConfigs.display_width = root.getMeasuredWidth();
                GlobalConfigs.display_height = root.getMeasuredHeight();
                height = GlobalConfigs.display_height;
                width = GlobalConfigs.display_width;
                heightCustom = height;

                if (GlobalConfigs.use_unipad_folder) {
                    rootFolder = new File(Environment.getExternalStorageDirectory() + "/Unipad");
                    GlobalConfigs.use_unipad_folder = true;
                }
                if (granted) {
                    if (!rootFolder.exists()) {
                        rootFolder.mkdirs();
                    }
                }

                button_floating_menu = findViewById(R.id.main_floating_menu_button);
                button_floating_menu.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {

                                setMenuFunctions();
                            }
                        });
                Readers getInfo = new Readers();
                ProjectListAdapter arrayCustom =
                        new ProjectListAdapter(MainActivity.this, getInfo.readInfo(context, rootFolder, granted));
                listaprojetos = findViewById(R.id.listViewProjects);
                listaprojetos.setAdapter(arrayCustom);

                listaprojetos.setOnItemClickListener(
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(
                                    AdapterView<?> adapterView, View view, int pos, long Long) {
                                View itemStt = view.findViewById(R.id.currentItemState);
                                switch ((Integer) itemStt.getTag()) {
                                    case 0:
                                        Intent playPads = new Intent(getBaseContext(), PlayPads.class);
                                        if(pos > 0) playPads.putExtra("project", (Serializable) adapterView.getItemAtPosition(pos));
                                        startActivity(playPads);
                                        break;
                                    case 2:
                                        checarPermissao();
                                        break;
                                }
                            }
                        });
                View splash_screen = findViewById(R.id.splash);
                splash_screen.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out_splash));
                splash_screen.setVisibility(View.GONE);
                MidiStaticVars.devicesManager = new DevicesManager(getApplicationContext());
                root.removeCallbacks(this);
            }
        });
    }

    public void checarPermissao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, ANDROID_11_REQUEST_PERMISSION_AMF);
            } else {
                makeActivity(true);
            }
        } else {
            if ((checkCallingPermission(per[0 + android11per])
                            & checkCallingPermission(per[1 + android11per]))
                    != PackageManager.PERMISSION_GRANTED)
                requestPermissions(per, STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        if (STORAGE_PERMISSION == requestCode) {
            if (grantResults[0 + android11per] == PackageManager.PERMISSION_GRANTED) {
                makeActivity(true);
            } else {
                makeActivity(false);
            }
        }
    }

    private void setMenuFunctions() {
        final int MAIN = 0;
        final int SKINS = 1;
        final int USB_MIDI = 2;

        AlertDialog.Builder floating_menu = new AlertDialog.Builder(MainActivity.this);
        View menu = getLayoutInflater().inflate(R.layout.main_float_menu, null);
        // Funcoes principais
        TextView barTitle = menu.findViewById(R.id.main_floating_menu_bar_title);
        ViewFlipper swit = menu.findViewById(R.id.main_floating_menu_background);
        Button prev = menu.findViewById(R.id.main_floating_menu_bar_button_prev);
        Button import_project = menu.findViewById(R.id.main_floating_menu_button_import_project);
        // View..
        View item_skins = menu.findViewById(R.id.main_floating_item_skins);
        View item_useUnipadFolder = menu.findViewById(R.id.main_floating_item_useunipadfolder);
        // View item_customHeight = menu.findViewById(R.id.main_floating_item_customHeight);
        View item_sourceCode = menu.findViewById(R.id.main_floating_item_sourcecode);
        View item_myChannel = menu.findViewById(R.id.main_floating_item_mychannel);
        View item_manual = menu.findViewById(R.id.main_floating_item_manual);
        Button list_usb_midi = menu.findViewById(R.id.main_floating_menu_button_midi_devices);

        CheckBox unipadfolder = menu.findViewById(R.id.main_floating_menu_useunipadfolder_check);
        unipadfolder.setChecked(GlobalConfigs.use_unipad_folder);

        floating_menu.setView(menu);
        Button floating_button_exit =
                (Button) menu.findViewById(R.id.main_floating_menu_button_exit);
        AlertDialog show = floating_menu.create();
        XayUpFunctions.showDiagInFullscreen(show);

        // Botoes principais
        floating_button_exit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        show.dismiss();
                    }
                });

        if(MidiStaticVars.devicesManager == null){
            list_usb_midi.setVisibility(View.GONE);
        } else {
            list_usb_midi.setOnClickListener((view) -> {
                list_usb_midi.setVisibility(View.GONE);
                barTitle.setText(getString(R.string.usb_midi));
                swit.setInAnimation(MainActivity.this, R.anim.move_in_to_left);
                swit.setOutAnimation(MainActivity.this, R.anim.move_out_to_left);
                swit.setDisplayedChild(USB_MIDI);
                ListView list_midis = ((ListView) swit.getChildAt(USB_MIDI));
                list_midis.setAdapter(MidiStaticVars.devicesManager.getListAdapter());
                list_midis.setOnItemClickListener((adapterView, iview, pos, id) -> {
                    Devices.MidiDevice mDevice = (Devices.MidiDevice) adapterView.getItemAtPosition(pos);
                    if(MidiStaticVars.midiDeviceController != null){
                        if (MidiStaticVars.midiDeviceController.midiDevice.name.equals(mDevice.usbDevice.getDeviceName())){
                            Toast.makeText(getApplicationContext(), mDevice.usbDevice.getDeviceName() + ": " + getString(R.string.midi_aready_connected), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        MidiStaticVars.midiDeviceController.dataReceiverThread.stop();
                        MidiStaticVars.midiDeviceController = null;
                    }
                    MidiStaticVars.devicesManager.callWhenMidiDeviceOpened(
                        new DevicesManager.OpenedDeviceCallback() {
                            @Override
                            public void onDeviceOpened(UsbDeviceConnection device) {
                                Log.i("Device opened", "callback");
                                (MidiStaticVars.midiDeviceController = new MidiDeviceController(getApplicationContext(), device, mDevice))
                                        .dataReceiverThread.start();
                                MidiStaticVars.devicesManager.removeCallWhenMidiDeviceOpened(this);
                                Toast.makeText(context, context.getString(R.string.device_selected).replace("%d", mDevice.name), Toast.LENGTH_SHORT).show();
                            }
                        }
                    );
                    Log.e("Try open USB", "Opened " + MidiStaticVars.devicesManager.openDevice(mDevice.usbDevice));
                });
            });
        }

        import_project.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new FileExplorerDialog(context).getExplorerDialog();
                    }
                });

        prev.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        switch (swit.getDisplayedChild()) {
                            case SKINS:
                                swit.setInAnimation(
                                        getApplicationContext(), R.anim.move_in_to_right);
                                swit.setOutAnimation(
                                        getApplicationContext(), R.anim.move_out_to_right);
                                swit.setDisplayedChild(MAIN);
                                barTitle.setText(getString(R.string.main_floating_title));
                                break;
                            case USB_MIDI:
                                swit.setInAnimation(
                                        getApplicationContext(), R.anim.move_in_to_right);
                                swit.setOutAnimation(
                                        getApplicationContext(), R.anim.move_out_to_right);
                                swit.setDisplayedChild(MAIN);
                                list_usb_midi.setVisibility(View.VISIBLE);
                                barTitle.setText(getString(R.string.main_floating_title));
                                break;
                            default:
                                break;
                        }
                    }
                });

        // itens clicked
        item_skins.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        View layout = swit.getChildAt(SKINS);
                        ListView list_skins = (ListView) layout.findViewById(R.id.skins_list_view);
                        Switch wSwitch = (Switch) layout.findViewById(R.id.skins_list_switch);
                        wSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if(b) list_skins.setAdapter(SkinManager.getAdapterSkinsFromApps(context));
                                else list_skins.setAdapter(SkinManager.getAdapterSkinsFromStorage(context));
                            }
                        });
                        list_skins.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                if(wSwitch.isChecked()) GlobalConfigs.saveSkin((String) ((String[]) adapterView.getItemAtPosition(i))[SkinManager.SkinInfo.package_name]);
                                else { try { GlobalConfigs.saveSkin((String) ((JSONObject) adapterView.getItemAtPosition(i)).getString(SkinManager.JSON_SKIN_PATH));
                                    } catch (JSONException e) { Log.e("Load skin path", "Failed to get path"); }}
                            }
                        });
                        layout.findViewById(R.id.skins_list_button_default_skin).setOnClickListener((button)->{
                            GlobalConfigs.saveSkin(BuildConfig.APPLICATION_ID);
                            Toast.makeText(context, context.getString(R.string.skin_set_default), Toast.LENGTH_SHORT).show();
                        });
                        wSwitch.setChecked(true);
                        barTitle.setText(getString(R.string.skins));
                        swit.setInAnimation(MainActivity.this, R.anim.move_in_to_left);
                        swit.setOutAnimation(MainActivity.this, R.anim.move_out_to_left);
                        swit.setDisplayedChild(SKINS);
                    }
                });
        item_useUnipadFolder.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (GlobalConfigs.app_configs.getBoolean("useUnipadFolder", false)) {
                            unipadfolder.setChecked(false);
                            GlobalConfigs.app_configs.edit().putBoolean("useUnipadFolder", false).apply();
                        } else {
                            unipadfolder.setChecked(true);
                            GlobalConfigs.app_configs.edit().putBoolean("useUnipadFolder", true).apply();
                        }
                        //editConfigs.commit();
                        MainActivity.this.recreate();
                    }
                });

        item_sourceCode.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent sorce_code_page = new Intent(Intent.ACTION_VIEW);
                        sorce_code_page.setData(Uri.parse("https://github.com/XayUp/MultiPad"));
                        startActivity(sorce_code_page);
                    }
                });
        item_myChannel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent my_channel_page = new Intent(Intent.ACTION_VIEW);
                        my_channel_page.setData(
                                Uri.parse("https://youtube.com/channel/UCQUG1PVbnmIIYRDbC-qYTqA"));
                        startActivity(my_channel_page);
                    }
                });
        item_manual.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        AlertDialog.Builder manual =
                                new AlertDialog.Builder(
                                        MainActivity.this, R.style.alertdialog_transparent);
                        ImageView manualImg = new ImageView(MainActivity.this);
                        manualImg.setImageDrawable(getDrawable(R.drawable.manual));
                        manual.setView(manualImg);
                        Dialog show = manual.create();
                        XayUpFunctions.showDiagInFullscreen(show);
                        manualImg.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View arg0) {
                                        show.dismiss();
                                    }
                                });
                    }
                });

        show.getWindow().setLayout(height, WindowManager.LayoutParams.MATCH_PARENT);
        show.getWindow().setGravity(Gravity.RIGHT);
        show.getWindow().setBackgroundDrawable(getDrawable(R.drawable.inset_floating_menu));
    }

    @Override
    public void onWindowFocusChanged(boolean bool) {
        super.onWindowFocusChanged(bool);
        if (bool) {
            XayUpFunctions.hideSystemBars(getWindow());
        }
    }
}
