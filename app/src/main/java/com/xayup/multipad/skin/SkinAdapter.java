package com.xayup.multipad.skin;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.view.ViewGroup;
import android.view.View;
import android.widget.BaseAdapter;
import com.xayup.multipad.pads.Render.SkinManager;
import java.util.ArrayList;
import java.util.List;

class SkinAdapter extends BaseAdapter implements SkinVariables {
    protected Activity context;
    protected List<Object[]> skins;
    protected long custom_view_id;

    public SkinAdapter(Context context, long custom_view_id) {
        this.context = (Activity) context;
        this.skins = new ArrayList<>();
        this.custom_view_id = custom_view_id;
        this.add(context.getPackageName());
    }

    protected void add(String skin_package) {
        if (SkinManager.setCurrentResource(skin_package) != true) skins.add(SkinManager.getSkinInfo(skin_package, false));
    }

    public void updateList() {
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        for (PackageInfo pi : packages) {
            if (pi.packageName.indexOf("com.kimjisub.launchpad.theme.") == 0) {
                add(pi.packageName);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return skins.size();
    }

    @Override
    public SkinProperties getItem(int pos) {
        Object[] skin = skins.get(pos);
        SkinManager.setCurrentResource((String) skin[SKIN_PACKAGE]);
        skin[SKIN_RESOURCES] = SkinManager.mResources;
        return new SkinProperties(skin);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        return null;
    }
}
