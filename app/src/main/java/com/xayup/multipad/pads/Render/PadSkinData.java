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
            draw_pad_led,
            draw_chain_led,
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

        draw_chainled = res.getDrawable(res.getIdentifier("chainled", "drawable", skin_package_name), null);
        draw_btn = res.getDrawable(res.getIdentifier("btn", "drawable", skin_package_name), null);
        draw_btn_ = res.getDrawable(res.getIdentifier("btn_", "drawable", skin_package_name), null);

        //// Exclusive ////
        int tmp_id = res.getIdentifier("phantom_xml", "drawable", skin_package_name);
        tmp_id = (tmp_id == 0) ? res.getIdentifier("phantom", "drawable", skin_package_name) : tmp_id;
        draw_phantom = res.getDrawable(tmp_id, null);

        tmp_id = res.getIdentifier("phantom__xml", "drawable", skin_package_name);
        tmp_id = (tmp_id == 0) ? res.getIdentifier("phantom_", "drawable", skin_package_name) : tmp_id;
        draw_phantom_ = res.getDrawable(tmp_id, null);

        tmp_id = res.getIdentifier("led_xml", "drawable", skin_package_name);
        tmp_id = (tmp_id == 0) ? res.getIdentifier("led_old_xml", "drawable", context.getPackageName()) : tmp_id;
        draw_pad_led = res.getDrawable(tmp_id, null);

        tmp_id = res.getIdentifier("chain_led_xml", "drawable", skin_package_name);
        tmp_id = (tmp_id == 0) ? res.getIdentifier("led_old_xml", "drawable", context.getPackageName()) : tmp_id;
        draw_chain_led = res.getDrawable(tmp_id, null);

        tmp_id = res.getIdentifier("playbg_pro", "drawable", skin_package_name);
        draw_playbg = res.getDrawable((tmp_id == 0) ? res.getIdentifier("playbg", "drawable", skin_package_name) : tmp_id, null);

        tmp_id = res.getIdentifier("applogo", "drawable", skin_package_name);
        if(tmp_id == 0){ tmp_id = res.getIdentifier("logo", "drawable", skin_package_name); }
        draw_logo = (tmp_id == 0) ?
                context.getResources().getDrawable(context.getResources().getIdentifier("applogo", "drawable", context.getPackageName()), null) :
                res.getDrawable(tmp_id, null);

        //Color Resource
        tmp_id = res.getIdentifier("autoplay_practical_1", "color", skin_package_name);
        color_autoplay_practical_1 = (tmp_id == 0) ?
                context.getResources().getColor(context.getResources().getIdentifier("autoplay_practical_1", "color", context.getPackageName()), null) :
                res.getColor(tmp_id, null);

        tmp_id = res.getIdentifier("autoplay_practical_2", "color", skin_package_name);
        color_autoplay_practical_2 = (tmp_id == 0) ?
                context.getResources().getColor(context.getResources().getIdentifier("autoplay_practical_2", "color", context.getPackageName()), null) :
                res.getColor(tmp_id, null);
    }
}
