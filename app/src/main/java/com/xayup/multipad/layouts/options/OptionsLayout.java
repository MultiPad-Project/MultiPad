package com.xayup.multipad.layouts.options;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

public class OptionsLayout extends Options {
    RelativeLayout layout;
    public OptionsLayout(Activity context){
        super();
        layout = context.getLayoutInflater().inflate();
    }

}
