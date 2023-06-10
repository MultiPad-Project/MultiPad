package com.xayup.multipad.project.autoplay;

import android.app.Activity;
import com.google.common.io.Files;
import com.xayup.multipad.load.ProjectMapData;
import com.xayup.multipad.load.thread.LoadProject;
import com.xayup.multipad.project.MapData;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AutoPlayReader implements MapData {
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
    public void read(
            File autoPlay, List<int[]> auto_play_map, LoadProject.LoadingProject mLoadingProject) {
        try {
            List<String> keys = Files.readLines(autoPlay, StandardCharsets.UTF_8);
            mLoadingProject.onStartReadFile(autoPlay.getName());
            next_line:
            while (!keys.isEmpty()) {
                char[] chars = keys.remove(0).toCharArray();
                if (chars.length > 1) {
                    int[] autoplay_map = new int[4];
                    next_char:
                    switch (Character.toString(chars[0])) {
                        case "delay":
                        case "d":
                            {
                                autoplay_map[FRAME_TYPE] = FRAME_TYPE_DELAY;
                                autoplay_map[FRAME_VALUE] = Character.getNumericValue(chars[1]);
                                auto_play_map.add(autoplay_map);
                                continue next_line;
                            }
                        case "chain":
                        case "c":
                            {
                                autoplay_map[FRAME_TYPE] = FRAME_TYPE_CHAIN;
                                break;
                            }
                        case "on":
                        case "o":
                            {
                                autoplay_map[FRAME_TYPE] = FRAME_TYPE_ON;
                                break;
                            }
                        case "off":
                        case "f":
                            {
                                autoplay_map[FRAME_TYPE] = FRAME_TYPE_OFF;
                                break;
                            }
                        case "touch":
                        case "t":
                            {
                                autoplay_map[FRAME_TYPE] = FRAME_TYPE_TOUCH;
                                break;
                            }
                    }
                    autoplay_map[FRAME_PAD_X] = Character.getNumericValue(chars[1]);
                    autoplay_map[FRAME_PAD_Y] = Character.getNumericValue(chars[2]);
                    auto_play_map.add(autoplay_map);
                }
            }
        } catch (IOException e) {
            mLoadingProject.onFileError(autoPlay.getName(), 0, "Corrupted");
        }
    }
}
