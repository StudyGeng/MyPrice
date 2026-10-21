# MyPrice

Myprice is my diploma Android project for shopping with barcode scanning, a spending limit, a cart, and purchase history. This repository holds the active Android project source. Its screen layouts are unchanged.

## Current status

The original source built a debug APK after the fixes recorded in [PROJECT_REVIEW.md](PROJECT_REVIEW.md). The app has not been tested on a phone or against the live Firebase and email services. This repository is a **source backup**, not a ready-to-use APK or a production release.

The source here has been sanitized for GitHub. It omits the Firebase service-account JSON, email password, encryption keys, generated APKs, an empty local database, and an older incomplete duplicate project. The online account, store, and email features cannot work from this copy as supplied. Existing encrypted account data also needs the original keys, which are deliberately absent.

## Open and build

1. Open this folder in Android Studio.
2. Use JDK 17, Android SDK 32, and Python 3.8 for the Chaquopy build.
3. Point the Gradle property `myPriceBuildPython` to your Python 3.8 executable. For example, run `./gradlew.bat :app:assembleDebug "-PmyPriceBuildPython=C:/path/to/python38/python.exe"` from PowerShell.

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. A successful build alone does not establish that the app works end to end.

## Before real use

The historical design runs Firebase Admin and SMTP operations inside the Android app. Do not put new service-account or email credentials into an APK. Move those operations to a trusted backend, use Firebase client authentication and database rules, and replace reversible password storage. Revoke or rotate the credentials from the old project because they were embedded in its source and APK. See [PROJECT_REVIEW.md](PROJECT_REVIEW.md) for the review findings.

## License

The original MyPrice project source is available under the [MIT License](LICENSE). Third-party dependencies and assets retain their own licenses.
