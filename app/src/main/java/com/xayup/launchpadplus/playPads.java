package com.xayup.launchpadplus;

import android.app.*;
import android.content.AsyncTaskLoader;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.media.*;
import android.os.*;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.xayup.alertdialog.skinthmeAdapter;
import android.widget.ListView;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

import android.net.*;
import android.view.View;
import android.widget.TextView;

public class playPads extends Activity {
	
	public static String chainSl = "1";
	public static String getCurrentPath;
	
	Color cor;
	
	Readers filter = new Readers();
	
	public static Map<String, Map<String, List<Uri>>> keySound;
	public static MediaPlayer startSound;
	public static Map<String, MediaPlayer> padPlayer;
	public static Map<String, Integer> soundrpt;
	public static Map<String, Integer> ledrpt;
	public static Map<String, File> fileProj;
	public static Map<String, Map<Integer, List<String>>> ledFiles;
	
	public static int otherChain;
	public static int oldPad;
	
	public static boolean pressLed;
	public static boolean mk2;
	public static boolean autoPlayCheck;
	
	public static List<String> autoPlay;
	public static List<String> invalid_formats;
	
	public static makePads makepads;
	
	public static Thread ledOn;
	boolean lodedSkin = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playpads);
	//	ProgressBar progress = findViewById(R.id.loading_project_progress);
//		progress.setVisibility(View.VISIBLE);
		varInstance();
		SkinTheme.varInstance(true);
		 /*
		setDrawables(getDrawable(R.drawable.phantom_), getDrawable(R.drawable.phantom),
				getDrawable(R.drawable.chainled), getDrawable(R.drawable.btn), getDrawable(R.drawable.btn_),
				getDrawable(R.drawable.playbg), getDrawable(R.drawable.customlogo)); */
		getCurrentPath = getIntent().getExtras().getString("currentPath");
		
		//getFiles();
		new GetFilesTask(this).execute();
			
		
	//	progress.setVisibility(View.GONE);
	}

	public void varInstance() {
		
		padPlayer = new HashMap<String, MediaPlayer>();
		soundrpt = new HashMap<String, Integer>();
		ledrpt = new HashMap<String, Integer>();
		fileProj = new HashMap<String, File>();
		ledFiles = new HashMap<String, Map<Integer, List<String>>>();
		
		invalid_formats = new ArrayList<String>();
		autoPlay = new ArrayList<String>();
		
		//logo = new ImageView();
		
		otherChain = 19;
		oldPad = 0;
		
		autoPlayCheck = false;
		mk2 = false;
		
		startSound = new MediaPlayer();
		
		SkinTheme.playBgimg = findViewById(R.id.playbgimg);
	}
	
	private boolean checkLine(String line, String fileName) {
		line = line.replace(" ", "");
		switch (line.substring(0, 1)) {
		case "o":
			boolean ye;
			if (line.contains("mc")) {
				ye = line.matches("[on]{1,2}mc[0-3]?[0-9]a\\d{1,3}");

			} else {
				ye = line.matches("[on]{1,2}[1-8]{2}a\\d{1,3}");
			}
			if (ye) {
				if (Integer.parseInt(line.substring(line.indexOf("a") + 1)) > 127) {
					invalid_formats.add(getString(R.string.invalid_led_color) + " "
							+ line.substring(line.indexOf("a") + 1) + ", File: " + fileName);
				}
				return true;
			} else {
				return false;
			}
		case "f":
			return line.matches("[off]{1,3}[1-8]{2}");
		case "d":
			return line.matches("\\w\\d+");
		default:
			return false;
		}
	}

	@Override
	public void onBackPressed() {
		View onExitDialog = getLayoutInflater().inflate(R.layout.alertexit_dialog, null);
		RelativeLayout onExitButton = onExitDialog.findViewById(R.id.alertExitButtonExit);
		ListView listSkins = onExitDialog.findViewById(R.id.alertExitListSkins);
		SkinTheme getSkinList = new SkinTheme(playPads.this, listSkins, true);
		getSkinList.getSkinsTheme();
		onExitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				exitPads();
				playPads.super.onBackPressed();
			}
		});
		AlertDialog.Builder alertExit = new AlertDialog.Builder(this);
		alertExit.setView(onExitDialog);
		AlertDialog alertDialog = alertExit.create();
		alertDialog.show();
		alertDialog.getWindow().setLayout(450, WindowManager.LayoutParams.MATCH_PARENT);
		alertDialog.getWindow().setGravity(Gravity.RIGHT);
	}

	public void exitPads() {
		autoPlayCheck = false;
	}
	
	public static void end(Activity context){
		if (!invalid_formats.isEmpty()) {
			AlertDialog.Builder alertInvalidFiles = new AlertDialog.Builder(context);
			View alertDiagView = context.getLayoutInflater().inflate(R.layout.alert_dialog, null);
			ListView listInvalids = alertDiagView.findViewById(R.id.listWarnings);
			listInvalids.setAdapter(new ArrayAdapter(context, android.R.layout.simple_list_item_1, invalid_formats));
			alertInvalidFiles.setView(alertDiagView);
			alertInvalidFiles.create().show();
		}
		new makePads(getCurrentPath, R.id.contAllPads, context.getIntent().getExtras().getInt("height"), context).makePadInLayout();
		
		SkinTheme.playBgimg.setImageDrawable(SkinTheme.playBg);
		
	}
}
