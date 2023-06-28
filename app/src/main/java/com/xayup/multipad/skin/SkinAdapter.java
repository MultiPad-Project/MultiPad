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
    protected Context context;
    protected List<SkinProperties> skins;

    public SkinAdapter(Context context) {
        this.context = context;
        this.skins = new ArrayList<>();
    }

    protected void add(String skin_package) { skins.add(SkinManager.getSkinProperties(context, skin_package)); }
    public SkinProperties remove(int pos){ return skins.remove(pos); }

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
    public int getCount() { return skins.size(); }

    @Override
    public SkinProperties getItem(int pos) { return skins.get(pos); }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        return null;
    }
}
