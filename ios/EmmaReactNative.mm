#import "EmmaReactNative.h"

#import <React/RCTLog.h>
#import <UserNotifications/UserNotifications.h>


#import "emma_react_native_sdk-Swift.h"


@implementation EmmaReactNative

RCT_EXPORT_MODULE();

+ (BOOL)requiresMainQueueSetup {
    return NO;
}

// MARK: AppDelegate
+(void) setPushNotificationsDelegate: (id<UNUserNotificationCenterDelegate>) notificationCenterDelegate {
    [[EmmaReactNativePush shared] setPushNotificationsDelegate:notificationCenterDelegate];
}

+(void) registerToken: (NSData *) deviceToken {
    [[EmmaReactNativePush shared] registerToken: deviceToken];
}

+(void) didReceiveNotificationResponse:(UNNotificationResponse *) response {
    [[EmmaReactNativePush shared] didReceiveNotificationWithUserInfo:response.notification.request.content.userInfo];
}

+(void) didReceiveNotificationResponse:(UNNotificationResponse *)response withActionIdentifier:(NSString*) actionIdentifier {
    [[EmmaReactNativePush shared] didReceiveNotificationWithUserInfo:response.notification.request.content.userInfo actionIdentifier:actionIdentifier];
}

+(void) willPresentNotification:(UNNotification *)notification {
    [[EmmaReactNativePush shared] willPresentNotificationWithUserInfo:notification.request.content.userInfo];
}

+(void) handleLink: (NSURL*) url {
    [EmmaReactNativeLinking handleLinkWithUrl:url];
    
}

//MARK: Start session and basics
RCT_EXPORT_METHOD(startSession:(NSDictionary*) configurationMap resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager startSession:configurationMap resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(trackUserLocation:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager trackUserLocation:resolve rejecter:reject];
}

//MARK: Events
RCT_EXPORT_METHOD(trackEvent:(NSDictionary*) requestMap resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager trackEvent:requestMap resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(loginUser:(NSDictionary*) loginMap resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager loginUser:loginMap resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(registerUser:(NSDictionary*) registerMap resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager registerUser:registerMap resolver:resolve rejecter:reject];
}

//MARK: Track user info
RCT_EXPORT_METHOD(trackUserExtraInfo:(NSDictionary*) infoMap resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager trackUserExtraInfo:infoMap resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(setCustomerId:(NSString*) customerId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager setCustomerId:customerId resolver:resolve rejecter:reject];
}

//MARK: In-App messaging
RCT_EXPORT_METHOD(inAppMessage:(NSDictionary*) messageMap resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager inAppMessage:messageMap resolve:^(id result){
        resolve(result);
    } reject:^(NSString *code, NSString *message, NSError *error){
        reject(code, message, error);
    }];
}

RCT_EXPORT_METHOD(sendInAppImpression:(NSDictionary*) params resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager sendInAppImpression:params resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(sendInAppClick:(NSDictionary*) params resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager sendInAppClick:params resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(openNativeAd:(NSDictionary*) params resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager openNativeAd:params resolver:resolve rejecter:reject];
}

//MARK: Push methods
RCT_EXPORT_METHOD(startPush:(NSDictionary*) pushParams resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager startPush:pushParams resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(sendPushToken: (NSString*) token resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager sendPushToken:token resolver:resolve rejecter:reject];
}

//MARK: GDPR
RCT_EXPORT_METHOD(isUserTrackingEnabled:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager isUserTrackingEnabled:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(enableUserTracking:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager enableUserTracking:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(disableUserTracking:(BOOL) deleteUser
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager disableUserTracking:deleteUser resolver:resolve rejecter:reject];
}

//MARK: Purchase
RCT_EXPORT_METHOD(startOrder:(NSDictionary*) orderParams resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager startOrder:orderParams resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(addProduct:(NSDictionary*) productParams resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager addProduct:productParams resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(trackOrder:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager trackOrder:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(cancelOrder:(NSString*)orderId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager cancelOrder:orderId resolver:resolve rejecter:reject];
}

//MARK: Track with IDFA
RCT_EXPORT_METHOD(requestTrackingWithIdfa:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [EmmaReactNativeManager requestTrackingWithIdfa:resolve rejecter:reject];
}

@end
