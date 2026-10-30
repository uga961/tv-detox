package com.detox.tvlauncher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class TvHorizontalRecyclerView extends RecyclerView {

    public TvHorizontalRecyclerView(@NonNull Context context) {
        super(context);
    }

    public TvHorizontalRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TvHorizontalRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public View focusSearch(View focused, int direction) {
        if (direction == View.FOCUS_RIGHT || direction == View.FOCUS_LEFT) {
            View next = super.focusSearch(focused, direction);
            if (next != null) {
                View parent = next;
                boolean isChild = false;
                while (parent != null) {
                    if (parent == this) {
                        isChild = true;
                        break;
                    }
                    if (parent.getParent() instanceof View) {
                        parent = (View) parent.getParent();
                    } else {
                        break;
                    }
                }
                // Never jump to top pills or anywhere outside when pressing LEFT or RIGHT
                if (!isChild) {
                    return focused;
                }
            }
            return next;
        }
        return super.focusSearch(focused, direction);
    }
}
