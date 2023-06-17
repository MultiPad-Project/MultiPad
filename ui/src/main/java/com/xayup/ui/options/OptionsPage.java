package com.xayup.ui.options;

import java.util.ArrayList;
import java.util.List;

public class OptionsPage extends Options {
    protected String title;

    public OptionsPage(String title){
        super();
        this.title = null;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getTitle(){
        return title;
    }

    @Override
    public void onUpdatedList(){}
}
