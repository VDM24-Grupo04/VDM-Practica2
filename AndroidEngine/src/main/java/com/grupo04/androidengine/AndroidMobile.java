package com.grupo04.androidengine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;

import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.grupo04.engine.interfaces.IMobile;
import com.grupo04.engine.utilities.Callback;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AndroidMobile implements IMobile {
    private final String REWARD_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";

    private final Activity mainActivity;
    private final SurfaceView window;
    private RewardedAd rewardedAd;

    public static String CHANNEL_ID = "Reward Channel ID";
    public static String CHANNEL_NAME = "Reward Channel";
    public static String CHANNEL_DESCRIPTION = "This channel gives rewards to the user";
    public static String WORKERS_TAG = "Rewards workers";

    public AndroidMobile(Activity mainActivity, SurfaceView window, AdView adView) {
        this.mainActivity = mainActivity;
        this.window = window;
        this.rewardedAd = null;

        this.initializeNotifications();

        MobileAds.initialize(this.mainActivity, initializationStatus -> System.out.println("Advertisements loaded"));

        loadBannerAd(adView);
        loadRewardedAd();
    }

    private void loadBannerAd(AdView adView) {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void fullScreenCallback() {
        if (this.rewardedAd != null) {
            // Este callback se encarga de los eventos que suceden cuando se trata de
            // visualizar un anuncio ya cargado. Se debe establecer antes de llamar
            // al metodo show(), que hace que se reproduzca.
            // Ademas, los Rewarded ads siempre se muestran en pantalla completa.
            this.rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                // Se llama cuando se clica en el anuncio
                @Override
                public void onAdClicked() {
                    System.out.println("Advertisement clicked");
                }

                // Se llama el usuario cierra el anuncio
                @Override
                public void onAdDismissedFullScreenContent() {
                    System.out.println("Advertisement finished");
                    loadRewardedAd();
                }

                // Se llama cuando se produce un error al reproducir el anuncio
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    System.err.println("Advertisement failed to show: " + adError);
                }

                // Se llama cuando se registra una visualizacion (impresion) en el anuncio
                @Override
                public void onAdImpression() {
                    System.out.println("Advertisement viewed");
                }

                // Se llama cuando se muestra el anuncio
                @Override
                public void onAdShowedFullScreenContent() {
                    System.out.println("Advertisement shown");
                }
            });
        }
    }

    // No es recomendable mostrar el mismo anuncio ya que se cobra por anuncio cargado.
    // Por lo tanto, cada vez que el usuario ve un Rewarded Ad, hay que cargar uno nuevo
    private void loadRewardedAd() {
        this.rewardedAd = null;

        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this.mainActivity, REWARD_AD_UNIT_ID, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                System.err.println("Failed to load reward advertisement: " + loadAdError);
                loadRewardedAd();
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                System.out.println("Reward advertisement loaded");
                rewardedAd = ad;
                fullScreenCallback();
            }
        });
    }

    @Override
    public void showRewardedAd(Callback onReward) {
        if (this.rewardedAd != null) {
            this.mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rewardedAd.show(mainActivity, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            onReward.call();
                        }
                    });
                }
            });
        } else {
            System.err.println("The rewarded ad wasn't ready yet");
        }
    }

    @Override
    public void shareAction(ShareActionType type, ShareParams params) {
        switch (type) {
            case IMAGE:
                if (params.fullScreen) {
                    this.shareImageAction(params.extraText, 0, 0, this.window.getWidth(), this.window.getHeight(), params.shareTitle);
                } else {
                    // worldWidth y worldHeight no pueden ser 0
                    if (params.worldWidth == 0 || params.worldHeight == 0) {
                        System.err.println("worldWidth or worldHeight cannot be zero.");
                        return;
                    }

                    float scaleX = this.window.getWidth() / (float) (params.worldWidth);
                    float scaleY = this.window.getHeight() / (float) (params.worldHeight);
                    int x = (int) (params.x * scaleX);
                    int y = (int) (params.y * scaleY);
                    int w = (int) (params.w * scaleX);
                    int h = (int) (params.h * scaleY);
                    this.shareImageAction(params.extraText, x, y, w, h, params.shareTitle);
                }
                break;
            case TEXT:
                this.shareTextAction(params.extraText, params.shareTitle);
                break;
            // ...
        }
    }

    private Uri saveBitmapToGallery(Bitmap bitmap, Context context, String name) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name + ".png");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (imageUri == null) {
            Toast.makeText(context, "Failed to create a new MediaStore record", Toast.LENGTH_SHORT).show();
            return null;
        }

        try (OutputStream fos = resolver.openOutputStream(imageUri)) {
            assert fos != null;
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            Toast.makeText(context, "Image saved in gallery", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "Error saving image: " + e.getMessage(), Toast.LENGTH_LONG).show();
            resolver.delete(imageUri, null, null);
            return null;
        }
        return imageUri;
    }

    private void shareImageAction(String extraText, int x, int y, int w, int h, String shareTitle) {
        Bitmap bitmap = Bitmap.createBitmap(this.window.getWidth(), this.window.getHeight(), Bitmap.Config.ARGB_8888);
        HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        PixelCopy.request(this.window, bitmap, new PixelCopy.OnPixelCopyFinishedListener() {
            @Override
            public void onPixelCopyFinished(int copyResult) {
                try {
                    if (copyResult == PixelCopy.SUCCESS) {
                        Bitmap finalBitmap = Bitmap.createBitmap(bitmap, x, y, w, h);
                        String uniqueImageName = "screenshot_" + System.currentTimeMillis();
                        Uri imageUri = saveBitmapToGallery(finalBitmap, mainActivity.getApplicationContext(), uniqueImageName);
                        if (imageUri != null) {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("image/*");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, extraText);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            mainActivity.startActivity(Intent.createChooser(shareIntent, shareTitle));
                        }
                        finalBitmap.recycle();
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                } finally {
                    // Se lanza siempre sin importar si hubo excepción o no
                    bitmap.recycle();
                    handlerThread.quitSafely();
                }
            }
        }, new Handler(handlerThread.getLooper()));
    }

    private void shareTextAction(String text, String shareTitle) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        this.mainActivity.startActivity(Intent.createChooser(shareIntent, shareTitle));
    }

    @Override
    public void initializeNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager notificationManager = this.mainActivity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private int convertPriority(NotificationPriority priority) {
        switch (priority) {
            case HIGH:
                return NotificationCompat.PRIORITY_HIGH;
            case DEFAULT:
                return NotificationCompat.PRIORITY_DEFAULT;
            case LOW:
                return NotificationCompat.PRIORITY_LOW;
            case MAN:
                return NotificationCompat.PRIORITY_MAX;
            case MIN:
                return NotificationCompat.PRIORITY_MIN;
            default:
                return 0;
        }
    }

    private int convertVisibility(NotificationVisibility visibility) {
        switch (visibility) {
            case PUBLIC:
                return NotificationCompat.VISIBILITY_PUBLIC;
            case PRIVATE:
                return NotificationCompat.VISIBILITY_PRIVATE;
            case SECRET:
                return NotificationCompat.VISIBILITY_SECRET;
            default:
                return 0;
        }
    }

    @Override
    public void programNotification(int duration, TimeUnit unit, String key, String title, String message,
                                    int icon, NotificationPriority priority, NotificationVisibility visibility) {
        String packageName = this.mainActivity.getPackageName();
        icon = validateIcon(icon);
        if (icon != -1) {
            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                    .setInitialDelay(duration, unit)
                    .setInputData(new Data.Builder()
                            .putString("package_name", packageName)
                            .putString("channel_id", CHANNEL_ID)
                            .putString("key", key)
                            .putString("title", title)
                            .putString("message", message)
                            .putInt("icon", icon)
                            .putInt("priority", convertPriority(priority))
                            .putInt("visibility", convertVisibility(visibility))
                            .build())
                    .addTag(WORKERS_TAG)
                    .build();

            WorkManager.getInstance(this.mainActivity.getApplicationContext()).enqueue(request);
        } else {
            System.err.println("Didn't program any notification");
        }
    }

    private int validateIcon(int iconId) {
        // Intenta acceder al icono
        try {
            Resources res = this.mainActivity.getResources();
            ResourcesCompat.getDrawable(res, iconId, this.mainActivity.getTheme());
            return iconId;
        } catch (Resources.NotFoundException e) {
            System.err.println("Notification icon id was not found");
            return -1;
        }
    }

    @Override
    public boolean isNotification(String type) {
        Intent intent = this.mainActivity.getIntent();
        // Comprueba especificamente que sea la clave key de la Uri en data
        return intent != null && Objects.equals(intent.getDataString(), type);
    }

    @SuppressLint("DiscouragedApi")
    @Override
    public int getAsset(String fileName, String defType) {
        return this.mainActivity.getResources().getIdentifier(fileName, defType, this.mainActivity.getPackageName());
    }

    @Override
    public int getAsset(String fileName) {
        return this.getAsset(fileName, "drawable");
    }
}
