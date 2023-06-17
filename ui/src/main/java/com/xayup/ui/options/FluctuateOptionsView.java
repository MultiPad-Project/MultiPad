package com.xayup.ui.options;

import android.app.Activity;
import android.app.AlertDialog;

public class FluctuateOptionsView extends OptionsLayout {
    protected AlertDialog.Builder ad;
    public FluctuateOptionsView(Activity context){
        super(context);
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
    }

    public AlertDialog.Builder getBuilder(){
        return ad;
    }

    public void show(){
        ad.setView(layout).create().show();
    }
}
