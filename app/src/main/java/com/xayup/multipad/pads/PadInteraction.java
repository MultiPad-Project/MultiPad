package com.xayup.multipad.pads;
import android.view.View;
import com.xayup.multipad.projects.Project;

public interface PadInteraction {
    public View.OnTouchListener onPadClick(View pad, Project project);
}
