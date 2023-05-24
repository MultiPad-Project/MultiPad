package com.xayup.multipad.load;
import com.xayup.multipad.ProjectsAdapter;
import java.util.Map;

public class Project {
    public String title = "";
    public String produceName = "";
    public String path = "";
    public String state = "";
    
    public final int TITLE = 0;
    public final int PRODUCER_NAME = 1;
    public final int PATH = 2;
    public final int STATE = 3;
    
    public Map<Integer, String> project_properties;
    
    public void onProjectClicked(Map<Integer, String> project_properties){
        this.project_properties = project_properties;
        this.title = this.project_properties.get(TITLE);
        this.produceName = this.project_properties.get(PRODUCER_NAME);
        this.path = this.project_properties.get(PATH);
        this.state = this.project_properties.get(STATE);        
    }
}
