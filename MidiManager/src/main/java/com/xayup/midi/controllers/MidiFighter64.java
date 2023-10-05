package com.xayup.midi.controllers;

import com.xayup.midi.types.Devices;

import java.util.Map;

/**
 * Class created based on <a href="https://github.com/Project-Amethyst/amethyst-player/tree/code/src/hardware">this</a> package
 * Thanks <a href="https://github.com/203Null">@203null</a>
 */
public class MidiFighter64 {
    public static Devices.GridDeviceConfig configs = new Devices.GridDeviceConfig(){{
        name = "Midi Fighter 64";
        midiNameRegex = "^Midi Fighter 64";

        paletteChannel = Map.of("classic", 1);

        keymap = new Object[][]{
            {null, null, null, null, null, null, null, null, null, null},
            {null, 64, 65, 66, 67, 96, 97, 98, 99, null},
            {null, 60, 61, 62, 63, 92, 93, 94, 95, null},
            {null, 56, 57, 58, 59, 88, 89, 90, 91, null},
            {null, 52, 53, 54, 55, 84, 85, 86, 87, null},
            {null, 48, 49, 50, 51, 80, 81, 82, 83, null},
            {null, 44, 45, 46, 47, 76, 77, 78, 79, null},
            {null, 40, 41, 42, 43, 72, 73, 74, 75, null},
            {null, 36, 37, 38, 39, 68, 69, 70, 71, null},
            {null, null, null, null, null, null, null, null, null, null}
        };

        dimension = new int[]{8, 8};
        gridDimension = new int[]{8, 8};
        gridOffset = new int[]{0, 0};
        chainKey = new int[][]{};


        noteToXY = (note) ->
        {
            if(note > 35 && note < 100) // grid
            {
                return new int[][]{{0,7},{1,7},{2,7},{3,7},{0,6},{1,6},{2,6},{3,6},{0,5},{1,5},{2,5},{3,5},{0,4},{1,4},{2,4},{3,4},{0,3},{1,3},{2,3},{3,3},{0,2},{1,2},{2,2},{3,2},{0,1},{1,1},{2,1},{3,1},{0,0},{1,0},{2,0},{3,0},{4,7},{5,7},{6,7},{7,7},{4,6},{5,6},{6,6},{7,6},{4,5},{5,5},{6,5},{7,5},{4,4},{5,4},{6,4},{7,4},{4,3},{5,3},{6,3},{7,3},{4,2},{5,2},{6,2},{7,2},{4,1},{5,1},{6,1},{7,1},{4,0},{5,0},{6,0},{7,0}}
                    [note - 36];
            }
            return new int[]{-1, -1};
        };
    }};
}
