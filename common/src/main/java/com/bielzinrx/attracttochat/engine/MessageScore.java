package com.bielzinrx.attracttochat.engine;

import com.bielzinrx.attracttochat.config.AttractToChatConfig;

import java.util.UUID;

public final class MessageScore {
    public final int caps;
    public final int excl;
    public final int letters;
    public final double loudness;
    public final double saturation;
    public final String factor;
    public final UUID playerUUID;

    public MessageScore(String msg, UUID playerUUID) {
        this.playerUUID = playerUUID;
        int c = 0, e = 0, l = 0;
        boolean capsEnabled = AttractToChatConfig.COMMON.enableCapsFeature.get();
        int limit = Math.min(msg != null ? msg.length() : 0, 256);

        for (int i = 0; i < limit; i++) {
            char ch = msg.charAt(i);
            if (Character.isLetter(ch)) {
                l++;
                if (capsEnabled && Character.isUpperCase(ch)) c++;
            }
            else if (ch == '!') e++;
        }

        this.caps   = c;
        this.excl   = e;
        this.letters = l;
        this.saturation = 1.0 - Math.exp(-(c + e * 1.5) / 12.0);
        this.loudness   = 0.8 + this.saturation * 2.2;

        boolean shoutByRatio = l >= 2 && c > l * 0.55;
        boolean shoutByVolume = c >= 3 || e >= 2 || (c + e) >= 4;
        this.factor = (shoutByRatio || shoutByVolume) ? "shout" : "normal";
    }
}
