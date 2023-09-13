package com.xayup.midi.controllers;

import com.xayup.midi.types.Devices;

import java.util.Map;

/**
 * Class created based on <a href="https://github.com/Project-Amethyst/amethyst-player/tree/code/src/hardware">this</a> package
 * Thanks <a href="https://github.com/203Null">@203null</a>
 */
public class Index {
    public static Map<String, Devices.GridDeviceConfig> launchpads = Map.of(
        Matrix.configs.name, Matrix.configs,
        LaunchpadMK2.configs.name, LaunchpadMK2.configs,
        LaunchpadX.configs.name, LaunchpadX.configs,
        LaunchpadProMK2.configs.name, LaunchpadProMK2.configs,
        LaunchpadProMK2CFW.configs.name, LaunchpadProMK2CFW.configs,
        LaunchpadProMK3.configs.name, LaunchpadProMK3.configs,
        LaunchpadMiniMK3.configs.name, LaunchpadMiniMK3.configs,
        MidiFighter64.configs.name, MidiFighter64.configs
    );
}
