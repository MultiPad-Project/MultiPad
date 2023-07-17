package com.xayup.multipad.projects.project.autoplay;

import com.google.common.io.Files;
import com.xayup.multipad.projects.thread.LoadProject;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.projects.project.MapData;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AutoPlayReader implements MapData {
    // Algoritimo autoPlay
    /**
     * Read AutoPLay file and return list with possibles errors.
     *
     * @param autoPlay "autoplay" file
     * @param auto_play_map List to be interacted. Essa lista conterá arrays de 4 slots, sendo:
     *                      index 0 (slot 1), o tipo. Nunca será um delay pois o delay estará no index 4 (slot 5)
     *                      index 1 (slot 2), a chain em que esse frame deverá executado.
     *                      index 2 (slot 3), a localização Y (linha).
     *                      index 3 (slot 4), a localização X (Coluna).
     *                      index 4 (slot 5), o dalay antes de executar a frame.
     * @param mLoadingProject .
     * @return List of possible errors
     */
    public void read(
            File autoPlay, List<int[]> auto_play_map, LoadProject.LoadingProject mLoadingProject) {
        try {
            List<String> keys = Files.readLines(autoPlay, StandardCharsets.UTF_8);
            mLoadingProject.onStartReadFile(autoPlay.getName());
            int chain_mc = 1;
            int delay = 0;
            next_line:
            while (!keys.isEmpty()) {
                String[] chars = keys.remove(0).split("\\s");
                if (chars.length > 1) {
                    int[] autoplay_map = new int[5];
                    switch (chars[0]) {
                        case "delay":
                        case "d":
                            {
                                delay += Integer.parseInt(chars[1]);
                                continue;
                            }
                        case "chain":
                        case "c":
                            {
                                autoplay_map[FRAME_TYPE] = FRAME_TYPE_CHAIN;
                                autoplay_map[FRAME_VALUE] = chain_mc;
                                int[] xy = MakePads.PadID.getChainXY(chain_mc = Integer.parseInt(chars[1]), 9);
                                autoplay_map[FRAME_PAD_X] = xy[0];
                                autoplay_map[FRAME_PAD_Y] = xy[1];
                                autoplay_map[FRAME_AUTOPLAY_DELAY] = delay;
                                auto_play_map.add(autoplay_map);
                                continue;
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
                        default: {
                            continue;
                        }
                    }
                    autoplay_map[FRAME_VALUE] = chain_mc;
                    autoplay_map[FRAME_PAD_X] = Integer.parseInt(chars[1]);
                    autoplay_map[FRAME_PAD_Y] = Integer.parseInt(chars[2]);
                    autoplay_map[FRAME_AUTOPLAY_DELAY] = delay;
                    auto_play_map.add(autoplay_map);
                    delay = 0;
                }
            }
        } catch (IOException e) {
            mLoadingProject.onFileError(autoPlay.getName(), 0, "Corrupted");
        }
    }
}
