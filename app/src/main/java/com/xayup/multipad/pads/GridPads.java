package com.xayup.multipad.pads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.xayup.multipad.R;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.pads.Render.PadSkinData;
import com.xayup.multipad.projects.Project;
import com.xayup.multipad.skin.SkinManager;
import com.xayup.multipad.skin.SkinProperties;
import com.xayup.multipad.skin.SkinSupport;

import java.util.*;

public class GridPads {
    protected Activity context;
    protected Map<String, List<PadGrid>> mGridViews;
    public SkinManager mSkinManager;
    protected PadGrid active_pad = null;
    protected View.OnTouchListener resize_touch;
    protected View.OnTouchListener rotate_touch;
    protected View.OnTouchListener move_touch;
    /*Shared*/
    public MakePads.ChainInfo current_chain;
    public boolean watermark_press = false;
    public boolean watermark = true;

    public interface PadLayoutMode {
        int LAYOUT_PRO_MODE = 0;
        int LAYOUT_MK2_MODE = 1;
        int LAYOUT_UNIPAD_MODE = 2;
        int LAYOUT_MATRIX_MODE = 3;
    }

    public GridPads(Context context) {
        this.context = (Activity) context;
        this.mSkinManager = new SkinManager();
        this.current_chain = new MakePads.ChainInfo(1, 9);
        mGridViews = new HashMap<>();
    }

    /**
     * Make new Pads Object. Get this or last created pad with getActivePads()
     * @param skin_package : SKin name to get and set on the Pads
     * @param rows : rows count
     * @param columns : columns count.
     */
    public void newPads(String skin_package, int rows, int columns) {
        PadGrid padGrid = new PadGrid(SkinManager.getSkinProperties(context, skin_package), rows, columns);
        padGrid.setId(mGridViews.size());
        padGrid.setName("pad_" + padGrid.getId());
        active_pad = padGrid;
    }

    public void setEditMode(boolean enable){
        for(List<PadGrid> listPads : mGridViews.values()){
            for(PadGrid mPadGrid : listPads){
                if(enable) {
                    context.getLayoutInflater().inflate(R.layout.pads_editor_view, mPadGrid.getRootPads(), true);
                } else {
                    mPadGrid.getRootPads().removeViewAt(mPadGrid.getRootPads().getChildCount()-1);
                }
            }
        }
    }
    
    public List<PadGrid> getPadsWithIndex(int index){
        return mGridViews.get(String.valueOf(index));
    }

    public List<PadGrid> getAllPadsList(){
        List<PadGrid> tmp = new ArrayList<>();
        for(List<PadGrid> list: mGridViews.values()){
            tmp.addAll(list);
        }
        return tmp;
    }

    /**
     * Isso retorna a Pads ativa (Que será definido pelo usuário ou quando uma nova Pads é criada)
     * @return um Objeto Pads atual
     */
    public PadGrid getActivePads(){
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

    public class PadGrid implements PadsLayoutInterface, SkinSupport {
        protected Project current_project;
        protected String name;
        public int layout_mode;
        protected SkinProperties mSkinProperties;
        protected PadSkinData mSkinData;
        protected ImageView pad_background;
        protected RelativeLayout mRootPads;
        protected RelativeLayout pads_settings_overlay;
        protected GridLayout mGrid;
        protected int lp_id;
        protected MakePads.Pads mPads;

        public PadGrid(SkinProperties skin, int rows, int columns) {
            this.mSkinProperties = skin;
            this.mSkinData = new PadSkinData();

            this.mGrid = (this.mPads = new MakePads(context).make(rows, columns)).getGrid();

            this.pad_background = new ImageView(context);
            this.pad_background.setScaleType(ImageView.ScaleType.CENTER_CROP);
            (this.mRootPads = new RelativeLayout(context)).addView(pad_background, new ViewGroup.LayoutParams(-1, -1));
            this.mRootPads.addView(this.mGrid, new ViewGroup.LayoutParams(-1, -1));
            this.pads_settings_overlay = new RelativeLayout(context);
            this.mRootPads.addView(this.pads_settings_overlay, new ViewGroup.LayoutParams(-1, -1));

            this.layout_mode = PadLayoutMode.LAYOUT_PRO_MODE;
            this.lp_id = 0;
            applySkin(this.mSkinProperties);
        }

        public void setForAllPadInteraction(PadInteraction padInteraction){
            forAllPads((pad, mPadGrid) -> pad.setOnTouchListener(padInteraction.onPadClick(pad, mPadGrid)));
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

        public MakePads.Pads getPads(){
            return mPads;
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

        public void setProject(Project project){
            this.current_project = project;
        }
        public Project getProject(){ return this.current_project; }


        public void forAllPads(ForAllPads fap) {
            for (int i = 0; i < mGrid.getChildCount(); i++) {
                if (mGrid.getChildAt(i) instanceof ViewGroup) {
                    fap.run((ViewGroup) mGrid.getChildAt(i), this);
                }
            }
        }

        public RelativeLayout getPadsSettingsOverlay(){
            return pads_settings_overlay;
        }

        @Override
        public PadSkinData getSkinData() {
            return mSkinData;
        }

        @Override
        public RelativeLayout getRootPads() {
            return mRootPads;
        }

        @Override
        public GridLayout getGridPads() {
            return mGrid;
        }

        @Override
        public boolean applySkin(SkinProperties mSkinProperties) {
            mSkinManager.loadSkin(
                    context,
                    mSkinProperties,
                    mSkinData,
                    (skin) -> {
                        ((ImageView) mRootPads.getChildAt(0)).setImageDrawable(mSkinData.draw_playbg);
                        forAllPads(
                                (pad, mPadGrid) -> {
                                    for (int ii = 0; ii < pad.getChildCount(); ii++) {
                                        View view = pad.getChildAt(ii);
                                        if(view.getTag() == null) continue;
                                        byte type = (byte) view.getTag();
                                        switch (type) {
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

    public interface ForAllPads {
        void run(ViewGroup view, PadGrid mPadGrid);
    }
}
