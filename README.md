# 🌿 Zen Stone Garden Widget

A peaceful Android home screen widget that brings the tranquility of Japanese zen stone gardens to your device.

## ✨ Features

- **Dynamic Stone Garden**: Each update creates a unique zen garden with stones and rippling water effects
- **Time-based Atmosphere**: Background colors change based on time of day
  - 🌞 Morning/Day: Pure white background
  - 🌇 Evening: Warm orange tones
  - 🌙 Night: Deep navy blue
- **Interactive Updates**: Tap the widget to refresh and see a new garden layout
- **Automatic Refresh**: Widget updates every 15 minutes by default
- **Minimalist Design**: Clean, zen-inspired aesthetic perfect for Nothing phones

## 🏗️ Technical Details

- **Grid System**: 32x32 pixel grid for precise dot placement
- **Ripple Effects**: Mathematical wave propagation simulation
- **Bitmap Rendering**: Custom Canvas drawing for smooth performance
- **AppWidget Provider**: Standard Android widget architecture
- **WorkManager**: Reliable background updates

## 📱 Installation

1. Build the APK using Android Studio or Gradle
2. Install on your Android device
3. Long-press on home screen → Widgets → Zen Stone Garden
4. Place and enjoy the peaceful experience

## 🎨 Customization

The widget automatically adapts to:
- Different widget sizes (resizable)
- System theme preferences
- Time of day changes

## 🛠️ Development

### Prerequisites
- Android Studio
- Android SDK API 21+
- Kotlin support

### Building
```bash
./gradlew assembleRelease
```

### Architecture
- `ZenRenderer.kt`: Core bitmap generation logic
- `ZenWidgetProvider.kt`: Android widget lifecycle management
- `MainActivity.kt`: Optional configuration and preview UI

## 📋 Play Store Distribution

The app is designed for Play Store distribution with:
- AAB (Android App Bundle) support
- Target SDK 34
- Proper permissions and metadata
- Professional app icon and screenshots

## 🔮 Future Enhancements

- Settings for customizing ripple speed and stone count
- Sound effects toggle
- Live wallpaper integration
- Multiple garden themes
- Seasonal color variations

## 📄 License

This project is designed for educational and personal use. Feel free to modify and distribute according to your needs.

---

*Experience zen in your pocket* 🧘‍♂️
