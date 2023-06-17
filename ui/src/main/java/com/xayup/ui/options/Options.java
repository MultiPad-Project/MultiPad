package com.xayup.ui.options;

import java.util.ArrayList;
import java.util.List;

public abstract class Options {
    protected List<OptionsItemInterface> optionsList;

    public Options(){
        optionsList = new ArrayList<>();
    }

    public void putOption(OptionsItemInterface options){
        optionsList.add(options);
        onUpdatedList();
    }
    public OptionsItemInterface getOptionFromIndex(int index){
        return optionsList.get(index);
    }
    public List<OptionsItemInterface> getOptionsList(){
        return optionsList;
    }

    public abstract void onUpdatedList();
}
