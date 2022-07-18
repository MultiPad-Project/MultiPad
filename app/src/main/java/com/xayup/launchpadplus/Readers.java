package com.xayup.launchpadplus;

import java.io.*;
import java.util.*;

import android.app.*;
import android.net.*;

public class Readers {
    FileFilter filterFolder = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.isDirectory();
        }
    };
    public FileFilter filterFiles = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.isFile();
        }

    };

    public Map<String, Map> readInfo(File projectDir, boolean granted) {
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
                        infoInfo.put("title", title.replaceFirst(title.substring(0, title.indexOf("=") + 1), "").trim());
                        infoInfo.put("producerName", producerName.replaceFirst(producerName.substring(0, producerName.indexOf("=") + 1), "").trim());
                    } else {
                        infoInfo.put("title", "Aquivo 'Info' não existe");
                        infoInfo.put("producerName", "Projeto incompleto");
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

    public boolean checkKeySound(String line) {

       return line.matches("[1-8]{3}\\S+.\\w{3}");
    }

    public Map<String, Map<String, List<Uri>>> readKeySounds(File keySound, String soundPath, Activity act) {
        String chain = "";
        String id = "";
        String soundName = "";
        Map<String, Map<String, List<Uri>>> chainMap = new HashMap<String, Map<String, List<Uri>>>();
        Map<String, List<Uri>> soundMap = new HashMap<String, List<Uri>>();
        int rpt = 1;
        //MediaPlayer wavv;
        if (keySound.exists()) {
            try {
                List<String> invalid_format = new ArrayList<String>();
                BufferedReader bufferInfo = new BufferedReader(new FileReader(keySound));
                String line = bufferInfo.readLine();
                while (line != null) {
                    if (!line.replace(" ", "").isEmpty()) {
                        while(!Character.isLetter(line.charAt(line.length()-1))){
                            line = line.substring(0, line.length()-1);
                        }
                        if (checkKeySound(line.replace(" ", ""))) {
                            line = line.replaceAll(" ", "");
                            chain = line.substring(0, 1);
                            if (chainMap.size() != Integer.parseInt(chain)) {
                                soundMap = new HashMap<String, List<Uri>>();
                            }

                            id = line.substring(1, 3);
                            soundName = line.substring(3);
                            Uri som = Uri.fromFile(new File(soundPath + "/" + soundName));
                            if(soundMap.get(id) != null){
                                soundMap.get(id).add(som);
                            } else{
                                List<Uri> uri = new ArrayList<>();
                                uri.add(som);
                                soundMap.put(id, uri);
                            }
                            chainMap.put(chain, soundMap);
                            rpt = 1;
                        } else {
                            invalid_format.add("KeySound inválido: " + line);
                        }
                    }
                    line = bufferInfo.readLine();
                }
                bufferInfo.close();
                if(!invalid_format.isEmpty()){
                    playPads.invalid_formats = invalid_format;
                }
            } catch (IOException e) {
            }
        }
        return chainMap;
    }

    private boolean checkAutoPlayFormat(String line){
        switch (line.toLowerCase().substring(0, 1)){
           case "d":
                return line.matches("\\w+\\d+");
           case "c":
               line = line.replace("chain", "c");
               return line.matches("c[1-8]");
           default:
                return line.matches("\\w{1,2}[1-8]{2}");
        }
    }

    public List<String> readautoPlay(File autoPlay) {
        List<String> autoplayLineList = new ArrayList<String>();
        List<String> invalid_format = new ArrayList<String>();
        try {
            BufferedReader autoplayReader = new BufferedReader(new FileReader(autoPlay));
            String line = autoplayReader.readLine();
            while (line != null) {
                if(!line.replace(" ", "").isEmpty()){
                    if (checkAutoPlayFormat(line.replace(" ", ""))) {
                        autoplayLineList.add(line);
                    }
                    else {
                        playPads.invalid_formats.add("autoPlay Inválido: " + line);
                    }
                }
                line = autoplayReader.readLine();
            }

        } catch (IOException e) {
        }

        return autoplayLineList;
    }

}
