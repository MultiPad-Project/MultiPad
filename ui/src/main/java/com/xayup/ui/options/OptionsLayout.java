package com.xayup.ui.options;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.xayup.ui.R;

import java.util.List;

public class OptionsLayout extends Options {
    protected final ViewGroup layout;
    protected List<List<OptionsInterface>> flipper_pages;
    public OptionsLayout(Activity context){
        super();
        this.layout = (ViewGroup) context.getLayoutInflater().inflate(R.layout.flutuant_options_layout, null);
    }

    public ViewGroup getLayout(){
        return layout;
    }

    public void setTitle(String title){
        ((TextView) layout.findViewById(R.id.floating_options_top_bar_title)).setText(title);
    }

    public void addViewToBottomBar(View view){
        ((ViewGroup) layout.findViewById(R.id.floating_options_bottom_bar)).addView(view);
    }

    public void addViewToBottomBar(View view, int index){
        ((ViewGroup) layout.findViewById(R.id.floating_options_bottom_bar)).addView(view);
    }

    public void addViewToBottomBar(View view, int index, ViewGroup.LayoutParams params){
        ((ViewGroup) layout.findViewById(R.id.floating_options_bottom_bar)).addView(view, index, params);
    }

    public void addViewToBottomBar(View view, int width, int height){
        ((ViewGroup) layout.findViewById(R.id.floating_options_bottom_bar)).addView(view, width, height);
    }

    @Override
    public void onUpdatedList() {

    }
}
