package com.xayup.multipad.pads.Render;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.service.autofill.FillEventHistory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.IntDef;
import androidx.annotation.RequiresFeature;
import com.xayup.multipad.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.xayup.multipad.pads.Render.MakePads.ForAllChild.FOR_ALL;

public class MakePads {
    protected Context context;

    public abstract static class ChildInfo {

        protected byte row;
        protected byte colum;
        protected @PadType byte type;

        public ChildInfo(byte row, byte colum, byte type){
            this.row = row;
            this.colum = colum;
            this.type = type;
        }
        public abstract Pads getPads();
        public int getRow(){ return row; }
        public int getColum(){ return colum; }
        public @PadType byte getType(){ return type; }
    }

    public static class PadInfo extends ChildInfo {
        @Retention(RetentionPolicy.SOURCE)
        @IntDef({PadLayerType.BTN, PadLayerType.BTN_, PadLayerType.CHAIN_LED, PadLayerType.LED,
                PadLayerType.LOGO, PadLayerType.LOGO_BTN, PadLayerType.PHANTOM, PadLayerType.PHANTOM_, PadLayerType.TOUCH_MAP})
        public @interface PadLayerType {
            /**
             * Skin identification
             */
            byte LED = 104;
            /**
             * Skin identification
             */
            byte BTN = 105;
            /**
             * Skin identification
             */
            byte BTN_ = 106;
            /**
             * Skin identification
             */
            byte LOGO = 107;
            /**
             * Skin identification
             */
            byte LOGO_BTN = 112;
            /**
             * Skin identification
             */
            byte CHAIN_LED = 108;
            /**
             * Skin identification
             */
            byte PHANTOM = 109;
            /**
             * Skin identification
             */
            byte PHANTOM_ = 110;
            /**
             * Button Layer identification
             */
            byte TOUCH_MAP = 111;
        }

        protected boolean activated;

        /**
         * Make PadInfo with:
         * @param row .
         * @param colum .
         * @param type . Set type with PadInfoIdentifier
         */
        protected PadInfo(byte row, byte colum, @PadType byte type) {
            super(row, colum, type);
            this.activated = false;
        }

        @Override
        public Pads getPads() { return null; }

        public void setRow(byte row){ this.row = row; }
        public void setColum(byte colum){ this.colum = colum; }
        public void setType(byte type){ this.type = type; }

        public int getId(){ return PadID.getId(row, colum); }

        public void markAsActivated(boolean mark){ this.activated = mark; }
        public boolean isActivated(){ return this.activated; }
    }

    public static class ChainInfo extends PadInfo {
        protected byte mc;
        public ChainInfo(int row, int colum){
            super((byte) row, (byte) colum, PadType.CHAIN);
            if(this.type == PadType.CHAIN){
                this.mc = (byte) PadID.getChainMc(row, colum, 9);
            } else {
                this.mc = -1;
            }
        }

        /**
         * Only change chain info
         * @param row chain row
         * @param colum chain colum
         */
        public void setCurrentChain(int row, int colum){
            setMc(row, colum);
            setRow((byte) row);
            setColum((byte) colum);
        }

        public void setMc(int row, int colum) {
            this.mc = (byte) PadID.getChainMc(row, colum, 9);
        }

        public int getMc(){
            return mc;
        };
    }


    public static class PadID {
        public static int getGridIndexFromXY(int grid_columns, int row, int colum) {
            return (grid_columns * row) + colum;
        }
        public static byte[ /*X*/][ /*Y*/] ids = new byte[10][10];

        public static int getId(int x, int y) {
            return ids[x][y];
        }

