package com.xayup.ui.options;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.xayup.ui.R;

public class OptionsItem implements OptionsItemInterface {
    protected final View layout;
    protected int type;
    public Object tag;

    public OptionsItem(Context context, int type){
        this.type = type;
        switch (type){
            case TYPE_SIMPLE_WITH_CHECKBOX:{
                this.layout = LayoutInflater.from(context).inflate(R.layout.flutuant_options_item_layout, null, false);
                ((ViewGroup) this.layout.findViewById(R.id.options_item_identify_container)).removeView(this.layout.findViewById(R.id.options_item_identify_arrow));
                break;
            }
            case TYPE_CENTER_WITH_IMAGE:{
                this.layout = LayoutInflater.from(context).inflate(R.layout.options_item_center_with_image, null, false);
                break;
            }
            case TYPE_SIMPLE:{
                this.layout = LayoutInflater.from(context).inflate(R.layout.flutuant_options_item_layout, null, false);
                ((ViewGroup) this.layout.findViewById(R.id.options_item_identify_container)).removeView(this.layout.findViewById(R.id.options_item_identify_arrow));
                ((ViewGroup) this.layout.findViewById(R.id.options_item_identify_container)).removeView(this.layout.findViewById(R.id.options_item_identify_checkbox));
                break;
            }
            default /*TYPE_SIMPLE_WITH_ARROW*/:{
                this.layout = LayoutInflater.from(context).inflate(R.layout.flutuant_options_item_layout, null, false);
                ((ViewGroup) this.layout.findViewById(R.id.options_item_identify_container)).removeView(this.layout.findViewById(R.id.options_item_identify_checkbox));
                break;
            }
        }
    }

    public void setTag(Object tag){
        this.tag = tag;
    }
    public Object getTag(){
        return this.tag;
    }

    public void setChecked(boolean checked){
        getCheckBox().setChecked(checked);
    }

    public CheckBox getCheckBox(){
        return ((CheckBox) this.layout.findViewById(R.id.options_item_identify_checkbox));
    }

    public void setImg(Drawable img){
        ((ImageView) this.layout.findViewById(R.id.options_item_with_image_image)).setImageDrawable(img);
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
        ((TextView) layout.findViewById(R.id.options_item_title)).setText(title);
    }

    @Override
    public void setDescription(String desc) {
        ((TextView) layout.findViewById(R.id.options_item_description)).setText(desc);
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
