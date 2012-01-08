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
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
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

    HashMap<String, Preference>     screenImagePrefs = new HashMap<String, Preference>();
    private PreferenceScreen        screenImages;
    private ListPreference          randomImageFolder;
    private ListPreference          randomSpan;
    private HashMap<String, String> buckets          = new HashMap<String, String>();

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

        CheckBoxPreference anime = new CheckBoxPreference(this);
        anime.setKey(Const.KEY_ENABLE_ANIMATION);
        anime.setTitle(R.string.enable_animation);
        anime.setDefaultValue(Const.ENABLE_ANIMATION_DEFAULT);
        anime.setSummaryOn(R.string.enable_animation_on);
        anime.setSummaryOff(R.string.enable_animation_off);
        root.addPreference(anime);

        ListPreference type = new ListPreference(this);
        type.setKey(Const.KEY_VIEW_TYPE);
        type.setTitle(R.string.view_type);
        type.setEntries(R.array.view_type_entries);
        type.setEntryValues(R.array.view_type_entry_values);
        type.setSummary(pref.getString(Const.KEY_VIEW_TYPE,
                Const.VIEW_TYPE_MANUAL).equals(Const.VIEW_TYPE_MANUAL) ? getResources()
                .getStringArray(R.array.view_type_entries)[0] : getResources()
                .getStringArray(R.array.view_type_entries)[1]);
        type.setDefaultValue(Const.VIEW_TYPE_MANUAL);
        type.setOnPreferenceChangeListener(this);
        root.addPreference(type);

        if (pref.getString(Const.KEY_VIEW_TYPE, Const.VIEW_TYPE_MANUAL).equals(
                Const.VIEW_TYPE_MANUAL)) {
            setupScreenImagesPreference(pref, 0);
            root.addPreference(screenImages);
        } else {
            setupRandomImageFolderPreference(pref);
            root.addPreference(randomImageFolder);
            setupRandomSpan();
            root.addPreference(randomSpan);
        }

        setPreferenceScreen(root);
    }

    private void setupRandomSpan() {
        if (randomSpan == null) {
            randomSpan = new ListPreference(this);
            randomSpan.setKey(Const.KEY_RANDOM_SPAN);
            randomSpan.setTitle(R.string.random_span);
            randomSpan.setEntries(R.array.random_span_entries);
            randomSpan.setEntryValues(R.array.random_span_entry_values);
            randomSpan.setDefaultValue(Const.RANDOM_SPAN_DEFAULT);
            randomSpan.setOnPreferenceChangeListener(this);
        }
    }

    private void setupRandomImageFolderPreference(SharedPreferences pref) {
        if (randomImageFolder == null) {
            randomImageFolder = new ListPreference(this);
            randomImageFolder.setKey(Const.KEY_BUCKET);
            randomImageFolder.setTitle(R.string.random_image_folder);
            randomImageFolder.setOnPreferenceChangeListener(this);
        }
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor c = managedQuery(uri, null, null, null, null);
        buckets.clear();
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            String bucket_id = c
                    .getString(c.getColumnIndexOrThrow("bucket_id"));
            String bucket_name = c.getString(c
                    .getColumnIndexOrThrow("bucket_display_name"));
            buckets.put(bucket_id, bucket_name);

            c.moveToNext();
        }
        /*
         * for (String bid : buckets.keySet()) { CheckBoxPreference p = new
         * CheckBoxPreference(Setting.this); p.setTitle(buckets.get(bid));
         * randomImageFolder.addPreference(p); }
         */
        randomImageFolder.setEntries(buckets.values().toArray(new String[] {}));
        randomImageFolder.setEntryValues(buckets.keySet().toArray(
                new String[] {}));
        randomImageFolder
                .setSummary(pref.getString(Const.KEY_BUCKET, null) != null ? buckets
                        .get(pref.getString(Const.KEY_BUCKET, null)) : "");
    }

    private void setupScreenImagesPreference(SharedPreferences pref,
            int screenCount) {
        if (screenImages == null) {
            screenImages = getPreferenceManager().createPreferenceScreen(this);
            screenImages.setTitle(R.string.screen_images_title);
        }
        screenImages.removeAll();
        if (screenCount == 0) {
            screenCount = Integer.valueOf(pref.getString(
                    Const.KEY_SCREEN_COUNT, Const.SCREEN_COUNT_DEFAULT));
        }
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
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());

            if (preference.getKey().equals(Const.KEY_SCREEN_COUNT)) {
                int num = 0;

                try {
                    num = Integer.valueOf((String) newValue);
                    if (num <= 0) {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    newValue = Const.SCREEN_COUNT_DEFAULT;
                    Editor editor = pref.edit();
                    editor.putString(Const.KEY_SCREEN_COUNT,
                            Const.SCREEN_COUNT_DEFAULT);
                    editor.commit();
                    return false;
                }
                preference.setSummary((CharSequence) newValue);
                setupScreenImagesPreference(pref, num);
            } else if (preference.getKey().equals(Const.KEY_VIEW_TYPE)) {
                try {
                    getPreferenceScreen().removePreference(screenImages);
                } catch (Exception e) {
                    // 存在しないとエラーになるので
                }
                try {
                    getPreferenceScreen().removePreference(randomImageFolder);
                    getPreferenceScreen().removePreference(randomSpan);
                } catch (Exception e) {
                    // 存在しないとエラーになるので
                }
                if (newValue.equals(Const.VIEW_TYPE_MANUAL)) {
                    preference.setSummary(getResources().getStringArray(
                            R.array.view_type_entries)[0]);
                    setupScreenImagesPreference(pref, Integer.valueOf(pref
                            .getString(Const.KEY_SCREEN_COUNT,
                                    Const.SCREEN_COUNT_DEFAULT)));
                    getPreferenceScreen().addPreference(screenImages);
                } else {
                    setupRandomImageFolderPreference(pref);
                    setupRandomSpan();
                    getPreferenceScreen().addPreference(randomImageFolder);
                    getPreferenceScreen().addPreference(randomSpan);
                }

            } else if (preference.getKey().equals(Const.KEY_BUCKET)) {
                preference.setSummary(buckets.get(newValue));

            }

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
