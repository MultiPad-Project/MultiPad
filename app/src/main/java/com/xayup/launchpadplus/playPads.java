package com.xayup.launchpadplus;
import android.app.*;
import android.graphics.*;
import android.media.*;
import android.os.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

import android.net.*;
import android.view.View;
import android.widget.TextView;

public class playPads extends Activity
{
	public static String chainSl = "1";
	String getCurrentPath;
	public static boolean pressLed;
	Color cor;
	Readers filter = new Readers();
	public static Map<String, File> fileProj = new HashMap<String, File>();
	public static Map<String, Map<Integer, List<String>>> ledFiles = new HashMap<String, Map<Integer, List<String>>>();
	public static int otherChain = 19;
	public static Map<String, Map<String, List<Uri>>> keySound;
	public static MediaPlayer startSound = new MediaPlayer();
	public static Map<String, MediaPlayer> padPlayer = new HashMap<String, MediaPlayer>();
	public static List<String> autoPlay;
	public static boolean autoPlayCheck = false;
	public static Map<String, Integer> soundrpt = new HashMap<String, Integer>();
	public static Map<String, Integer> ledrpt = new HashMap<String, Integer>();
	public static int oldPad = 0;
	public static boolean mk2 = false;
	public static List<String> invalid_formats = new ArrayList<>();
	public static Thread ledOn;

    ProgressDialog progress;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playpads);
		getCurrentPath = getIntent().getExtras().getString("currentPath");
        progress = new ProgressDialog(this);
        progress.setMessage("Carregando dados...");
        progress.show();
		getFiles();
		new makePads(getCurrentPath, R.id.contAllPads, getIntent().getExtras().getInt("height"), this).makePadInLayout();
        progress.dismiss();
	}
	private boolean checkLine(String line, String fileName){
            line = line.replace(" ", "");
			switch (line.substring(0, 1)){
				case "o":
					boolean ye;
				    if(line.contains("mc")){
						ye = line.matches("[on]{1,2}mc[0-3]?[0-9]a\\d{1,3}");

					}else{
						ye = line.matches("[on]{1,2}[1-8]{2}a\\d{1,3}");
                    }
					if(ye){
						if(Integer.parseInt(line.substring(line.indexOf("a")+1)) > 127){
							invalid_formats.add(R.string.invalid_led_color + "Color" + line.substring(line.indexOf("a")+1) + ", File: " + fileName);
						}
						return true;
					} else{
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

	public void getFiles()
	{
		String[] fs = {"keyled", "sounds", "autoplay", "info", "keysound"};

		for(File f : new File(getCurrentPath).listFiles())
		{
			if(Arrays.asList(fs).contains(f.getName().toLowerCase()))
			{
				fileProj.put(f.getName().toLowerCase(), f);
				if(f.getName().equalsIgnoreCase("keyled"))
				{
					progress.setMessage(f.getPath());
					List<String> keyReaded;
					for(File l : f.listFiles(filter.filterFiles))
					{
							progress.setMessage(l.getPath());
							keyReaded = new ArrayList<String>();
							try
							{
									BufferedReader read = new BufferedReader(new FileReader(l));
									String line = read.readLine();
									while (line != null)
									{
											if (!line.isEmpty())
												if(checkLine(line.toLowerCase(), l.getName())){
													line = line.replace(" ", "").toLowerCase().replace("off", "f").replace("on", "o");
													keyReaded.add(line);
												} else{
													invalid_formats.add(line);
												}
											line = read.readLine();
									}
									read.close();
							}
							catch (IOException e)
							{}
                            String idLed = l.getName().replace(" ", "").substring(0, 3);
							if(ledFiles.get(idLed) != null){
							    int size = ledFiles.get(idLed).size();
							    ledFiles.get(idLed).put(size, keyReaded);
                            } else{
							    Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();
							    map.put(0, keyReaded);
							    ledFiles.put(idLed, map);
                            }
							
					} 
					keyReaded = null;

				} 
			}
		}
			if (fileProj.containsKey("keysound") && fileProj.containsKey("sounds")){
					keySound = filter.readKeySounds(fileProj.get("keysound"), fileProj.get("sounds").getPath(), this);
			}
			if(fileProj.containsKey("autoplay")){
				autoPlay = filter.readautoPlay(fileProj.get("autoplay"));
				//autoPlayPross = ;
			}
	}
}
