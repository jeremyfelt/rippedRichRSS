# RippedRichRSS

An Android port of [RichRSS](https://github.com/richardtape/RichRSS), feature-matching Rich's iOS RSS reader for Android devices.

## About

This project is a complete Android implementation that mirrors the features of RichRSS, an iOS RSS reader developed entirely by Claude. Just as the original RichRSS was built through LLM-assisted development, this Android port was also created by Claude to demonstrate feature parity across platforms.

## Features

### Core Functionality
- **RSS Feed Management**: Add, delete, and favorite RSS/Atom feeds
- **Feed Discovery**: Automatically discover RSS feeds from website URLs
- **Concurrent Feed Fetching**: Parallel updates of up to 8 feeds simultaneously for faster refresh
- **Article Reading**: Clean, distraction-free article reading experience with WebView
- **Offline Storage**: All feeds and articles stored locally using Room database

### User Experience
- **Material Design 3**: Modern Android UI with Jetpack Compose
- **Dark Mode**: System-wide dark theme support
- **Pull-to-Refresh**: Intuitive gesture to refresh feeds
- **Relative Time Formatting**: Human-friendly timestamps ("2 hours ago")
- **Read/Saved States**: Track read articles and save favorites
- **Favicon Support**: Feed icons fetched and displayed

### Background Features
- **Background Refresh**: Automatic feed updates using WorkManager
- **Wi-Fi Only Option**: Conserve mobile data with Wi-Fi-only refresh
- **Smart Feed Prioritization**: Favorites refreshed first, then oldest feeds
- **Network-Aware**: Respects network conditions and battery state

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite)
- **Networking**: OkHttp + Retrofit
- **Background Tasks**: WorkManager
- **Async**: Kotlin Coroutines & Flow
- **Image Loading**: Coil
- **Preferences**: DataStore

## Requirements

- Android 8.0 (API 26) or higher
- Recommended: Android 16 (Pixel 8)

## Building

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on device or emulator

## Architecture

The app follows modern Android development best practices:

```
app/
├── data/
│   ├── model/          # Room entities (Feed, Article)
│   ├── dao/            # Data access objects
│   ├── repository/     # Repository layer
│   ├── rss/            # RSS parsing and fetching
│   └── preferences/    # App settings
├── ui/
│   ├── screens/        # Composable screens
│   ├── theme/          # Material Design theme
│   └── viewmodel/      # ViewModels
├── util/               # Utility classes
└── worker/             # Background workers
```

## Feature Comparison with iOS Version

| Feature | iOS (RichRSS) | Android (RippedRichRSS) |
|---------|---------------|-------------------------|
| RSS/Atom Parsing | ✅ | ✅ |
| Feed Management | ✅ | ✅ |
| Concurrent Fetching | ✅ (8 concurrent) | ✅ (8 concurrent) |
| Background Refresh | ✅ (BGTaskScheduler) | ✅ (WorkManager) |
| Wi-Fi Only Mode | ✅ | ✅ |
| Relative Time | ✅ | ✅ |
| Dark Mode | ✅ | ✅ |
| Article Reader | ✅ | ✅ |
| Saved Articles | ✅ | ✅ |
| Favicon Support | ✅ | ✅ |

## Credits

- **Original iOS App**: [RichRSS by Rich Tape](https://github.com/richardtape/RichRSS)
- **Android Port**: Built by Claude (Anthropic)
- **Inspiration**: A friendly port to bring RSS reading to Android users

## License

Built as a demonstration project. See the original RichRSS repository for licensing information.
