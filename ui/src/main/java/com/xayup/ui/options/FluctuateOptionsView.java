package com.xayup.ui.options;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

public class FluctuateOptionsView extends OptionsLayout {
    protected AlertDialog.Builder ad;
    public AlertDialog mAlertDialog;
    public FluctuateOptionsView(Context context){
        super(context);
        this.ad = new AlertDialog.Builder(context);
    }

    public AlertDialog.Builder getBuilder(){
        return ad;
    }

    public AlertDialog create(){
        return (mAlertDialog = ad.setView(layout).create());
    }

    public void show(){
        create().show();
    }
}
