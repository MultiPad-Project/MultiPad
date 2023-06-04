package com.xayup.multipad.project.keyled;
import com.xayup.multipad.project.keyled.KeyLEDReader;
import java.util.Arrays;
import java.util.Map;

public class LedMap implements KeyLEDReader.KeyLEDMap {
    protected int led_count = 0;
    protected int[/*chain*/][/*pad*/][/*sequence*/][/*frames*/][/*TYPE, VALUE, PAD_ID*/] led_map;
    public LedMap(){
        led_map = new int[24][96]/*Dynamic add with put()*/[0][0][0];
    }
    @Override
    public void putFrame(int chain, int pad, int sequence, int[/*TYPE, VALUE, PAD_ID*/] led_frame){
        int[][] tmp = led_map[chain][pad][sequence];
        tmp = Arrays.copyOf(tmp, tmp.length-1);
        tmp[tmp.length-1] = led_frame;
        led_map[chain][pad][sequence] = tmp;
    }
    @Override
    public void putSequence(int chain, int pad, int[][/*TYPE, VALUE, PAD_ID*/] led_frames_sequence){
        int[][][] tmp = led_map[chain][pad];
        tmp = Arrays.copyOf(tmp, tmp.length+1);
        tmp[tmp.length-1] = led_frames_sequence;
        led_map[chain][pad] = tmp;
        led_count++;
    }
    @Override
    public int[/*FRAMES*/][/*TYPE, VALUE, PAD_ID*/] getLedData(int chain, int pad, int sequence){
        return led_map[chain][pad][sequence];
    }
    @Override
    public int framesCount(int chain, int pad, int sequence){
        return led_map[chain][pad][sequence].length;
    }
    @Override
    public int sequenceCount(int chain, int pad){
        return led_map[chain][pad].length;
    }
    @Override
    public int ledsCount(){
        return led_count;
    }
}
