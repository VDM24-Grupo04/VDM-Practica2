package com.grupo04.androidengine;

import android.app.Activity;

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
    private final String REWARD_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";

    private Activity mainActivity;
    private RewardedAd rewardedAd;

    public AndroidMobile(Activity mainActivity, AdView adView) {
        this.mainActivity = mainActivity;
        this.rewardedAd = null;

        MobileAds.initialize(this.mainActivity, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
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
        if (rewardedAd != null) {
            // Este callback se encarga de los eventos que suceden cuando se trata de
            // visualizar un anuncio ya cargado. Se debe establecer antes de llamar
            // al metodo show(), que hace que se reproduzca.
            // Ademas, los Rewarded ads siempre se muestran en pantalla completa.
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
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
        if (rewardedAd != null) {
            mainActivity.runOnUiThread(new Runnable() {
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
}
