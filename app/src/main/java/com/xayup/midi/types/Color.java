package com.xayup.midi.types;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import static com.xayup.midi.types.Palette.palettes;

public class Color {
    public final byte type;
    public final Object[] value;

    @IntDef({ColorType.RGB, ColorType.PALETTE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ColorType{
        byte RGB = 1;
        byte PALETTE = 2;
    }
    public Color(@ColorType byte type, int[] value){
        this.type = type;
        this.value = new Object[value.length];
        for(int i = 0; i < value.length; i++) this.value[i] = value[i];
    }
    public Color(@ColorType byte type, Object[] value){
        this.type = type;
        this.value = value;
    }

    public int[] rgb(){
        if(this.value != null)
            if (this.type == ColorType.PALETTE)
                if (palettes.get((String) this.value[0]) != null)
                    return palettes.get((String) this.value[0])[(int) this.value[1]];
            else return new int[]{(int) this.value[0], (int) this.value[1], (int) this.value[2]};
        return new int[3];
    }

    public String[] rgb_str()
    {
        int[] rgb = this.rgb();
        return new String[]{
                String.valueOf(rgb[0]),
                String.valueOf(rgb[1]),
                String.valueOf(rgb[2])
        };
    }

    public Color overlay(Color base)
    {
        int[] self_rgb = this.rgb();
        int[] base_rgb = base.rgb();

        int[] rgb = new int[]{
                base_rgb[0] + (255 - base_rgb[0]) * (self_rgb[0] / 255),
                base_rgb[1] + (255 - base_rgb[1]) * (self_rgb[1] / 255),
                base_rgb[2] + (255 - base_rgb[2]) * (self_rgb[2] / 255)};

        return new Color(ColorType.RGB, rgb);
    }

    public String palette()
    {
        if(this.type == ColorType.PALETTE) {
            return String.valueOf(this.value[0]);
        }
        return null;
    }

    public int index()
    {
        if(this.type == ColorType.PALETTE)
        {
            return (int) this.value[1];
        }
        return -1;
    }

    public boolean isBlack() {
        int[] rgb = this.rgb();
        return !(rgb[0] > 0 && rgb[1] > 0 && rgb[2] > 0);
    }
}
