package com.xayup.ui.options;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.xayup.ui.R;

import java.util.ArrayList;
import java.util.List;

public class OptionsLayout {
    protected final ViewGroup layout;
    protected List<OptionsPage> flipper_pages;
    protected ViewFlipper flipper;
    public OptionsLayout(Activity context){
        super();
        this.layout = (ViewGroup) context.getLayoutInflater().inflate(R.layout.flutuant_options_layout, null);
        this.flipper = this.layout.findViewById(R.id.floating_options_flipper);
    }

    public ViewGroup getLayout(){
        return layout;
    }

    public void setTitle(String title){
        ((TextView) layout.findViewById(R.id.floating_options_top_bar_title)).setText((title == null) ? "" : title);
    }

    public void switchTo(int index){
        flipper.setDisplayedChild(index);
        setTitle(flipper_pages.get(index).getTitle());
    }

    /**
     * Adicione uma nova página limpa no Flipper
     * @return Retorna o index da página, do contrario retorna -1 se houver falha
     */
    public int newPage(String title){
        if(flipper_pages.add(new OptionsPage(title){
            @Override
            public void onUpdatedList(){
                flipper.removeAllViews();
            }
        })){
            return flipper_pages.size()-1;
        }
        return -1;
    }

    public OptionsPage getPage(int index){
        return flipper_pages.get(index);
    }

    public void getBackButton(){
        layout.findViewById(R.id.floating_options_top_bar_button_prev);
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
}
