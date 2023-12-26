package com.xayup.midi.controllers;

import com.xayup.midi.types.Devices;

import java.util.Map;

/**
 * Class created based on <a href="https://github.com/Project-Amethyst/amethyst-player/tree/code/src/hardware">this</a> package
 * Thanks <a href="https://github.com/203Null">@203null</a>
 */
public class LaunchpadMiniMK3 extends Devices.GridDeviceConfig {
   public static Devices.GridDeviceConfig configs = new Devices.GridDeviceConfig(){{
        name = "Launchpad Mini MK3";
        midiNameRegex = "Launchpad Mini|LPMiniMK3 MIDI";

        paletteChannel = Map.of("classic", 1);

        keymap = new Object[][]{
            {null, 91, 92, 93, 94, 95, 96, 97, 98, 99, null},
            {null, 81, 82, 83, 84, 85, 86, 87, 88, 89},
            {null, 71, 72, 73, 74, 75, 76, 77, 78, 79},
            {null, 61, 62, 63, 64, 65, 66, 67, 68, 69},
            {null, 51, 52, 53, 54, 55, 56, 57, 58, 59},
            {null, 41, 42, 43, 44, 45, 46, 47, 48, 49},
            {null, 31, 32, 33, 34, 35, 36, 37, 38, 39},
            {null, 21, 22, 23, 24, 25, 26, 27, 28, 29},
            {null, 11, 12, 13, 14, 15, 16, 17, 18, 19},
            {null, null, null, null, null, null, null, null, null, null}
        };

        dimension = new int[]{9, 9};
        gridDimension = new int[]{8, 8};
        //gridOffset = new int[]{0, 1};
        gridOffset = new int[]{-1, 0};
        chainKey = new int[][]{{8, 0}, {8, 1}, {8, 2}, {8, 3}, {8, 4}, {8, 5}, {8, 6}, {8, 7}};

        noteToXY = (int note) ->
        {
            if(note >= 1 && note <= 99 && note != 9 && note != 90)
                return new int[]{9 - (note / 10) - this.gridOffset[1], (note % 10 - 1 - this.gridOffset[0])};
            return new int[]{-1, -1};
        };
        specialLED = new Devices.KeyID(8, -1);
        initializationSysex = new byte[][]{{0, 32, 41, 2, 13, 14, 1}}; //Enter Programmer Mode
    }};
}
