package com.xayup.ui.options;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import com.xayup.ui.R;

public class OptionsItem implements  OptionsInterface {
    protected final View layout;
    protected int type;

    public OptionsItem(Activity context){
        this.layout = context.getLayoutInflater().inflate(R.layout.flutuant_options_item_layout, null, false);
        this.type = TYPE_CLICKABLE;
    }

    @Override
    public View getItemView() {
        return layout;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setTitle(String title) {
        ((TextView) layout.findViewById(0000)).setText(title);
    }

    @Override
    public void setDescription(String desc) {

    }

    @Override
    public void setOnClick(View.OnClickListener onClick) {

    }

    @Override
    public void setOnLongClick(View.OnLongClickListener onClick) {

    }

    @Override
    public boolean enabled() {
        return false;
    }
}
