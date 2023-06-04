package com.xayup.multipad.pads;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import com.xayup.multipad.pads.PadInteraction;
import com.xayup.multipad.pads.PadsLayoutInterface;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.pads.Render.PadSkinData;
import com.xayup.multipad.pads.Render.SkinManager;
import com.xayup.multipad.skin.SkinProperties;
import com.xayup.multipad.skin.SkinSupport;
import java.util.ArrayList;
import java.util.List;

public class Pad {
    public Activity context;
    public List<Pads> mGridViews;
    public SkinManager mSkinManager;
    public PadInteraction mPadInteraction;

    public Pad(Context context, PadInteraction mPadInteraction) {
        this.context = (Activity) context;
        this.mPadInteraction = mPadInteraction;
        this.mSkinManager = new SkinManager(context);
        mGridViews = new ArrayList<>();
    }

    public Pads newPads(String skin_package) {
        Pads pads = new Pads(mSkinManager.getPropertiesFromPackage(skin_package, true));
        mGridViews.add(pads);
        return pads;
    }
    
    public class Pads implements PadsLayoutInterface, SkinSupport {
        SkinProperties mSkinProperties;
        PadSkinData mSkinData;
        ViewGroup mRootPads;
        GridLayout mGrid;

        public Pads(SkinProperties skin) {
            this.mSkinProperties = mSkinProperties;
            this.mSkinData = new PadSkinData();
            mSkinManager.loadSkin(skin, mSkinData, null);
            this.mRootPads = new MakePads(context).make(mSkinData);
            this.mGrid = (GridLayout) mRootPads.getChildAt(1);
            
            setPadsFunctions();
        }
        
        protected void setPadsFunctions(){
            forAllPads(new Operator(){
                @Override
                    public void run(ViewGroup pad){
                        pad.setOnClickListener(mPadInteraction.onPadClick(pad));
                    }
            });
        }

        public void forAllPads(Operator ops) {
            for (int i = 0; i < mGrid.getChildCount(); i++) {
                if (mGrid.getChildAt(i) instanceof ViewGroup) {
                    ViewGroup pad = (ViewGroup) mGrid.getChildAt(i);
                    ops.run(pad);
                }
            }
        }

        @Override
        public PadSkinData getSkinData() {
            return mSkinData;
        }

        @Override
        public ViewGroup getRootPads() {
            return mRootPads;
        }

        @Override
        public GridLayout getGridPads() {
            return mGrid;
        }

        @Override
        public boolean applySkin(SkinProperties mSkinProperties) {
            mSkinManager.loadSkin(
                    mSkinProperties,
                    mSkinData,
                    new SkinManager.LoadedSkin() {
                        @Override
                        public void onLoaded(SkinProperties skin) {
                            ((ImageView) mRootPads.getChildAt(0))
                                    .setImageDrawable(mSkinData.draw_playbg);
                            forAllPads(
                                    new Operator() {
                                        @Override
                                        public void run(ViewGroup pad) {
                                            for (int ii = 0; ii < pad.getChildCount(); ii++) {
                                                View view = pad.getChildAt(ii);
                                                String tag = (String) view.getTag();
                                                if (tag.contains("_btn")) {
                                                    if (tag.lastIndexOf("_") < tag.length()) {
                                                        ((ImageView) view)
                                                                .setImageDrawable(
                                                                        mSkinData.draw_btn);
                                                    } else {
                                                        ((ImageView) view)
                                                                .setImageDrawable(
                                                                        mSkinData.draw_btn_);
                                                    }
                                                } else if (tag.contains("_phantom")) {
                                                    if (tag.lastIndexOf("_") < tag.length()) {
                                                        ((ImageView) view)
                                                                .setImageDrawable(
                                                                        mSkinData.draw_phantom);
                                                    } else {
                                                        ((ImageView) view)
                                                                .setImageDrawable(
                                                                        mSkinData.draw_phantom_);
                                                    }
                                                } else if (tag.contains("_chainled")) {
                                                    ((ImageView) view)
                                                            .setImageDrawable(
                                                                    mSkinData.draw_chainled);
                                                } else if (tag.contains("_logo")) {
                                                    ((ImageView) view)
                                                            .setImageDrawable(mSkinData.draw_logo);
                                                }
                                            }
                                        }
                                    });
                        }
                    });
            return false;
        }
    }

    protected interface Operator {
        void run(ViewGroup view);
    }
}
