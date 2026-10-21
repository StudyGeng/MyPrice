# MyPrice project review - 21 December 2021

## Which project to open

Open this folder in Android Studio. Its top-level `app/` is the fuller application. The nested `MyPrice/` directory is an older, smaller copy and was not changed during this review. The presentation and poster were used only as references; their layouts and the app's XML layouts were not changed.

## What the app does

MyPrice is an Android app with Java screens and Chaquopy Python logic. A shopper scans a store QR code, downloads the store's items from Firebase Realtime Database, sets a budget, scans product barcodes, manages a cart, and saves purchase history in SQLite. A store account can register, log in, manage items and promotions, and email its QR code. Barcode lookups use the local database after the store data has been downloaded.

## Code fixes made in the top-level app

- Preserved all OTP request details when resending a code, including the registration key and the email-change/reset/delete details.
- Fixed registration-key lookup so a matching key removes the correct database entry, and handled empty Firebase nodes during lookup and login.
- Made store and item refresh preserve the previous local session if the remote download fails. Data is now fetched as a single Firebase snapshot instead of relying on a separate network check.
- Made unrelated or damaged QR codes show the existing invalid-code message instead of crashing the scanner flow.
- Moved the SQLite database and generated QR/PDF files into app-private storage. The database code attempts to copy an existing database from the old packaged location if it is still available.
- The packaged legacy database is empty; this migration cannot recover history that an earlier app update already removed.
- Updated QR PDF creation to the current PyPDF2 method names without changing the PDF template.
- Prevented an empty checkout from creating another history entry and gave new history entries unique, sortable IDs.
- Preserved a successful registration or account deletion when the follow-up email fails, with a clear message to the user.
- Prevented a zero-price item from causing division by zero in promotion calculations, parameterized local SQL queries, and stopped other apps from directly opening two internal activities.
- Pinned Python dependencies compatible with this Chaquopy version and added a portable Gradle property for selecting a Python 3.8 build interpreter.
- Stopped writing passwords into new Firebase log entries. Account logs can fail without making a completed account operation appear to fail, and deletion now removes the account and store data in one Firebase update.
- Configured Android backup and device-transfer rules to exclude app data while the local login database contains a password. Existing local history will not be restored automatically on another device.

## Current verification status

- All six top-level Python source files and 125 Android XML files passed syntax/parsing checks.
- Mocked checks passed for registration-key lookup, Firebase snapshot handling, login result codes, the new persistent SQLite path, account creation when optional follow-up writes fail, password-free log failure handling, and account deletion paths.
- The final source built successfully into a debug APK with JDK 17, Python 3.8.20, Android Gradle Plugin 7.2.2, Gradle 7.3.3, and Chaquopy 12.0.1. The build no longer reports the earlier Python dependency conflicts.
- The Firebase project, SMTP account, camera scanning, account flows, and checkout were **not** tested against live services or a device. Their current availability cannot be inferred from source code alone.
- Current Java flows perform SQLite calls on the screen thread after remote work finishes. If database calls move to background threads later, each thread will need a suitable connection and coordinated writes.

## Building a private demonstration

Open this outer folder in Android Studio. Install Android SDK 32, JDK 17, and Python 3.8. Set the Gradle property `myPriceBuildPython` to the full path of the Python 3.8 executable, for example in your user-level `gradle.properties` file. From PowerShell, use `./gradlew.bat :app:assembleDebug "-PmyPriceBuildPython=C:/path/to/python38/python.exe"`. The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. These are build instructions; the APK still contains old privileged credentials and must not be shared.

## Work required before using real accounts or customer data

1. **Replace exposed credentials.** Firebase Admin service-account JSON files are bundled in both project copies. Gmail app credentials are embedded in Python source files. Treat both as exposed: revoke/rotate them in their owning accounts. Do not distribute an APK or source archive containing new privileged credentials.
2. **Move privileged work off the phone.** The Firebase Admin SDK grants broad database privileges and is intended for a trusted server. Move account management, database writes that need admin access, and mail sending to a backend. Use Firebase client authentication and database security rules for the Android app. The current password scheme is reversible, and a local login copy stores a password; migrate accounts to a proper authentication service.
3. **Check the backend setup.** Registration requires records under `/Registration_Key`; the included `AddRedeemCode` function is not called from the app. Confirm the Firebase project and mail account still exist and configure a safe test environment. No live data or credentials were accessed during this review.
4. **Modernize before Google Play release.** The project targets Android API 32. Google's 2026 Play submission requirement for new apps and updates is API 36. Upgrading the target and build dependencies will need device regression testing and may require screen adjustments, so it was left out of this layout-preserving review.
5. **Test the full flow on an Android device.** Build the top-level `app`, then test store registration, QR email generation, store QR scan, barcode lookup, budget alerts, cart, checkout/history, offline behavior after a successful download, and failed-network recovery. A successful build alone does not prove the backend services work.

## Presentation and poster notes

- The claim that item scanning needs no internet is only true after a store's data has been downloaded into SQLite. Starting a new store session requires Firebase access.
- The material describes advertisements, but the code implements product promotions and discounts. There is no separate advertisement manager.
- Claims that the app works for all stores and needs little maintenance are not established by the code or testing.
- If the files are reused, proofread phrases such as "An Android application that created", "Customer spent more than their budget", and "Agile methodology being used to ensure it achieve".

The slides, poster, screen XML, and PDF template were not edited.

## Reference links

- [Firebase Admin SDK setup](https://firebase.google.com/docs/admin/setup) and [Realtime Database security rules](https://firebase.google.com/docs/database/security)
- [Google Play target API requirement](https://developer.android.com/google/play/requirements/target-sdk)
- [Chaquopy version support](https://chaquo.com/chaquopy/doc/current/versions.html)
- [Android Auto Backup and data transfer rules](https://developer.android.com/identity/data/autobackup)
