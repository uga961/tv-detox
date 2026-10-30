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
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private final List<AppItem> featuredApps = new ArrayList<>();

    private BroadcastReceiver timeReceiver;
    private BroadcastReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupDateTime();
        setupPills();
        loadFeaturedApps();
        setupAppsRecyclerView();
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
        // Try Xiaomi DroidLogic TV Input Service first
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName("com.droidlogic.droidlivetv", "com.droidlogic.droidlivetv.DroidLiveTv");
        try {
            startActivity(intent);
            return;
        } catch (Exception ignored) {}

        // Fallback to Live TV or TV Inputs intent
        try {
            Intent liveTv = new Intent("android.media.tv.action.VIEW_INPUTS");
            startActivity(liveTv);
            return;
        } catch (Exception ignored) {}

        // Fallback to Settings Inputs
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

    private void loadFeaturedApps() {
        featuredApps.clear();

        // 1. Netflix
        featuredApps.add(new AppItem("com.netflix.ninja", "Netflix", R.drawable.banner_netflix, false));

        // 2. Amazon Prime Video
        featuredApps.add(new AppItem("com.amazon.amazonvideo.livingroom", "Prime Video", R.drawable.banner_prime, false));

        // 3. YouTube
        featuredApps.add(new AppItem("com.google.android.youtube.tv", "YouTube", R.drawable.banner_youtube, false));

        // 4. Disney+ Hotstar
        featuredApps.add(new AppItem("in.startv.hotstar", "Disney+ Hotstar", R.drawable.banner_hotstar, false));

        // 5. FanCode
        featuredApps.add(new AppItem("com.fancode.tv", "FanCode", R.drawable.banner_fancode, false));

        // 6. All Apps Action Card
        featuredApps.add(new AppItem("", "All Applications", R.drawable.banner_all_apps, true));
    }

    private void setupAppsRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvApps.setLayoutManager(layoutManager);
        appAdapter = new AppAdapter(featuredApps, this, this);
        rvApps.setAdapter(appAdapter);

        // Default title to the first app
        if (!featuredApps.isEmpty()) {
            tvFocusedTitle.setText(featuredApps.get(0).getTitle());
        }

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
                (int) (getResources().getDisplayMetrics().widthPixels * 0.7),
                (int) (getResources().getDisplayMetrics().heightPixels * 0.7)
            );
        }

        RecyclerView rvGrid = dialog.findViewById(R.id.rvAllAppsGrid);
        rvGrid.setLayoutManager(new GridLayoutManager(this, 4));

        PackageManager pm = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        if (resolveInfos.isEmpty()) {
            mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        }

        List<AppItem> allInstalled = new ArrayList<>();
        String currentPkg = getPackageName();

        for (ResolveInfo ri : resolveInfos) {
            if (ri.activityInfo != null && !currentPkg.equals(ri.activityInfo.packageName)) {
                String pkg = ri.activityInfo.packageName;
                String label = ri.loadLabel(pm).toString();
                allInstalled.add(new AppItem(pkg, label, ri.loadIcon(pm)));
            }
        }

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
                holder.ivIcon.setImageDrawable(item.getIcon());
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
            // Do not exit home launcher on back press
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
    }
}
