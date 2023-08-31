package com.xayup.midi.controllers;

import com.xayup.midi.types.Devices;
import com.xayup.midi.types.Devices.KeyID;

import java.util.Map;

/**
 * Class created based on <a href="https://github.com/Project-Amethyst/amethyst-player/tree/code/src/hardware">this</a> package
 * Thanks <a href="https://github.com/203Null">@203null</a>
 */
public class LaunchpadProMK2CFW {
    public static Devices.GridDeviceConfig configs = new Devices.GridDeviceConfig(){{
        name = "Launchpad Pro MK2 (CFW)";
        midiNameRegex = "Launchpad Open";

        paletteChannel = Map.of("classic", 16);

        keymap = new Object[][]{
        {null, 28, 29, 30, 31, 32, 33, 34, 35, 27},
        {108, 64, 65, 66, 67, 96, 97, 98, 99, 100},
        {109, 60, 61, 62, 63, 92, 93, 94, 95, 101},
        {110, 56, 57, 58, 59, 88, 89, 90, 91, 102},
        {111, 52, 53, 54, 55, 84, 85, 86, 87, 103},
        {112, 48, 49, 50, 51, 80, 81, 82, 83, 104},
        {113, 44, 45, 46, 47, 76, 77, 78, 79, 105},
        {114, 40, 41, 42, 43, 72, 73, 74, 75, 106},
        {115, 36, 37, 38, 39, 68, 69, 70, 71, 107},
        {null, 116, 117, 118, 119, 120, 121, 122, null}
      };

        dimension = new int[]{10, 10};
        gridDimension = new int[]{8, 8};
        gridOffset = new int[]{1, 1};
        chainKey = new int[][]{{8, 0}, {8, 1}, {8, 2}, {8, 3}, {8, 4}, {8, 5}, {8, 6}, {8, 7},
                {-1, 7}, {-1, 6}, {-1, 5}, {-1, 4}, {-1, 3}, {-1, 2}, {-1, 1}, {-1, 0}};

        noteToXY = (note) -> {
            if (note >= 36 && note < 100) {
                return new int[][]{{0,7},{1,7},{2,7},{3,7},{0,6},{1,6},{2,6},{3,6},{0,5},{1,5},{2,5},{3,5},{0,4},{1,4},{2,4},{3,4},{0,3},{1,3},{2,3},{3,3},{0,2},{1,2},{2,2},{3,2},{0,1},{1,1},{2,1},{3,1},{0,0},{1,0},{2,0},{3,0},{4,7},{5,7},{6,7},{7,7},{4,6},{5,6},{6,6},{7,6},{4,5},{5,5},{6,5},{7,5},{4,4},{5,4},{6,4},{7,4},{4,3},{5,3},{6,3},{7,3},{4,2},{5,2},{6,2},{7,2},{4,1},{5,1},{6,1},{7,1},{4,0},{5,0},{6,0},{7,0}}
                        [note - 36];
            }
            else if (note >= 28 && note <= 35)
            {
                return new int[]{note - 28, -1};
            }
            else if (note >= 100 && note < 108)
            {
                return new int[]{8, note - 100};
            }
            else if (note >= 116 && note <= 123)
                return new int[]{note - 116, 8};
            else if (note >= 108 && note < 116)
                return new int[]{-1, note - 108};

            return new int[]{-1, -1};
        };

        specialLED = new KeyID(8, -1);

        initializationSysex = new byte[][]{
            {0, 32, 41, 2, 16, 33, 1}, //Enter Performance Mode
            {0, 32, 41, 2, 16, 14, 0}, //Clear canvast
        };
    }};
}
