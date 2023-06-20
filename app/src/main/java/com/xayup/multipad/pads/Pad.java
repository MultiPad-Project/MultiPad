package com.xayup.multipad.pads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import com.xayup.multipad.R;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.pads.Render.PadSkinData;
import com.xayup.multipad.skin.SkinManager;
import com.xayup.multipad.skin.SkinProperties;
import com.xayup.multipad.skin.SkinSupport;

import java.util.*;

public class Pad {
    protected Activity context;
    protected Map<String, List<Pads>> mGridViews;
    public SkinManager mSkinManager;
    protected PadInteraction mPadInteraction;
    protected Pads active_pad = null;
    protected View.OnTouchListener resize_touch;
    protected View.OnTouchListener rotate_touch;
    protected View.OnTouchListener move_touch;
    /*Shared*/
    public int current_chain = 1;
    public int[] current_chain_array = new int[] {1, 9};
    public boolean watermark_press = false;
    public boolean watermark = true;

    public interface PadLayoutMode {
        int LAYOUT_PRO_MODE = 0;
        int LAYOUT_MK2_MODE = 1;
        int LAYOUT_UNIPAD_MODE = 2;
        int LAYOUT_MATRIX_MODE = 3;
    }

    public Pad(Context context, PadInteraction mPadInteraction) {
        this.context = (Activity) context;
        this.mPadInteraction = mPadInteraction;
        this.mSkinManager = new SkinManager(context);
        mGridViews = new HashMap<>();
    }

    /**
     * Make new Pads Object. Get this or last created pad with getActivePads()
     * @param skin_package : SKin name to get and set on the Pads
     * @param rows : rows count
     * @param columns : columns count.
     */
    public void newPads(String skin_package, int rows, int columns) {
        Pads pads =
                new Pads(mSkinManager.getPropertiesFromPackage(context, skin_package, true), rows, columns);
        /*
        if(mGridViews.get(String.valueOf(mGridViews.size())) != null){
            Objects.requireNonNull(mGridViews.get(String.valueOf(mGridViews.size()))).add(pads);
        } else {
            mGridViews.put(String.valueOf(mGridViews.size()), new ArrayList<>(List.of(pads)));
        }*/
        pads.setId(mGridViews.size());
        pads.setName("pad_" + pads.getId());
        active_pad = pads;
    }

    public void setEditMode(boolean enable){
        for(List<Pads> listPads : mGridViews.values()){
            for(Pads mPads : listPads){
                if(enable) {
                    context.getLayoutInflater().inflate(R.layout.pads_editor_view, mPads.getRootPads(), true);
                } else {
                    mPads.getRootPads().removeViewAt(mPads.getRootPads().getChildCount()-1);
                }
            }
        }
    }
    
    public List<Pads> getPadsWithIndex(int index){
        return mGridViews.get(String.valueOf(index));
    }

    public List<Pads> getAllPadsList(){
        List<Pads> tmp = new ArrayList<>();
        for(List<Pads> list: mGridViews.values()){
            tmp.addAll(list);
        }
        return tmp;
    }

    /**
     * Isso retorna a Pads ativa (Que será definido pelo usuário ou quando uma nova Pads é criada)
     * @return um Objeto Pads atual
     */
    public Pads getActivePads(){
        return active_pad;
    }

    public View.OnTouchListener getResize_touch(){
        return new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){
                    int h = view.getLayoutParams().height;
                    int w = view.getLayoutParams().width;
                    view.getRootView().getLayoutParams().height = h - (int) motionEvent.getY();
                    view.getRootView().getLayoutParams().width = w - (int) motionEvent.getX();
                    return true;
                }
                return false;
            }
        };
    }

    public View.OnTouchListener getRotate_touch(){
        return new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){

                    return true;
                }
                return false;
            }
        };
    }

    public View.OnTouchListener getMove_touch(){
        return new View.OnTouchListener() {
            int x;
            int y;
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        x = (int) motionEvent.getX();
                        y = (int) motionEvent.getY();
                        return true;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        view.setTranslationX(view.getTranslationX() + (motionEvent.getX() - x));
                        view.setTranslationY(view.getTranslationY() + (motionEvent.getY() - y));
                        return true;
                    }
                }
                return false;
            }
        };
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
        protected String name;
        public int layout_mode;
        protected SkinProperties mSkinProperties;
        protected PadSkinData mSkinData;
        protected ViewGroup mRootPads;
        protected GridLayout mGrid;
        protected int lp_id;

        public Pads(SkinProperties skin, int rows, int columns) {
            this.mSkinProperties = skin;
            this.mSkinData = new PadSkinData();
            this.mRootPads = new MakePads(context, rows, columns).make();
            this.mGrid = (GridLayout) mRootPads.getChildAt(1);
            this.layout_mode = PadLayoutMode.LAYOUT_PRO_MODE;
            this.lp_id = 0;
            setPadsFunctions();
            applySkin(this.mSkinProperties);
        }

        protected void setId(int id){
            if(mGridViews.get(String.valueOf(lp_id)) != null) mGridViews.get(String.valueOf(lp_id)).remove(this);
            if(mGridViews.get(String.valueOf(id)) != null){
                Objects.requireNonNull(mGridViews.get(String.valueOf(id))).add(this);
            } else {
                mGridViews.put(String.valueOf(id), new ArrayList<>(List.of(this)));
            }
            lp_id = id;
        }

        public int getId(){
            return this.lp_id;
        }

        public void setName(String name){
            this.name = name;
        }

        public String getName(){
            return this.name;
        }

        public void removeThis(){
            Objects.requireNonNull(mGridViews.get(String.valueOf(lp_id))).remove(this);
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
