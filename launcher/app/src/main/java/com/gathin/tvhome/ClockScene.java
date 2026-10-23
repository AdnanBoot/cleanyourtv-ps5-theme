package com.gathin.tvhome;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
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
import java.util.Random;

/**
 * Clean screensaver scene:
 * - If photos exist in /sdcard/Screensaver/: clean full-screen photo slideshow, aspect ratio preserved.
 * - If no photos exist: falls back to CleanYourTV's default drifting clock and color-shifting ambient glow.
 */
final class ClockScene {

    private static final String TAG = "ClockScene";
    private static final long SLIDE_MS = 10_000L;
    private static final long MOVE_MS = 60_000L;
    private static final int[] PALETTE = {0xFF2B5CFF, 0xFF00A39A, 0xFF8A3FFC, 0xFFE0661B, 0xFFD6336C};

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final View root;

    // Fallback default CleanYourTV views
    private final View content;
    private final View ambient;
    private final GradientDrawable glow;
    private int colorIndex;
    private int glowColor;

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

    private final Runnable tickClock = new Runnable() {
        @Override
        public void run() {
            moveClock();
            shiftGlow();
            handler.postDelayed(this, MOVE_MS);
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

        content = root.findViewById(R.id.dreamContent);
        ambient = root.findViewById(R.id.dreamAmbient);
        if (ambient != null) {
            glow = new GradientDrawable();
            glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            ambient.setBackground(glow);
            ambient.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) ->
                    glow.setGradientRadius((r - l) * 0.55f));
            colorIndex = random.nextInt(PALETTE.length);
            setGlow(PALETTE[colorIndex]);
        } else {
            glow = null;
        }
    }

    void start() {
        scanPhotos();

        if (!photoFiles.isEmpty()) {
            // Photo mode: hide default clock and run photo slideshow
            if (content != null) content.setVisibility(View.GONE);
            if (ambient != null) ambient.setVisibility(View.GONE);
            handler.removeCallbacks(tickSlide);
            handler.removeCallbacks(tickClock);
            nextSlide(); // show first slide immediately
            handler.postDelayed(tickSlide, SLIDE_MS);
        } else {
            // Fallback default CleanYourTV mode: drifting clock + date over ambient glow
            if (slideA != null) slideA.setVisibility(View.GONE);
            if (slideB != null) slideB.setVisibility(View.GONE);
            if (ambient != null) ambient.setVisibility(View.VISIBLE);
            if (content != null) {
                content.setVisibility(View.VISIBLE);
                content.setAlpha(0f);
                content.post(() -> {
                    placeClock();
                    content.animate().alpha(1f).setDuration(1200).start();
                });
            }
            handler.removeCallbacks(tickSlide);
            handler.removeCallbacks(tickClock);
            handler.postDelayed(tickClock, MOVE_MS);
        }
    }

    void stop() {
        handler.removeCallbacks(tickSlide);
        handler.removeCallbacks(tickClock);
        recycleBitmaps();
    }

    private void moveClock() {
        if (content == null) return;
        content.animate().alpha(0f).setDuration(900).withEndAction(() -> {
            placeClock();
            content.animate().alpha(1f).setDuration(1200).start();
        }).start();
    }

    private void placeClock() {
        if (content == null || !(content.getParent() instanceof View)) return;
        View parent = (View) content.getParent();
        int maxX = Math.max(0, parent.getWidth() - content.getWidth());
        int maxY = Math.max(0, parent.getHeight() - content.getHeight());
        float x = maxX * (0.1f + 0.8f * random.nextFloat());
        float y = maxY * (0.1f + 0.8f * random.nextFloat());
        content.setX(x);
        content.setY(y);
        if (parent.getWidth() > 0 && parent.getHeight() > 0 && glow != null) {
            glow.setGradientCenter((x + content.getWidth() / 2f) / parent.getWidth(),
                    (y + content.getHeight() / 2f) / parent.getHeight());
        }
    }

    private void shiftGlow() {
        if (glow == null) return;
        int from = glowColor;
        colorIndex = (colorIndex + 1) % PALETTE.length;
        ValueAnimator anim = ValueAnimator.ofObject(new ArgbEvaluator(), from, PALETTE[colorIndex]);
        anim.setDuration(8000);
        anim.addUpdateListener(a -> setGlow((int) a.getAnimatedValue()));
        anim.start();
    }

    private void setGlow(int color) {
        if (glow == null) return;
        glowColor = color;
        int c = color & 0x00FFFFFF;
        glow.setColors(new int[]{0x40000000 | c, 0x14000000 | c, 0x00000000 | c});
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
