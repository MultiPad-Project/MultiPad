package com.xayup.multipad.skin;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import com.xayup.multipad.skin.SkinData;
import com.xayup.multipad.skin.SkinProperties;
import com.xayup.multipad.skin.SkinVariables;

public class SkinManager implements SkinVariables {

    public static SkinProperties getSkinProperties(Context context, String package_name){
        Resources mResources = getResources(context, package_name);
        return new SkinProperties(new Object[]{
                package_name,
                mResources.getDrawable(mResources.getIdentifier("theme_ic", "drawable", package_name), null),
                mResources.getString(mResources.getIdentifier("theme_name", "string", package_name)),
                mResources.getString(mResources.getIdentifier("theme_author", "string", package_name))
        });
    }

    /**
     * Get Resources from package name.
     * @param context application context.
     * @param package_name application package name to get resources.
     * @return Resources from package_name, else return resources from this application.
     */
    public static Resources getResources(Context context, String package_name){
        try { return context.getPackageManager().getResourcesForApplication(package_name); }
        catch (PackageManager.NameNotFoundException n){ return context.getResources(); }
    }

    /**
     * .
     * @param context .
     * @param skin .
     * @param mSkinData .
     * @param onLoaded .
     */
    public void loadSkin(Context context, SkinProperties skin, SkinData mSkinData, LoadedSkin onLoaded) {
        mSkinData.loadFromResources(context, skin.package_name);
        if (onLoaded != null) onLoaded.onLoaded(skin);
    }

    public interface LoadedSkin {
        void onLoaded(SkinProperties skin);
    }
}
