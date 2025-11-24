# Building RippedRichRSS

There are three ways to build the APK without Android Studio:

## Option 1: Command Line (Local Build)

If you have Java 17+ installed locally:

```bash
# Make gradlew executable (Linux/Mac)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK (unsigned)
./gradlew assembleRelease
```

The APK will be output to:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Option 2: GitHub Actions (Automated Cloud Build)

The easiest way if you're on your phone! To set it up:

1. Create `.github/workflows/build-apk.yml` with this content:

```yaml
name: Build Android APK

on:
  push:
    branches: [ "claude/*", "main" ]
  pull_request:
    branches: [ "main" ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Build debug APK
      run: ./gradlew assembleDebug

    - name: Build release APK
      run: ./gradlew assembleRelease

    - name: Upload debug APK
      uses: actions/upload-artifact@v4
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk

    - name: Upload release APK (unsigned)
      uses: actions/upload-artifact@v4
      with:
        name: app-release-unsigned
        path: app/build/outputs/apk/release/app-release-unsigned.apk
```

2. Push to GitHub
3. Go to the "Actions" tab in your repository
4. Wait for the build to complete (~5 minutes)
5. Download the APK from the build artifacts

The workflow automatically runs on:
- Any push to branches starting with `claude/`
- Any push to `main` branch
- Manual trigger via "Run workflow" button

## Option 3: Docker (Containerized Build)

If you have Docker installed:

```bash
# Build using official Android Docker image
docker run --rm -v "$PWD":/project -w /project \
  mingc/android-build-box:latest \
  bash -c "chmod +x gradlew && ./gradlew assembleDebug"
```

The APK will be in `app/build/outputs/apk/debug/`.

## Installing on Your Phone

### Debug APK (Easiest)
1. Download `app-debug.apk`
2. Transfer to your phone
3. Open the APK file
4. Allow installation from unknown sources if prompted
5. Install and enjoy!

### Release APK (Requires Signing)
The release APK needs to be signed before installation. You can:
- Use the debug APK instead (works fine for personal use)
- Sign it yourself with a keystore
- Use GitHub Actions with secrets to auto-sign

## Requirements

- **Java**: JDK 17 or higher
- **Gradle**: Handled by wrapper (gradlew)
- **Android SDK**: Downloaded automatically by Gradle
- **Memory**: At least 4GB RAM recommended
- **Disk**: ~5GB for SDK and build cache

## Troubleshooting

### "Permission denied: ./gradlew"
```bash
chmod +x gradlew
```

### "JAVA_HOME not set"
```bash
# Find Java installation
which java

# Set JAVA_HOME (add to ~/.bashrc or ~/.zshrc)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

### "Out of memory"
Edit `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
```

### Build takes forever
First build downloads the Android SDK (~2GB) and dependencies. Subsequent builds are much faster with caching.

## Quick Test Build

To test if everything works:

```bash
./gradlew tasks
```

This should list all available Gradle tasks without errors.
