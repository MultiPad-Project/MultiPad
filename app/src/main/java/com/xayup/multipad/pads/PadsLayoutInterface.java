package com.xayup.multipad.pads;
import android.view.ViewGroup;
import android.widget.GridLayout;
import com.xayup.multipad.skin.SkinData;

public interface PadsLayoutInterface {
    public SkinData getSkinData();
    public ViewGroup getRootPads();
    public GridLayout getGridPads();
}
