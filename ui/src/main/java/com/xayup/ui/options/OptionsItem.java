package com.xayup.ui.options;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import com.xayup.ui.R;

public class OptionsItem implements OptionsItemInterface {
    protected final View layout;
    protected int type;

    public OptionsItem(Activity context, int type){
        this.type = type;
        switch (type){
            case TYPE_SIMPLE:{
                this.layout = context.getLayoutInflater().inflate(R.layout.flutuant_options_item_layout, null, false);
                break;
            }
            case TYPE_SIMPLE_WITH_CHECKBOX:{
                this.layout = context.getLayoutInflater().inflate(R.layout.flutuant_options_item_layout, null, false);
                break;
            }
            case TYPE_CENTER_WITH_IMAGE:{
                this.layout = context.getLayoutInflater().inflate(R.layout.flutuant_options_item_layout, null, false);
                break;
            }
        }
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
        ((TextView) layout.findViewById(R.id.floating_options_item_title)).setText(title);
    }

    @Override
    public void setDescription(String desc) {
        ((TextView) layout.findViewById(R.id.floating_options_item_description)).setText(desc);
    }

    @Override
    public void setOnClick(View.OnClickListener onClick) {
        layout.setOnClickListener(onClick);
    }

    @Override
    public void setOnLongClick(View.OnLongClickListener onLongClick) {
        layout.setOnLongClickListener(onLongClick);

    }

    @Override
    public void setEnabled(boolean enable){
        if(enable){
            layout.setAlpha(1.0f);
        } else {
            layout.setAlpha(0.4f);
        }
        layout.setEnabled(enable);
    }

    @Override
    public boolean enabled() {
        return layout.isEnabled();
    }
}
