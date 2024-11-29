package com.grupo04.engine.interfaces;

import com.grupo04.engine.utilities.Callback;

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
}
