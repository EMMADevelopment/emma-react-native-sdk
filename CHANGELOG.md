# Changelog

## 1.8.1
- Update iOS SDK dependency to version 4.15.5.

## 1.8.0

- Update native sdk dependencies: added new setUserLanguage method that allows users to manually set the language instead of relying on auto-detection.

## 1.7.0

- Added new method sendInAppDismissedClick
- Updated project to support AGP 8
- Updated SDK native to 4.14
- Updated target to Android 34

## 1.6.1

- Added compatibility with use_frameworks active.

## 1.6.0

- Added native version SDK 4.13 and iOS SDK Privacy Manifest and signature.

## 1.5.1

- Fixed RN not processing Float values correctly, changed to Double.

## 1.5.0

- SkadNetwork 4 support and notifications title.

## 1.4.2

- Fixed Android issue with targetSdkVersion when it was lower than 33.

## 1.4.1

- Fixed Android compilatation issue with Kotlin versions >= 1.7.

## 1.4.0

- Reestructured iOS SDK to fix some problems with header import in AppDelegate.

```objective-c
// Change import
@import emma_react_native_sdk;
// to
#import <emma-react-native-sdk/EmmaReactNative.h>
```

Change the AppDelegate methods to these:

```objective-c
[EmmaReactNative setPushNotificationsDelegate:self];
[EmmaReactNative registerToken:deviceToken];
[EmmaReactNative didReceiveNotificationResponse:response];
[EmmaReactNative didReceiveNotificationResponse:response withActionIdentifier:response.actionIdentifier];
[EmmaReactNative willPresentNotification:notification];
[EmmaReactNative handleLink:url];
```

## 1.3.1

- Added method to check if target 33 is setted in App for Android 13 notification permission.

## 1.3.0

- Android 13 notification permission support. Added methods for android `requestNotificationPermission` and `areNotificationsEnabled`.

## 1.2.1

- Added method `setCustomerId`.

## 1.2.0

- Updated RN version to 0.69+.

## 1.1.2

- Fix crash version SDK 4.10.0 y 4.10.1 when app started and recover ASA attribution.

## 1.1.1

- Updated sdks to version 4.10.+

## 1.1.0

- Fixed iOS app extension

## 1.0.0

- Start Session
- Track events
- Login and register
- User tags
- In-app messages
- Push notifications
- Powlink and deeplinking
- GDPR
- Purchases
