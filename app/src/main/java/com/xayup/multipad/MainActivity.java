package com.xayup.multipad;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.hardware.display.DisplayManager;
import android.hardware.usb.UsbDevice;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.ClipboardManager;
import android.text.Layout;
import android.util.*;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.*;
import java.io.*;
import java.net.URL;

public class MainActivity extends Activity {
	String[] pastadeprojetos;
	ListView listaprojetos;
	Button button_floating_menu;
	File info;

	public static String skinConfig;
	public static boolean useUnipadFolderConfig;

	public static int height;
	public static int width;
	public static int heightCustom;
	
	private Context context = this;

	File rootFolder = new File(Environment.getExternalStorageDirectory() + "/MultiPad/Projects");
	final String[] per = new String[] { "android.permission.MANAGER_EXTERNAL_STORAGE",
			"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE" };

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

			copyToClipboard.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					clipboard.setText(traceLog);
					Toast.makeText(getApplicationContext(), R.string.cop, Toast.LENGTH_SHORT).show();
				}
			});
			finishApp.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finishAffinity();
				}
			});
			restartApp.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					recreate();
					
				}
			});
		} else {
			Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			setContentView(R.layout.main);

			if (Build.VERSION.SDK_INT > 29) {
				//		android11per = 0;
			}
			//	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
			//	}

			checarPermissao();
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);//Salva Activity
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);//Restaura o Activity
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case ANDROID_11_REQUEST_PERMISSION_AMF:
			if (Environment.isExternalStorageManager()) {
				makeActivity(true);
			} else {
				makeActivity(false);
			}
			break;
		}
	}

	public boolean logRastreador() {
		if (this.getFileStreamPath("stack.trace").exists()) {
			traceLog = null;
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(this.openFileInput("stack.trace")));
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

	public void makeActivity(boolean granted) {

		XayUpFunctions.hideSystemBars(getWindow());
		SkinTheme.cachedSkinSet(this);
		SharedPreferences app_config = getSharedPreferences("app_configs", MODE_PRIVATE);
		skinConfig = app_config.getString("skin", "default");
		useUnipadFolderConfig = app_config.getBoolean("useUnipadFolder", false);

		Display.Mode display = getDisplay().getMode();
		//Display.Mode mode = Display.
		if (display.getPhysicalHeight() < display.getPhysicalWidth()) {
			height = display.getPhysicalHeight();
			width = display.getPhysicalWidth(); //getWindow().getDecorView().getWidth();
		} else {
			height = display.getPhysicalWidth(); //getWindow().getDecorView().getWidth();
			width = display.getPhysicalHeight();
		}
		heightCustom = height;

		if (useUnipadFolderConfig) {
			rootFolder = new File(Environment.getExternalStorageDirectory() + "/Unipad");
		}
		if (granted) {
			if (!rootFolder.exists()) {
				rootFolder.mkdirs();
			}
		}

		button_floating_menu = findViewById(R.id.main_floating_menu_button);
		button_floating_menu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				setMenuFunctions();

			}
		});
		Readers getInfo = new Readers();
		customArray arrayCustom = new customArray(MainActivity.this, getInfo.readInfo(this, rootFolder, granted));
		listaprojetos = findViewById(R.id.listViewProjects);
		listaprojetos.setAdapter(arrayCustom);

		listaprojetos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int Int, long Long) {
				TextView pathTextv = view.findViewById(R.id.pathText);
				View itemStt = view.findViewById(R.id.currentItemState);
				switch ((Integer) itemStt.getTag()) {
				case 0:
					Intent playPads = new Intent(getBaseContext(), playPads.class);
					playPads.putExtra("currentPath", pathTextv.getText().toString());
					playPads.putExtra("height", height);
					startActivity(playPads);
					break;
				case 2:
					checarPermissao();
					break;
				}
			}
		});
		View splash_screen = findViewById(R.id.splash);
		splash_screen.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_splash));
		splash_screen.setVisibility(View.GONE);

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
					& checkCallingPermission(per[1 + android11per])) != PackageManager.PERMISSION_GRANTED)
				requestPermissions(per, STORAGE_PERMISSION);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
		//Funcoes principais
		TextView barTitle = menu.findViewById(R.id.main_floating_menu_bar_title);
		ViewFlipper swit = menu.findViewById(R.id.main_floating_menu_background);
		Button prev = menu.findViewById(R.id.main_floating_menu_bar_button_prev);
		//View..
		View item_skins = menu.findViewById(R.id.main_floating_item_skins);
		View item_useUnipadFolder = menu.findViewById(R.id.main_floating_item_useunipadfolder);
		//View item_customHeight = menu.findViewById(R.id.main_floating_item_customHeight);
		View item_sourceCode = menu.findViewById(R.id.main_floating_item_sourcecode);
		View item_myChannel = menu.findViewById(R.id.main_floating_item_mychannel);
		View item_manual = menu.findViewById(R.id.main_floating_item_manual);
		Button list_usb_midi = menu.findViewById(R.id.main_floating_menu_button_midi_devices);

		CheckBox unipadfolder = menu.findViewById(R.id.main_floating_menu_useunipadfolder_check);
		unipadfolder.setChecked(useUnipadFolderConfig);

		floating_menu.setView(menu);
		Button floating_button_exit = (Button) menu.findViewById(R.id.main_floating_menu_button_exit);
		AlertDialog show = floating_menu.create();
		XayUpFunctions.showDiagInFullscreen(show);

		//Botoes principais
		floating_button_exit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				show.dismiss();
			}
		});

		list_usb_midi.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				list_usb_midi.setVisibility(View.GONE);
				barTitle.setText("" + getString(R.string.usb_midi));
				swit.setInAnimation(MainActivity.this, R.anim.move_in_to_left);
				swit.setOutAnimation(MainActivity.this, R.anim.move_out_to_left);
				swit.setDisplayedChild(USB_MIDI);
				ListView list_mids = ((ListView) swit.getChildAt(USB_MIDI));
				list_mids.setAdapter(new UsbMidiAdapter(getApplicationContext(), true));
				list_mids.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapter, View v, int pos, long id) {
						try {
							UsbDevice usb_midi = (UsbDevice) adapter.getItemAtPosition(pos);

						} catch (ClassCastException e) {
						 
							MidiDeviceInfo usb_midi = (MidiDeviceInfo) adapter.getItemAtPosition(pos);
							new UsbDeviceActivity().openMidiDevice(context, usb_midi);
						}
					}
				});
			}
		});

		prev.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				switch (swit.getDisplayedChild()) {
				case SKINS:
					swit.setInAnimation(getApplicationContext(), R.anim.move_in_to_right);
					swit.setOutAnimation(getApplicationContext(), R.anim.move_out_to_right);
					swit.setDisplayedChild(MAIN);
					barTitle.setText(getString(R.string.main_floating_title));
					break;
				case USB_MIDI:
					swit.setInAnimation(getApplicationContext(), R.anim.move_in_to_right);
					swit.setOutAnimation(getApplicationContext(), R.anim.move_out_to_right);
					swit.setDisplayedChild(MAIN);
					list_usb_midi.setVisibility(View.VISIBLE);
					barTitle.setText(getString(R.string.main_floating_title));
					break;
				default:
					break;
				}
			}
		});

		//itens clicked
		item_skins.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SkinTheme getThemes = new SkinTheme(MainActivity.this, (ListView) swit.getChildAt(SKINS), false);
				getThemes.getSkinsTheme();
				barTitle.setText(getString(R.string.skins));
				swit.setInAnimation(MainActivity.this, R.anim.move_in_to_left);
				swit.setOutAnimation(MainActivity.this, R.anim.move_out_to_left);
				swit.setDisplayedChild(SKINS);
			}
		});
		item_useUnipadFolder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SharedPreferences app_configs = getSharedPreferences("app_configs", MODE_PRIVATE);
				SharedPreferences.Editor editConfigs = app_configs.edit();
				if (app_configs.getBoolean("useUnipadFolder", false)) {
					unipadfolder.setChecked(false);
					editConfigs.putBoolean("useUnipadFolder", false);
				} else {
					unipadfolder.setChecked(true);
					editConfigs.putBoolean("useUnipadFolder", true);
				}
				editConfigs.commit();
				MainActivity.this.recreate();
			}
		});

		item_sourceCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent sorce_code_page = new Intent(Intent.ACTION_VIEW);
				sorce_code_page.setData(Uri.parse("https://github.com/XayUp/MultiPad"));
				startActivity(sorce_code_page);
			}
		});
		item_myChannel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent my_channel_page = new Intent(Intent.ACTION_VIEW);
				my_channel_page.setData(Uri.parse("https://youtube.com/channel/UCQUG1PVbnmIIYRDbC-qYTqA"));
				startActivity(my_channel_page);
			}
		});
		item_manual.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder manual = new AlertDialog.Builder(MainActivity.this,
						R.style.alertdialog_transparent);
				ImageView manualImg = new ImageView(MainActivity.this);
				manualImg.setImageDrawable(getDrawable(R.drawable.manual));
				manual.setView(manualImg);
				Dialog show = manual.create();
				XayUpFunctions.showDiagInFullscreen(show);
				manualImg.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						show.dismiss();
					}
				});
			}
		});

		show.getWindow().setLayout(450, WindowManager.LayoutParams.MATCH_PARENT);
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
	//^hide bars
	/*
	@Override
	protected void onResume(){
		super.onResume();
		hideSystemBars(getWindow());
		
	} 
	 */

}
