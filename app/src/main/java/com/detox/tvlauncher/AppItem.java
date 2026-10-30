package com.detox.tvlauncher;

import android.graphics.drawable.Drawable;

public class AppItem {
    private final String packageName;
    private final String title;
    private final int bannerResId;
    private Drawable icon;
    private final boolean isAllAppsAction;

    public AppItem(String packageName, String title, int bannerResId, boolean isAllAppsAction) {
        this.packageName = packageName;
        this.title = title;
        this.bannerResId = bannerResId;
        this.isAllAppsAction = isAllAppsAction;
    }

    public AppItem(String packageName, String title, Drawable icon) {
        this.packageName = packageName;
        this.title = title;
        this.bannerResId = 0;
        this.icon = icon;
        this.isAllAppsAction = false;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getTitle() {
        return title;
    }

    public int getBannerResId() {
        return bannerResId;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean hasCustomBanner() {
        return bannerResId != 0;
    }

    public boolean isAllAppsAction() {
        return isAllAppsAction;
    }
}
