package com.xayup.multipad.project.keyled;

import com.xayup.multipad.project.MapData;

import java.util.Arrays;

public class KeyLEDData implements MapData {
    boolean looper;
    protected int[/*FRAMES*/][/*TYPE, VALUE, PAD_X, PAD_Y*/] frames;
    public KeyLEDData(){}
    public void setTypeLoop(boolean is_loop){
        looper = is_loop;
    }

    public boolean isLooper(){
        return looper;
    }

    public int[][] getFrames(){
        return frames;
    }

    public void putFrame(int type, int value, int pad_row , int pad_colum){
        frames = Arrays.copyOf(frames, frames.length+1);
        if(type == FRAME_TYPE_DELAY){
            frames[frames.length-1] = new int[]{type, value};
        } else {
            frames[frames.length-1] = new int[]{type, value, pad_row, pad_colum};
        }
    }

    public int length(){
        return frames.length;
    }
}
