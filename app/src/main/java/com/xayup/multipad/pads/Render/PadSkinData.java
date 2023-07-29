package com.xayup.multipad.pads.Render;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import android.graphics.drawable.Drawable;
import com.xayup.multipad.R;
import com.xayup.multipad.skin.SkinData;

public class PadSkinData implements SkinData {
    public int color_autoplay_practical_1,
            color_autoplay_practical_2,
            draw_btn__color;
    public Drawable draw_phantom_,
            draw_phantom,
            draw_phantom_led,
            draw_phantom__led,
            draw_chain_led,
            draw_chainled,
            draw_btn,
            draw_btn_,
            draw_playbg,
            draw_logo,
            draw_logo_led;
    
    @SuppressLint("DiscouragedApi")
    @Override
    public void loadFromResources(Context context, String skin_package_name){
        /*Get Resource from skin_package_name*/
        Resources res;
        try { res = context.getPackageManager().getResourcesForApplication(skin_package_name); }
        catch (PackageManager.NameNotFoundException n){ res = context.getResources(); }

        draw_chainled = res.getDrawable(res.getIdentifier("chainled", "drawable", skin_package_name), null);
        draw_btn = res.getDrawable(res.getIdentifier("btn", "drawable", skin_package_name), null);

        //// Exclusive ////
        int tmp_id = res.getIdentifier("phantom_xml", "drawable", skin_package_name);
        tmp_id = (tmp_id == 0) ? res.getIdentifier("phantom", "drawable", skin_package_name) : tmp_id;
        draw_phantom = res.getDrawable(tmp_id, null);

        tmp_id = res.getIdentifier("phantom__xml", "drawable", skin_package_name);
        tmp_id = (tmp_id == 0) ? res.getIdentifier("phantom_", "drawable", skin_package_name) : tmp_id;
        draw_phantom_ = res.getDrawable(tmp_id, null);

        tmp_id = res.getIdentifier("phantom_led_xml", "drawable", skin_package_name);
        draw_phantom_led = (tmp_id == 0) ? context.getDrawable(R.drawable.led_old_xml) :
                res.getDrawable(tmp_id, null);

        tmp_id = res.getIdentifier("phantom__led_xml", "drawable", skin_package_name);
        draw_phantom__led = (tmp_id == 0) ? context.getDrawable(R.drawable.led_old_xml):
                res.getDrawable(tmp_id, null);

        tmp_id = res.getIdentifier("chain_led_xml", "drawable", skin_package_name);
        draw_chain_led = (tmp_id == 0) ? context.getDrawable(R.drawable.led_old_xml):
                res.getDrawable(tmp_id, null);

        tmp_id = res.getIdentifier("playbg_pro", "drawable", skin_package_name);
        draw_playbg = res.getDrawable((tmp_id == 0) ? res.getIdentifier("playbg", "drawable", skin_package_name) : tmp_id, null);

        tmp_id = res.getIdentifier("applogo", "drawable", skin_package_name);
        if(tmp_id == 0){ tmp_id = res.getIdentifier("logo", "drawable", skin_package_name); }
        draw_logo = (tmp_id == 0) ?
                context.getDrawable(R.drawable.applogo) :
                res.getDrawable(tmp_id, null);

        tmp_id = res.getIdentifier("applogo_led_xml", "drawable", skin_package_name);
        draw_logo_led = (tmp_id == 0) ? context.getDrawable(R.drawable.led_old_xml) :
                res.getDrawable(tmp_id, null);

        if ((tmp_id = res.getIdentifier("btn__color", "color", skin_package_name)) != 0){
            draw_btn__color = res.getColor(tmp_id, null);
            draw_btn_ = null;
        } else if((tmp_id = res.getIdentifier("btn_", "drawable", skin_package_name)) != 0){
            draw_btn_ = res.getDrawable(tmp_id, null);
            draw_btn__color = -1;
        } else {
            draw_btn__color = context.getColor(R.color.btn__color);
            draw_btn_ = null;
        }

        //Color Resource
        tmp_id = res.getIdentifier("autoplay_practical_1", "color", skin_package_name);
        color_autoplay_practical_1 = (tmp_id == 0) ?
                context.getColor(R.color.autoplay_practical_1) :
                res.getColor(tmp_id, null);

        tmp_id = res.getIdentifier("autoplay_practical_2", "color", skin_package_name);
        color_autoplay_practical_2 = (tmp_id == 0) ?
                context.getColor(R.color.autoplay_practical_2) :
                res.getColor(tmp_id, null);
    }
}
