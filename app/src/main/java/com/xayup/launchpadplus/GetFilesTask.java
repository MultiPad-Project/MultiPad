package com.xayup.launchpadplus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
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
		bar.show();
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

	private boolean checkLine(String line, String fileName) {
		line = line.replace(" ", "").replaceAll("off", "f");
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
					playPads.invalid_formats.add("(" + fileName + ") " + context.getString(R.string.invalid_led_color)
							+ " " + line.substring(line.indexOf("a") + 1));
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

	public void getFiles() {
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
		}
		if (playPads.fileProj.containsKey("keysound") && playPads.fileProj.containsKey("sounds")) {
			bar.setMessage("Reading keySound");
			playPads.keySound = Readers.readKeySounds(context, playPads.fileProj.get("keysound"),
					playPads.fileProj.get("sounds").getPath(), context);
		}
		if (playPads.fileProj.containsKey("autoplay")) {
			bar.setMessage("Reading autoPlay");
			playPads.autoPlay = Readers.readautoPlay(context, playPads.fileProj.get("autoplay"));
			//autoPlayPross = ;
		}
	}

}