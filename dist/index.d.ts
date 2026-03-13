import { AddProductParams, InAppMessageParams, LoginRegisterUserParams, NativeAd, OpenNativeAdParams, PERMISSION_STATUS, PurchaseRequest, SendInAppParams, SetUserProfileParams, StartOrderParams, StartPushParams, StartSessionParams, TrackEventParams, TrackUserExtraInfoParams, UpdateConversionValueSkad4 } from './types/index.types';
export * from './types/index.types';
export default class EmmaSdk {
    static startSession(startSessionParams: StartSessionParams): Promise<void>;
    static startPush(startPushParams: StartPushParams): void;
    static trackEvent(trackEventParams: TrackEventParams): void;
    /**
     * @deprecated Use trackUserTags instead
     */
    static trackUserExtraInfo(trackUserExtraInfoParams: TrackUserExtraInfoParams): void;
    static trackUserTags(tags: Record<string, string>): void;
    static setEmail(email: string): void;
    static setUserProfile(setUserProfileParams: SetUserProfileParams): void;
    static trackUserLocation(): void;
    static loginUser(loginUserParams: LoginRegisterUserParams): void;
    static registerUser(registerUserParams: LoginRegisterUserParams): void;
    /**
     * @deprecated Use trackPurchase instead
     */
    static startOrder(startOrderParams: StartOrderParams): void;
    /**
     * @deprecated Use trackPurchase instead
     */
    static addProduct(addProductParams: AddProductParams): void;
    /**
     * @deprecated Use trackPurchase instead
     */
    static trackOrder(): void;
    static trackPurchase(purchaseRequest: PurchaseRequest): void;
    static inAppMessage(inAppMessageParams: InAppMessageParams): Promise<Array<NativeAd> | null>;
    static enableUserTracking(): void;
    static disableUserTracking(deleteUser: boolean): void;
    static isUserTrackingEnabled(): Promise<boolean>;
    static sendPushToken(token: string): void;
    static unregisterPushSystem(): void;
    static setCustomerId(customerId: string): void;
    static setUserLanguage(language: string): void;
    static sendInAppImpression(sendInAppParams: SendInAppParams): void;
    static sendInAppClick(sendInAppParams: SendInAppParams): void;
    static sendInAppDismissedClick(sendInAppParams: SendInAppParams): void;
    static openNativeAd(openNativeParams: OpenNativeAdParams): void;
    static requestTrackingWithIdfa(): void;
    static areNotificationsEnabled(): Promise<boolean>;
    static requestNotificationPermission(): Promise<PERMISSION_STATUS>;
    static updateConversionValue(conversionValue: number): void;
    static updateConversionValueSkad4(conversionModel: UpdateConversionValueSkad4): void;
}
