package com.xayup.alertdialog;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xayup.launchpadplus.R;
import java.util.List;

public class skinthmeAdapter extends BaseAdapter {
	List<PackageInfo> packageinfo;
	Activity context;

	public skinthmeAdapter(Activity context, List<PackageInfo> packageinfo) {
		this.context = context;
		this.packageinfo = packageinfo;
	}

	@Override
	public int getCount() {
		return packageinfo.size();
	}

	@Override
	public Object getItem(int position) {
		return packageinfo.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.skinstheme_layout, parent, false);
		ImageView skinLogo = convertView.findViewById(R.id.skinthemelogo);
		TextView skinName = convertView.findViewById(R.id.skinthemeName);
		TextView skinVersion = convertView.findViewById(R.id.skinthemeVersion);
		if(position != 0){
		try {
			Resources skinResource = context.getPackageManager().getResourcesForApplication(packageinfo.get(position).packageName);
			skinLogo.setImageDrawable(skinResource.getDrawable(2130837615));
			skinName.setText(packageinfo.get(position).applicationInfo.loadLabel(context.getPackageManager()));
			skinVersion.setText(packageinfo.get(position).versionName);
		} catch (PackageManager.NameNotFoundException n){}
		} else{
			skinLogo.setImageDrawable(context.getDrawable(R.drawable.ic_launcher));
			skinName.setText(context.getString(R.string.default_skin));
			skinVersion.setText(context.getString(R.string.app_version));
		}

		return convertView;
	}

}