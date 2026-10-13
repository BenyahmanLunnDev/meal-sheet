# Meal Sheet

A small Android app for people who get charged for meals at work. Tap to "initial" breakfast, lunch, or dinner, see every meal on a calendar, and check the total for each pay period against the deduction on your paystub.

- No ads, no account, no analytics
- **No internet permission**: everything stays on the phone
- Per-meal prices; weekly, biweekly, twice-a-month, or monthly pay periods
- Late entries are marked; share summaries or export CSV

## Build

Requires JDK 17 and the Android SDK (platform 36).

```bash
./gradlew assembleDebug      # debug APK
./gradlew bundleRelease      # Play Store bundle (needs keystore.properties, see keystore.properties.example)
```

Kotlin + Jetpack Compose, minSdk 26, targetSdk 36. Data is stored in SharedPreferences (`MealStore.kt`).

See [PUBLISHING.md](PUBLISHING.md) for the Play Store checklist and [PRIVACY.md](PRIVACY.md) for the privacy policy.

## Support

Meal Sheet is free, with no ads and no tracking, and it will stay that way. If it helps you and you'd like to chip in, there will be a link here soon.

## License

Copyright © 2026 [@BenyahmanLunnDev](https://github.com/BenyahmanLunnDev)

Meal Sheet is free software under the [GNU General Public License v3.0](LICENSE). You can use, study, share, and change it. If you distribute a modified version, it has to stay open source under the same license.