        /**
         * A contagem do Chain comeca do topo [row 0, colum 1]
         *
         * @param mc chain MC value (1 -> 32)
         * @param offset altera o inicio na contage (Sentido horario)
         * @return Chain ID
         */
        public static int getChainId(int mc, int offset) {
            int[] xy = getChainXY(mc, offset);
            return getId(xy[0], xy[1]);
        }
        /**
         * A contagem do Chain comeca do topo [row 0, colum 1]
         *
         * @param mc chain MC value (1 -> 32)
         * @param offset altera o inicio na contage (Sentido horario). O padrão é 1
         * @return Chain [row, colum]
         */
        public static int[] getChainXY(int mc, int offset) {
            if (offset < 1 || offset > 32)
                throw new NumberFormatException(
                        "The number must be greater than 1 or less than 33. Offset: " + offset);
            mc = mc + offset - 1;
            if(mc  > 32) mc -= 32;
            if (mc > 24) {
                return new int[] {33 - mc, 0};
            } else if (mc > 16) {
                return new int[] {9, 25 - mc};
            } else if (mc > 8) {
                return new int[] {mc - 8, 9};
            } else {
                return new int[] {0, mc};
            }
        }

        /**
         * Obtém o MC da chain através das suas cordenadas.
         * @param row a linha da localização da chain
         * @param colum a coluna da localização da chain.
         * @param offset altera o inicio na contage (Sentido horario). O padrão é 1
         * @return chain MC.
         */
        public static int getChainMc(int row, int colum, int offset) {
            if (offset < 1 || offset > 32)
                throw new NumberFormatException(
                        "The number must be greater than 1 or less than 33. Offset: " + offset);
            if(row == 0){
                offset = colum - offset;
            } else if (row == 9) {
                offset = (25-colum) - offset;
            } else if (colum == 0) {
                offset = (33-row) - offset;
            } else if (colum == 9){
                offset = (8+row)  - offset;
            } else {
                return -1;
            }
            if((offset += 1) < 1) offset += 32;
            return offset;
        }

