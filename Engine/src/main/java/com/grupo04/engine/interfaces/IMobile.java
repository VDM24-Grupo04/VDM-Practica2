package com.grupo04.engine.interfaces;

import com.grupo04.engine.utilities.Callback;

import java.util.concurrent.TimeUnit;

public interface IMobile {
    void showRewardedAd(Callback onReward);
    void shareAction(ShareActionType type, ShareParams params);
    enum ShareActionType { IMAGE, TEXT }
    class ShareParams {
        public String extraText;
        public int x, y, w, h;
        public int worldWidth, worldHeight;
        public boolean fullScreen = false;
        public String shareTitle = "";
    }
    void initializeNotifications(int channel_id, int channel_name, int channel_description, int notifications_workers_tag);
    void programNotification(int duration, TimeUnit unit, String key, String title, String message, int icon, int priority, int visibility);
    boolean isNotification(String type);
    int getAsset(String fileName, String defType);
    int getAsset(String fileName);
    int getHighPriorityValue();
    int getPublicVisibilityValue();
}
