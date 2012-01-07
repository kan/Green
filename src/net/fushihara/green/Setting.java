package net.fushihara.green;

import java.util.HashMap;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Setting extends PreferenceActivity implements
        OnPreferenceChangeListener, OnPreferenceClickListener {

    HashMap<String, Preference> screenImagePrefs = new HashMap<String, Preference>();

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

        PreferenceScreen screenImages = getPreferenceManager()
                .createPreferenceScreen(this);
        screenImages.setTitle(R.string.screen_images_title);
        int screenCount = Integer.valueOf(pref.getString(
                Const.KEY_SCREEN_COUNT, Const.SCREEN_COUNT_DEFAULT));
        for (int i = 0; i < screenCount; i++) {
            ScreenImagePreference preference = new ScreenImagePreference(this);
            preference.setKey(Const.KEY_SCREEN_IMAGE + (i + 1));
            preference.setTitle(getString(R.string.screen_image, i + 1));
            preference.setOrder(i + 1);
            preference.setOnPreferenceClickListener(this);
            preference.setLayoutResource(R.layout.screen_image_pref);
            screenImagePrefs.put(preference.getKey(), preference);
            screenImages.addPreference(preference);
        }
        root.addPreference(screenImages);

        setPreferenceScreen(root);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri imageUri = data.getData();

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        Editor editor = pref.edit();
        editor.putString(Const.KEY_SCREEN_IMAGE + requestCode,
                imageUri.toString());
        editor.commit();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue != null) {
            preference.setSummary((CharSequence) newValue);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, preference.getOrder());
        return true;
    }

    private class ScreenImagePreference extends Preference {

        public ScreenImagePreference(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public ScreenImagePreference(Context context) {
            super(context);
        }

        @Override
        protected void onBindView(View view) {
            super.onBindView(view);

            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());

            if (getTitle() != null) {
                ((TextView) view.findViewById(android.R.id.title))
                        .setText(getTitle());
            }

            if (pref.getString(getKey(), null) != null) {
                ContentResolver cr = getContentResolver();
                Cursor c = MediaStore.Images.Media.query(cr,
                        Uri.parse(pref.getString(getKey(), null)),
                        new String[] {});
                if (c.moveToFirst()) {
                    long origId = c.getLong(c.getColumnIndexOrThrow("_id"));
                    Bitmap image = MediaStore.Images.Thumbnails.getThumbnail(
                            cr, origId,
                            MediaStore.Images.Thumbnails.MICRO_KIND, null);
                    ((ImageView) view.findViewById(R.id.thumbnail))
                            .setImageBitmap(image);
                }
            }
        }
    }

}
