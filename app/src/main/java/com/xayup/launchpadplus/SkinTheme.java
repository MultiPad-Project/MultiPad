package com.xayup.launchpadplus;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.widget.AdapterView;
import android.graphics.drawable.Drawable;
import android.widget.ListView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.xayup.launchpadplus.playPads;

import java.util.ArrayList;
import java.util.List;
import com.xayup.alertdialog.skinthmeAdapter;

public class SkinTheme {
	
	boolean lodedSkin = false;
	
	private static List<PackageInfo> listSkinsPackage;
	
	skinthmeAdapter skinlistadapter;
	
	public static Drawable phantomCenter, phantons, chains, btn, btn_, playBg, customLogo;
	
	public static List<ImageView> pads, padsCenter, chainsled, btnlist;
	
	public static ImageView logo;
	
	private static boolean inplayPads;
	
	private View bg;
	public static ImageView playBgimg;
	
	public static String skin;
	static Activity context;
	ListView listSkins;
	
	public SkinTheme(Activity context, ListView listSkins, boolean inplayPads){
		this.context = context;
		this.listSkins = listSkins;
		this.inplayPads = inplayPads;
		this.listSkinsPackage = new ArrayList<PackageInfo>();
	}
	
	public static void varInstance(boolean inplayPads) {
		pads = new ArrayList<ImageView>();
		padsCenter = new ArrayList<ImageView>();
		chainsled = new ArrayList<ImageView>();
		btnlist = new ArrayList<ImageView>();
		listSkinsPackage = new ArrayList<PackageInfo>();
		
	//	if(inplayPads) playBgimg = context.findViewById(R.id.playbgimg);
	}
	
	private static boolean setResources(Activity context, boolean byPackageName, int arg2, String packageName){
		try{
			Resources skinRes;
			if(byPackageName){
				skinRes = context.getPackageManager()
				.getResourcesForApplication(packageName);
			}else{
				skinRes = context.getPackageManager()
			.getResourcesForApplication(listSkinsPackage.get(arg2).packageName);
			}
			Drawable bgDrawable;
			try {
				bgDrawable = skinRes.getDrawable(2130837633);
				} catch (Resources.NotFoundException nfr) {
				bgDrawable = skinRes.getDrawable(2130837612);
			}
			setDrawables(skinRes.getDrawable(2130837609), skinRes.getDrawable(2130837608),
			skinRes.getDrawable(2130837610), skinRes.getDrawable(2130837588),
			skinRes.getDrawable(2130837589), bgDrawable, skinRes.getDrawable(2130837593));
			return true;
			}catch (PackageManager.NameNotFoundException nnfe) {
			return false;
		}
	}
	
	public static void cachedSkinSet(Activity context){
		SharedPreferences skinSaved = context.getSharedPreferences("app_configs", context.MODE_PRIVATE);
		skin = skinSaved.getString("skin", "");
		SharedPreferences.Editor getSkinSaved = skinSaved.edit();
		switch(skin){
			case "":
				
				getSkinSaved.putString("skin", "default");
				
			case "default":
				setDrawables(context.getDrawable(R.drawable.phantom_), context.getDrawable(R.drawable.phantom),
				context.getDrawable(R.drawable.chainled), context.getDrawable(R.drawable.btn), context.getDrawable(R.drawable.btn_),
				context.getDrawable(R.drawable.playbg), context.getDrawable(R.drawable.customlogo));
				break;
			default:
				if(!setResources(context, true, 0, skin)){
					getSkinSaved.putString("skin", "default");
				}
				break;
		}
		getSkinSaved.commit();
	}
	
	private static void setDrawables(Drawable phatomCente, Drawable phanton, Drawable chain, Drawable btnn, Drawable btnn_,
	Drawable playBgg, Drawable customLogoo) {
		phantomCenter = phatomCente;
		phantons = phanton;
		chains = chain;
		btn = btnn;
		btn_ = btnn_;
		playBg = playBgg;
		customLogo = customLogoo;
	}
	
	public void getSkinsTheme(){
		
	//	if (!listSkins.isEnabled()) {
			skinlistadapter = getSkins();
			listSkins.setEnabled(true);
			
			
	//	}
		
		listSkins.setAdapter(skinlistadapter);
		listSkins.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				SharedPreferences app_configs = context.getSharedPreferences("app_configs", context.MODE_PRIVATE);
				SharedPreferences.Editor editConfigs = app_configs.edit();
				editConfigs.putString("skin", listSkinsPackage.get(arg2).packageName);
					if (arg2 != 0) {
						if(!setResources(context, false, arg2, "")){
							editConfigs.putString("skin", "default");
							}
						} else {
						setDrawables(context.getDrawable(R.drawable.phantom_), context.getDrawable(R.drawable.phantom),
						context.getDrawable(R.drawable.chainled), context.getDrawable(R.drawable.btn),
						context.getDrawable(R.drawable.btn_), context.getDrawable(R.drawable.playbg),
						context.getDrawable(R.drawable.customlogo));
						editConfigs.putString("skin", "default");
					}
					editConfigs.commit();
					if(inplayPads){
					for (int i = 0; i < pads.size(); i++) {
						pads.get(i).setImageDrawable(phantons);
					}
					for (int i = 0; i < padsCenter.size(); i++) {
						padsCenter.get(i).setImageDrawable(phantomCenter);
					}
					for (int i = 0; i < chainsled.size(); i++) {
						chainsled.get(i).setImageDrawable(chains);
					}
					for (int i = 0; i < btnlist.size(); i++) {
						btnlist.get(i).setImageDrawable(btn);
					}
					logo.setImageDrawable(customLogo);
					playBgimg.setImageDrawable(playBg);
					}
					//makePads.phantom.setImageDrawable(phantons);
				
			}
			
		});
	}
	
	private skinthmeAdapter getSkins() {
		final List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
		for (PackageInfo pi : packages) {
			if (pi.packageName.contains("com.kimjisub.launchpad.theme.")) {
				listSkinsPackage.add(pi);
			}
		}
		return new skinthmeAdapter(context, listSkinsPackage);
	}
}