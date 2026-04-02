# MetroWave

A modern music player app for Android with a Metro/Zune-inspired design.

## Features

- **Metro/Zune UI Design** - Dark theme with blur effects and accent colors
- **Horizontal Swipe Navigation** - 6 pages: Now Playing, Songs, Albums, Artists, Search, Favorites
- **Artist Images** - HD artist photos from TheAudioDB API
- **Quick Navigation** - Letter sidebar (A-Z + #) for fast scrolling through lists
- **Equalizer** - Built-in audio equalizer with presets
- **Favorites** - Save and manage your favorite songs
- **Widget** - Home screen widget with playback controls
- **Mini Player** - Compact player visible across all screens

## Screenshots

Metro/Zune inspired dark interface with smooth animations and gesture navigation.

## Tech Stack

- **Kotlin** - Modern Android development
- **Jetpack Compose** - Declarative UI
- **MediaSession** - Media playback and system integration
- **Coil** - Image loading
- **Glance** - App Widget

## Permissions

- `READ_MEDIA_AUDIO` - Access music files
- `POST_NOTIFICATIONS` - Media notifications
- `FOREGROUND_SERVICE` - Background playback

## Build

```bash
./gradlew assembleDebug
```

APK will be in `app/build/outputs/apk/debug/`

## License

MIT License
