package com.detox.tvlauncher;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AppAdapter.OnAppFocusListener, AppAdapter.OnAppClickListener {

    private TextView tvGreeting;
    private TextView tvClock;
    private TextView tvDate;
    private TextView tvFocusedTitle;

    private LinearLayout pillInputs;
    private LinearLayout pillWifi;
    private LinearLayout pillSettings;
    private TextView tvWifiText;

    private RecyclerView rvApps;
    private AppAdapter appAdapter;
    private final List<AppItem> displayedApps = new ArrayList<>();

    private BroadcastReceiver timeReceiver;
    private BroadcastReceiver networkReceiver;
    private BroadcastReceiver packageReceiver;

    // Curated high-resolution banners for well-known streaming services and Google Play Store
    private static final Map<String, Integer> CURATED_BANNERS = new HashMap<>();
    static {
        CURATED_BANNERS.put("com.netflix.ninja", R.drawable.banner_netflix);
        CURATED_BANNERS.put("com.amazon.amazonvideo.livingroom", R.drawable.banner_prime);
        CURATED_BANNERS.put("com.google.android.youtube.tv", R.drawable.banner_youtube);
        CURATED_BANNERS.put("in.startv.hotstar", R.drawable.banner_hotstar);
        CURATED_BANNERS.put("com.fancode.tv", R.drawable.banner_fancode);
        CURATED_BANNERS.put("com.android.vending", R.drawable.banner_playstore);
    }

    // Preferred display order for prominent apps
    private static final List<String> PREFERRED_ORDER = new ArrayList<>();
    static {
        PREFERRED_ORDER.add("com.netflix.ninja");
        PREFERRED_ORDER.add("com.amazon.amazonvideo.livingroom");
        PREFERRED_ORDER.add("com.google.android.youtube.tv");
        PREFERRED_ORDER.add("in.startv.hotstar");
        PREFERRED_ORDER.add("com.fancode.tv");
        PREFERRED_ORDER.add("com.android.vending"); // Google Play Store
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupDateTime();
        setupPills();
        setupAppsRecyclerView();
        loadInstalledApps();
        setupPackageReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Dynamically refresh installed apps when returning from Play Store or Settings
        loadInstalledApps();
        updateWifiStatus();
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvClock = findViewById(R.id.tvClock);
        tvDate = findViewById(R.id.tvDate);
        tvFocusedTitle = findViewById(R.id.tvFocusedTitle);

        pillInputs = findViewById(R.id.pillInputs);
        pillWifi = findViewById(R.id.pillWifi);
        pillSettings = findViewById(R.id.pillSettings);
        tvWifiText = findViewById(R.id.tvWifiText);

        rvApps = findViewById(R.id.rvApps);
    }

    private void setupDateTime() {
        updateTimeAndGreeting();

        timeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateTimeAndGreeting();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(timeReceiver, filter);
    }

    private void updateTimeAndGreeting() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            tvGreeting.setText(R.string.greeting_morning);
        } else if (hour >= 12 && hour < 17) {
            tvGreeting.setText(R.string.greeting_afternoon);
        } else if (hour >= 17 && hour < 21) {
            tvGreeting.setText(R.string.greeting_evening);
        } else {
            tvGreeting.setText(R.string.greeting_night);
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        tvClock.setText(timeFormat.format(new Date()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d MMMM", Locale.getDefault());
        tvDate.setText(dateFormat.format(new Date()).toUpperCase(Locale.getDefault()));
    }

    private void setupPills() {
        pillInputs.setOnClickListener(v -> openInputs());
        pillWifi.setOnClickListener(v -> openWifiSettings());
        pillSettings.setOnClickListener(v -> openSettings());

        pillInputs.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tvFocusedTitle.setText(R.string.pill_inputs);
        });

        pillWifi.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tvFocusedTitle.setText(R.string.pill_wifi);
        });

        pillSettings.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tvFocusedTitle.setText(R.string.pill_settings);
        });

        updateWifiStatus();
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateWifiStatus();
            }
        };
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void setupPackageReceiver() {
        packageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Instantly re-query and update UI whenever an app is installed, updated, or removed
                loadInstalledApps();
            }
        };
        IntentFilter pkgFilter = new IntentFilter();
        pkgFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        pkgFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        pkgFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        pkgFilter.addDataScheme("package");
        registerReceiver(packageReceiver, pkgFilter);
    }

    private void updateWifiStatus() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if (wm != null) {
                            WifiInfo info = wm.getConnectionInfo();
                            String ssid = info != null ? info.getSSID() : null;
                            if (ssid != null && !ssid.equals("<unknown ssid>") && !ssid.isEmpty()) {
                                tvWifiText.setText(ssid.replace("\"", ""));
                                return;
                            }
                        }
                        tvWifiText.setText("Wi-Fi");
                        return;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_ETHERNET) {
                        tvWifiText.setText("Ethernet");
                        return;
                    }
                }
            }
            tvWifiText.setText("Disconnected");
        } catch (Exception ignored) {
            tvWifiText.setText("Wi-Fi");
        }
    }

    private void openInputs() {
        // 1. Try Xiaomi DroidLogic TV Input Service
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName("com.droidlogic.droidlivetv", "com.droidlogic.droidlivetv.DroidLiveTv");
        try {
            startActivity(intent);
            return;
        } catch (Exception ignored) {}

        // 2. Try Standard Android TV Inputs intent
        try {
            Intent liveTv = new Intent("android.media.tv.action.VIEW_INPUTS");
            startActivity(liveTv);
            return;
        } catch (Exception ignored) {}

        // 3. Fallback to TV Input Settings
        try {
            Intent settingsInputs = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
            startActivity(settingsInputs);
        } catch (Exception e) {
            Toast.makeText(this, "Opening Inputs...", Toast.LENGTH_SHORT).show();
        }
    }

    private void openWifiSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        } catch (Exception e) {
            openSettings();
        }
    }

    private void openSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        } catch (Exception e) {
            Toast.makeText(this, "Could not open Settings", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Dynamically queries PackageManager for ALL installed launchable apps on the TV.
     * Ensures newly installed apps, sideloaded apps, and Google Play Store always appear!
     */
    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        String currentPkg = getPackageName();

        // 1. Query Android TV Leanback Apps
        Intent leanbackIntent = new Intent(Intent.ACTION_MAIN, null);
        leanbackIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> leanbackApps = pm.queryIntentActivities(leanbackIntent, 0);

        // 2. Query Standard Android Launchable Apps (for apps without Leanback category or sideloaded)
        Intent standardIntent = new Intent(Intent.ACTION_MAIN, null);
        standardIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> standardApps = pm.queryIntentActivities(standardIntent, 0);

        // Deduplicate apps by package name
        Map<String, ResolveInfo> appMap = new HashMap<>();
        for (ResolveInfo ri : leanbackApps) {
            if (ri.activityInfo != null && !currentPkg.equals(ri.activityInfo.packageName)) {
                appMap.put(ri.activityInfo.packageName, ri);
            }
        }
        for (ResolveInfo ri : standardApps) {
            if (ri.activityInfo != null && !currentPkg.equals(ri.activityInfo.packageName)) {
                if (!appMap.containsKey(ri.activityInfo.packageName)) {
                    appMap.put(ri.activityInfo.packageName, ri);
                }
            }
        }

        // Always check for Google Play Store directly if not captured
        if (!appMap.containsKey("com.android.vending")) {
            Intent playStoreIntent = pm.getLaunchIntentForPackage("com.android.vending");
            if (playStoreIntent != null) {
                List<ResolveInfo> playInfos = pm.queryIntentActivities(playStoreIntent, 0);
                if (!playInfos.isEmpty()) {
                    appMap.put("com.android.vending", playInfos.get(0));
                }
            }
        }

        List<AppItem> orderedList = new ArrayList<>();
        Set<String> processedPkgs = new HashSet<>();

        // A. Add Preferred Apps First (Netflix, Prime, YouTube, Hotstar, FanCode, PlayStore) if installed
        for (String preferredPkg : PREFERRED_ORDER) {
            if (appMap.containsKey(preferredPkg)) {
                ResolveInfo ri = appMap.get(preferredPkg);
                String label = ri.loadLabel(pm).toString();
                int curatedBanner = CURATED_BANNERS.containsKey(preferredPkg) ? CURATED_BANNERS.get(preferredPkg) : 0;

                Drawable tvBanner = ri.activityInfo.loadBanner(pm);
                Drawable icon = ri.activityInfo.loadIcon(pm);

                AppItem item = new AppItem(preferredPkg, label, curatedBanner);
                if (curatedBanner == 0) {
                    item = new AppItem(preferredPkg, label, tvBanner, icon);
                }
                orderedList.add(item);
                processedPkgs.add(preferredPkg);
            }
        }

        // B. Add all remaining installed apps (dynamically discovered!)
        List<AppItem> remainingApps = new ArrayList<>();
        for (Map.Entry<String, ResolveInfo> entry : appMap.entrySet()) {
            String pkg = entry.getKey();
            if (!processedPkgs.contains(pkg)) {
                ResolveInfo ri = entry.getValue();
                String label = ri.loadLabel(pm).toString();
                Drawable tvBanner = ri.activityInfo.loadBanner(pm);
                Drawable icon = ri.activityInfo.loadIcon(pm);

                // Check if we happen to have a curated banner for it
                int curatedBanner = CURATED_BANNERS.containsKey(pkg) ? CURATED_BANNERS.get(pkg) : 0;
                AppItem item;
                if (curatedBanner != 0) {
                    item = new AppItem(pkg, label, curatedBanner);
                } else {
                    item = new AppItem(pkg, label, tvBanner, icon);
                }
                remainingApps.add(item);
            }
        }

        // Sort remaining apps alphabetically by title
        Collections.sort(remainingApps, Comparator.comparing(a -> a.getTitle().toLowerCase(Locale.getDefault())));
        orderedList.addAll(remainingApps);

        // C. If user has lots of apps, also include the "All Applications" grid card
        if (orderedList.size() > 6) {
            orderedList.add(new AppItem("", "All Applications", R.drawable.banner_all_apps, true));
        }

        displayedApps.clear();
        displayedApps.addAll(orderedList);

        if (appAdapter != null) {
            appAdapter.notifyDataSetChanged();
        }

        if (!displayedApps.isEmpty() && (tvFocusedTitle.getText() == null || tvFocusedTitle.getText().toString().isEmpty())) {
            tvFocusedTitle.setText(displayedApps.get(0).getTitle());
        }
    }

    private void setupAppsRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvApps.setLayoutManager(layoutManager);
        appAdapter = new AppAdapter(displayedApps, this, this);
        rvApps.setAdapter(appAdapter);

        // Auto-focus the first app on boot
        rvApps.post(() -> {
            View firstCard = rvApps.getChildAt(0);
            if (firstCard != null) {
                firstCard.requestFocus();
            }
        });
    }

    @Override
    public void onAppFocused(AppItem app) {
        tvFocusedTitle.setText(app.getTitle());
    }

    @Override
    public void onAppClicked(AppItem app) {
        if (app.isAllAppsAction()) {
            showAllAppsDialog();
            return;
        }

        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLeanbackLaunchIntentForPackage(app.getPackageName());
        if (launchIntent == null) {
            launchIntent = pm.getLaunchIntentForPackage(app.getPackageName());
        }

        if (launchIntent != null) {
            try {
                startActivity(launchIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Error opening " + app.getTitle(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, app.getTitle() + " is not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAllAppsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_all_apps);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.75),
                (int) (getResources().getDisplayMetrics().heightPixels * 0.75)
            );
        }

        RecyclerView rvGrid = dialog.findViewById(R.id.rvAllAppsGrid);
        rvGrid.setLayoutManager(new GridLayoutManager(this, 4));

        PackageManager pm = getPackageManager();
        List<AppItem> allInstalled = new ArrayList<>(displayedApps);
        // Exclude the 'All Applications' action card itself from the modal
        allInstalled.removeIf(AppItem::isAllAppsAction);

        rvGrid.setAdapter(new RecyclerView.Adapter<GridAppViewHolder>() {
            @NonNull
            @Override
            public GridAppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_grid, parent, false);
                return new GridAppViewHolder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull GridAppViewHolder holder, int position) {
                AppItem item = allInstalled.get(position);
                holder.tvTitle.setText(item.getTitle());
                if (item.getIconDrawable() != null) {
                    holder.ivIcon.setImageDrawable(item.getIconDrawable());
                } else if (item.getBannerDrawable() != null) {
                    holder.ivIcon.setImageDrawable(item.getBannerDrawable());
                } else if (item.getBannerResId() != 0) {
                    holder.ivIcon.setImageResource(item.getBannerResId());
                }

                holder.itemView.setOnClickListener(v -> {
                    dialog.dismiss();
                    Intent launch = pm.getLeanbackLaunchIntentForPackage(item.getPackageName());
                    if (launch == null) launch = pm.getLaunchIntentForPackage(item.getPackageName());
                    if (launch != null) startActivity(launch);
                });
            }

            @Override
            public int getItemCount() {
                return allInstalled.size();
            }
        });

        dialog.show();
    }

    static class GridAppViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvTitle;

        GridAppViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivGridIcon);
            tvTitle = itemView.findViewById(R.id.tvGridTitle);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Keep user on the home screen rather than killing the launcher
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeReceiver != null) {
            try { unregisterReceiver(timeReceiver); } catch (Exception ignored) {}
        }
        if (networkReceiver != null) {
            try { unregisterReceiver(networkReceiver); } catch (Exception ignored) {}
        }
        if (packageReceiver != null) {
            try { unregisterReceiver(packageReceiver); } catch (Exception ignored) {}
        }
    }
}
