package com.xayup.multipad.pads.Render;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.xayup.debug.XLog;
import com.xayup.multipad.R;
import com.xayup.multipad.pads.Pad;
import com.xayup.multipad.pads.PadInterface;
import com.xayup.utils.Utils;

public class MakePads {
    protected Activity context;
    protected int rows, colums, height = 0;
    protected PadInterface mPadInterface;
    protected ViewGroup.LayoutParams mGridParams;
    protected GridLayout.LayoutParams mPadParams;
    protected Matrix chain_top_matrix, chain_bottom_matrix;

    public static class PadInfo {
        public interface PadInfoIdentifier {
            byte CHAIN = 0;
            byte PAD = 1;
            byte PHANTOM = 2;
            byte PHANTOM_ = 3;
            byte BTN = 4;
            byte BTN_ = 5;
            byte PAD_LOGO = 6;
            byte LOGO = 7;
            byte CHAIN_LED = 8;
            byte LED = 9;
            byte TOUCH_MAP = 10;
        }

        public final byte row;
        public final byte colum;
        public final byte type;
        /**
         * mc is -1 if pad and not chain
         */

        /**
         * @param padinfo {row, colum, type}
         */
        protected PadInfo(byte[] padinfo) {
            this.row = padinfo[0];
            this.colum = padinfo[1];
            this.type = padinfo[2];
        }

        public int getId(){ return PadID.getId(row, colum); }
    }

    public static class ChainInfo extends PadInfo {
        protected byte mc;
        public ChainInfo(int row, int colum){
            super(new byte[]{(byte) row, (byte) colum, PadInfoIdentifier.CHAIN});
            if(this.type == PadInfoIdentifier.CHAIN){
                this.mc = (byte) PadID.getChainMc(row, colum, 9);
            } else {
                this.mc = -1;
            }
        }

        public void setMc(int row, int colum) {
            this.mc = (byte) PadID.getChainMc(row, colum, 9);
        }

        public int getMc(){
            return mc;
        };
    }
    /** Obtenha o id da pad atraves de suas cordenada X e Y */
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

        private static void putId(int x, int y) {
            ids[x][y] = (byte) ((x * 10) + y);
        }
    }

    public MakePads(Context context, int rows, int colums) {
        this.context = (Activity) context;
        this.rows = rows;
        this.colums = colums;
        chain_top_matrix = new Matrix();
        chain_bottom_matrix = new Matrix();
    }

    public MakePads(Context context) {
        this.context = (Activity) context;
    }

    /**
     *
     * @return
     */
    public ViewGroup make() {
        View led;
        ImageView btn, btn_, phantom, playbg;
        TextView touch_map;
        RelativeLayout grid_root = new RelativeLayout(context);
        GridLayout mGrid = new GridLayout(context);
        playbg = new ImageView(context);
        playbg.setScaleType(ImageView.ScaleType.CENTER);
        RelativeLayout.LayoutParams rLayout = new RelativeLayout.LayoutParams(-1, -1);
        grid_root.addView(playbg, rLayout);
        rLayout = new RelativeLayout.LayoutParams(-2, -2);
        rLayout.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        grid_root.addView(mGrid, rLayout);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < colums; c++) {
                View pad;
                mPadParams =
                        new GridLayout.LayoutParams(
                                GridLayout.spec(r, GridLayout.FILL, 1f),
                                GridLayout.spec(c, GridLayout.FILL, 1f));
                mPadParams.height = 0;
                mPadParams.width = 0;
                if ((r == 0 && c == 0) || (r == 9 && c == 0) || (r == 9 && c == 9)) {
                    pad = new View(context);
                    pad.setVisibility(View.INVISIBLE);
                    pad.setTag("");
                } else {
                    pad = context.getLayoutInflater().inflate(R.layout.pad, null, false);
                    btn = pad.findViewById(R.id.pad);
                    btn_ = pad.findViewById(R.id.press);
                    phantom = pad.findViewById(R.id.phantom);
                    led = pad.findViewById(R.id.led);
                    touch_map = pad.findViewById(R.id.touch_map);
                    touch_map.setTag(
                            new PadInfo(
                                    new byte[] {
                                        (byte) r, (byte) c, PadInfo.PadInfoIdentifier.TOUCH_MAP
                                    }));
                    if (r == 0 || r == 9 || c == 0 || c == 9) {
                        if (r == 0 && c == 9) {
                            pad.setTag(
                                    new PadInfo(
                                            new byte[] {
                                                (byte) r,
                                                (byte) c,
                                                PadInfo.PadInfoIdentifier.PAD_LOGO
                                            }));
                            phantom.setTag(
                                    new PadInfo(
                                            new byte[] {
                                                (byte) r, (byte) c, PadInfo.PadInfoIdentifier.LOGO
                                            }));
                        } else {
                            phantom.setTag(
                                    new PadInfo(
                                            new byte[] {
                                                (byte) r,
                                                (byte) c,
                                                PadInfo.PadInfoIdentifier.CHAIN_LED
                                            }));
                            pad.setTag(
                                    new PadInfo(
                                            new byte[] {
                                                    (byte) r,
                                                    (byte) c,
                                                    PadInfo.PadInfoIdentifier.CHAIN
                                            }));
                            if (r == 0) { pad.setRotation(-90); }
                            else if (r == 9) { pad.setRotation(90); }
                            else if (c == 0) { phantom.setScaleX(phantom.getScaleX() * -1); }
                        }
                    } else {
                        pad.setTag(
                                new PadInfo(
                                        new byte[] {
                                            (byte) r, (byte) c, PadInfo.PadInfoIdentifier.PAD
                                        }));
                        if ((r == 4 || r == 5) && (c == 4 || c == 5)) {
                            phantom.setTag(
                                    new PadInfo(
                                            new byte[] {
                                                (byte) r,
                                                (byte) c,
                                                PadInfo.PadInfoIdentifier.PHANTOM_
                                            }));
                            if (r == 4 && c == 5) {
                                phantom.setScaleX(phantom.getScaleX() * -1);
                            } else if (r == 5 && c == 4) {
                                phantom.setScaleY(phantom.getScaleY() * -1);
                            } else if (r == 5 && c == 5) {
                                phantom.setScaleX(phantom.getScaleX() * -1);
                                phantom.setScaleY(phantom.getScaleY() * -1);
                            }
                        } else {
                            phantom.setTag(
                                    new PadInfo(
                                            new byte[] {
                                                (byte) r,
                                                (byte) c,
                                                PadInfo.PadInfoIdentifier.PHANTOM
                                            }));
                        }
                    }
                    btn.setTag(
                            new PadInfo(
                                    new byte[] {
                                        (byte) r, (byte) c, PadInfo.PadInfoIdentifier.BTN
                                    }));
                    btn_.setTag(
                            new PadInfo(
                                    new byte[] {
                                        (byte) r, (byte) c, PadInfo.PadInfoIdentifier.BTN_
                                    }));
                    led.setTag(
                            new PadInfo(
                                    new byte[] {
                                        (byte) r, (byte) c, PadInfo.PadInfoIdentifier.LED
                                    }));
                    PadID.putId(r, c);
                    pad.setId(PadID.getId(r, c));
                }
                mGrid.addView(pad, mPadParams);
            }
        }
        return grid_root;
    }
}
