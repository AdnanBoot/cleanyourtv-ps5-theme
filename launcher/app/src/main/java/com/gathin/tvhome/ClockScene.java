package com.gathin.tvhome;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clean full-screen screensaver scene:
 * - Slideshow of photos from /sdcard/Screensaver/
 * - 100% original aspect ratio preserved (zero stretching)
 * - Portrait photos centered in fitCenter with subtle ambient background
 * - Landscape photos displayed full screen
 * - No phone frame, no clocks, no dates
 */
final class ClockScene {

    private static final String TAG = "ClockScene";
    private static final long SLIDE_MS = 10_000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final View root;

    // Slide views
    private FrameLayout slideA;
    private ImageView ambientA;
    private ImageView photoA;

    private FrameLayout slideB;
    private ImageView ambientB;
    private ImageView photoB;

    private boolean showingA = false;
    private final List<File> photoFiles = new ArrayList<>();
    private int currentPhotoIndex = 0;
    private Bitmap currentBitmapA;
    private Bitmap currentBitmapB;

    private final Runnable tickSlide = new Runnable() {
        @Override
        public void run() {
            nextSlide();
            handler.postDelayed(this, SLIDE_MS);
        }
    };

    ClockScene(View root) {
        this.root = root;

        slideA = root.findViewById(R.id.dreamSlideA);
        if (slideA != null) {
            ambientA = root.findViewById(R.id.dreamAmbientA);
            photoA = root.findViewById(R.id.dreamPhotoA);
        }

        slideB = root.findViewById(R.id.dreamSlideB);
        if (slideB != null) {
            ambientB = root.findViewById(R.id.dreamAmbientB);
            photoB = root.findViewById(R.id.dreamPhotoB);
        }
    }

    void start() {
        scanPhotos();

        if (!photoFiles.isEmpty()) {
            handler.removeCallbacks(tickSlide);
            nextSlide(); // show first slide immediately
            handler.postDelayed(tickSlide, SLIDE_MS);
        }
    }

    void stop() {
        handler.removeCallbacks(tickSlide);
        recycleBitmaps();
    }

    private void scanPhotos() {
        photoFiles.clear();
        File[] candidateDirs = new File[]{
                new File(Environment.getExternalStorageDirectory(), "Screensaver"),
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Screensaver"),
                new File("/sdcard/Screensaver")
        };

        for (File dir : candidateDirs) {
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        String name = f.getName().toLowerCase();
                        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")) {
                            if (!photoFiles.contains(f)) {
                                photoFiles.add(f);
                            }
                        }
                    }
                }
            }
        }

        if (!photoFiles.isEmpty()) {
            Collections.shuffle(photoFiles);
            currentPhotoIndex = 0;
            Log.d(TAG, "Found " + photoFiles.size() + " screensaver photos.");
        }
    }

    private void nextSlide() {
        if (photoFiles.isEmpty()) return;
        if (slideA == null || slideB == null) return;

        File file = photoFiles.get(currentPhotoIndex);
        currentPhotoIndex = (currentPhotoIndex + 1) % photoFiles.size();

        Bitmap bitmap = decodeSampledBitmap(file.getAbsolutePath(), 1920, 1080);
        if (bitmap == null) return;

        boolean isPortrait = bitmap.getHeight() > bitmap.getWidth();

        final FrameLayout incomingSlide = showingA ? slideB : slideA;
        final FrameLayout outgoingSlide = showingA ? slideA : slideB;
        final ImageView incomingAmbient = showingA ? ambientB : ambientA;
        final ImageView incomingPhoto = showingA ? photoB : photoA;

        // Set up incoming layer
        if (isPortrait) {
            // Ambient dimmed background on sides for portrait photos
            incomingAmbient.setVisibility(View.VISIBLE);
            incomingAmbient.setImageBitmap(bitmap);
        } else {
            incomingAmbient.setVisibility(View.GONE);
        }

        // Pure photo centered with original aspect ratio (never stretched)
        incomingPhoto.setImageBitmap(bitmap);

        // Cache bitmap reference
        if (showingA) {
            if (currentBitmapB != null && currentBitmapB != bitmap) {
                currentBitmapB.recycle();
            }
            currentBitmapB = bitmap;
        } else {
            if (currentBitmapA != null && currentBitmapA != bitmap) {
                currentBitmapA.recycle();
            }
            currentBitmapA = bitmap;
        }

        incomingSlide.setAlpha(0f);
        incomingSlide.setVisibility(View.VISIBLE);

        incomingSlide.animate().cancel();
        outgoingSlide.animate().cancel();

        incomingSlide.animate()
                .alpha(1f)
                .setDuration(900)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        outgoingSlide.animate()
                .alpha(0f)
                .setDuration(900)
                .withEndAction(() -> outgoingSlide.setVisibility(View.GONE))
                .start();

        showingA = !showingA;
    }

    private Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            int height = options.outHeight;
            int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            options.inSampleSize = Math.max(1, inSampleSize);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, options);
        } catch (Throwable t) {
            Log.e(TAG, "Failed to decode " + path, t);
            return null;
        }
    }

    private void recycleBitmaps() {
        if (currentBitmapA != null && !currentBitmapA.isRecycled()) {
            currentBitmapA.recycle();
            currentBitmapA = null;
        }
        if (currentBitmapB != null && !currentBitmapB.isRecycled()) {
            currentBitmapB.recycle();
            currentBitmapB = null;
        }
    }
}
