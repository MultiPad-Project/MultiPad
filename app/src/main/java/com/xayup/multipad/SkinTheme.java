package com.xayup.multipad;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SkinTheme {
	private static List<PackageInfo> listSkinsPackage;

	public static Drawable phantom_, phantom, chainled, btn, btn_, playBg, customLogo, practice, led,
			chain, chain__; //old_unipad_skin

	public PackageInfo current_Skin;

	Activity context;
	ListView listSkins;

	public SkinTheme(Activity context, ListView listSkins) {
		this.context = context;
		this.listSkins = listSkins;
		listSkinsPackage = new ArrayList<>();
	}

	public static void varInstance() {
		listSkinsPackage = new ArrayList<>();
	}

	/**
	 * Carrega a skin pelo nome de pacote. Caso essa skin não exista, a skin padrão será carregada.
	 * @param skin_package_name nome do pacote da skin (com.kimjisub.launchpad.theme...).
	 * @return false se a skin não existir.
	 */
	public static boolean loadSkin(Context context, String skin_package_name) {
		Resources res;
		boolean result;
		try {
			res = context.getPackageManager().getResourcesForApplication(skin_package_name);
			result = true;
		} catch (PackageManager.NameNotFoundException ignore){
			res = context.getResources();
			skin_package_name = context.getPackageName();
			result = false;
		}

		practice = new ColorDrawable(Color.GREEN);

		int tmp_id;
		// playbg
		playBg = res.getDrawable(
				((tmp_id = res.getIdentifier("playbg_pro", "drawable", skin_package_name)) == 0) ?
						res.getIdentifier("playbg", "drawable", skin_package_name) : tmp_id
				, null);

		//chainled
		chainled = null;
		chain = null;
		led = null;
		chain__ = null;
		if((tmp_id = res.getIdentifier("chainled", "drawable", skin_package_name)) == 0){
			// Support Old Skins
			chain = res.getDrawable(res.getIdentifier("chain", "drawable", skin_package_name), null);
			led = res.getDrawable(res.getIdentifier("chain_", "drawable", skin_package_name), null);
			chain__ = res.getDrawable(res.getIdentifier("chain__", "drawable", skin_package_name), null);
		} else {
			chainled = res.getDrawable(tmp_id, null);
			led = new ColorDrawable(Color.WHITE);
		}

		if((tmp_id = res.getIdentifier("applogo", "drawable", skin_package_name)) != 0
		|| (tmp_id = res.getIdentifier("logo", "drawable", skin_package_name)) != 0
		|| (tmp_id = res.getIdentifier("custom_logo", "drawable", skin_package_name)) != 0
		|| (tmp_id = res.getIdentifier("theme_ic", "drawable", skin_package_name)) != 0)
			customLogo = res.getDrawable(tmp_id, null);
		else customLogo = context.getDrawable(
				context.getResources().getIdentifier("customlogo", "drawable", context.getPackageName()));

		//btn
		btn = res.getDrawable(res.getIdentifier("btn", "drawable", skin_package_name), null);

		//btn_
		btn_ = res.getDrawable(res.getIdentifier("btn_", "drawable", skin_package_name), null);

		//phantom
		phantom = res.getDrawable(res.getIdentifier("phantom", "drawable", skin_package_name), null);

		//phantom_
		phantom_ = res.getDrawable(res.getIdentifier("phantom_", "drawable", skin_package_name), null);
		return result;
	}

	public static void cachedSkinSet(Context context) {
		SharedPreferences skinSaved = context.getSharedPreferences("app_configs", Context.MODE_PRIVATE);
		if(!loadSkin(context, skinSaved.getString("skin", context.getPackageName()))){
			skinSaved.edit().putString("skin", context.getPackageName()).apply();
		}
	}

	public void updateListSkin() {
		listSkins.setEnabled(true);
		listSkins.setAdapter(getListSkinsAdapter());
	}

	private SkinThemeAdapter getListSkinsAdapter() {
		final List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);

		// Default skin (First)
		try { listSkinsPackage.add(context.getPackageManager().getPackageInfo(context.getPackageName(), 0)); }
		catch (PackageManager.NameNotFoundException ignore){}
		// Custom Skins
		for (PackageInfo pi : packages) {
			if (pi.packageName.contains("com.kimjisub.launchpad.theme.")) {
				listSkinsPackage.add(pi);
			}
		}
		return new SkinThemeAdapter(context, listSkinsPackage);
	}
}