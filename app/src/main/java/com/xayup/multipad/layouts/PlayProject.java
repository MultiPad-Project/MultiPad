package com.xayup.multipad.layouts;
import java.util.Map;

public interface PlayProject {
    public void onPreStartIntent();
    public void startIntent(Map<Byte, Object> project_properties);
    public void onLoadedProject(Map<Byte, Object> project_properties);
}
