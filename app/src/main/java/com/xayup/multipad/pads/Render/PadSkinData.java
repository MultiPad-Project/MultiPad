package com.xayup.multipad.pads.Render;

import android.content.res.Resources;

import android.graphics.drawable.Drawable;
import com.xayup.multipad.skin.SkinData;

public class PadSkinData implements SkinData {
    public Drawable draw_phantom_,
            draw_phantom,
            draw_chainled,
            draw_btn,
            draw_btn_,
            draw_playbg,
            draw_logo;
    
    @Override
    public void loadFromResources(Resources res, String skin_package_name){
        draw_phantom = res.getDrawable(res.getIdentifier("phantom", "drawable", skin_package_name), null);
        draw_phantom_ = res.getDrawable(res.getIdentifier("phantom_", "drawable", skin_package_name), null);
        draw_chainled = res.getDrawable(res.getIdentifier("chainled", "drawable", skin_package_name), null);
        draw_btn = res.getDrawable(res.getIdentifier("btn", "drawable", skin_package_name), null);
        draw_btn_ = res.getDrawable(res.getIdentifier("btn_", "drawable", skin_package_name), null);
        int playbg_id = res.getIdentifier("playbg", "drawable", skin_package_name);
        draw_playbg = res.getDrawable((playbg_id == 0) ? res.getIdentifier("playbg", "drawable", skin_package_name) : playbg_id, null);
        draw_logo = res.getDrawable(res.getIdentifier("applogo", "drawable", skin_package_name), null);
    }
}
