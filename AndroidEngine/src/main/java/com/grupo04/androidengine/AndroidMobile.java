package com.grupo04.androidengine;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.view.PixelCopy;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

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

public class AndroidMobile implements IMobile {
    private final Activity mainActivity;
    private final SurfaceView window;
    private RewardedAd rewardedAd;

    private String rewardAdUnitId;

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

        try {
            // Obtiene la informacion del manifest
            PackageManager packageManager = this.mainActivity.getPackageManager();
            Bundle metaData =  packageManager.getApplicationInfo(this.mainActivity.getPackageName(), packageManager.GET_META_DATA).metaData;

            // Obtiene la ad unit id del metadata del manifest
            this.rewardAdUnitId = metaData.getString("com.google.android.gms.ads.APPLICATION_ID");
        }
        catch (Exception e) {
            System.out.println("Couldn't get app metadata");
        }

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

        // Si ha podido obtener la ad unit id, intenta crear el banner
        if (rewardAdUnitId != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(this.mainActivity, rewardAdUnitId, adRequest, new RewardedAdLoadCallback() {
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
        else {
            System.out.println("Couldn't get ad unit id");
        }
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
}
