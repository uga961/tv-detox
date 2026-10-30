package com.detox.tvlauncher;

import android.graphics.drawable.Drawable;

public class AppItem {
    private final String packageName;
    private final String title;
    private int bannerResId;
    private Drawable bannerDrawable;
    private Drawable iconDrawable;
    private final boolean isAllAppsAction;

    // Constructor for curated built-in banners (Netflix, Prime, YouTube, Hotstar, FanCode, PlayStore, etc.)
    public AppItem(String packageName, String title, int bannerResId) {
        this.packageName = packageName;
        this.title = title;
        this.bannerResId = bannerResId;
        this.isAllAppsAction = false;
    }

    // Constructor for dynamically discovered apps
    public AppItem(String packageName, String title, Drawable bannerDrawable, Drawable iconDrawable) {
        this.packageName = packageName;
        this.title = title;
        this.bannerResId = 0;
        this.bannerDrawable = bannerDrawable;
        this.iconDrawable = iconDrawable;
        this.isAllAppsAction = false;
    }

    // Constructor for special actions (e.g. All Apps button)
    public AppItem(String packageName, String title, int bannerResId, boolean isAllAppsAction) {
        this.packageName = packageName;
        this.title = title;
        this.bannerResId = bannerResId;
        this.isAllAppsAction = isAllAppsAction;
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

    public void setBannerResId(int bannerResId) {
        this.bannerResId = bannerResId;
    }

    public Drawable getBannerDrawable() {
        return bannerDrawable;
    }

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public boolean hasCustomBanner() {
        return bannerResId != 0 || bannerDrawable != null;
    }

    public boolean isAllAppsAction() {
        return isAllAppsAction;
    }
}
