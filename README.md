# MarisSettings

MarisSettings is a player settings plugin used to let players toggle common chat, duel, auction, teleport, sound, and visibility features from one menu.

## What It Handles

- Central player settings GUI
- Per-player feature toggles stored in plugin data
- Optional integration point for other Maris plugins
- Admin reload command

## Current Features

The default settings menu ships with these toggles:

- `PUBLIC_CHAT` - Enable or disable public chat visibility.
- `PRIVATE_MESSAGE` - Enable or disable private messages.
- `HOTBAR_SERVER_MESSAGES` - Show or hide hotbar server messages.
- `PAY_ALERTS` - Enable or disable payment alerts.
- `CHAT_SERVER_MESSAGE` - Show or hide chat-based server broadcasts.
- `AUCTION_TOGGLE` - Enable or disable auction alerts.
- `TOTEM_PARTICLES` - Show or hide totem particle effects.
- `EXPLOSION_PARTICLES` - Show or hide explosion particles.
- `EXPLOSION_SOUNDS` - Enable or disable explosion sounds.
- `AUCTION_FAST_BUY` - Allow quick buy flow in auction menus.
- `CHAINMAIL_SPAWN` - Toggle the chainmail spawn effect or state used by the server.
- `DISABLE_MOB_SPAWN` - Personal toggle related to mob spawn handling where your server uses it.
- `PLAYER_VISIBILITY` - Show or hide other players.
- `COMMAND` - Dispatch a configured command shortcut from the settings menu.
- `TPAGUI_TOGGLE` - Enable or disable the TPA menu flow.
- `SOUND_NOTIFICATIONS` - Enable or disable general sound notifications.
- `ORDER_NOTIFICATION` - Enable or disable order market notifications.
- `AUCTION_FAST_SELL` - Allow quick sell flow in auction menus.
- `REQUEST_DUEL` - Accept or block duel requests.
- `TPA_TOGGLE` - Accept or block `/tpa` requests.
- `TPAHERE_TOGGLE` - Accept or block `/tpahere` requests.
- `TEAM_TOGGLE` - Accept or block team invites.
- `PAY_TOGGLE` - Accept or block payments.
- `TEAM_CHAT` - Enable or disable team chat mode.
- `WORTHT_TOGGLE` - Show or hide worth display support used by your economy flow.
- `AFTER_SONG_DUELS` - Enable or disable post-duel song playback.

Some features depend on other Maris plugins being installed and actually reading the stored toggle.

## Requirements

- Paper / Folia 1.21+
- Java 21

## Installation

1. Place the plugin jar in `plugins`.
2. Start the server once.
3. Edit `config.yml` and `messages.yml`.
4. Review the GUI item list in `config.yml` if you want to change enabled features, slots, materials, or permissions.
5. Restart the server.

## Commands

- `/settings` - Open player settings.
- `/settingsadmin reload` - Reload plugin files.

## Files

- `config.yml` - Main settings, database backend, GUI layout, and toggle list.
- `messages.yml` - Messages and UI text.

## Storage

MarisSettings supports:

- SQLite
- MySQL

The backend is selected in `config.yml` under `database.type`.

## Notes

- Other Maris plugins may use this plugin as an optional dependency.
- Keep setting keys stable if other plugins read them indirectly.
- This plugin is marked as Folia supported.