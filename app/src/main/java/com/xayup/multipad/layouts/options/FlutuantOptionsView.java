package com.xayup.multipad.layouts.options;

import android.app.AlertDialog;
import android.content.Context;

public class FlutuantOptionsView extends Options {
    AlertDialog dialog;
    Context context;

    public FlutuantOptionsView(Context context){
        super();
        this.context = context;
    }

    public void Builder(){
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
    }

    public void show(){

    }

}
