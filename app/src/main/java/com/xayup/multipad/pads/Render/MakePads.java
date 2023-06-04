package com.xayup.multipad.pads.Render;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.xayup.multipad.R;
import com.xayup.multipad.pads.PadInterface;

public class MakePads {
    protected Activity context;
    protected int rows, colums, height = 0;
    protected PadInterface mPadInterface;
    protected ViewGroup.LayoutParams mGridParams;
    protected GridLayout.LayoutParams mPadParams;

    public MakePads(Context context, int rows, int colums, int height) {
        this.context = (Activity) context;
        this.mPadInterface = mPadInterface;
        this.rows = rows;
        this.colums = colums;
        this.height = height;
    }
    
    public MakePads(Context context){
        this.context = (Activity) context;
    }

    public ViewGroup make(PadSkinData mSkinData) {
        View led;
        ImageView btn, btn_, phantom, playbg;
        RelativeLayout grid_root = new RelativeLayout(context);
        GridLayout mGrid = new GridLayout(context);
        playbg = new ImageView(context);
        playbg.setImageDrawable(mSkinData.draw_playbg);
        playbg.setScaleType(ImageView.ScaleType.CENTER);
        grid_root.addView(playbg, new RelativeLayout.LayoutParams(720, 720));
        grid_root.addView(mGrid, new RelativeLayout.LayoutParams(720, 720));
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < colums; c++) {
                ViewGroup pad;
                if ((r == 0 && c == 0) || (r == 9 && c == 0) || (r == 9 && c == 9)) {
                    pad = (ViewGroup) new View(context);
                    pad.setVisibility(View.INVISIBLE);
                } else {
                    pad =
                            (ViewGroup)
                                    context.getLayoutInflater().inflate(R.layout.pad, null, false);
                    btn = pad.findViewById(R.id.pad);
                    btn_ = pad.findViewById(R.id.press);
                    phantom = pad.findViewById(R.id.phantom);
                    led = pad.findViewById(R.id.led);
                    mPadParams =
                            new GridLayout.LayoutParams(
                                    GridLayout.spec(rows, GridLayout.LayoutParams.MATCH_PARENT, 1f),
                                    GridLayout.spec(
                                            colums, GridLayout.LayoutParams.MATCH_PARENT, 1f));
                    mPadParams.height = 0;
                    mPadParams.width = 0;
                    btn.setImageDrawable(mSkinData.draw_btn);
                    btn_.setImageDrawable(mSkinData.draw_btn_);
                    if (r == 0 || r == 9 || c == 0 || c == 9) {
                        if (r == 0 && c == 9) {
                            phantom.setImageDrawable(mSkinData.draw_logo);
                            phantom.setTag(r + "" + c + "_logo");
                        } else {
                            pad.setTag(r + "" + c + "_chain");
                            phantom.setImageDrawable(mSkinData.draw_chainled);
                            phantom.setTag(r + "" + c + "_chainled");
                            if (r == 0) {
                                phantom.setRotation(-90);
                            } else if (r == 9) {
                                phantom.setRotation(90);
                            } else if (c == 0) {
                                phantom.setScaleX(phantom.getScaleX() * -1);
                            }
                        }
                    } else {
                        pad.setTag(r + "" + c + "_pad");
                        if ((r == 4 || r == 5) && (c == 4 || c == 5)) {
                            phantom.setImageDrawable(mSkinData.draw_phantom_);
                            phantom.setTag(r + "" + c + "_phantom_");
                            if (r == 4 && c == 5) {
                                phantom.setScaleX(phantom.getScaleX() * -1);
                            } else if (r == 5 && c == 4) {
                                phantom.setScaleY(phantom.getScaleY() * -1);
                            } else if (r == 5 && c == 5) {
                                phantom.setScaleX(phantom.getScaleX() * -1);
                                phantom.setScaleY(phantom.getScaleY() * -1);
                            }
                        } else {
                            phantom.setImageDrawable(mSkinData.draw_phantom);
                            phantom.setTag(r + "" + c + "_phantom");
                        }
                    }
                    btn.setTag(r + "" + c + "_btn");
                    btn_.setTag(r + "" + c + "_btn_");
                    led.setTag(r + "" + c + "_led");
                }
                mGrid.addView(pad, mPadParams);
            }
        }
        return grid_root;
    }
}
