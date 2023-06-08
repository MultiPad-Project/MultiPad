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
		autoPlayRecord.add("c " + 1/*GET CURRENT CHAIIN*/);
		oldTime = SystemClock.uptimeMillis();
		this.context = context;
	}
	public static void addChain(String chain){
		autoPlayRecord.add("c " + chain);
	}
	public static void autoPlayRecord(int button_id){
		String add = "d "+(SystemClock.uptimeMillis()-oldTime);
		autoPlayRecord.add(add);		
		final String[] button = (String.valueOf(button_id)).split("");
		autoPlayRecord.add("t " + button[0] + " " + button[1]);		
		oldTime = SystemClock.uptimeMillis();
	}
	public static void saveAutoplay(String path){
		try{
			final File autoplay_file = new File(path, "autoplay");
			if(autoplay_file.exists()){
				new File(path, "autoplay").renameTo(new File(path, "autoplay" + SystemClock.uptimeMillis()));
			}
			autoplay_file.createNewFile();
			FileWriter save_autoplay_file = new  FileWriter(autoplay_file);
			for(String line : autoPlayRecord){
				save_autoplay_file.write(line + "\n");
			}
			save_autoplay_file.close();
			} catch (IOException e){
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
		}
	}
}