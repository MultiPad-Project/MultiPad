package com.xayup.ui.options;

import android.view.View;

public interface OptionsInterface {
    int TYPE_CHECKBOX = 0;
    int TYPE_CLICKABLE = 1;

    View getItemView();
    int getType();
    void setTitle(String title);
    void setDescription(String desc);
    void setOnClick(View.OnClickListener onClick);
    void setOnLongClick(View.OnLongClickListener onClick);
    boolean enabled();
}