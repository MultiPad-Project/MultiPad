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
    public int current_chain = 1;
    public boolean watermark_press = false;
    public boolean watermark = true;

    public interface PadLayoutMode {
        public int LAYOUT_PRO_MODE = 0;
        public int LAYOUT_MK2_MODE = 1;
        public int LAYOUT_UNIPAD_MODE = 2;
        public int LAYOUT_MATRIX_MODE = 3;
    }

    public Pad(Context context, PadInteraction mPadInteraction) {
        this.context = (Activity) context;
        this.mPadInteraction = mPadInteraction;
        this.mSkinManager = new SkinManager(context);
        mGridViews = new ArrayList<>();
    }

    public Pads newPads(String skin_package, int rows, int colums) {
        Pads pads =
                new Pads(mSkinManager.getPropertiesFromPackage(skin_package, true), rows, colums);
        mGridViews.add(pads);
        return pads;
    }

    public class Pads implements PadsLayoutInterface, SkinSupport {
        public int layout_mode;
        protected SkinProperties mSkinProperties;
        protected PadSkinData mSkinData;
        protected ViewGroup mRootPads;
        protected GridLayout mGrid;

        public Pads(SkinProperties skin, int rows, int colums) {
            this.mSkinProperties = mSkinProperties;
            this.mSkinData = new PadSkinData();
            mSkinManager.loadSkin(skin, mSkinData, null);
            this.mRootPads = new MakePads(context, rows, colums).make(mSkinData);
            this.mGrid = (GridLayout) mRootPads.getChildAt(1);

            setPadsFunctions();
        }

        protected void setPadsFunctions() {
            forAllPads(
                    new Operator() {
                        @Override
                        public void run(ViewGroup pad) {
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
                                                MakePads.PadInfo mPadInfo = (MakePads.PadInfo) view.getTag();
                                                if (mPadInfo == null)  continue;
                                                switch (mPadInfo.type) {
                                                    case MakePads.PadInfo.PadInfoIdentifier.BTN:
                                                        {
                                                            ((ImageView) view).setImageDrawable(null);
                                                            ((ImageView) view)
                                                                    .setImageDrawable(
                                                                            mSkinData.draw_btn);
                                                            break;
                                                        }
                                                    case MakePads.PadInfo.PadInfoIdentifier.BTN_:
                                                        {
                                                            ((ImageView) view).setImageDrawable(null);
                                                            ((ImageView) view)
                                                                    .setImageDrawable(
                                                                            mSkinData.draw_btn_);
                                                            break;
                                                        }
                                                    case MakePads.PadInfo.PadInfoIdentifier.PHANTOM:
                                                        {    
                                                            ((ImageView) view).setImageDrawable(null);
                                                            ((ImageView) view)
                                                                    .setImageDrawable(
                                                                            mSkinData.draw_phantom);
                                                            break;
                                                        }
                                                    case MakePads.PadInfo.PadInfoIdentifier
                                                            .PHANTOM_:
                                                        {
                                                            ((ImageView) view).setImageResource(0);
                                                            ((ImageView) view)
                                                                    .setImageDrawable(
                                                                            mSkinData
                                                                                    .draw_phantom_);
                                                            break;
                                                        }
                                                    case MakePads.PadInfo.PadInfoIdentifier
                                                            .CHAIN_LED:
                                                        {
                                                            ((ImageView) view).setImageDrawable(null);
                                                            ((ImageView) view)
                                                                    .setImageDrawable(
                                                                            mSkinData
                                                                                    .draw_chainled);
                                                            break;
                                                        }
                                                    case MakePads.PadInfo.PadInfoIdentifier.LOGO:
                                                        {
                                                            ((ImageView) view).setImageDrawable(null);
                                                            ((ImageView) view)
                                                                    .setImageDrawable(
                                                                            mSkinData.draw_logo);
                                                            break;
                                                        }
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
