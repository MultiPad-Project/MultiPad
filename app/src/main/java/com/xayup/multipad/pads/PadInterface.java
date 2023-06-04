package com.xayup.multipad.pads;
import android.view.View;

public abstract interface PadInterface {
        public int getDisplayHeigth();
        public int getPadsColums();
        public int getPadsRows();
        public int getAutoPlayButtonId();
        public int getWaterMarkButtonId();
        public int getLedSwitchButton();
        public int getPadsLayoutSwitchButtonId();
        public void padClick(View pad);
        public void chainClick(View pad);
        public boolean glowEnabled();
}
