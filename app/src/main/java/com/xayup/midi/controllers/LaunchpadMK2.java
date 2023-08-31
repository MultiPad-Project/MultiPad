package com.xayup.midi.controllers;

import com.xayup.midi.types.Devices;
import com.xayup.midi.types.Devices.GridDeviceConfig;
import com.xayup.midi.types.Devices.KeyType;
import com.xayup.midi.types.Devices.KeyID;

import java.util.Map;

/**
 * Class created based on <a href="https://github.com/Project-Amethyst/amethyst-player/tree/code/src/hardware">this</a> package
 * Thanks <a href="https://github.com/203Null">@203null</a>
 */
public class LaunchpadMK2 {
    public static GridDeviceConfig configs = new GridDeviceConfig() {{
        name = "Launchpad MK2";
        midiNameRegex = "Launchpad MK2";

        paletteChannel = Map.of("classic", 1);

        keymap = new Object[][]{
            {new KeyID(KeyType.CC, 104), new KeyID(KeyType.CC, 105), new KeyID(KeyType.CC, 106), new KeyID(KeyType.CC, 107), new KeyID(KeyType.CC, 108), new KeyID(KeyType.CC, 109), new KeyID(KeyType.CC, 110),new KeyID(KeyType.CC, 111), null},
            {81, 82, 83, 84, 85, 86, 87, 88, 89},
            {71, 72, 73, 74, 75, 76, 77, 78, 79},
            {61, 62, 63, 64, 65, 66, 67, 68, 69},
            {51, 52, 53, 54, 55, 56, 57, 58, 59},
            {41, 42, 43, 44, 45, 46, 47, 48, 49},
            {31, 32, 33, 34, 35, 36, 37, 38, 39},
            {21, 22, 23, 24, 25, 26, 27, 28, 29},
            {11, 12, 13, 14, 15, 16, 17, 18, 19}};

        dimension = new int[]{9, 9};
        gridDimension = new int[]{8, 8};
        gridOffset = new int[]{0, 1};
        chainKey = new int[][]{{8, 0}, {8, 1}, {8, 2}, {8, 3}, {8, 4}, {8, 5}, {8, 6}, {8, 7}};

        noteToXY = (note) ->
        {
            if(note >= 11 && note <= 89)
                return new int[]{(note % 10) - 1 - this.gridOffset[0], (9 - (note / 10)  - this.gridOffset[1])};
            else if(note >= 104 && note <= 111)
                return new int[]{note - 104 - this.gridOffset[0], -this.gridOffset[1]};
            return new int[]{-1, -1};
        };
    }};
}
