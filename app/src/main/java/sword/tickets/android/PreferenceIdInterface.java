package sword.tickets.android;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public interface PreferenceIdInterface {
    void put(@NonNull SharedPreferences.Editor editor, String preferenceName);
}
