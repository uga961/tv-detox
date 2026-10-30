package com.detox.tvlauncher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    public interface OnAppFocusListener {
        void onAppFocused(AppItem app);
    }

    public interface OnAppClickListener {
        void onAppClicked(AppItem app);
    }

    private final List<AppItem> appList;
    private final OnAppFocusListener focusListener;
    private final OnAppClickListener clickListener;

    public AppAdapter(List<AppItem> appList, OnAppFocusListener focusListener, OnAppClickListener clickListener) {
        this.appList = appList;
        this.focusListener = focusListener;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppItem app = appList.get(position);

        if (app.hasCustomBanner()) {
            holder.ivBanner.setVisibility(View.VISIBLE);
            holder.layoutFallback.setVisibility(View.GONE);
            holder.ivBanner.setImageResource(app.getBannerResId());
        } else {
            holder.ivBanner.setVisibility(View.GONE);
            holder.layoutFallback.setVisibility(View.VISIBLE);
            if (app.getIcon() != null) {
                holder.ivIcon.setImageDrawable(app.getIcon());
            }
            holder.tvTitle.setText(app.getTitle());
        }

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.animate().scaleX(1.08f).scaleY(1.08f).setDuration(120).start();
                v.setElevation(10f);
                if (focusListener != null) {
                    focusListener.onAppFocused(app);
                }
            } else {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                v.setElevation(0f);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onAppClicked(app);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivBanner;
        final LinearLayout layoutFallback;
        final ImageView ivIcon;
        final TextView tvTitle;
        final View vFocusRing;

        ViewHolder(View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.ivBanner);
            layoutFallback = itemView.findViewById(R.id.layoutFallback);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            vFocusRing = itemView.findViewById(R.id.vFocusRing);
        }
    }
}
