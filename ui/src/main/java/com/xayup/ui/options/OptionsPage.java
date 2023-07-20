package com.xayup.ui.options;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

public class OptionsPage extends Options {
    private byte index;
    protected String title;
    protected LinearLayout page;
    protected ScrollView scroll;
    protected boolean with_scroll;

    public OptionsPage(Context context, boolean with_scroll){
        super();
        this.title = null;
        this.page = new LinearLayout(context);
        this.scroll = new ScrollView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        this.page.setLayoutParams(params);
        this.page.setOrientation(LinearLayout.VERTICAL);
        this.with_scroll = with_scroll;
        if(with_scroll) this.scroll.addView(page);
    }

    /**
     * Define a "Title" that will be returned by "getTitle()" in the future
     * @param title the title
     */
    public void setTitle(String title){
        this.title = title;
    }

    public String getTitle(){
        return title;
    }

    public View getPageView(){
        return (with_scroll) ? scroll : page;
    }

    public void clear(){
        page.removeAllViews();
        optionsList.clear();
    }

    public byte getPageIndex(){
        return index;
    }
    public void setPageIndex(byte index){
        this.index = index;
    }


    @Override
    public void onUpdatedList(){
        page.removeAllViews();
        for(OptionsItemInterface item : optionsList){
            page.addView(((OptionsItem) item).getItemView());
        }
    }
}
