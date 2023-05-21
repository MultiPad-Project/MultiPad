package com.xayup.multipad.layouts;

import android.content.Context;
import android.content.res.Resources;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import java.util.Map;

public class CoverLayout {
	Context context;
	ScrollView scroll;
	ViewGroup layout;
	
	public CoverLayout(Context context, String layout){
		this.context = context;
		Resources res = context.getPackageManager().getResourcesForApplication(context.getPackageName());
		this.layout = (ViewGroup) LayoutInflater.from(context).inflate(res.getLayout(res.getIdentifier(layout, "layout", context.getPackageName())), null, false);
		scroll = this.layout.getResources()
	}
	
	public void addToList(Map<Integer, String> project_properties){
		
	}
}