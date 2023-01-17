package com.xayup.multipad;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class GetFilesTask extends AsyncTask<Void, Void, Boolean> {
	Activity context;
	ProgressDialog bar;

	public GetFilesTask(Activity context) {
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		bar = new ProgressDialog(context);
		bar.setCancelable(false);
		XayUpFunctions.showDiagInFullscreen(bar);
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void[] arg0) {

		getFiles();
		return true;
	}

	@Override
	protected void onPostExecute(Boolean arg0) {
		playPads.end(context);
		bar.dismiss();
		super.onPostExecute(arg0);
	}

	public void getFiles() { /*
		String[] fs = { "keyled", "sounds", "autoplay", "info", "keysound" };
		for (File f : new File(playPads.getCurrentPath).listFiles()) {
			if (Arrays.asList(fs).contains(f.getName().toLowerCase())) {
				bar.setMessage("Reading " + f.getName());
				playPads.fileProj.put(f.getName().toLowerCase(), f);
				if (f.getName().equalsIgnoreCase("keyled")) {
					List<String> keyReaded;
					for (File l : f.listFiles(Readers.filterFiles)) {
						if (l.getName().replaceAll(" ", "").substring(0, 3).matches("[1-8]{3}")) {
							keyReaded = new ArrayList<String>();
							try {
								BufferedReader read = new BufferedReader(new FileReader(l));
								String line = read.readLine();
								while (line != null) {
									if (!line.isEmpty())
										if (checkLine(line.toLowerCase(), l.getName())) {
											line = line.replace(" ", "").toLowerCase().replace("off", "f").replace("on",
													"o");
											keyReaded.add(line);
										} else {
											playPads.invalid_formats.add("(" + l.getName() + ") "
													+ context.getString(R.string.invalid_keyled) + " " + line);
										}

									line = read.readLine();
								}
								read.close();
							} catch (IOException e) {
							}
							String idLed = l.getName().replace(" ", "").substring(0, 3);
							if (playPads.ledFiles.get(idLed) != null) {
								int size = playPads.ledFiles.get(idLed).size();
								playPads.ledFiles.get(idLed).put(size, keyReaded);
							} else {
								Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();
								map.put(0, keyReaded);
								playPads.ledFiles.put(idLed, map);
							}
							//	bar.setMessage(l.getPath());
						} else {
							playPads.invalid_formats
									.add(context.getString(R.string.invalid_led_file) + " " + l.getName());
						}
					}
					keyReaded = null;
				}
			}
		} */
		for(File file : new File(playPads.getCurrentPath).listFiles(Readers.projectFiles)){
			switch (file.getName().toLowerCase()){
				case "keysound":
					bar.setMessage("Reading keySound");
                if(playPads.useSoundPool){
                    playPads.soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
                    playPads.streamsPool = new HashMap<String, Integer>();
                    playPads.keySoundPool = Readers.readKeySoundsPool(context, file, file.getParent() + "/sounds");
                } else {
					playPads.keySound = Readers.readKeySounds(context, file, file.getParent() + "/sounds");
				}
                	break;
				case "autoplay":
					bar.setMessage("Reading autoPlay");
					playPads.autoPlay = Readers.readautoPlay(context, file);
					playPads.autoPlayThread = new autoPlayFunc(context);
					playPads.progressAutoplay = context.findViewById(R.id.seekBarProgressAutoplay);
				//	playPads.progressAutoplay.setRotation(270.0f);
					playPads.progressAutoplay.setMin(0);
					playPads.progressAutoplay.setMax(playPads.autoPlay.size()-1);
					playPads.progressAutoplay.setContext(context);
					break;
				case "keyled":
					bar.setMessage("Reading keyLEDs");
					playPads.ledFiles = Readers.readKeyLEDs(context, file);
					break;
			//	default:
			//	return false;
			}
		}
		/*
		if (playPads.fileProj.containsKey("keysound") && playPads.fileProj.containsKey("sounds")) {
			
			playPads.keySound = Readers.readKeySounds(context, playPads.fileProj.get("keysound"),
					playPads.fileProj.get("sounds").getPath(), context);
		}
		if (playPads.fileProj.containsKey("autoplay")) {
				//autoPlayPross = ;
		} */
	}

}