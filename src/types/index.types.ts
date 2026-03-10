export enum SHOW_ON {
  BROWSER = 'browser',
  IN_APP = 'inapp',
}

export enum IN_APP_TYPE {
  NATIVE_AD = 'nativeAd',
  ADBALL = 'adball',
  BANNER = 'banner', // Only Android
  STARTVIEW = 'startview',
  STRIP = 'strip',
}

export enum PERMISSION_STATUS {
  GRANTED,
  DENIED,
  SHOULD_PERMISSION_RATIONALE,
  UNSUPPORTED,
}

export interface StartSessionParams {
  sessionKey: string;
  apiUrl?: string;
  queueTime?: number;
  isDebug?: boolean;
  customPowlinkDomains?: string[];
  customShortPowlinkDomains?: string[];
  trackScreenEvents?: boolean;
  skanAttribution?: boolean;
  skanCustomManagementAttribution?: boolean;
}

export interface StartPushParams {
  classToOpen: string;
  iconResource: string;
  color?: string;
  channelId?: string;
  channelName?: string;
}

export interface TrackEventParams {
  eventToken: string;
  eventAttributes?: Record<string, string | number>;
}

export interface TrackUserExtraInfoParams {
  userTags: Record<string, string>;
}

export interface SetUserProfileParams {
  customerId: string;
  email?: string;
  tags?: Record<string, string>;
}

export interface Product {
  id: string;
  name: string;
  price: number;
  qty: number;
  extras?: Record<string, string>;
}

export interface PurchaseRequest {
  id: string;
  totalPrice: number;
  products: Product[];
  customerId?: string;
  coupon?: string;
  extras?: Record<string, string>;
}

export interface LoginRegisterUserParams {
  userId: string;
  email?: string;
}

export interface StartOrderParams {
  orderId: string;
  totalPrice: number;
  customerId: string;
  currencyCode?: string;
  coupon?: string;
  extras?: Record<string, string>;
}

export interface AddProductParams {
  productId: string;
  productName: string;
  quantity: number;
  price: number;
  extras?: Record<string, string>;
}

export interface CancelOrderParams {
  orderId: string;
}

export interface InAppMessageParams {
  type: IN_APP_TYPE;
  templateId?: string;
  batch?: boolean;
}

export interface SetCustomerIdParams {
  customerId: string;
}

export interface SendInAppParams {
  type: IN_APP_TYPE;
  campaignId: number;
}

export interface NativeAd {
  id: number;
  templateId: string;
  times: number;
  tag: string;
  params: Record<string, string>;
  showOn: SHOW_ON;
  fields: Record<string, string>;
}

export interface OpenNativeAdParams {
  campaignId: number;
  showOn: SHOW_ON;
  cta: string;
}

export interface UpdateConversionValueSkad4 {
  conversionValue: number;
  coarseValue: string;
  lockWindow?: boolean;
}
