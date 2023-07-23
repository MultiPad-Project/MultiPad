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
import com.xayup.debug.XLog;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.pads.Render.PadSkinData;
import com.xayup.multipad.projects.Project;
import com.xayup.multipad.projects.ProjectManager;
import com.xayup.multipad.projects.project.autoplay.AutoPlay;
import com.xayup.multipad.projects.project.keyled.KeyLED;
import com.xayup.multipad.projects.project.keysounds.KeySounds;
import com.xayup.multipad.skin.SkinManager;
import com.xayup.multipad.skin.SkinProperties;
import com.xayup.multipad.skin.SkinSupport;

import java.util.*;

public abstract class GridPadsReceptor {
    protected Activity context;
    protected Map<Integer, List<String>> project_use_grid; //Get grid by project id
    protected Map<String, PadGrid> grids; //Get grid by name
    protected Map<Integer, List<String>> grid_ids; //Get grid by ID

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
        byte LAYOUT_PRO_MODE = 0;
        byte LAYOUT_MK2_MODE = 1;
        byte LAYOUT_UNIPAD_MODE = 2;
        byte LAYOUT_MATRIX_MODE = 3;
    }

    public GridPadsReceptor(Context context) {
        this.context = (Activity) context;
        this.mSkinManager = new SkinManager();
        this.current_chain = new MakePads.ChainInfo(1, 9);
        this.grids = new HashMap<>();
        this.project_use_grid = new HashMap<>();
    }

    /**
     * Chamado quando algum botão é clicado em uma determinada grade
     * @param grid a grade do botão.
     * @param pad o botão
     * @param event o evento de toque
     */
    public abstract boolean onTheButtonSign(PadGrid grid, MakePads.PadInfo pad, MotionEvent event);

    /**
     * Make new Pads Object. Get this or last created pad with getActivePads()
     * @param skin_package : SKin name to get and set on the Pads
     * @param rows : rows count
     * @param columns : columns count.
     */
    public void newPads(String skin_package, int rows, int columns) {
        PadGrid padGrid = new PadGrid(SkinManager.getSkinProperties(context, skin_package), rows, columns);
        padGrid.setName("Grid_" + grids.size());
        padGrid.setId(0);
        grids.put(padGrid.getName(), padGrid);
        active_pad = padGrid;
    }
    
    public List<String> getPadsWithId(int id){
        return grid_ids.get(id);
    }
    public PadGrid getGridByName(String name){
        return grids.get(name);
    }

    public List<PadGrid> getAllPadsList(){

        return new ArrayList<>(grids.values());
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
        protected CurrentProject current_project;
        protected String name;
        protected byte layout_mode;
        protected SkinProperties mSkinProperties;
        protected PadSkinData mSkinData;
        protected ImageView pad_background;
        protected RelativeLayout container;
        protected RelativeLayout mRootPads;
        protected RelativeLayout pads_settings_overlay;
        protected GridLayout mGrid;
        protected int lp_id;
        protected MakePads.Pads mPads;

        public PadGrid(SkinProperties skin, int rows, int columns) {
            this.mSkinProperties = skin;
            this.mSkinData = new PadSkinData();
            this.current_project = new CurrentProject();

            this.mGrid = (this.mPads = new MakePads(context).make(rows, columns)).getGrid();

            this.pad_background = new ImageView(context);
            this.pad_background.setScaleType(ImageView.ScaleType.CENTER_CROP);
            (this.mRootPads = new RelativeLayout(context)).addView(pad_background, new ViewGroup.LayoutParams(-1, -1));
            this.mRootPads.addView(this.mGrid, new ViewGroup.LayoutParams(-1, -1));
            this.pads_settings_overlay = new RelativeLayout(context);
            this.mRootPads.addView(this.pads_settings_overlay, new ViewGroup.LayoutParams(-1, -1));

            this.container = new RelativeLayout(context);
            container.addView(this.mRootPads);
            container.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));

            this.layout_mode = PadLayoutMode.LAYOUT_PRO_MODE;
            this.lp_id = 0;
            applySkin(this.mSkinProperties);
            forAllPads(
                (pad, mPadGrid) -> mPadGrid.getPads().getPadView(pad.getRow(), pad.getColum()).setOnTouchListener(
                    (pad_view, event) -> {
                        pad_view.performClick();
                        return onTheButtonSign(
                            mPadGrid, pad, event
                        );
                    }
                )
            );
        }

        //// INTERACTION ////
        public void led(int row, int colum, int android_color){
            mPads.setLedColor(row, colum, android_color);
        }

        //// INFORMATION'S ////
        public MakePads.Pads getPads(){ return mPads; }
        public int getId(){ return this.lp_id; }
        public void setName(String name){ this.name = name; }
        public String getName(){ return this.name; }
        public void setId(int id){
            /*
            if(grid_ids.get(lp_id) != null) grid_ids.get(lp_id).remove(name);
            if(grid_ids.get(id) != null){
                Objects.requireNonNull(grid_ids.get(id)).add(name);
            } else {
                grid_ids.put(id, new ArrayList<>(List.of(name)));
            }
            */
            if(current_project.getProjectManager() != null){
                current_project.getProjectManager().removeGrid(this);
                lp_id = id;
                current_project.getProjectManager().addGrid(this);
            } else { lp_id = id; }
        }

        //// MANAGER ////
        public void removeThis(){
            Objects.requireNonNull(grid_ids.get(lp_id)).remove(name);
        }
        public void forAllPads(ForAllPads fap) {
            for (int i = 0; i < mGrid.getChildCount(); i++) {
                if (mGrid.getChildAt(i).getTag() instanceof MakePads.PadInfo) {
                    fap.run((MakePads.PadInfo) mGrid.getChildAt(i).getTag(), this);
                }
            }
        }

        //// PROJECT ////
        public void setProject(ProjectManager projectManager){
            this.current_project.setProjectManager(projectManager);
            this.current_project.getProjectManager().addGrid(this);
        }
        public CurrentProject getProject(){ return this.current_project; }
        public void removeProject(){
            if(this.current_project.getProjectManager() != null) {
                this.current_project.getProjectManager().removeGrid(this);
                this.current_project.setProjectManager(null);
            }
        }

        //// LAYOUT ////
        public byte getLayoutMode(){
            return layout_mode;
        }

        //// CONTAINERS ////
        public RelativeLayout getContainer(){
            return this.container;
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
                                (padInfo, padGrid) -> {
                                    ViewGroup pad = ((ViewGroup) padGrid.getPads().getPadView(padInfo.getRow(), padInfo.getColum()));
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
                                                    padGrid.getPads().getLed(padInfo.getRow(), padInfo.getColum()).setImageDrawable(mSkinData.draw_phantom__led);
                                                    break;
                                                }
                                            case MakePads.PadInfo.PadInfoIdentifier.CHAIN_LED:
                                                {
                                                    ((ImageView) view).setImageDrawable(null);
                                                    ((ImageView) view)
                                                            .setImageDrawable(
                                                                    mSkinData.draw_chainled);
                                                    padGrid.getPads().getLed(padInfo.getRow(), padInfo.getColum()).setImageDrawable(mSkinData.draw_chain_led);
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

        public class CurrentProject{
            protected ProjectManager projectManager;

            public void setProjectManager(ProjectManager projectManager){
                this.projectManager = projectManager;
            }
            public ProjectManager getProjectManager(){
                return this.projectManager;
            }
            public AutoPlay getAutoPlay(){
                return (projectManager != null) ? projectManager.getAutoPlay() : null;
            }
            public KeyLED getKeyLED(){
                return (projectManager != null) ? projectManager.getKeyLED() : null;
            }
            public KeySounds getKeySounds(){
                return (projectManager != null) ? projectManager.getKeySounds() : null;
            }
            public void callPress(MakePads.ChainInfo chain, MakePads.PadInfo pad){
                if(getPadPress() != null) projectManager.getPadPressCall().call(chain, pad);
            }
            public PadPressCall getPadPress(){
                if (projectManager != null) {
                    XLog.e("getCallPress: CurrentProject", "Success");
                    return projectManager.getPadPressCall();
                } else {
                    XLog.e("getCallPress: CurrentProject", "Error");
                    return null;
                }
            }

        }
    }

    public interface ForAllPads {
        void run(MakePads.PadInfo padInfo, PadGrid padGrid);
    }
}
