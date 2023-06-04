package com.xayup.multipad.pads.Render;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import com.xayup.multipad.skin.SkinData;
import com.xayup.multipad.skin.SkinProperties;
import com.xayup.multipad.skin.SkinVariables;

public class SkinManager implements SkinVariables {

    public static Activity context;
    public static Resources mResources;

    public SkinManager(Context context) {
        SkinManager.context = (Activity) context;
    }
    
    public SkinProperties getPropertiesFromPackage(String package_name, boolean and_resources){
        return new SkinProperties(getSkinInfo(package_name, and_resources));
    }
    public static Object[] getSkinInfo(String package_name, boolean and_resources) {
        setCurrentResource(package_name);
        Object[] skin = new Object[ARRAY_SIZE];
        skin[SKIN_PACKAGE] = package_name;
        skin[SKIN_LOGO] =
                mResources.getDrawable(
                        mResources.getIdentifier("theme_ic", "drawable", package_name), null);
        skin[SKIN_NAME] =
                mResources.getString(
                        mResources.getIdentifier("theme_name", "string", package_name));
        skin[SKIN_AUTHOR] =
                mResources.getString(
                        mResources.getIdentifier("theme_author", "string", package_name));
        if (and_resources) skin[SKIN_RESOURCES] = mResources;
        return skin;
    }
    
    /**
    * @return true if success.
    */
    public static boolean setCurrentResource(String package_name) {
        try {
            mResources = context.getPackageManager().getResourcesForApplication(package_name);
            return true;
        } catch (PackageManager.NameNotFoundException n) {
            mResources = context.getResources();
            return false;
        }
    }

    public void loadSkin(SkinProperties skin, SkinData mSkinData, LoadedSkin onLoaded) {
        mSkinData.loadFromResources(skin.resources, skin.package_name);
        if (onLoaded != null) onLoaded.onLoaded(skin);
    }

    public interface LoadedSkin {
        public void onLoaded(SkinProperties skin);
    }
}
