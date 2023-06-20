package com.xayup.multipad.skin;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import com.xayup.multipad.skin.SkinData;
import com.xayup.multipad.skin.SkinProperties;
import com.xayup.multipad.skin.SkinVariables;

public class SkinManager implements SkinVariables {

    public Activity context;
    public static Resources mResources;

    public SkinManager(Context context) {
        this.context = (Activity) context;
    }
    
    public SkinProperties getPropertiesFromPackage(Context context, String package_name, boolean and_resources){
        return new SkinProperties(getSkinInfo(context, package_name, and_resources));
    }
    public static Object[] getSkinInfo(Context context, String package_name, boolean and_resources) {
        setCurrentResource(context, package_name);
        Object[] skin = new Object[ARRAY_SIZE];
        try {
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
        } catch (Resources.NotFoundException n){
            SkinManager.getSkinInfo(context, context.getPackageName(), and_resources);
        }
        return skin;
    }
    
    /**
    * @return true if success.
    */
    public static boolean setCurrentResource(Context context, String package_name) {
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
