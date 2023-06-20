package com.xayup.ui.options;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

public class OptionsPage extends Options {
    protected String title;
    protected LinearLayout page;
    protected ScrollView scroll;

    public OptionsPage(Context context){
        super();
        this.title = null;
        this.page = new LinearLayout(context);
        this.scroll = new ScrollView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        this.page.setLayoutParams(params);
        this.page.setOrientation(LinearLayout.VERTICAL);
        this.scroll.addView(page);
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getTitle(){
        return title;
    }

    public View getPageView(){
        return scroll;
    }

    public void clear(){
        page.removeAllViews();
        optionsList.clear();
    }

    @Override
    public void onUpdatedList(){
        page.removeAllViews();
        for(OptionsItemInterface item : optionsList){
            page.addView(((OptionsItem) item).getItemView());
        }
    }
}
