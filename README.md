# MarisSettings

MarisSettings is a player settings plugin with an admin reload command.

## What It Handles

- Player-facing settings menu
- Configurable messages
- Admin reload path

## Requirements

- Paper / Folia 1.21+
- Java 21

## Installation

1. Place the plugin jar in `plugins`.
2. Start the server once.
3. Edit `config.yml` and `messages.yml`.
4. Restart the server.

## Commands

- `/settings` - Open player settings.
- `/settingsadmin reload` - Reload plugin files.

## Files

- `config.yml` - Main settings.
- `messages.yml` - Messages and UI text.

## Notes

- Other Maris plugins may use this plugin as an optional dependency.
- Keep setting keys stable if other plugins read them indirectly.