package com.xayup.multipad.layouts;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.xayup.multipad.ProjectsAdapter;
import com.xayup.multipad.R;
import com.xayup.net.ReadCovers;
import java.util.Map;

public class CoverLayout {
    Context context;
    ScrollView scroll;
    ViewGroup layout;
    ReadCovers readCovers;
    TextView textTitle, textProducer, textDificult, textAdded, textSize;
    Button

    public CoverLayout(Context context, String layout) {
        this.context = context;
        readCovers = new ReadCovers(context);
        try {
            Resources res =
                    context.getPackageManager()
                            .getResourcesForApplication(context.getPackageName());
            this.layout =
                    (ViewGroup)
                            LayoutInflater.from(context)
                                    .inflate(
                                            res.getLayout(
                                                    res.getIdentifier(
                                                            layout,
                                                            "layout",
                                                            context.getPackageName())),
                                            null,
                                            false);
            scroll = this.layout.findViewById(R.id.cover_layout_project_list);
        } catch (PackageManager.NameNotFoundException n) {
        }
    }

    public void addToList(Map<Integer, String> project_properties) {
        ImageView cover = new ImageView(context);
        readCovers.findAndSetLocalCover(cover, project_properties.get(ProjectsAdapter.PATH));
        cover.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onCoverClicked(view);
                    }
                });
    }
    
    protected void onCoverClicked(View view){
        
    }
}
