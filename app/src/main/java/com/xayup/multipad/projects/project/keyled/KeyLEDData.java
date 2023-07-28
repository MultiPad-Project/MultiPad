package com.xayup.multipad.projects.project.keyled;

import com.xayup.multipad.projects.project.MapData;

import java.util.Arrays;

public class KeyLEDData implements MapData {
    protected boolean looper;
    protected int[/*FRAMES*/][/*TYPE, VALUE, PAD_X, PAD_Y, LP_INDEX*/] frames;
    public KeyLEDData(){
        frames = new int[0][0];
    }
    public void setTypeLoop(boolean is_loop){
        looper = is_loop;
    }

    public boolean isLooper(){
        return looper;
    }

    public int[][] getFrames(){
        return frames;
    }

    public void putFrame(int type, int value, int pad_row , int pad_colum, int lp_index) throws NumberFormatException {
        /* Throw */
        if((pad_row & pad_colum) > 10 || (pad_row & pad_colum) < 0) throw new NumberFormatException("Invalid row or colum number: Row: " + pad_row + ", Colum: " + pad_colum);
        else if (type != FRAME_TYPE_DELAY && !(value < 0)){
            if (value > 127) throw new NumberFormatException("Invalid value number: " + value);
        }
        /* Add frame */
        frames = Arrays.copyOf(frames, frames.length+1);
        if(type == FRAME_TYPE_DELAY) frames[frames.length-1] = new int[]{type, value};
        else frames[frames.length-1] = new int[]{type, value, pad_row, pad_colum, lp_index};
    }

    public int length(){ return frames.length; }
}
