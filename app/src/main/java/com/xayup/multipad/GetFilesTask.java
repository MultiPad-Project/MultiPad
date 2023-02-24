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
	private Activity context;
	private ProgressDialog bar;

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
		PlayPads.end(context);
		bar.dismiss();
		super.onPostExecute(arg0);
	}

	public void getFiles() {
		for(File file : new File(PlayPads.getCurrentPath).listFiles(Readers.projectFiles)){
			switch (file.getName().toLowerCase()){
				case "keysound":
					bar.setMessage("Reading keySound");
					Readers.readKeySoundsNew(context, file, file.getParent() + File.separator + "sounds");
                	break;
				case "autoplay":
					bar.setMessage("Reading autoPlay");
					PlayPads.autoPlay = Readers.readautoPlay(context, file);
					PlayPads.autoPlayThread = new AutoPlayFunc(context);
					PlayPads.progressAutoplay = context.findViewById(R.id.seekBarProgressAutoplay);
					PlayPads.progressAutoplay.setMin(0);
					PlayPads.progressAutoplay.setMax(PlayPads.autoPlay.size()-1);
					PlayPads.progressAutoplay.setContext(context);
					break;
				case "keyled":
					bar.setMessage("Reading keyLEDs");
					PlayPads.ledFiles = Readers.readKeyLEDs(context, file);
					break;
			}
		}
	}

}