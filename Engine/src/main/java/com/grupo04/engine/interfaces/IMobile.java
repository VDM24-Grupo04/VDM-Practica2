package com.grupo04.engine.interfaces;

import com.grupo04.engine.utilities.Callback;

import java.util.concurrent.TimeUnit;

public interface IMobile {
    enum NotificationVisibility {PUBLIC, PRIVATE, SECRET}
    enum NotificationPriority {HIGH, DEFAULT, LOW, MAN, MIN}

    enum ShareActionType {IMAGE, TEXT}
    class ShareParams {
        public String extraText;
        public int x, y, w, h;
        public int worldWidth, worldHeight;
        public boolean fullScreen = false;
        public String shareTitle = "";
    }

    void showRewardedAd(Callback onReward);

    void shareAction(ShareActionType type, ShareParams params);

    void initializeNotifications();
    void programNotification(int duration, TimeUnit unit, String key, String title, String message, int icon, NotificationPriority priority, NotificationVisibility visibility);
    boolean isNotification(String type);

    int getAsset(String fileName, String defType);
    int getAsset(String fileName);
}
