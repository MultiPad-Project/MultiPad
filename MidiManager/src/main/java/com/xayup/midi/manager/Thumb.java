package com.xayup.midi.manager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.res.ResourcesCompat;
import com.xayup.midi.R;
import com.xayup.midi.controllers.*;

public class Thumb {
    public static Drawable getThumbFromProduct(Context context, String product_name){
        if(product_name.equals(LaunchpadMiniMK3.configs.name)) return ResourcesCompat.getDrawable(context.getResources(), R.drawable.lp_pro_mk3, context.getTheme());
        else if (product_name.equals(LaunchpadMK2.configs.name)) return ResourcesCompat.getDrawable(context.getResources(), R.drawable.lp_mini_mk2, context.getTheme());
        else if (product_name.equals(LaunchpadProMK2.configs.name)) return ResourcesCompat.getDrawable(context.getResources(), R.drawable.lp_pro_mk2, context.getTheme());
        else if (product_name.equals(LaunchpadProMK2CFW.configs.name)) return ResourcesCompat.getDrawable(context.getResources(), R.drawable.lp_pro_mk2, context.getTheme());
        else if (product_name.equals(LaunchpadProMK3.configs.name)) return ResourcesCompat.getDrawable(context.getResources(), R.drawable.lp_pro_mk3, context.getTheme());
        else if (product_name.equals(LaunchpadX.configs.name)) return ResourcesCompat.getDrawable(context.getResources(), R.drawable.lp_x, context.getTheme());
        else if (product_name.equals(Matrix.configs.name)) return ResourcesCompat.getDrawable(context.getResources(), R.drawable.matrix, context.getTheme());
        else if (product_name.equals(MidiFighter64.configs.name)) return ResourcesCompat.getDrawable(context.getResources(), R.drawable.midi_fighter_64, context.getTheme());
        return ResourcesCompat.getDrawable(context.getResources(), R.drawable.peripheral_usb, context.getTheme());
    }
}
