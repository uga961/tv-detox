package com.detox.tvlauncher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class TvHorizontalRecyclerView extends RecyclerView {

    public TvHorizontalRecyclerView(@NonNull Context context) {
        super(context);
        init();
    }

    public TvHorizontalRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TvHorizontalRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setHasFixedSize(true);
        // Pre-cache all views so offscreen cards are always alive in the view hierarchy
        setItemViewCacheSize(30);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                View focused = getFocusedChild();
                if (focused != null && getLayoutManager() != null && getAdapter() != null) {
                    int pos = getLayoutManager().getPosition(focused);
                    int count = getAdapter().getItemCount();
                    if (pos < count - 1) {
                        int target = pos + 1;
                        smoothScrollToPosition(target);
                        postDelayed(() -> {
                            View next = getLayoutManager().findViewByPosition(target);
                            if (next != null) {
                                next.requestFocus();
                            }
                        }, 50);
                    }
                    // ALWAYS consume RIGHT key: it will NEVER escape to the top pills!
                    return true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                View focused = getFocusedChild();
                if (focused != null && getLayoutManager() != null) {
                    int pos = getLayoutManager().getPosition(focused);
                    if (pos > 0) {
                        int target = pos - 1;
                        smoothScrollToPosition(target);
                        postDelayed(() -> {
                            View prev = getLayoutManager().findViewByPosition(target);
                            if (prev != null) {
                                prev.requestFocus();
                            }
                        }, 50);
                    }
                    // ALWAYS consume LEFT key
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
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
                if (!isChild) {
                    return focused; // Never jump out of carousel horizontally
                }
            }
            return next;
        }
        return super.focusSearch(focused, direction);
    }
}
