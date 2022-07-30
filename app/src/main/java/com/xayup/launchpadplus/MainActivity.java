package com.xayup.launchpadplus;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.*;
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


public class MainActivity extends Activity
{
	String[] pastadeprojetos;
	ListView listaprojetos;
	Button button_floating_menu;
	File info;
	
	public static String skinConfig;
	public static boolean useUnipadFolderConfig;

	DisplayMetrics display = new DisplayMetrics();
	int height;
	File rootFolder = new File(Environment.getExternalStorageDirectory() + "/LaunchpadPlus/Projects");
	final String[] per = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
	final int STORAGE_PERMISSION = 1000;
	String traceLog;
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		
		if(logRastreador()){
			setContentView(R.layout.crash);
			
			TextView textLog = findViewById(R.id.logText);
			textLog.setText(traceLog);
		
			Button copyToClipboard = findViewById(R.id.copyLog);
			Button finishApp = findViewById(R.id.exitcrash);
			Button restartApp = findViewById(R.id.restartApp);
			
			copyToClipboard.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v){
					ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
					clipboard.setText(traceLog);
					Toast.makeText(getApplicationContext(), R.string.cop, Toast.LENGTH_SHORT).show();
				}
			});
			finishApp.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v){
				finishAffinity();
				}
			});
			restartApp.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v){
					recreate();
				}
			});
		} else{
			Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
			setContentView(R.layout.main);
		//	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		//	}
			View splash_screen = findViewById(R.id.splash);
			splash_screen.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_splash));
			splash_screen.setVisibility(View.INVISIBLE);
				getWindowManager().getDefaultDisplay().getMetrics(display);
				if(display.heightPixels < display.widthPixels){
					height = display.heightPixels;
				} else{
					height = display.widthPixels;
				}
				checarPermissao();
			}
	}
	public boolean logRastreador(){
		if(this.getFileStreamPath("stack.trace").exists()){
			traceLog = null;
			try {
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(this.openFileInput("stack.trace")));
				String line = null;
				while ((line = reader.readLine()) != null)
				{
					traceLog += line + "\n";
				}

			} catch(FileNotFoundException fnfe) {
				// ...
			} catch(IOException ioe) {
				// ...
			}
			this.deleteFile("stack.trace");
			return true;
		}
		return false;
	}

	public void makeActivity(boolean granted)
	{
		SkinTheme.cachedSkinSet(this);
		SharedPreferences app_config = getSharedPreferences("app_configs", MODE_PRIVATE);
		skinConfig = app_config.getString("skin", "default");
		useUnipadFolderConfig = app_config.getBoolean("useUnipadFolder", false);
		
		if(useUnipadFolderConfig){
			rootFolder = new File(Environment.getExternalStorageDirectory() + "/Unipad");
		}
		if(granted){
			if(!rootFolder.exists()){
				rootFolder.mkdirs();
			}
		}
		
		button_floating_menu = findViewById(R.id.main_floating_menu_button);
		button_floating_menu.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				setMenuFunctions();
			}
		});
		Readers getInfo = new Readers();
		customArray arrayCustom = new customArray(MainActivity.this, getInfo.readInfo(this, rootFolder, granted));
	    listaprojetos = findViewById(R.id.listViewProjects);
		listaprojetos.setAdapter(arrayCustom);
		listaprojetos.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int Int, long Long)
				{
					TextView pathTextv = view.findViewById(R.id.pathText);
					View itemStt = view.findViewById(R.id.currentItemState);
					switch ((Integer)itemStt.getTag())
					{
						case 0:
							Intent playPads = new Intent(getBaseContext(), playPads.class);
							playPads.putExtra("currentPath", pathTextv.getText().toString());
							playPads.putExtra ("height", height);
							startActivity(playPads);
							break;
						case 2:
							checarPermissao();
							break;
					}
				}
			});
	}
	public void checarPermissao()
	{
			if ((checkCallingPermission(per[0]) & checkCallingPermission(per[1])) != PackageManager.PERMISSION_GRANTED)
		{
			requestPermissions(per, STORAGE_PERMISSION);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if (STORAGE_PERMISSION == requestCode)
		{
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				makeActivity(true);
			}
			else
			{
				makeActivity(false);
			}
		}
	}
	
	private void setMenuFunctions(){
		AlertDialog.Builder floating_menu = new AlertDialog.Builder(MainActivity.this, R.style.alertdialog_transparent);
		View menu = getLayoutInflater().inflate(R.layout.main_float_menu, null);
		
		TextView barTitle = menu.findViewById(R.id.main_floating_menu_bar_title);
		ViewSwitcher swit = menu.findViewById(R.id.main_floating_menu_background);
		ListView listSkins = new ListView(MainActivity.this);
		swit.addView(getLayoutInflater().inflate(R.layout.main_floating_menu_main, null, true));
		swit.addView(listSkins);
		//View..
		View item_skins = menu.findViewById(R.id.main_floating_item_skins);
		View item_useUnipadFolder = menu.findViewById(R.id.main_floating_item_useunipadfolder);
		View item_sourceCode = menu.findViewById(R.id.main_floating_item_sourcecode);
		View intem_myChannel = menu.findViewById(R.id.main_floating_item_mychannel);
		CheckBox unipadfolder = menu.findViewById(R.id.main_floating_menu_useunipadfolder_check);
		unipadfolder.setChecked(useUnipadFolderConfig);
				
		floating_menu.setView(menu);
		Button floating_button_exit = (Button) menu.findViewById(R.id.main_floating_menu_button_exit);
		Dialog show = floating_menu.create();
		show.show();
		floating_button_exit.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				show.dismiss();
			}
		});	
		
		//itens clicked
		item_skins.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				SkinTheme getThemes = new SkinTheme(MainActivity.this, listSkins, false);
				getThemes.getSkinsTheme();
				barTitle.setText(getString(R.string.skins));
				swit.setInAnimation(MainActivity.this, R.anim.move_in_to_left);
				swit.setOutAnimation(MainActivity.this, R.anim.move_out_to_left);
				swit.showNext();
				Button prev = menu.findViewById(R.id.main_floating_menu_bar_button_prev);
				prev.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View arg0) {
						swit.setInAnimation(MainActivity.this, R.anim.move_in_to_right);
						swit.setOutAnimation(MainActivity.this, R.anim.move_out_to_right);
						swit.showPrevious();
						barTitle.setText(R.string.main_floating_title);
						prev.setOnClickListener(null);
					}
				});
			}
		});		
		item_useUnipadFolder.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				SharedPreferences app_configs = getSharedPreferences("app_configs", MODE_PRIVATE);
				SharedPreferences.Editor editConfigs = app_configs.edit();
				if(app_configs.getBoolean("useUnipadFolder", false)){
					unipadfolder.setChecked(false);
					editConfigs.putBoolean("useUnipadFolder", false);
				} else{
					unipadfolder.setChecked(true);
					editConfigs.putBoolean("useUnipadFolder", true);
				}
				editConfigs.commit();
				MainActivity.this.recreate();
			}
		});
		item_sourceCode.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				Intent sorce_code_page = new Intent(Intent.ACTION_VIEW);
				sorce_code_page.setData(Uri.parse("https://github.com/XayUp/MultiPad"));
				startActivity(sorce_code_page);
			}
		});
		intem_myChannel.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				Intent my_channel_page = new Intent(Intent.ACTION_VIEW);
				my_channel_page.setData(Uri.parse("https://youtube.com/channel/UCQUG1PVbnmIIYRDbC-qYTqA"));
				startActivity(my_channel_page);
			}
		});
		
		show.getWindow().setLayout(450, WindowManager.LayoutParams.MATCH_PARENT);
		show.getWindow().setGravity(Gravity.RIGHT);
		show.getWindow().setBackgroundDrawable(getDrawable(R.drawable.inset_floating_menu));
		
	}
}
