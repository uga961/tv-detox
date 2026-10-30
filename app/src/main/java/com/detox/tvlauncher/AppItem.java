package com.detox.tvlauncher;

import android.graphics.drawable.Drawable;

public class AppItem {
    private final String packageName;
    private final String title;
    private final Drawable bannerDrawable;
    private final Drawable iconDrawable;
    private final boolean isAllAppsAction;

    public AppItem(String packageName, String title, Drawable bannerDrawable, Drawable iconDrawable) {
        this.packageName = packageName;
        this.title = title;
        this.bannerDrawable = bannerDrawable;
        this.iconDrawable = iconDrawable;
        this.isAllAppsAction = false;
    }

    public AppItem(String title, Drawable bannerDrawable, Drawable iconDrawable, boolean isAllAppsAction) {
        this.packageName = "";
        this.title = title;
        this.bannerDrawable = bannerDrawable;
        this.iconDrawable = iconDrawable;
        this.isAllAppsAction = isAllAppsAction;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getTitle() {
        return title;
    }

    public Drawable getBannerDrawable() {
        return bannerDrawable;
    }

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public boolean hasBanner() {
        return bannerDrawable != null;
    }

    public boolean isAllAppsAction() {
        return isAllAppsAction;
    }
}
