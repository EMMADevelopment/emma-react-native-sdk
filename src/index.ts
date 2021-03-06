import { NativeModules, Platform } from 'react-native';

import {
  AddProductParams,
  IN_APP_TYPE,
  InAppMessageParams,
  LoginRegisterUserParams,
  NativeAd,
  OpenNativeAdParams,
  SendInAppParams,
  StartOrderParams,
  StartPushParams,
  StartSessionParams,
  TrackEventParams,
  TrackUserExtraInfoParams,
} from './types/index.types';

export * from './types/index.types';

const { EmmaReactNative } = NativeModules;

export default class EmmaSdk {
  static startSession(startSessionParams: StartSessionParams): Promise<void> {
    return EmmaReactNative.startSession(startSessionParams);
  }
  static startPush(startPushParams: StartPushParams): void {
    EmmaReactNative.startPush(startPushParams);
  }
  static trackEvent(trackEventParams: TrackEventParams): void {
    EmmaReactNative.trackEvent(trackEventParams);
  }
  static trackUserExtraInfo(trackUserExtraInfoParams: TrackUserExtraInfoParams): void {
    EmmaReactNative.trackUserExtraInfo(trackUserExtraInfoParams);
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
  static startOrder(startOrderParams: StartOrderParams): void {
    EmmaReactNative.startOrder(startOrderParams);
  }
  static addProduct(addProductParams: AddProductParams): void {
    EmmaReactNative.addProduct(addProductParams);
  }
  static trackOrder(): void {
    EmmaReactNative.trackOrder();
  }
  static cancelOrder(orderId: string): void {
    EmmaReactNative.cancelOrder(orderId);
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
  static setCustomerId(customerId: string): void {
    EmmaReactNative.setCustomerId(customerId);
  }
  static sendInAppImpression(sendInAppParams: SendInAppParams): void {
    EmmaReactNative.sendInAppImpression(sendInAppParams);
  }
  static sendInAppClick(sendInAppParams: SendInAppParams): void {
    EmmaReactNative.sendInAppClick(sendInAppParams);
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
}