        /**
         * Assign ID with row and colum.
         * @param row .
         * @param colum .
         * @return return ID
         */
        private static int assign(int row, int colum) {
            ids[row][colum] = (byte) ((row * 10) + colum);
            return ids[row][colum];
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PadType.NONE, PadType.CHAIN, PadType.PAD, PadType.PAD_LOGO})
    public @interface PadType {
        /**
         * Button identification
         */
        byte NONE = 0;
        /**
         * Button identification
         */
        byte CHAIN = 1;
        /**
         * Button identification
         */
        byte PAD = 2;
        /**
         * Button identification
         */
        byte PAD_LOGO = 3;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ForAllChild.FOR_ALL, PadType.NONE, PadType.CHAIN, PadType.PAD, PadType.PAD_LOGO})
    public @interface ForAllChild {
        byte FOR_ALL = -1;
    }

    public interface ForPadLayers{
        public abstract boolean onPadLayer(View padLayer, @PadInfo.PadLayerType byte padLayerType);
    }
    
    public MakePads(Context context) { this.context = context; }

    public class Pads {
        public class Glows {
            public static final int GLOW_ID = 123;

            //Initialize
            private final GridLayout glowsGrid;

            //States
            protected boolean enabled;

            // Information
            private int glowChainIntensity;
            private int glowPadIntensity;
            private int glowChainRadius;
            private int glowPadRadius;

            public Glows() {
                this.enabled = false;
                this.glowsGrid = new GridLayout(context);
            }

            public boolean isEnabled()		{ return enabled; }
            public int chainRadius() 		{ return glowChainRadius; }
            public int padRadius()	 		{ return glowPadRadius; }
            public int chainIntensity()		{ return glowChainIntensity; }
            public int padIntensity()		{ return glowPadIntensity; }

            private ImageView newGlow(){
                ImageView glow = new ImageView(context);
                Drawable glow_drawable = context.getDrawable(R.drawable.glow);
                glow.setImageDrawable(glow_drawable);
                glow.setAlpha(0f);
                return glow;
            }

            public ImageView getGlow(int row, int colum){ return (ImageView) pads[row][colum][GLOW]; }

            public void led(int row, int colum, int color){

            }

            private void add(int row, int colum){
                GridLayout.LayoutParams mPadParams =
                        new GridLayout.LayoutParams(
                                GridLayout.spec(row, GridLayout.FILL, 1f),
                                GridLayout.spec(colum, GridLayout.FILL, 1f));
                mPadParams.height = 0;
                mPadParams.width = 0;
                this.glowsGrid.addView((View) (pads[row][colum][GLOW] = newGlow()), mPadParams);
            }

            public void setOnGlows() {
                glowsGrid.setVisibility(View.VISIBLE);
                this.enabled = true;
            }

            public void setOffGlows() {
                glowsGrid.setVisibility(View.INVISIBLE);
                this.enabled = false;
            }

            public void changeCfg(int radius, int alpha, boolean chain) {
                if(chain){
                    glowChainRadius = Math.max(radius, 1);
                    if(alpha < 0) glowChainIntensity = 0; else glowChainIntensity = Math.min(alpha, 100);
                } else {
                    if(radius < 1)  glowPadRadius = 0; else glowPadRadius = radius;
                    if(alpha < 0) glowPadIntensity = 0; else glowPadIntensity = Math.min(alpha, 100);
                }
                if(chain) forAllChildInstance(PadType.CHAIN, (pad, info) -> {
                    Log.v("Resize", info.getRow() + " " + info.getColum());
                    View glow = (View) pads[info.getRow()][info.getColum()][GLOW];
                    glow.setScaleX(glowChainRadius/100f);
                    glow.setScaleY(glowChainRadius/100f);
                });
                else forAllChildInstance(PadType.PAD, (pad, info) -> {
                    Log.v("Resize", info.getRow() + " " + info.getColum());
                    View glow = (View) pads[info.getRow()][info.getColum()][GLOW];
                    glow.setScaleX(glowPadRadius/100f);
                    glow.setScaleY(glowPadRadius/100f);
                });
                glowsGrid.requestLayout();
            }
        }

        public Pads getInstance(){ return this; }

        //Layouts identifiers
        public static final byte GRID_LAYOUT_PRO = 0; // Default
        public static final byte GRID_LAYOUT_MK2 = 1;
        public static final byte GRID_LAYOUT_UNIPAD = 2;
        public static final byte GRID_LAYOUT_MATRIX = 3;
        //pads array index
        protected final byte PAD_INFO = 0;
        protected final byte VIEW = 1;
        protected final byte GLOW = 2;
        protected byte rows;
        protected byte columns;

        public byte layout;

        protected Object[][][] pads;

        protected GridLayout mGrid;
        protected Glows glows;
        protected RelativeLayout root;

        private Pads(byte grid_rows, byte grid_columns){
            this.rows = grid_rows;
            this.columns = grid_columns;
            this.pads = new Object[grid_rows][grid_columns][6];
            this.mGrid = new GridLayout(context);
            this.glows = new Glows();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
            (this.root = new RelativeLayout(context)).addView(this.mGrid, params);
            this.root.addView(this.getGlows().glowsGrid, params);

            this.root.setClipToPadding(false);
            this.root.setClipChildren(false);
            this.mGrid.setClipToPadding(false);
            this.glows.glowsGrid.setClipToPadding(false);
            this.mGrid.setClipChildren(false);
            this.glows.glowsGrid.setClipChildren(false);

            this.layout = GRID_LAYOUT_PRO;
        }
        private void add(byte row, byte colum, View pad, byte type){
            switch (type){
                case PadType.NONE: {
                    pad.setTag(new ChildInfo(row, colum, PadType.NONE) {
                        @Override  public Pads getPads() {return getInstance();}});
                    break;
                }
                case PadType.PAD: {
                    pad.setTag(new PadInfo(row, colum, PadType.PAD) {
                        @Override  public Pads getPads() {return getInstance();}});
                    break;
                }
                case PadType.PAD_LOGO: {
                    pad.setTag(new PadInfo(row, colum, PadType.PAD_LOGO) {
                        @Override  public Pads getPads() {return getInstance();}});
                    break;
                }
                case PadType.CHAIN: {
                    pad.setTag(new ChainInfo(row, colum) {
                        @Override  public Pads getPads() {return getInstance();}});
                    break;
                }
            }
            GridLayout.LayoutParams mPadParams = new GridLayout.LayoutParams(
                            GridLayout.spec(row, GridLayout.FILL, 1f),
                            GridLayout.spec(colum, GridLayout.FILL, 1f));
            mPadParams.height = 0;
            mPadParams.width = 0;
            mGrid.addView(pad, mPadParams);
            if(pad.getTag() instanceof PadInfo) {
                pads[row][colum][PAD_INFO] = pad.getTag();
                pads[row][colum][VIEW] = pad;
                glows.add(row, colum);
            } else {
                View view = new View(context);
                view.setTag(pad.getTag());
                glows.glowsGrid.addView((View) (pads[row][colum][GLOW] = view), mPadParams);
            }
        }
        public byte getRows()                           { return rows; }
        public byte getColumns()                        { return columns; }
        public Glows getGlows()                         { return glows; }
        public GridLayout getGrid()                     { return mGrid; }
        public RelativeLayout getRoot()                 { return root; }
        public View getPadView(int row, int colum)      {
            return row < 10 && row >= 0 && colum < 10 && colum >= 0 ?
                    (View) pads[row][colum][VIEW] : null;}
        public ImageView getLed(int row, int colum)     { return getPadView(row, colum).findViewById(PadInfo.PadLayerType.LED); }
        //public int getId(int row, int colum)          { return (int) pads[row][colum][ID]; }
        public PadInfo getPadInfo(int row, int colum)   { return (PadInfo) pads[row][colum][PAD_INFO]; }


        /**
         * Switch grid layout
         */
        public void switchLayout(){
            if(this.layout == 3) this.layout = 0;
            else this.layout++;
            this.changeLayout(this.layout);
        }

        public void changeSkinLayer(@PadInfo.PadLayerType byte padLayerType, Drawable drawable){
            forAllPadsLayers((layer, padLayerTypee)->{
                Log.v("changeSkinLayer()", "padLayerType: " + String.valueOf(padLayerType) + ", padLayerTypee: " + String.valueOf(padLayerTypee));
                if(padLayerTypee == padLayerType) {
                    ((ImageView) layer).setImageDrawable(drawable);
                    return true;
                }
                return false;
            });
        }

        public void forAllPadsLayers(ForPadLayers forPadLayers){
            forAllChildInstance(ForAllChild.FOR_ALL, (pad, padInfo)-> {
                if(pad instanceof ViewGroup){
                    ViewGroup vPad = (ViewGroup) pad;
                    for(int l = 0; l < vPad.getChildCount(); l++){
                        View layer = vPad.getChildAt(l);
                        if(forPadLayers.onPadLayer(layer, (byte) layer.getId())) l = vPad.getChildCount(); //break
                    }
                }
            });
        }

        public void defaultRotationSetting(byte r, byte c) {
            View pad = getPadView(r, c);
            if(pad == null) return;
            if (r == 0 && c != 9) {
                pad.setRotation(-90);
            } else if (r == 9) {
                pad.setRotation(90);
            } else if (c == 0) {
                pad.setScaleX(-1);
            } else if (r == 4 && c == 5) {
                pad.setScaleX(-1);
            } else if (r == 5 && c == 4) {
                pad.setScaleY(-1);
            } else if (r == 5 && c == 5) {
                pad.setScaleX(-1);
                pad.setScaleY(-1);
            }
        }

        public void defaultRotationSetting(){
            forAllChildInstance(FOR_ALL, (pad, padInfo)-> defaultRotationSetting(padInfo.row, padInfo.colum));
        }

        /**
         * Change grid layout type
         * @param layout layout type. Get with MakePads.Pads.GRID_LAYOUT_....
         */
        public void changeLayout(byte layout){
            if(layout <= 3) {
                switch (layout) {
                    case GRID_LAYOUT_PRO: {
                        // Switch to PRO
                        forAllChildInstance(FOR_ALL, (pad, padInfo) -> {
                            if (padInfo.getColum() == 0 || padInfo.getColum() == getColumns()-1 ||
                                    padInfo.getRow() == 0 || padInfo.getRow() == getRows()-1) {
                                pad.setVisibility(View.VISIBLE);
                                pad.setAlpha(1f);
                                ((View) pads[padInfo.getRow()][padInfo.getColum()][GLOW]).setVisibility(View.VISIBLE);
                            }
                        });
                        //Calculator
                        getRoot().getLayoutParams().width = getRoot().getMeasuredHeight();
                        break;
                    }
                    case GRID_LAYOUT_MATRIX: {
                        // Switch to MATRIX
                        forAllChildInstance(FOR_ALL, (pad, padInfo) -> {
                            if (padInfo.getRow() == 0 || padInfo.getRow() == getRows()-1) {
                                pad.setVisibility(View.GONE);
                                ((View) pads[padInfo.getRow()][padInfo.getColum()][GLOW]).setVisibility(View.GONE);
                            } else if (padInfo.getColum() == 0 || padInfo.getColum() == getColumns()-1) {
                                pad.setAlpha(0f);
                                ((View) pads[padInfo.getRow()][padInfo.getColum()][GLOW]).setVisibility(View.INVISIBLE);
                            } else {
                                pad.setVisibility(View.VISIBLE);
                                pad.setAlpha(1f);
                                ((View) pads[padInfo.getRow()][padInfo.getColum()][GLOW]).setVisibility(View.VISIBLE);
                            }
                        });
                        //Calculator
                        getRoot().getLayoutParams().width =
                                (int) (getRoot().getMeasuredHeight() + ((getRoot().getMeasuredHeight() / 8f) * 2));
                        break;
                    }
                    case GRID_LAYOUT_MK2: {
                        // Switch to MK2
                        forAllChildInstance(FOR_ALL, (pad, padInfo) -> {
                            if (padInfo.getRow() == getRows()-1) {
                                pad.setVisibility(View.GONE);
                                ((View) pads[padInfo.getRow()][padInfo.getColum()][GLOW]).setVisibility(View.GONE);
                            } else if (padInfo.getColum() == 0) {
                                pad.setVisibility(View.GONE);
                                ((View) pads[padInfo.getRow()][padInfo.getColum()][GLOW]).setVisibility(View.GONE);
                            } else {
                                pad.setVisibility(View.VISIBLE);
                                pad.setAlpha(1f);
                                ((View) pads[padInfo.getRow()][padInfo.getColum()][GLOW]).setVisibility(View.VISIBLE);
                            }
                        });
                        //Calculator
                        getRoot().getLayoutParams().width =
                                (int) (getRoot().getMeasuredHeight());

                        break;
                    }
                    case GRID_LAYOUT_UNIPAD: {
                        // Switch to UNIPAD
                        forAllChildInstance(FOR_ALL, (pad, padInfo) -> {
                            if (padInfo.getRow() == 0 || padInfo.getRow() == getRows() -1) {
                                pad.setVisibility(View.GONE);
                                ((View) pads[padInfo.getRow()][padInfo.getColum()][GLOW]).setVisibility(View.GONE);
                            } else if (padInfo.getColum() == 0){
                                pad.setVisibility(View.VISIBLE);
                                ((View) pads[padInfo.getRow()][padInfo.getColum()][GLOW]).setVisibility(View.VISIBLE);
                            } else {
                                pad.setVisibility(View.VISIBLE);
                                pad.setAlpha(1f);
                                ((View) pads[padInfo.getRow()][padInfo.getColum()][GLOW]).setVisibility(View.VISIBLE);
                            }
                        });
                        //Calculator
                        getRoot().getLayoutParams().width =
                                (int) (getRoot().getMeasuredHeight() + ((getRoot().getMeasuredHeight() / 8f) * 2));
                        break;
                    }
                }
                getRoot().requestLayout();
                this.layout = layout;
            }
        }

        /**
         *
         * @param type child type. Use -1 for all
         * @param operation .
         */
        public void forAllChildInstance(@ForAllChild int type, ForAllPads operation){
            for(int pi = mGrid.getChildCount()-1; !(pi < 0); pi--) {
                View pad;
                ChildInfo info;
                if ((info = (ChildInfo) (pad = mGrid.getChildAt(pi)).getTag()).type == type || type == ForAllChild.FOR_ALL) {
                    operation.obtainedPad(pad, info);
                }
            }
        }

        public void setLedColor(int row, int colum, int android_color){
            PadInfo padInfo = getPadInfo(row, colum);
            if(padInfo != null && !padInfo.isActivated()) {
                getLed(row, colum).setBackgroundColor(android_color);
            }
        }
    }

    public interface ForAllPads {
        void obtainedPad(View pad, ChildInfo info);
    }

    public interface OnPadCreated {
        void padCreated(RelativeLayout pad);
    }

    /**
     * Make new Grid
     * @return Pads class
     */
    @SuppressLint("InflateParams")
    public Pads make(byte rows, byte columns, OnPadCreated onPadCreated) {
        View led;
        ImageView btn, btn_, phantom;
        TextView touch_map;
        Pads mPads = new Pads(rows, columns);
        RelativeLayout.LayoutParams pad_children_params = new RelativeLayout.LayoutParams(-1, -1);
        pad_children_params.addRule(RelativeLayout.CENTER_IN_PARENT);
        for (byte r = 0; r < rows; r++) {
            for (byte c = 0; c < columns; c++) {
                if ((r == 0 && c == 0) || (r == rows-1 && c == 0) || (r == 9 && c == columns-1)) {
                    View pad = new View(context);
                    pad.setVisibility(View.INVISIBLE);
                    mPads.add(r, c, pad, PadType.NONE);
                } else {
                    RelativeLayout pad = new RelativeLayout(context);
                    phantom = new ImageView(context);
                    (touch_map = new TextView(context)).setId(PadInfo.PadLayerType.TOUCH_MAP);
                    (btn = new ImageView(context)).setId(PadInfo.PadLayerType.BTN);
                    (btn_ = new ImageView(context)).setId(PadInfo.PadLayerType.BTN_);
                    (led = new ImageView(context)).setId(PadInfo.PadLayerType.LED);
                    btn.setScaleType(ImageView.ScaleType.FIT_XY);
                    btn_.setScaleType(ImageView.ScaleType.FIT_XY);
                    phantom.setScaleType(ImageView.ScaleType.FIT_XY);
                    //Make pad layout
                    pad.addView(btn, pad_children_params);
                    pad.addView(led, pad_children_params);
                    pad.addView(btn_, pad_children_params);
                    pad.addView(phantom, pad_children_params);
                    pad.addView(touch_map, pad_children_params);
                    pad.setId(PadID.assign(r, c));
                    if (r == 0 || r == rows-1 || c == 0 || c == columns-1) {
                        if (r == 0 && c == columns-1) {
                            mPads.add(r, c, pad, PadType.PAD_LOGO);
                            phantom.setId(PadInfo.PadLayerType.LOGO);
                        } else {
                            /*CHAIN*/
                            phantom.setId(PadInfo.PadLayerType.CHAIN_LED);
                            mPads.add(r, c, pad, PadType.CHAIN);
                            mPads.defaultRotationSetting(r, c);
                        }
                    } else {
                        mPads.add(r, c, pad, PadType.PAD);
                        if ((r == 4 || r == 5) && (c == 4 || c == 5)) {
                            phantom.setId(PadInfo.PadLayerType.PHANTOM_);
                            mPads.defaultRotationSetting(r, c);
                        } else {
                            phantom.setId(PadInfo.PadLayerType.PHANTOM);
                        }
                    }
                    if(onPadCreated != null) onPadCreated.padCreated(pad);
                }
            }
        }
        return mPads;
    }
}
