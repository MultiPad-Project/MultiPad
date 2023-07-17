package com.xayup.multipad.projects;
import java.util.List;

public class ProjectMapData {
    protected List<String[/*Process, File, line_number, line*/]> problems;
    /*Gets the store of how many times the same pad in the current chain has been clicked*/
    public final int SEQUENCE = 0;
    /*Identifier to add to or get a list of led sequences for the current pad and chain*/
    public final int DATA = 1;
    
    /* LEDs Map */
    public final byte LED_FRAME_PAD_ID = 0;
    public final byte LED_FRAME_COLOR = 1;
    
    /* AutoPlay Map */
    public final byte AUTOPLAY_TYPE_CHAIN = 0; 
    public final byte AUTOPLAY_TYPE_ON = 1; 
    public final byte AUTOPLAY_TYPE_OFF = 2; 
    public final byte AUTOPLAY_TYPE_TOUCH = 3;
    public final byte AUTOPLAY_TYPE_DELAY = 4; 
    public final byte AUTOPLAY_TYPE_LOGO = 5; 
    public final byte AUTOPLAY_FRAME_TYPE = 0; // 
    public final byte AUTOPLAY_FRAME_VALUE = 1; // Pad id or ms delay
    
    
    /*Calculator to define the next sequence of leds, that is, if there is another
    * led file after this one then increment it so that the next led is reproduced in the future*/
    public int getSequence(int current, int max){
        return (current >= max) ? 0 : current+1;
    }
}
