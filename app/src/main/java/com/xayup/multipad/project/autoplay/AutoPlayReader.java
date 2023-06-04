package com.xayup.multipad.project.autoplay;

import android.app.Activity;
import com.google.common.io.Files;
import com.xayup.multipad.load.ProjectMapData;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AutoPlayReader extends ProjectMapData {
    // Algoritimo autoPlay
    private boolean checkAutoPlayFormat(String line) {
        switch (line.toLowerCase().substring(0, 1)) {
            case "d":
                return line.matches("d\\d+");
            case "c":
                return line.matches("c([1-2][0-9]|[1-9])");
            default:
                return line.matches("\\w{1,2}[1-8]{2}");
        }
    }
    /**
     * Read AutoPLay file and return list with possibles errors.
     *
     * @param autoPlay "autoplay" file
     * @param aut_play_map List to be interacted
     * @return List of possible errors
     */
    public List<String[]> read(File autoPlay, List<int[]> auto_play_map) {
        List<String[]> problems = new ArrayList<>();
        try {
            List<String> keys = Files.readLines(autoPlay, StandardCharsets.UTF_8);
            // mAutoPlay = new AutoPlay(context);
            String value;
            int index = 0;
            int[] autoplay_map;
            int line_number = 0;
            next_frame:
            for (String line : keys) {
                line_number++;
                char[] chars = line.toCharArray();
                autoplay_map = new int[2];
                value = "";
                for (int i = 0; i < chars.length; i++) {
                    char c = chars[i];
                    if (Character.isDigit(c)) {
                        value += Character.toString(c);
                    } else {
                        if (Character.toString(c).equals(" ")) {
                            if (index == 0) {
                                switch (value.toLowerCase()) {
                                    case "chain":
                                    case "c":
                                        {
                                            autoplay_map[AUTOPLAY_FRAME_TYPE] = AUTOPLAY_TYPE_CHAIN;
                                            break;
                                        }
                                    case "delay":
                                    case "d":
                                        {
                                            autoplay_map[AUTOPLAY_FRAME_TYPE] = AUTOPLAY_TYPE_DELAY;
                                            break;
                                        }
                                    case "on":
                                    case "o":
                                        {
                                            autoplay_map[AUTOPLAY_FRAME_TYPE] = AUTOPLAY_TYPE_ON;
                                            break;
                                        }
                                    case "off":
                                    case "f":
                                        {
                                            autoplay_map[AUTOPLAY_FRAME_TYPE] = AUTOPLAY_TYPE_OFF;
                                            break;
                                        }
                                    case "touch":
                                    case "t":
                                        {
                                            autoplay_map[AUTOPLAY_FRAME_TYPE] = AUTOPLAY_TYPE_TOUCH;
                                            break;
                                        }
                                    case "logo":
                                    case "l":
                                        {
                                            autoplay_map[AUTOPLAY_FRAME_TYPE] = AUTOPLAY_TYPE_LOGO;
                                            break;
                                        }
                                    default:
                                        {
                                        }
                                }
                                index++;
                            } else {
                                value += Character.toString(c);
                                if (i == chars.length - 1) {
                                    try {
                                        autoplay_map[AUTOPLAY_FRAME_VALUE] =
                                                Integer.parseInt(value);
                                        auto_play_map.add(autoplay_map);
                                    } catch (NumberFormatException n) {
                                        problems.add(
                                                new String[] {
                                                    "AutoPay",
                                                    autoPlay.getName(),
                                                    String.valueOf(line_number),
                                                    keys.get(line_number - 1)
                                                });
                                        continue next_frame;
                                    }
                                }
                            }
                        } else {

                        }
                    }
                }
            }
        } catch (IOException e) {
            problems.add(
                    new String[] {
                        "AutoPay",
                        autoPlay.getName(),
                        "",
                        "File error"
                    });
        }
        return problems;
    }
}
