package com.xayup.midi.controllers;

import com.xayup.midi.types.Devices;

import java.util.List;
import java.util.Map;

/**
 * Class created based on <a href="https://github.com/Project-Amethyst/amethyst-player/tree/code/src/hardware">this</a> package
 * Thanks <a href="https://github.com/203Null">@203null</a>
 */
public class MidiKeyboard {
    public static int[] map = new int[]{
            81, 82, 83, 84, 71, 72, 73, 74,
            61, 62, 63, 64, 51, 52, 53, 54,
            41, 42, 43, 44, 31, 32, 33, 34,
            21, 22, 23, 24, 11, 12, 13, 14,
            85, 86, 87, 88, 75, 76, 77, 78,
            65, 66, 67, 68, 55, 56, 57, 58,
            45, 46, 47, 48, 35, 36, 37, 38,
            25, 26, 27, 28, 15, 16, 17, 18,
            19, 29, 39, 49, 59, 69, 79, 89, //Left chains
            1, 2, 3, 4, 5, 6, 7, 8 //Top chains
    };
    public static Devices.GridDeviceConfig configs = new Devices.GridDeviceConfig(){{
        name = "Midi Keyboard";
        midiNameRegex = "MidiKeyboard";

        paletteChannel = Map.of("classic", 1);

        keymap = new Object[][]{
            {null, 108, 109, 110, 111, 112, 113, 114, 115, null},
            {null, 64, 65, 66, 67, 96, 97, 98, 99, 100},
            {null, 60, 61, 62, 63, 92, 93, 94, 95, 101},
            {null, 56, 57, 58, 59, 88 ,89, 90, 91, 102},
            {null, 52, 53, 54, 55, 84 ,85, 86, 87, 103},
            {null, 48, 49, 50, 51, 80 ,81, 82, 83, 104},
            {null, 44, 45, 46, 47, 76, 77, 78, 79, 105},
            {null, 40, 41, 42, 43, 72, 73, 74, 75, 106},
            {null, 36, 37, 38, 39, 68, 69, 70, 71, 107},
            {null, null, null, null, null, null, null, null, null, null}
        };

        dimension = new int[]{0, 0};
        gridDimension = new int[]{0, 0};
        gridOffset = new int[]{36, 115};
        noteToXY = (note) -> {
            if(note >= 36 && note <= 115){
                int xy = map[note - gridOffset[0]];
                return new int[]{xy/10, xy%10};
            }
            return new int[]{-1, -1};
        };
    }};
}
