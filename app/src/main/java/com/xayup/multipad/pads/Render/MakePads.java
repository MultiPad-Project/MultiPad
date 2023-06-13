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
import com.xayup.multipad.pads.PadInterface;

public class MakePads {
    protected Activity context;
    protected int rows, colums, height = 0;
    protected PadInterface mPadInterface;
    protected ViewGroup.LayoutParams mGridParams;
    protected GridLayout.LayoutParams mPadParams;
    protected Matrix chain_top_matrix, chain_bottom_matrix;

    public static class PadInfo {
        public interface PadInfoIdentifier {
            public final byte CHAIN_TOP = 0;
            public final byte CHAIN_LEFT = 1;
            public final byte CHAIN_RIGHT = 2;
            public final byte CHAIN_BOTTOM = 3;
            public final byte PAD = 4;
            public final byte PHANTOM = 5;
            public final byte PHANTOM_ = 6;
            public final byte BTN = 7;
            public final byte BTN_ = 8;
            public final byte PAD_LOGO = 9;
            public final byte LOGO = 10;
            public final byte CHAIN_LED = 11;
            public final byte LED = 12;
            public final byte TOUCH_MAP = 13;
        }

        public final byte row;
        public final byte colum;
        public final byte type;

        protected PadInfo(byte[] padinfo) {
            this.row = padinfo[0];
            this.colum = padinfo[1];
            this.type = padinfo[2];
        }
    }
    /** Obtenha o id da pad atraves de suas cordenada X e Y */
    public static class PadID {
        public static byte[ /*X*/][ /*Y*/] ids = new byte[10][10];

        public static int getId(int x, int y) {
            return ids[x][y];
        }

        public static int getChainId(int mc) {
            if (mc > 24) {
                return getId(33 - mc, 0);
            } else if (mc > 16) {
                return getId(9, 25 - mc);
            } else if (mc > 8) {
                return getId(mc - 8, 9);
            } else {
                return getId(0, mc);
            }
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

    public ViewGroup make(PadSkinData mSkinData) {
        View led;
        ImageView btn, btn_, phantom, playbg;
        TextView touch_map;
        RelativeLayout grid_root = new RelativeLayout(context);
        GridLayout mGrid = new GridLayout(context);
        playbg = new ImageView(context);
        playbg.setImageDrawable(mSkinData.draw_playbg);
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
                    btn_.setVisibility(View.INVISIBLE);
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

                            phantom.setImageDrawable(mSkinData.draw_chainled);
                            phantom.setTag(
                                    new PadInfo(
                                            new byte[] {
                                                (byte) r,
                                                (byte) c,
                                                PadInfo.PadInfoIdentifier.CHAIN_LED
                                            }));
                            if (r == 0) {
                                pad.setRotation(-90);
                                pad.setTag(
                                        new PadInfo(
                                                new byte[] {
                                                    (byte) r,
                                                    (byte) c,
                                                    PadInfo.PadInfoIdentifier.CHAIN_TOP
                                                }));
                            } else if (r == 9) {
                                pad.setRotation(90);
                                pad.setTag(
                                        new PadInfo(
                                                new byte[] {
                                                    (byte) r,
                                                    (byte) c,
                                                    PadInfo.PadInfoIdentifier.CHAIN_BOTTOM
                                                }));
                            } else if (c == 0) {
                                phantom.setScaleX(phantom.getScaleX() * -1);
                                pad.setTag(
                                        new PadInfo(
                                                new byte[] {
                                                    (byte) r,
                                                    (byte) c,
                                                    PadInfo.PadInfoIdentifier.CHAIN_LEFT
                                                }));
                            } else {
                                pad.setTag(
                                        new PadInfo(
                                                new byte[] {
                                                    (byte) r,
                                                    (byte) c,
                                                    PadInfo.PadInfoIdentifier.CHAIN_RIGHT
                                                }));
                            }
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
