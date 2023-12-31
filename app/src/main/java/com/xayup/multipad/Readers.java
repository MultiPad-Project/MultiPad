package com.xayup.multipad;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import com.google.common.io.Files;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import android.app.*;
import com.xayup.debug.XLog;

public class Readers {

  public static FileFilter filterFolder =
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          return file.isDirectory();
        }
      };
  public static FileFilter filterFiles =
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          return file.isFile();
        }
      };
  protected static FileFilter projectFiles =
      new FileFilter() {
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

  // Algoritimo info
  public Map<String, Map> readInfo(Context context, File projectDir, boolean granted) {
    Map<String, Object> infoInfo;
    Map<String, Map> mapFolder = new HashMap<>();
    if (!granted) {
      mapFolder.put(ProjectListAdapter.KEY_STORAGE_PERMISSION, null);
      return mapFolder;
    } else {
      if (projectDir.listFiles(filterFolder).length != 0) {
        for (final File projectFolder : projectDir.listFiles(filterFolder)) {
          File info = new File(projectFolder.getPath() + "/info");
          infoInfo = new HashMap<>();
          File ableton = new File(projectFolder.getPath() + "/[a-Z]*.als");
          if (ableton.isFile()) {
            String producerName = "?";
            String title = ableton.getName();
            infoInfo.put(ProjectListAdapter.KEY_TYPE, ProjectListAdapter.TYPE_BAD);

          } else if (info.exists()) {
            String producerName = "?";
            String title = "?";
            String chains = "?";
            infoInfo.put(ProjectListAdapter.KEY_TYPE, ProjectListAdapter.TYPE_BAD);

            try {
              BufferedReader bufferInfo = new BufferedReader(new FileReader(info));
              String line = bufferInfo.readLine();
              while (line != null) {
                if (line.toLowerCase().replaceAll("\\s+", "").contains("producername=")) {
                  producerName = line;
                } else if (line.toLowerCase().replaceAll("\\s+", "").contains("title=")) {
                  title = line;
                } else if (line.toLowerCase().replaceAll("\\s+", "").contains("chain=")) {
                  chains = line;
                }
                line = bufferInfo.readLine();
              }
              bufferInfo.close();
            } catch (IOException e) {
            }
            infoInfo.put(
                ProjectListAdapter.KEY_TITLE, title.replaceFirst(title.substring(0, title.indexOf("=") + 1), "").trim());
            infoInfo.put(
                ProjectListAdapter.KEY_PRODUCER_NAME,
                producerName
                    .replaceFirst(producerName.substring(0, producerName.indexOf("=") + 1), "")
                    .trim());
            infoInfo.put(
                ProjectListAdapter.KEY_CHAINS,
                Integer.parseInt(chains
                    .replaceFirst(chains.substring(0, chains.indexOf("=") + 1), "")
                    .trim()));
          } else {
            infoInfo.put(ProjectListAdapter.KEY_TITLE, projectFolder.getName());
            infoInfo.put(ProjectListAdapter.KEY_PRODUCER_NAME, context.getString(R.string.incomplet_project));
            infoInfo.put(ProjectListAdapter.KEY_TYPE, ProjectListAdapter.TYPE_GOOD);
          }
          infoInfo.put(ProjectListAdapter.KEY_PATH, projectFolder.getPath());
          mapFolder.put((String) infoInfo.get(ProjectListAdapter.KEY_TITLE), infoInfo);
        }
      } else {
        mapFolder.put(ProjectListAdapter.KEY_EMPTY, null);
      }

      return mapFolder;
    }
  }

  // Algoritimo keyLED
  private static boolean checkkeyLED(Activity context, String line, String fileName, int led_code) {
    switch (line.substring(0, 1)) {
      case "o":
        /*
         * Verifique se o mapeamento está correto.
         * Se correto, verique o tipo de codigo de cor (velocity or HEX)
         */
        boolean accept_mapper =
            line.matches(
                "[on]{1,2}\\s(mc\\s([1-9]|[1-2][0-9]|3[0-2])|[1-8]\\s[1-8]|l)\\s(a\\s([0-9]|[1-9][0-9]|1([0-1][0-9]|2[0-7]))|([A-F]|[0-9]){6})");
        if (accept_mapper) { // verificar o codigo de cor
          /*if (led_code > 127) {
             throw new NumberFormatException("");
          }*/
          if (line.substring(line.indexOf("a") + 2)
              .matches("([0-9]|[1-9][0-9]|1([0-1][0-9]|2[0-7]))")) {
            return true;
          } else if (line.substring(line.lastIndexOf(" ") + 1).matches("([A-F]|[0-9]){6}")) {
            return true;
          } else {
            PlayPads.invalid_formats.add(
                "("
                    + fileName
                    + ") "
                    + context.getString(R.string.invalid_led_color)
                    + " "
                    + line.substring(line.lastIndexOf(" ") + 1));
            return false;
          }
        } else {
          return false;
        }
      case "f":
        return line.matches("f\\s(l|[1-8]\\s[1-8]|mc\\s([1-9]|[1-2][0-9]|3[0-2]))");
      case "d":
        return line.matches("d\\s\\d+");
      default:
        return false;
    }
  }

  protected static boolean checkKeyLEDname(String keyLED_name, int index) {
    return keyLED_name.substring(0, index).matches("(1[0-9]|2[0-4]|[1-9])\\s[1-9]\\s[0-8]");
  }

  protected static Map<String /*chain+pad position+repeat*/, List<List<String>> /*led file readed*/>
      readKeyLEDs(Activity context, File keyLED_Path, int start_index, int end_index) {
    Map<String, List<List<String>>> mapLedLED = new HashMap<String, List<List<String>>>();
    // ordenar
    List<File> files = Arrays.asList(keyLED_Path.listFiles());
    Collections.sort(
        files,
        new Comparator<File>() {
          @Override
          public int compare(File f1, File f2) {
            return f1.getName().compareTo(f2.getName());
          }
        });
    File ledFile;
    for (; start_index < end_index; start_index++) {
      ledFile = files.get(start_index);
      String fileName = ledFile.getName();
      // lineIndex é o nome do arquivo. Conta os espaços
      int lineIndex = 5;

      // Caso haja leds para as chains 10+
      if (fileName.substring(0, 3).matches("[1-2][0-9]\\s")) lineIndex = 6;
      if (checkKeyLEDname(fileName, lineIndex)) {
        try {
          List<String> keys = Files.readLines(ledFile, StandardCharsets.UTF_8);
          List<String> v_keys = new ArrayList<String>(); // linhas verificadas
          v_keys.add(
              fileName.substring(lineIndex + 1, (fileName + " ").indexOf(" ", lineIndex + 1)));
          for (String line : keys) {
            if (!line.replace(" ", "").isEmpty()) {

              //	if (line.contains("*"))
              String orLine = line;
              line =
                  line.trim()
                      .replace("*", "mc")
                      .replace("off", "f")
                      .replace("on", "o")
                      .replace("auto", "a")
                      .replace("delay", "d")
                      .replace("logo", "l");
              if (checkkeyLED(context, line, ledFile.getName(), 0)) {
                v_keys.add(line.replace(" ", ""));
              } else {
                PlayPads.invalid_formats.add(
                    "("
                        + ledFile.getName()
                        + ") "
                        + context.getString(R.string.invalid_keyled)
                        + ": "
                        + orLine);
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
        PlayPads.invalid_formats.add(
            "(" + ledFile.getName() + "): " + context.getString(R.string.invalid_led_file));
      }
    }
    return mapLedLED;
  }

  private static Integer getDuration(File file) {
    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
    String durationStr =
        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    /*Retorno em segundos*/
    return ((int) ((Long.parseLong(durationStr) % (1000 * 60 * 60)) % (1000 * 60) / 1000));
  }

  // algoritimo keySound
  public static boolean checkKeySound(String line) {

    return line.matches(
        "([1-2][0-9]|[1-9])[1-9][0-8][\\w\\W]+.\\w{3}([0-9][1-9]|[0-9][1-2][0-9])?");
  }

  public static Map<String, List<Integer>> readKeySoundsNew(
      Activity context, File keySound, String soundPath) {
    if (keySound.exists()) {
      try {
        List<String> keys = Files.readLines(keySound, StandardCharsets.UTF_8);
        Map<String, List<Integer>> sounds = new HashMap<String, List<Integer>>();
        PlayPads.mSoundLoader = new SoundLoader(context);
        PlayPads.have_sounds = true;
        for (String line : keys) {
          int indextheSound = 3;
          int indexPad = 1;
          if ((!line.replaceAll("\\s", "").isEmpty()))
            if (checkKeySound(line.replaceAll("\\s", ""))) {
              if (line.substring(0, 2).matches("[1-2][0-9]")) {
                indextheSound = 4;
                indexPad = 2;
              }
              line = line.replace(" ", "");
              String ifToChain = line.substring(line.lastIndexOf(".") + 4);
              if (ifToChain.matches("1([1-9]|[1-2][0-9])")) ifToChain = ifToChain.substring(1);
              else ifToChain = null;
              line = line.substring(0, line.lastIndexOf(".") + 4);
              String sound = soundPath + "/" + line.substring(indextheSound);
              try {
                int sound_length = getDuration(new File(sound));
                PlayPads.mSoundLoader.loadSound(
                    sound, sound_length, line.substring(0, indextheSound), ifToChain);
              } catch (IllegalArgumentException i) {
                PlayPads.invalid_formats.add(
                    "("
                        + keySound.getName()
                        + ")"
                        + context.getString(R.string.invalid_sound)
                        + " "
                        + line);
                XLog.e("Load sound from reader", i.toString());
              }
            } else {
              PlayPads.invalid_formats.add(
                  "("
                      + keySound.getName()
                      + ")"
                      + context.getString(R.string.invalid_sound)
                      + " "
                      + line);
            }
        }
        //PlayPads.mSoundLoader.prepare();
        return sounds;
      } catch (IOException e) {
      }
    }
    return null;
  }

  // Algoritimo autoPlay
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
          line =
              line.replaceAll("chain", "c")
                  .replaceAll(" ", "")
                  .replaceAll("delay", "d")
                  .replaceAll("off", "f");
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
  // Ler arquivo .ct
  public static void getColorTableForCTFile(File table_file, int list_pos, boolean EYEDROP, int for_chain) {
    final String FILE_EXTENSION = ".ct";
    VariaveisStaticas.customColorMap[for_chain] = new int[VariaveisStaticas.color_map_length];
    if (EYEDROP) {
      for (int i = 0; i < VariaveisStaticas.newColorInt.length; i++) {
        VariaveisStaticas.customColorMap[for_chain][i] = VariaveisStaticas.newColorInt[i];
      }
      VariaveisStaticas.defaultColorMap = VariaveisStaticas.newColorInt;
    } else {
      for (int i = 0; i < VariaveisStaticas.colorInt.length; i++) {
        VariaveisStaticas.customColorMap[for_chain][i] = VariaveisStaticas.colorInt[i];
      }
      VariaveisStaticas.defaultColorMap = VariaveisStaticas.colorInt;
    }
    try {
      BufferedReader ct_file = new BufferedReader(new FileReader(table_file));
      String line = ct_file.readLine();
      // Map<Integer, Map<String, Integer>> color_table = new HashMap<Integer, Map<String,
      // Integer>>();
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
          VariaveisStaticas.customColorMap[for_chain][color_code] = Color.rgb(r, g, b);
          if(VariaveisStaticas.customColorMap[for_chain][color_code] == Color.BLACK){
            VariaveisStaticas.customColorMap[for_chain][color_code] = Color.TRANSPARENT;
          }
        }

        line = ct_file.readLine();
      }
      ct_file.close();
      //	Toast.makeText(getApplicationContext(), color_table.values() + "",
      // Toast.LENGTH_SHORT).show();
    } catch (IOException f) {
    }
  }

  public static ColorTableAdapter listColorTable(Context context) {
    final String FILE_EXTENSION = ".ct";
    File[] file_ct_name =
        new File(VariaveisStaticas.COLOR_TABLE_PATH)
            .listFiles(
                new FilenameFilter() {
                  @Override
                  public boolean accept(File dir, String name) {
                    return (name.contains(".") && name.substring(name.indexOf(".")).equals(FILE_EXTENSION));
                  }
                });
    if (file_ct_name != null) {
      return new ColorTableAdapter(context, file_ct_name);
    }
    return null;
  }
}
