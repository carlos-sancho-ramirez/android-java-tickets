package sword.tickets.android.activities;

import android.graphics.Insets;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.annotation.NonNull;

import sword.tickets.android.ApiUtils;

final class ActivityUtils {
    static void applyMainInsets(@NonNull View view) {
        if (ApiUtils.isAtLeastApiLevel35()) {
            view.setOnApplyWindowInsetsListener((v, windowInsets) -> {
                final Insets insets = windowInsets.getInsets(WindowInsets.Type.systemBars());
                final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                mlp.leftMargin = insets.left;
                mlp.topMargin = insets.top;
                mlp.rightMargin = insets.right;
                mlp.bottomMargin = insets.bottom;
                v.setLayoutParams(mlp);

                return WindowInsets.CONSUMED;
            });
        }
    }

    private ActivityUtils() {
    }
}
