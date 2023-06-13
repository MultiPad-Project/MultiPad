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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pad {
    public Activity context;
    protected Map<String, Pads> mGridViews;
    public SkinManager mSkinManager;
    public PadInteraction mPadInteraction;
    public int current_chain = 1;
    public int[] current_chain_array = new int[] {1, 9};
    public boolean watermark_press = false;
    public boolean watermark = true;
    protected String active_layout = null;

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
        mGridViews = new HashMap<>();
    }

    public Pads newPads(String skin_package, int rows, int colums) {
        Pads pads =
                new Pads(mSkinManager.getPropertiesFromPackage(skin_package, true), rows, colums);
        active_layout = "pad_" + mGridViews.size();
        mGridViews.put(active_layout, pads);
        return pads;
    }

    public void setCurrentChain(int x, int y) {
        current_chain_array[0] = x;
        current_chain_array[1] = y;
        if (y == 9) {
            current_chain = x;
        } else if (x == 9) {
            current_chain = 17 - y;
        } else if (y == 0) {
            current_chain = 25 - x;
        }
    }
    /* index: 0 = X, 1 = Y, 2 = MC */
    public int[] getCurrentChain() {
        return new int[] {current_chain_array[0], current_chain_array[1], current_chain};
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
                    (pad) -> {
                        pad.setOnTouchListener(mPadInteraction.onPadClick(pad));
                    });
        }

        public void forAllPads(Operator ops) {
            for (int i = 0; i < mGrid.getChildCount(); i++) {
                if (mGrid.getChildAt(i) instanceof ViewGroup) {
                    ops.run((ViewGroup) mGrid.getChildAt(i));
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
                    (skin) -> {
                        ((ImageView) mRootPads.getChildAt(0))
                                .setImageDrawable(mSkinData.draw_playbg);
                        forAllPads(
                                (pad) -> {
                                    for (int ii = 0; ii < pad.getChildCount(); ii++) {
                                        View view = pad.getChildAt(ii);
                                        MakePads.PadInfo mPadInfo =
                                                (MakePads.PadInfo) view.getTag();
                                        if (mPadInfo == null) continue;
                                        switch (mPadInfo.type) {
                                            case MakePads.PadInfo.PadInfoIdentifier.BTN:
                                                {
                                                    ((ImageView) view).setImageDrawable(null);
                                                    ((ImageView) view)
                                                            .setImageDrawable(mSkinData.draw_btn);
                                                    break;
                                                }
                                            case MakePads.PadInfo.PadInfoIdentifier.BTN_:
                                                {
                                                    ((ImageView) view).setImageDrawable(null);
                                                    ((ImageView) view)
                                                            .setImageDrawable(mSkinData.draw_btn_);
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
                                            case MakePads.PadInfo.PadInfoIdentifier.PHANTOM_:
                                                {
                                                    ((ImageView) view).setImageResource(0);
                                                    ((ImageView) view)
                                                            .setImageDrawable(
                                                                    mSkinData.draw_phantom_);
                                                    break;
                                                }
                                            case MakePads.PadInfo.PadInfoIdentifier.CHAIN_LED:
                                                {
                                                    ((ImageView) view).setImageDrawable(null);
                                                    ((ImageView) view)
                                                            .setImageDrawable(
                                                                    mSkinData.draw_chainled);
                                                    break;
                                                }
                                            case MakePads.PadInfo.PadInfoIdentifier.LOGO:
                                                {
                                                    ((ImageView) view).setImageDrawable(null);
                                                    ((ImageView) view)
                                                            .setImageDrawable(mSkinData.draw_logo);
                                                    break;
                                                }
                                        }
                                    }
                                });
                    });
            return false;
        }
    }

    protected interface Operator {
        void run(ViewGroup view);
    }
}
