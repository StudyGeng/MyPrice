# MyPrice project review — 21 October 2026

## What was checked

The active Android project is the outer folder's `app/` module. The nested `MyPrice/` directory is an older copy. The supplied presentation and poster were treated as project references, not as instructions to change the app. No screen XML layouts were changed.

## Changes made for the new Firebase project

- Connected the app package `pmujtmk.yeo.myprice` to the new Firebase project `suprise1happyday` using `app/google-services.json` and the Google Services Gradle plug-in.
- Replaced the old Firebase Admin database access in the active app with Firebase Authentication and user-scoped Realtime Database requests. Removed the Admin key files from the working tree and removed the old client SMTP password and sender code.
- Added email verification and a manual approval request. After a store owner registers, an administrator must set `merchantApprovals/<uid>` to the Boolean `true` in Firebase Console. The app then publishes that owner's public store name, location, and items.
- Kept passwords out of public database records and the local SQLite login cache. A one-time migration clears old store caches and any cached owner password while preserving personal purchase history.
- Replaced the old six-digit email code with Firebase's verification and password reset links. Store QR codes now contain the new Firebase user ID, and the owner can display the QR code in the app.
- Added proposed Realtime Database rules in `firebase-database.rules.json`. These are **not published automatically**; the current console screenshot showed the default deny-all rules.

## Verification and current limits

- `:app:assembleDebug` completed successfully with JDK 17, Python 3.10, Gradle 8.2, Android Gradle Plugin 8.1.1, and Chaquopy 15.0.1.
- Python syntax and mocked adapter checks passed. The rules file parses as JSON. The built APK was checked for the old project ID, Firebase Admin key filename, and SMTP code; none was found.
- No Android phone or live Firebase end-to-end test was available. The APK is a **test build**, not a confirmed production release.
- The old Firebase project's accounts and items do not appear in the new empty project. New owners and items must be entered again.
- Account deletion is currently a manual Firebase Console operation. The button tells the user this instead of claiming that it deleted the account.
- The local Git history and old backup archive contained credentials. Do not push this checkout or archive to GitHub. Publish only a clean export of the active source.

## Next test

Publish the rules from `firebase-database.rules.json`, register with your own email, verify the link, approve that account UID in Realtime Database, sign in, add an item, then scan the store QR code as a shopper. See `README.md` for the exact console steps.
