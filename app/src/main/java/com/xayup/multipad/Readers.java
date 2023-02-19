package com.xayup.multipad;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.util.TimeUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.google.android.exoplayer2.MediaItem;
import com.google.common.io.Files;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import android.app.*;

public class Readers {
    
	public static FileFilter filterFolder = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory();
		}
	};
	public static FileFilter filterFiles = new FileFilter() {
		@Override

		public boolean accept(File file) {
			return file.isFile();
		}

	};
	protected static FileFilter projectFiles = new FileFilter() {
		@Override
		public boolean accept(File file) {
			switch (file.getName().toLowerCase()) {
			case "keysound":
				return true;
			case "info":
				return true;
			case "autoplay":
				return true;
			case "sounds":
				return true;
			case "keyled":
				return true;
			default:
				return false;
			}
		}
	};

	//Algoritimo info
	public Map<String, Map> readInfo(Activity context, File projectDir, boolean granted) {
		Map<String, String> infoInfo;
		Map<String, Map> mapFolder = new HashMap<String, Map>();
		if (!granted) {
			mapFolder.put("pr", null);
			return mapFolder;
		} else {

			if (projectDir.listFiles(filterFolder).length != 0) {
				for (final File projectFolder : projectDir.listFiles(filterFolder)) {
					File info = new File(projectFolder.getPath() + "/info");
					infoInfo = new HashMap<String, String>();
                    File ableton = new File(projectFolder.getPath() + "/[a-Z]*.als");
                    if(ableton.isFile()){
                        String producerName= "?";
                        String title = ableton.getName();
                        infoInfo.put("bad", "False");
                        
                        
                        
                    } else
					if (info.exists()) {
						String producerName = "?";
						String title = "?";
						infoInfo.put("bad", "False");

						try {
							BufferedReader bufferInfo = new BufferedReader(new FileReader(info));
							String line = bufferInfo.readLine();
							while (line != null) {
								if (line.toLowerCase().replaceAll("\\s+", "").contains("producername=")) {
									producerName = line;
								}
								if (line.toLowerCase().replaceAll("\\s+", "").contains("title=")) {
									title = line;
								}
								//infoInfo.put("title", title);
								//infoInfo.put("producerName", producerName);
								line = bufferInfo.readLine();
							}
							bufferInfo.close();
						} catch (IOException e) {
						}
						infoInfo.put("title",
								title.replaceFirst(title.substring(0, title.indexOf("=") + 1), "").trim());
						infoInfo.put("producerName", producerName
								.replaceFirst(producerName.substring(0, producerName.indexOf("=") + 1), "").trim());
					} else {
						infoInfo.put("title", context.getString(R.string.without_info));
						infoInfo.put("producerName", context.getString(R.string.incomplet_project));
						infoInfo.put("bad", "True");
					}
					infoInfo.put("local", projectFolder.getPath());
					mapFolder.put(projectFolder.getName(), infoInfo);
				}
			} else {
				mapFolder.put("Empyt", infoInfo = null);
			}

			return mapFolder;
		}
	}

	//Algoritimo keyLED
	private static boolean checkkeyLED(Activity context, String line, String fileName) {
	/*	line = line.replace(" ", "").replace("off", "f");
		if (line.contains("*"))
			line =line.replace("*", "mc"); */
		switch (line.substring(0, 1)) {
		case "o":
			boolean ye;
			//ye = line.matches("[on]{1,2}mc[0-3]?[0-9]a\\d{1,3}");

			ye = line.matches("[on]{1,2}(mc([0-2]?[0-9]|3[0-3])|[1-8]{2}|l)a\\d{1,3}");

			if (ye) { //verificar o codigo de cor
				if (Integer.parseInt(line.substring(line.indexOf("a") + 1)) > 127) {
					PlayPads.invalid_formats.add("(" + fileName + ") " + context.getString(R.string.invalid_led_color)
							+ " " + line.substring(line.indexOf("a") + 1));
				}
				return true;
			} else {
				return false;
			}
		case "f":
			return line.matches("[off]{1,3}(l|[1-8]{2}|mc[1-3]?[0-9])");
		case "d":
			return line.matches("\\w\\d+");
		default:
			return false;
		}
	}

	protected static boolean checkKeyLEDname(String keyLED_name, int index) {
		return keyLED_name.substring(0, index).matches("([1-2][0-9]|[1-9])\\s[1-9]\\s[0-8]");
	}

	protected static Map<String /*chain+pad position+repeat*/, List<List<String>> /*led file readed*/> readKeyLEDs(
		Activity context, File keyLED_Path) {
		Map<String, List<List<String>>> mapLedLED = new HashMap<String, List<List<String>>>();
		//ordenar
		List<File> files = Arrays.asList(keyLED_Path.listFiles());
		Collections.sort(files, new Comparator<File>(){
			@Override
			public int compare(File f1, File f2){
				return f1.getName().compareTo(f2.getName());
			}
		});
		for (final File ledFile : files) {
            String fileName = ledFile.getName();
            //lineIndex é o nome do arquivo. Conta os espaços
			int lineIndex = 5;
            
            //Caso haja leds para as chains 10+
			if(fileName.substring(0, 3).matches("[1-2][0-9]\\s"))
				lineIndex = 6;
			if (checkKeyLEDname(fileName, lineIndex)) {
				try {
					List<String> keys = Files.readLines(ledFile, StandardCharsets.UTF_8);
					List<String> v_keys = new ArrayList<String>(); //linhas verificadas
                    v_keys.add(fileName.substring(lineIndex+1, (fileName+" ").indexOf(" ", lineIndex+1)));
					for (String line : keys) {
						if (!line.replace(" ", "").isEmpty()) {
							
						//	if (line.contains("*"))
							String orLine = line;
							line = line.replace(" ", "").replace("*", "mc").replace("off", "f").replace("on", "o").replace("auto", "a").replace("delay", "d").replace("logo", "l");
							if (checkkeyLED(context, line, ledFile.getName())) {
								v_keys.add(line);
							} else {
								PlayPads.invalid_formats.add("(" + ledFile.getName() + ") "
										+ context.getString(R.string.invalid_keyled) + ": " + orLine);
							}
						}
					}
					String ledFileID = ledFile.getName().substring(0, lineIndex).replace(" ", "");
					if (mapLedLED.containsKey(ledFileID)) {
						mapLedLED.get(ledFileID).add(v_keys);
					} else {
						List<List<String>> keyLEDs = new ArrayList<List<String>>();
						keyLEDs.add(v_keys);
						mapLedLED.put(ledFileID, keyLEDs);
					}
				} catch (IOException e) {
				}
			} else {
				PlayPads.invalid_formats
						.add("(" + ledFile.getName() + "): " + context.getString(R.string.invalid_led_file));
			}
		}
		return mapLedLED;
	}

	private static Integer getDuration(File file) {
		MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
		mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
		String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		/*Retorno em segundos*/
		return ((int) ((Long.parseLong(durationStr) % (1000 * 60 * 60)) % (1000 * 60) / 1000));
	}

	//algoritimo keySound
	public static boolean checkKeySound(String line) {

		return line.matches("([1-2][0-9]|[1-9])[1-9][0-8][\\w\\W]+.\\w{3}([0-9][1-9]|[0-9][1-2][0-9])?");
	}

	public static Map<String, List<MediaItem>> readKeySounds(Activity context, File keySound, String soundPath) {
		if (keySound.exists()) {
			//	System.out.println()
			try {
				List<String> keys = Files.readLines(keySound, StandardCharsets.UTF_8);
				Map<String, List<MediaItem>> sounds = new HashMap<String, List<MediaItem>>();
				for (String line : keys) {
					int indextheSound = 3;
					int indexPad = 1;
				if((!line.replaceAll("\\s", "").isEmpty()))
					if (checkKeySound(line.replaceAll("\\s", ""))) {
						if (line.substring(0, 2).matches("[1-2][0-9]")){
							indextheSound = 4;
							indexPad = 2;
						}	
						line = line.replace(" ", "");
						String toChain = "";
						String ifToChain = line.substring(line.indexOf(".") + 4);
						if (ifToChain.matches("1([1-9]|[1-2][0-9])"))
							toChain = ifToChain.substring(1);
						//	System.out.println("CMC "+toChain);
				//		System.out.println(toChain);
						line = line.substring(0, line.indexOf(".") + 4);
						if (sounds.get(line.substring(0, indextheSound)) == null) {
							List<MediaItem> mediaitem = new ArrayList<MediaItem>();
							//	MediaItem item = new MediaItem.Builder().setMediaId(line.substring(3)).setUri(soundPath + "/" + line.substring(3)).build();
							//	MediaItem.fromUri(soundPath + "/" + line.substring(3));
							mediaitem.add(new MediaItem.Builder().setMediaId(toChain)
									.setUri(soundPath + "/" + line.substring(indextheSound)).build());
							sounds.put(line.substring(0, indextheSound), mediaitem);
							PlayPads.chainClickable.put(line.substring(indexPad, indextheSound), null);
						} else {
							sounds.get(line.substring(0, indextheSound)).add(new MediaItem.Builder().setMediaId(toChain)
									.setUri(soundPath + "/" + line.substring(indextheSound)).build());
						}
					//	System.out.println("FNL "+toChain + " "+line);
					} else {
						PlayPads.invalid_formats.add("("+keySound.getName()+")"+context.getString(R.string.invalid_sound) + " " + line);
					}
					
				}
				return sounds;
			} catch (IOException e) {
			}
		}
		return null;
	}
    
    public static Map<String, List<Integer>> readKeySoundsPool(Activity context, File keySound, String soundPath) {
		if (keySound.exists()) {
			//	System.out.println()
			try {
				List<String> keys = Files.readLines(keySound, StandardCharsets.UTF_8);
				Map<String, List<Integer>> sounds = new HashMap<String, List<Integer>>();
				for (String line : keys) {
					int indextheSound = 3;
					int indexPad = 1;
				if((!line.replaceAll("\\s", "").isEmpty()))
					if (checkKeySound(line.replaceAll("\\s", ""))) {
						if (line.substring(0, 2).matches("[1-2][0-9]")){
							indextheSound = 4;
							indexPad = 2;
						}	
						line = line.replace(" ", "");
						String ifToChain = line.substring(line.indexOf(".") + 4);
						line = line.substring(0, line.indexOf(".") + 4);
                        String sound = soundPath + "/" + line.substring(indextheSound);
						int soundId = PlayPads.soundPool.load(sound, 1);
                        if (sounds.get(line.substring(0, indextheSound)) == null) {
                            List<Integer> soundSec = new ArrayList<Integer>();
                            soundSec.add(soundId);
							sounds.put(line.substring(0, indextheSound), soundSec);
							PlayPads.chainClickable.put(line.substring(indexPad, indextheSound), null);
						} else {
							sounds.get(line.substring(0, indextheSound)).add(soundId);
						}
                        if (ifToChain.matches("1([1-9]|[1-2][0-9])"))
							PlayPads.toChainPool.put(soundId, ifToChain.substring(1));
					//	System.out.println("FNL "+toChain + " "+line);
					} else {
						PlayPads.invalid_formats.add("("+keySound.getName()+")"+context.getString(R.string.invalid_sound) + " " + line);
					}
					
				}
				return sounds;
			} catch (IOException e) {
			}
		}
		return null;
	}
    
    public static Map<String, List<Integer>> readKeySoundsNew(Activity context, File keySound, String soundPath) {
		if (keySound.exists()) {
			try {
				List<String> keys = Files.readLines(keySound, StandardCharsets.UTF_8);
				Map<String, List<Integer>> sounds = new HashMap<String, List<Integer>>();
				PlayPads.mSoundLoader = new SoundLoader(context);
				for (String line : keys) {
					int indextheSound = 3;
					int indexPad = 1;
				if((!line.replaceAll("\\s", "").isEmpty()))
					if (checkKeySound(line.replaceAll("\\s", ""))) {
						if (line.substring(0, 2).matches("[1-2][0-9]")){
							indextheSound = 4;
							indexPad = 2;
						}	
						line = line.replace(" ", "");
						String ifToChain = line.substring(line.indexOf(".") + 4);
						if (ifToChain.matches("1([1-9]|[1-2][0-9])")) ifToChain = ifToChain.substring(1); else ifToChain = "";
						line = line.substring(0, line.indexOf(".") + 4);
                        String sound = soundPath + "/" + line.substring(indextheSound);
						int sound_length = getDuration(new File(sound));
						PlayPads.mSoundLoader.loadSound(sound, sound_length, line.substring(0, indextheSound), ifToChain);
					} else {
						PlayPads.invalid_formats.add("(" + keySound.getName() + ")" + context.getString(R.string.invalid_sound) + " " + line);
					}
				}
				PlayPads.mSoundLoader.prepare();
				return sounds;
			} catch (IOException e) {
			}
		}
		return null;
	}

	//Algoritimo autoPlay
	private static boolean checkAutoPlayFormat(String line) {
		switch (line.toLowerCase().substring(0, 1)) {
		case "d":
			return line.matches("d\\d+");
		case "c":
			return line.matches("c([1-2][0-9]|[1-9])");
		default:
			return line.matches("\\w{1,2}[1-8]{2}");
		}
	}

	public static List<String> readautoPlay(Activity context, File autoPlay) {
		List<String> autoplayLineList = new ArrayList<String>();
		// List<String> invalid_format = new ArrayList<String>();
		String chain = "19";
		try {
			BufferedReader autoplayReader = new BufferedReader(new FileReader(autoPlay));
			String line = autoplayReader.readLine();
			while (line != null) {
				if (!line.replaceAll(" ", "").isEmpty()) {
					line = line.replaceAll("chain", "c").replaceAll(" ", "").replaceAll("delay", "d").replaceAll("off", "f");
					if (checkAutoPlayFormat(line)) {
						if (line.replace(" ", "").substring(0, 1).equalsIgnoreCase("c"))
							chain = VariaveisStaticas.chainsIDlist.get(Integer.parseInt(line.substring(1)));
						line = chain + line;
						autoplayLineList.add(line);
					} else {
						PlayPads.invalid_formats.add(context.getString(R.string.invalid_autoplay) + " " + line);
					}
				}
				line = autoplayReader.readLine();
			}

		} catch (IOException e) {
		}

		return autoplayLineList;
	}
