package net.fushihara.green;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class Setting extends PreferenceActivity implements
        OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(
                this);
        EditTextPreference screen = new EditTextPreference(this);
        screen.setKey(Const.KEY_SCREEN_COUNT);
        screen.setTitle(R.string.screen_count);
        screen.setSummary(pref.getString(Const.KEY_SCREEN_COUNT,
                Const.SCREEN_COUNT_DEFAULT));
        screen.setDefaultValue(Const.SCREEN_COUNT_DEFAULT);
        screen.setOnPreferenceChangeListener(this);
        root.addPreference(screen);

        setPreferenceScreen(root);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue != null) {
            preference.setSummary((CharSequence) newValue);
            return true;
        }
        return false;
    }

}
