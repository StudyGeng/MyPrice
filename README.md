# MyPrice

MyPrice is an Android diploma project for scanning a store QR code, viewing items, and tracking a shopping budget. Store owners can sign in and maintain their item list. The original screen layouts are preserved.

## Current status

The Android debug APK builds. The new Firebase project is connected in the source, but the app has **not** been tested on a phone. Firebase Realtime Database rules also need to be published before registration and item updates will work. Data and accounts from the deleted Firebase project are not in the new project.

## Connect the new Firebase project

1. In Firebase Console, select the **MyPrice** project with project ID `suprise1happyday`.
2. Check that the Android app package is `pmujtmk.yeo.myprice`. Keep the matching `google-services.json` in `app/`.
3. In **Authentication > Sign-in method**, enable **Email/Password**.
4. In **Realtime Database > Rules**, replace the locked default rules with the contents of [firebase-database.rules.json](firebase-database.rules.json), then click **Publish**. The rules allow anyone to read public stores and items. Only an approved, signed-in owner can write their own store and items. Never change the root rules to `.read: true` or `.write: true`.

## Approve a store account

1. On the app's **Register** screen, the store owner submits their details and opens the verification link sent to their email.
2. In Firebase Console **Authentication > Users**, find that email and copy its **User UID**.
3. In **Realtime Database > Data**, check that `approvalRequests/<User UID>` contains the store name and location.
4. Add `merchantApprovals/<User UID>` with the **Boolean** value `true` (not the text `"true"`).
5. The store owner signs in again. The app creates their public store listing. They can then add items and display their store QR code from the Account screen.

For a test, use your own email first. A shopper does not need an account; the shopper scans an approved store's QR code.

To remove an approval later, set `merchantApprovals/<User UID>` to `false` and delete that owner's `stores/<User UID>` and `items/<User UID>` records. Setting approval to false stops writes but does not hide already published public data.

## Build and test

Open this root folder in Android Studio. Use JDK 17 for Gradle and make Python 3.10 available for Chaquopy, then sync and run the `app` module. The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

Test on a phone: register and verify an owner, approve the UID, sign in, add an item, display its QR code, scan it as a shopper, and check the item and budget screens. The debug APK is for testing; publishing a release requires your own signing key and device testing.

## Security and limitations

- Firebase Authentication handles owner passwords. The app no longer bundles a Firebase Admin key or Gmail password. If the old Gmail app password still works, revoke it in the Google account.
- Store approval happens in Firebase Console. The app cannot approve itself.
- The old six-digit email code and old registration keys are retired. Firebase sends verification and password reset links.
- Account deletion currently requires an owner to remove the Firebase Authentication user and related Realtime Database records in Firebase Console. The app's Delete Account button explains this limitation.
- The old local Git history and source archives contained credentials. Do not publish this checkout's Git history or old backup ZIP. Use a clean export of the active app source for a public repository.
