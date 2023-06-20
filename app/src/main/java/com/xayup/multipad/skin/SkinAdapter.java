package com.xayup.multipad.skin;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.view.ViewGroup;
import android.view.View;
import android.widget.BaseAdapter;
import com.xayup.debug.XLog;
import com.xayup.multipad.skin.SkinManager;
import java.util.ArrayList;
import java.util.List;

public class SkinAdapter extends BaseAdapter implements SkinVariables {
    protected Activity context;
    protected List<Object[]> skins;
    protected long custom_view_id;

    public SkinAdapter(Context context) {
        this.context = (Activity) context;
        this.skins = new ArrayList<>();
    }

    protected void add(String skin_package) {
        boolean success = SkinManager.setCurrentResource(context, skin_package);
        XLog.v("Success skin data", String.valueOf(success));
        if (success) skins.add(SkinManager.getSkinInfo(context, skin_package, false));
    }

    public void updateList() {
        skins.clear();
        add(context.getPackageName());
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
        SkinManager.setCurrentResource(context, (String) skin[SKIN_PACKAGE]);
        skin[SKIN_RESOURCES] = SkinManager.mResources;
        return new SkinProperties(skin);
    }

    public SkinProperties remove(int pos){
        return new SkinProperties(skins.remove(pos));
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
