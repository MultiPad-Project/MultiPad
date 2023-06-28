package com.xayup.multipad.skin;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class SkinProperties implements SkinVariables {
    public final String package_name;
    public final String name;
    public final String author;
    public final Drawable icon;

    public SkinProperties(Object[] skin) {
        package_name = (String) skin[SKIN_PACKAGE];
        name = (String) skin[SKIN_NAME];
        author = (String) skin[SKIN_AUTHOR];
        icon = (Drawable) skin[SKIN_LOGO];
    }
}
