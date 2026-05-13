package com.maris7.settings.model;

import java.util.Locale;

public enum SettingFeature {
    HOTBAR_SERVER_MESSAGES,
    TOTEM_PARTICLES,
    EXPLOSION_PARTICLES,
    EXPLOSION_SOUNDS,
    DISABLE_MOB_SPAWN,
    PLAYER_VISIBILITY,
    COMMAND,
    SOUND_NOTIFICATIONS,
    TPA_TOGGLE,
    TPAHERE_TOGGLE,
    TPAGUI_TOGGLE,
    PRIVATE_MESSAGE,
    PUBLIC_CHAT,
    WORTHT_TOGGLE,
    AFTER_SONG_DUELS,
    REQUEST_DUEL,
    AUCTION_FAST_BUY,
    AUCTION_FAST_SELL,
    AUCTION_TOGGLE,
    PAY_TOGGLE,
    PAY_ALERTS,
    TEAM_CHAT,
    TEAM_TOGGLE,
    CHAINMAIL_SPAWN,
    DEATH_MESSAGE,
    ORDER_NOTIFICATION,
    CHAT_SERVER_MESSAGE;

    public static SettingFeature fromConfig(String input) {
        String normalized = input == null ? "" : input.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        return switch (normalized) {
            case "MOB_SPAWN", "DISABLE_MOB_SPAWN" -> DISABLE_MOB_SPAWN;
            case "PRIVATE_MESSAGES", "PRIVATE_MESSAGE" -> PRIVATE_MESSAGE;
            case "TPACCEPT_GUI", "TPAGUI", "TPAGUI_TOGGLE" -> TPAGUI_TOGGLE;
            case "WORTH_TOGGLE", "TOGGLE_WORTH", "WORTHT_TOGGLE" -> WORTHT_TOGGLE;
            case "AFTER_MUSIC_DUELS", "AFTERMUSIC_DUELS", "AFTER_SONG_DUELS" -> AFTER_SONG_DUELS;
            case "REQUEST_DUELS", "DUEL_REQUEST", "REQUEST_DUEL" -> REQUEST_DUEL;
            case "FAST_BUY", "AUCTION_FASTBUY", "AUCTION_FAST_BUY" -> AUCTION_FAST_BUY;
            case "FAST_SELL", "AUCTION_FASTSELL", "AUCTION_FAST_SELL" -> AUCTION_FAST_SELL;
            case "AH_TOGGLE", "AHTOGGLE", "AUCTION_TOGGLE" -> AUCTION_TOGGLE;
            case "TEAMCHAT", "TEAM_CHAT" -> TEAM_CHAT;
            case "TEAM_INVITES", "TEAMINVITES", "TEAM_TOGGLE" -> TEAM_TOGGLE;
            case "CHAINMAIL", "CHAINMAIL_SPAWN" -> CHAINMAIL_SPAWN;
            case "DEATH_MESSAGES", "DEATH_MESSAGE" -> DEATH_MESSAGE;
            case "ORDER_NOTIFICATIONS", "ORDER_NOTIFICATION" -> ORDER_NOTIFICATION;
            case "CHAT_MESSAGES", "CHAT_SERVER_MESSAGES", "CHAT_SERVER_MESSAGE" -> CHAT_SERVER_MESSAGE;
            default -> SettingFeature.valueOf(normalized);
        };
    }
}
