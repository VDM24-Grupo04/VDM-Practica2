package com.grupo04.androidengine;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.view.PixelCopy;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
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
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.grupo04.engine.interfaces.IMobile;
import com.grupo04.engine.utilities.Callback;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AndroidMobile implements IMobile {
    private final String REWARD_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";

    private final Activity mainActivity;
    private final SurfaceView window;
    private RewardedAd rewardedAd;

    public static String CHANNEL_ID = "channel_id";
    public static String CHANNEL_NAME = "channel_name";
    public static String CHANNEL_DESCRIPTION = "channel_description";
    public static String WORKERS_TAG = "workers_tag";

    public AndroidMobile(Activity mainActivity, SurfaceView window, AdView adView) {
        this.mainActivity = mainActivity;
        this.window = window;
        this.rewardedAd = null;

        MobileAds.initialize(this.mainActivity, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                System.out.println("Advertisements loaded");
            }
        });

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
                    this.shareImageAction(params.extraText, 0,0, this.window.getWidth(), this.window.getHeight(), params.shareTitle);
                } else {
                    // worldWidth y worldHeight no pueden ser 0
                    if (params.worldWidth == 0 || params.worldHeight == 0) {
                        System.err.println("worldWidth or worldHeight cannot be zero.");
                        return;
                    }

                    float scaleX = this.window.getWidth() / (float)(params.worldWidth);
                    float scaleY = this.window.getHeight() / (float)(params.worldHeight);
                    int x = (int)(params.x * scaleX);
                    int y = (int)(params.y * scaleY);
                    int w = (int)(params.w * scaleX);
                    int h = (int)(params.h * scaleY);
                    this.shareImageAction(params.extraText, x, y, w, h, params.shareTitle);
                }
                break;
            case TEXT: this.shareTextAction(params.extraText, params.shareTitle); break;
            // ...
        }
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
                        String imageUri = MediaStore.Images.Media.insertImage(mainActivity.getContentResolver(), finalBitmap, uniqueImageName, null);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/*");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, extraText);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageUri));
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        mainActivity.startActivity(Intent.createChooser(shareIntent, shareTitle));
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
    public void initializeNotifications(int channel_id, int channel_name, int channel_description, int notifications_workers_tag) {
        CHANNEL_ID = this.mainActivity.getString(channel_id);
        CHANNEL_NAME = this.mainActivity.getString(channel_name);
        CHANNEL_DESCRIPTION = this.mainActivity.getString(channel_description);
        WORKERS_TAG = this.mainActivity.getString(notifications_workers_tag);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES. O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager notificationManager = this.mainActivity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void programNotification(int duration, TimeUnit unit, String key, String title, String message, int icon, int priority, int visibility) {
        String packageName = this.mainActivity.getPackageName();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(duration, unit)
                .setInputData(new Data.Builder()
                    .putString("package_name", packageName)
                    .putString("channel_id", CHANNEL_ID)
                    .putString("key", key)
                    .putString("title", title)
                    .putString("message", message)
                    .putInt("icon", icon)
                    .putInt("priority", priority)
                    .putInt("visibility", visibility)
                    .build())
                .addTag(WORKERS_TAG)
                .build();

        WorkManager.getInstance(this.mainActivity.getApplicationContext()).enqueue(request);
    }

    @Override
    public boolean isNotification(String type) {
        Intent intent = this.mainActivity.getIntent();
        // Comprueba especificamente que sea la clave key de la Uri en data
        return intent != null && Objects.equals(intent.getDataString(), type);
    }
}
