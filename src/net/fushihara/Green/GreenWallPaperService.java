package net.fushihara.Green;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class GreenWallPaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        // TODO 自動生成されたメソッド・スタブ
        return new GreenEngine();
    }

    private class GreenEngine extends Engine {

        private final int SCREEN = 5;

        private int       width;
        private int       height;

        private int       screen;

        private int getXOffset() {
            return width / (SCREEN - 1);
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
                    c.drawText(String.valueOf(screen), width / 2, height / 2, p);
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
            screen = SCREEN - (width + xPixelOffset) / getXOffset();
            Log.d("Green", "screen:" + screen);

            draw();

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
