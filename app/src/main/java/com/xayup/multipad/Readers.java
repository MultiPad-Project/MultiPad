package com.xayup.multipad;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.widget.ArrayAdapter;
import com.google.common.io.Files;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import android.app.*;

public class Readers {

    protected Activity context;

    public Readers(Context context) {
        this.context = (Activity) context;
    }

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
                        case "info":
                        case "autoplay":
                        case "sounds":
                        case "keyled":
                            return true;
                        default:
                            return false;
                    }
                }
            };

    // Algoritimo info
    public Map<Object, Map> readInfo(File projectDir) {
        Map<Object, String> infoInfo;
        Map<Object, Map> mapFolder;
        File[] folders = projectDir.listFiles(filterFolder);
        if (folders == null || folders.length == 0) {
            return null;
        } else {
            mapFolder = new HashMap<>();
            for (final File projectFolder : folders) {
                File info = new File(projectFolder.getPath() + "/info");
                infoInfo = new HashMap<>();
                if (info.exists()) {
                    String producerName = "?";
                    String title = "?";
                    infoInfo.put(ProjectsAdapter.STATE, "false");
                    try {
                        BufferedReader bufferInfo = new BufferedReader(new FileReader(info));
                        String line = bufferInfo.readLine();
                        while (line != null) {
                            if (line.toLowerCase()
                                    .replaceAll("\\s+", "")
                                    .contains("producername=")) {
                                producerName = line;
                            }
                            if (line.toLowerCase().replaceAll("\\s+", "").contains("title=")) {
                                title = line;
                            }
                            line = bufferInfo.readLine();
                        }
                        bufferInfo.close();
                    } catch (IOException e) {
                        Log.e("Readers", "Try read '" + info.getPath() + "'");
                    }
                    infoInfo.put(
                            ProjectsAdapter.TITLE,
                            title.replaceFirst(title.substring(0, title.indexOf("=") + 1), "")
                                    .trim());
                    infoInfo.put(
                            ProjectsAdapter.PRODUCER_NAME,
                            producerName
                                    .replaceFirst(
                                            producerName.substring(
                                                    0, producerName.indexOf("=") + 1),
                                            "")
                                    .trim());
                } else {
                    infoInfo.put(ProjectsAdapter.TITLE, context.getString(R.string.without_info));
                    infoInfo.put(
                            ProjectsAdapter.PRODUCER_NAME,
                            context.getString(R.string.incomplet_project));
                    infoInfo.put(ProjectsAdapter.STATE, "true");
                }
                infoInfo.put(ProjectsAdapter.PATH, projectFolder.getPath());
                mapFolder.put(projectFolder.getName(), infoInfo);
            }
        }
        return mapFolder;
    }

    // Algoritimo keyLED
    private static boolean checkkeyLED(
            Context context, String line, String fileName, int led_code) {
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
                    } else if (line.substring(line.lastIndexOf(" ") + 1)
                            .matches("([A-F]|[0-9]){6}")) {
                        return true;
                    } else {
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

    public static Map<String /*chain+pad position+repeat*/, List<List<String>> /*led file readed*/>
            readKeyLEDs(Context context, List<File> keyleds) {
        Map<String, List<List<String>>> mapLedLED = new HashMap<String, List<List<String>>>();
        for (File ledFile : keyleds) {
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
                            fileName.substring(
                                    lineIndex + 1, (fileName + " ").indexOf(" ", lineIndex + 1)));
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
            }
        }
        return mapLedLED;
    }

    private static Integer getDuration(File file) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
        String durationStr =
                mediaMetadataRetriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_DURATION);
        /*Retorno em segundos*/
        return ((int) ((Long.parseLong(durationStr) % (1000 * 60 * 60)) % (1000 * 60) / 1000));
    }

    // algoritimo keySound
    public static boolean checkKeySound(String line) {

        return line.matches(
                "([1-2][0-9]|[1-9])[1-9][0-8][\\w\\W]+.\\w{3}([0-9][1-9]|[0-9][1-2][0-9])?");
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

    public static List<String> readAutoPlay(Activity context, File autoPlay) {
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
                            chain =
                                    VariaveisStaticas.chainsIDlist.get(
                                            Integer.parseInt(line.substring(1)));
                        line = chain + line;
                        autoplayLineList.add(line);
                    } else {
                    }
                }
                line = autoplayReader.readLine();
            }

        } catch (IOException e) {
        }

        return autoplayLineList;
    }

    // Ler arquivo .ct
    public static void getColorTableForCTFile(File rootDir, int list_pos, boolean EYEDROP) {
        final String FILE_EXTENSION = ".ct";
        File[] files_list_index =
                rootDir.listFiles(
                        new FileFilter() {
                            @Override
                            public boolean accept(File pathname) {
                                return (pathname.isFile()
                                        && pathname.getName()
                                                .substring(pathname.getName().indexOf("."))
                                                .equals(FILE_EXTENSION));
                            }
                        });
        if (EYEDROP) {
            for (int i = 0; i < VariaveisStaticas.newColorInt.length; i++) {
                VariaveisStaticas.customColorInt.put(i, VariaveisStaticas.newColorInt[i]);
            }
        } else {
            for (int i = 0; i < VariaveisStaticas.colorInt.length; i++) {
                VariaveisStaticas.customColorInt.put(i, VariaveisStaticas.colorInt[i]);
            }
        }
        try {
            BufferedReader ct_file =
                    new BufferedReader(
                            new FileReader(
                                    rootDir.getPath().toString()
                                            + "/"
                                            + files_list_index[list_pos].getName()));
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
                    VariaveisStaticas.customColorInt.put(color_code, Color.rgb(r, g, b));
                }

                line = ct_file.readLine();
            }
            ct_file.close();
            //	Toast.makeText(getApplicationContext(), color_table.values() + "",
            // Toast.LENGTH_SHORT).show();
        } catch (IOException f) {
        }
    }

    public static ArrayAdapter listColorTable(Context context) {
        final String FILE_EXTENSION = ".ct";
        String[] file_ct_name =
                new File(VariaveisStaticas.COLOR_TABLE_PATH)
                        .list(
                                new FilenameFilter() {
                                    @Override
                                    public boolean accept(File dir, String name) {
                                        return (name.substring(name.indexOf("."))
                                                .equals(FILE_EXTENSION));
                                    }
                                });
        if (file_ct_name == null) {
            return null;
        }
        return new ArrayAdapter<String>(context, R.layout.simple_list_item, file_ct_name);
    }
}
