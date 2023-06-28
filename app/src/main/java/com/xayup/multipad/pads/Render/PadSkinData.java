package com.xayup.multipad.pads.Render;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import android.graphics.drawable.Drawable;
import com.xayup.multipad.skin.SkinData;

public class PadSkinData implements SkinData {
    public int color_autoplay_practical_1,
            color_autoplay_practical_2;
    public Drawable draw_phantom_,
            draw_phantom,
            draw_chainled,
            draw_btn,
            draw_btn_,
            draw_playbg,
            draw_logo;
    
    @Override
    public void loadFromResources(Context context, String skin_package_name){
        /*Get Resource from skin_package_name*/
        Resources res;
        try { res = context.getPackageManager().getResourcesForApplication(skin_package_name); }
        catch (PackageManager.NameNotFoundException n){ res = context.getResources(); }

        draw_phantom = res.getDrawable(res.getIdentifier("phantom", "drawable", skin_package_name), null);
        draw_phantom_ = res.getDrawable(res.getIdentifier("phantom_", "drawable", skin_package_name), null);
        draw_chainled = res.getDrawable(res.getIdentifier("chainled", "drawable", skin_package_name), null);
        draw_btn = res.getDrawable(res.getIdentifier("btn", "drawable", skin_package_name), null);
        draw_btn_ = res.getDrawable(res.getIdentifier("btn_", "drawable", skin_package_name), null);

        /* Exclusive */
        int playbg_id = res.getIdentifier("playbg_pro", "drawable", skin_package_name);
            draw_playbg = res.getDrawable((playbg_id == 0) ? res.getIdentifier("playbg", "drawable", skin_package_name) : playbg_id, null);
        int applogo_id = res.getIdentifier("applogo", "drawable", skin_package_name);
            draw_logo = res.getDrawable((applogo_id == 0) ? res.getIdentifier("logo", "drawable", skin_package_name): applogo_id, null);
        //Color Resource
        int color_autoplay_practical_1_id = res.getIdentifier("autoplay_practical_1", "color", skin_package_name);
            color_autoplay_practical_1 = res.getColor((color_autoplay_practical_1_id == 0) ? res.getIdentifier("autoplay_practical_1", "color", context.getPackageName()) : color_autoplay_practical_1_id, null);
        int color_autoplay_practical_2_id = res.getIdentifier("autoplay_practical_2", "color", skin_package_name);
            color_autoplay_practical_2 = res.getColor((color_autoplay_practical_2_id == 0) ? res.getIdentifier("autoplay_practical_2", "color", context.getPackageName()) : color_autoplay_practical_2_id, null);
    }
}
