import { NativeModules, Platform } from 'react-native';

import {
  AddProductParams,
  IN_APP_TYPE,
  InAppMessageParams,
  LoginRegisterUserParams,
  NativeAd,
  OpenNativeAdParams,
  PERMISSION_STATUS,
  PurchaseRequest,
  SendInAppParams,
  SetUserProfileParams,
  StartOrderParams,
  StartPushParams,
  StartSessionParams,
  TrackEventParams,
  TrackUserExtraInfoParams,
  UpdateConversionValueSkad4,
} from './types/index.types';

export * from './types/index.types';

const { EmmaReactNative } = NativeModules;

export default class EmmaSdk {
  static getSdkVersion(): Promise<string> {
    return EmmaReactNative.getSdkVersion();
  }
  static startSession(startSessionParams: StartSessionParams): Promise<void> {
    return EmmaReactNative.startSession(startSessionParams);
  }
  static startPush(startPushParams: StartPushParams): void {
    EmmaReactNative.startPush(startPushParams);
  }
  static trackEvent(trackEventParams: TrackEventParams): void {
    EmmaReactNative.trackEvent(trackEventParams);
  }
  /**
   * @deprecated Use trackUserTags instead
   */
  static trackUserExtraInfo(trackUserExtraInfoParams: TrackUserExtraInfoParams): void {
    EmmaReactNative.trackUserExtraInfo(trackUserExtraInfoParams);
  }
  static trackUserTags(tags: Record<string, string>): void {
    EmmaReactNative.trackUserTags({ userTags: tags });
  }
  static setEmail(email: string): void {
    EmmaReactNative.setEmail(email);
  }
  static setUserProfile(setUserProfileParams: SetUserProfileParams): void {
    EmmaReactNative.setUserProfile(setUserProfileParams);
  }
  static trackUserLocation(): void {
    EmmaReactNative.trackUserLocation();
  }
  static loginUser(loginUserParams: LoginRegisterUserParams): void {
    EmmaReactNative.loginUser(loginUserParams);
  }
  static registerUser(registerUserParams: LoginRegisterUserParams): void {
    EmmaReactNative.registerUser(registerUserParams);
  }
  /**
   * @deprecated Use trackPurchase instead
   */
  static startOrder(startOrderParams: StartOrderParams): void {
    EmmaReactNative.startOrder(startOrderParams);
  }
  /**
   * @deprecated Use trackPurchase instead
   */
  static addProduct(addProductParams: AddProductParams): void {
    EmmaReactNative.addProduct(addProductParams);
  }
  /**
   * @deprecated Use trackPurchase instead
   */
  static trackOrder(): void {
    EmmaReactNative.trackOrder();
  }
  static trackPurchase(purchaseRequest: PurchaseRequest): Promise<void> {
    return EmmaReactNative.trackPurchase(purchaseRequest);
  }
  static inAppMessage(inAppMessageParams: InAppMessageParams): Promise<Array<NativeAd> | null> {
    if (inAppMessageParams.type === IN_APP_TYPE.BANNER && Platform.OS !== 'android') {
      return Promise.reject('Banner is unsupported on this device');
    }
    return EmmaReactNative.inAppMessage(inAppMessageParams);
  }
  static enableUserTracking(): void {
    EmmaReactNative.enableUserTracking();
  }
  static disableUserTracking(deleteUser: boolean): void {
    EmmaReactNative.disableUserTracking(deleteUser);
  }
  static isUserTrackingEnabled(): Promise<boolean> {
    return EmmaReactNative.isUserTrackingEnabled();
  }
  static sendPushToken(token: string): void {
    EmmaReactNative.sendPushToken(token);
  }
  static unregisterPushSystem(): void {
    EmmaReactNative.unregisterPushSystem();
  }
  static setCustomerId(customerId: string): void {
    EmmaReactNative.setCustomerId(customerId);
  }
  static setUserLanguage(language: string): void {
    EmmaReactNative.setUserLanguage(language);
  }
  static sendInAppImpression(sendInAppParams: SendInAppParams): void {
    EmmaReactNative.sendInAppImpression(sendInAppParams);
  }
  static sendInAppClick(sendInAppParams: SendInAppParams): void {
    EmmaReactNative.sendInAppClick(sendInAppParams);
  }
  static sendInAppDismissedClick(sendInAppParams: SendInAppParams): void {
    EmmaReactNative.sendInAppDismissedClick(sendInAppParams);
  }
  static openNativeAd(openNativeParams: OpenNativeAdParams): void {
    EmmaReactNative.openNativeAd(openNativeParams);
  }
  static requestTrackingWithIdfa(): void {
    if (Platform.OS === 'ios') {
      EmmaReactNative.requestTrackingWithIdfa();
    } else {
      console.error(`Unsupported platform: ${Platform.OS}`);
    }
  }

  static areNotificationsEnabled(): Promise<boolean> {
    if (Platform.OS === 'android') {
      return EmmaReactNative.areNotificationsEnabled();
    }
    return Promise.resolve(false);
  }

  static requestNotificationPermission(): Promise<PERMISSION_STATUS> {
    if (Platform.OS === 'android') {
      return EmmaReactNative.requestNotificationPermission();
    }
    return Promise.resolve(PERMISSION_STATUS.UNSUPPORTED);
  }

  static updateConversionValue(conversionValue: number) {
    if (Platform.OS === 'ios') {
      EmmaReactNative.updateConversionValue(conversionValue);
    }
  }

  static updateConversionValueSkad4(conversionModel: UpdateConversionValueSkad4) {
    if (Platform.OS === 'ios') {
      EmmaReactNative.updateConversionValueSkad4(conversionModel);
    }
  }
}
