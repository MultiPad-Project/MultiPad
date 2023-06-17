package com.xayup.ui.options;

import java.util.ArrayList;
import java.util.List;

public abstract class Options {
    protected List<OptionsInterface> optionsList;

    public Options(){
        optionsList = new ArrayList<>();
    }

    public void putOption(OptionsInterface options){
        optionsList.add(options);
        onUpdatedList();
    }
    public OptionsInterface getOptionFromIndex(int index){
        return optionsList.get(index);
    }
    public List<OptionsInterface> getOptionsList(){
        return optionsList;
    }

    public abstract void onUpdatedList();
}