//Ler arquivo .ct
	public static void getColorTableForCTFile(File rootDir, int list_pos, boolean EYEDROP){
		final String FILE_EXTENSION = ".ct";
		File[] files_list_index = rootDir.listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
			    return (pathname.isFile() && pathname.getName().substring(pathname.getName().indexOf(".")).equals(FILE_EXTENSION));
			}
		});
		if(EYEDROP){
			for(int i = 0; i < VariaveisStaticas.newColorInt.length; i++){
				VariaveisStaticas.customColorInt.put(i, VariaveisStaticas.newColorInt[i]);
			}
		} else {
			for(int i = 0; i < VariaveisStaticas.colorInt.length; i++){
				VariaveisStaticas.customColorInt.put(i, VariaveisStaticas.colorInt[i]);
			}
		}
		try {
			BufferedReader ct_file = new BufferedReader(new FileReader(
			rootDir.getPath().toString() + "/" + files_list_index[list_pos].getName()));
			String line = ct_file.readLine();
			//Map<Integer, Map<String, Integer>> color_table = new HashMap<Integer, Map<String, Integer>>();
			Integer color_code = null;
			Integer r = null;
			Integer g = null;
			Integer b = null;
			while (line != null) {
				if (line.contains("{")) {
					color_code = null;
					r = null;
					g = null;
					b = null;
					} else if (line.contains("id=")) {
					if (color_code == null) {
						color_code = Integer.parseInt(line.replaceAll("id=", ""));		
					}
					} else if (line.contains("r=")) {
					if (r == null) {
						r = Integer.parseInt(line.replaceAll("r=", ""));
					}
					} else if (line.contains("g=")) {
					if (g == null) {
						g = Integer.parseInt(line.replaceAll("g=", ""));
					}
					} else if (line.contains("b=")) {
					if (b == null) {
						b = Integer.parseInt(line.replaceAll("b=", ""));
					}
					} else if (line.contains("}")) {
					VariaveisStaticas.customColorInt.put(color_code, Color.rgb(r, g, b));
				}
				
				line = ct_file.readLine();
			}
			ct_file.close();
			//	Toast.makeText(getApplicationContext(), color_table.values() + "", Toast.LENGTH_SHORT).show();
			} catch (IOException f) {
		}
	}
	
	public static ArrayAdapter listColorTable(Context context){
		final String FILE_EXTENSION = ".ct";
		String[] file_ct_name = new File(VariaveisStaticas.COLOR_TABLE_PATCH).list(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
			    return (name.substring(name.indexOf(".")).equals(FILE_EXTENSION));
			}
		});
        if(file_ct_name == null){
            return null;
        }
		return new ArrayAdapter<String>(context, R.layout.simple_list_item, file_ct_name);
	}
}
