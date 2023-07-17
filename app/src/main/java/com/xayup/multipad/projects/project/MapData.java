package com.xayup.multipad.projects.project;

public interface MapData {
    /* autoplay/led file Frame array index */
    /*Slot index*/
    public final byte FRAME_TYPE = 0;
    public final byte FRAME_VALUE = 1;
    public final byte FRAME_PAD_X = 2;
    public final byte FRAME_PAD_Y = 3;
    byte FRAME_LP_INDEX = 4;
    byte FRAME_AUTOPLAY_DELAY = 4;
    public final byte FRAME_TYPE_CHAIN = 0;
    public final byte FRAME_TYPE_ON = 1;
    public final byte FRAME_TYPE_OFF = 2;
    public final byte FRAME_TYPE_TOUCH = 3;
    public final byte FRAME_TYPE_DELAY = 4;
    public final byte FRAME_TYPE_LOGO = 5;
    public final byte FRAME_LED_TYPE_LOOP = 6;
    public final byte FRAME_TYPE_LED_HEX = 7;
}
