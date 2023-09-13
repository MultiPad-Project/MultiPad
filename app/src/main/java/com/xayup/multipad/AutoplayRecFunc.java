package com.xayup.multipad;

import android.app.Activity;
import android.os.SystemClock;
import android.widget.Toast;
import android.widget.SeekBar;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class AutoplayRecFunc {
	private static long oldTime;
	private static List<String> autoPlayRecord;
	private static Activity context;
	
	public AutoplayRecFunc(Activity context){
		autoPlayRecord = new ArrayList<String>();
		autoPlayRecord.add("c " + PlayPads.currentChainMC);
		oldTime = SystemClock.uptimeMillis();
		this.context = context;
	}
	public static void addChain(String chain){
		autoPlayRecord.add("c " + chain);
	}
	public static void autoPlayRecord(int button_id){
		String add = "d "+(SystemClock.uptimeMillis()-oldTime);
		autoPlayRecord.add(add);		
		final String[] button = ((String)""+button_id).split("");
		autoPlayRecord.add("t " + button[0] + " " + button[1]);		
		oldTime = SystemClock.uptimeMillis();
	}
	public static void saveAutoplay(){
		try{
			final File autoplay_file = new File(PlayPads.getCurrentPath, "autoplay");
			if(autoplay_file.exists()){
				new File(PlayPads.getCurrentPath, "autoplay").renameTo(new File(PlayPads.getCurrentPath, "autoplay" + SystemClock.uptimeMillis()));
			}
			autoplay_file.createNewFile();
			FileWriter save_autoplay_file = new  FileWriter(autoplay_file);
			for(String line : autoPlayRecord){
				save_autoplay_file.write(line + "\n");
			}
			save_autoplay_file.close();
			PlayPads.autoPlay = Readers.readautoPlay(context, autoplay_file);
			PlayPads.progressAutoplay = context.findViewById(R.id.seekBarProgressAutoplay);
			PlayPads.progressAutoplay.setMax(PlayPads.autoPlay.size()-1);
			PlayPads.progressAutoplay.setContext(context);
			PlayPads.progressAutoplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				}
				@Override
				public void onStartTrackingTouch(SeekBar arg0) {
				}
				@Override
				public void onStopTrackingTouch(SeekBar arg0) {
				}
			});
			} catch (IOException e){
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
		}
	}
}