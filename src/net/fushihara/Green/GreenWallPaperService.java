package net.fushihara.green;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class GreenWallPaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new GreenEngine();
    }

    private class GreenEngine extends Engine {

        private int                                width;
        private int                                height;

        private int                                screen;

        private Map<String, WeakReference<Bitmap>> cache = new HashMap<String, WeakReference<Bitmap>>();

        public GreenEngine() {
            super();
        }

        private int getXOffset() {
            return width / (getScreenCount() - 1);
        }

        private int getScreenCount() {
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());

            return Integer.valueOf(pref.getString(Const.KEY_SCREEN_COUNT,
                    Const.SCREEN_COUNT_DEFAULT));
        }

        private void draw() {
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;

            try {
                c = holder.lockCanvas();

                if (c != null) {
                    c.drawColor(Color.WHITE);
                    Paint p = new Paint();
                    p.setColor(Color.BLUE);
                    p.setTextSize(80);

                    SharedPreferences pref = PreferenceManager
                            .getDefaultSharedPreferences(getApplicationContext());

                    Log.d("Green", "get: " + Const.KEY_SCREEN_IMAGE + screen);

                    String imageUri = pref.getString(Const.KEY_SCREEN_IMAGE
                            + screen, null);
                    if (imageUri != null) {
                        try {
                            WeakReference<Bitmap> imageCache = cache
                                    .get(imageUri);
                            Bitmap image = null;
                            if (imageCache != null) {
                                image = imageCache.get();
                            }
                            if (image == null) {
                                image = MediaStore.Images.Media.getBitmap(
                                        getContentResolver(),
                                        Uri.parse(imageUri));
                                cache.put(imageUri, new WeakReference<Bitmap>(
                                        image));
                            }
                            Matrix matrix = new Matrix();
                            float xScale = (float) getDesiredMinimumWidth()
                                    / image.getWidth();
                            float yScale = (float) getDesiredMinimumHeight()
                                    / image.getHeight();
                            if (xScale > yScale) {
                                matrix.postScale(yScale, yScale);
                            } else {
                                matrix.postScale(xScale, xScale);
                            }
                            p.setAntiAlias(true);
                            p.setFilterBitmap(true);
                            p.setDither(true);
                            c.drawBitmap(image, matrix, p);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        c.drawText(String.valueOf(screen), width / 2,
                                height / 2, p);
                    }
                }
            } finally {
                if (c != null) {
                    holder.unlockCanvasAndPost(c);
                }
            }
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xOffsetStep, float yOffsetStep, int xPixelOffset,
                int yPixelOffset) {

            Log.d("Green", xPixelOffset + "," + yPixelOffset);
            int newScreen = getScreenCount() - (width + xPixelOffset)
                    / getXOffset();
            Log.d("Green", "screen:" + screen);

            if (screen != newScreen) {
                screen = newScreen;
                draw();
            }

            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
                    xPixelOffset, yPixelOffset);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format,
                int width, int height) {

            this.width = width;
            this.height = height;
            super.onSurfaceChanged(holder, format, width, height);
        }
    }

}
