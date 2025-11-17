import EMMA_iOS

enum InAppAction {
    case click
    case impression
    case dismissedClick
}

enum DefaultEvent {
    case login
    case register
}

public class EmmaReactNativeManager: NSObject {
    static var sessionStarted = false
    /**
        This method avoids that SDK launching on main queue
     */
    @objc static func requiresMainQueueSetup() -> Bool {
        return false
    }

    // MARK: - Start session and basics
    @objc
    public class func startSession(_ configurationMap: [String : Any],
                      resolver resolve: RCTPromiseResolveBlock,
                      rejecter reject: RCTPromiseRejectBlock) {
        
        guard let configuration = EmmaSerializer.mapToConfiguration(config: configurationMap) else {
            let error = NSError(domain: Error.invalidSessionKey, code: 0, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        DispatchQueue.main.async {
            EMMA.startSession(with: configuration)
        }
        
        resolve(NSNull())
    }
    
    @objc
    public class func trackUserLocation(_ resolve: RCTPromiseResolveBlock,
                           rejecter reject: RCTPromiseRejectBlock) {
        DispatchQueue.main.async {
            EMMA.trackLocation()
        }
        resolve(nil)
    }
    
    // MARK: - Track user info
    @objc
    public class func trackUserExtraInfo(_ infoMap: [String : Any],
                            resolver resolve: RCTPromiseResolveBlock,
                            rejecter reject: RCTPromiseRejectBlock) {
        
        guard let info = infoMap["userTags"] as? Dictionary<String, String>  else {
            let error = NSError(domain: Error.invalidUserInfoTags, code: 0, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        EMMA.trackExtraUserInfo(info: info)
        resolve(nil)
    }
    
    @objc
    public class func setCustomerId(_ customerId: String,
                       resolver resolve: RCTPromiseResolveBlock,
                       rejecter reject: RCTPromiseRejectBlock) {
        
        guard Utils.isValidField(customerId) else {
            let error = NSError(domain: Error.invalidToken, code: 0, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        EMMA.setCustomerId(customerId: customerId)
        resolve(nil)
    }

    // MARK: - Language
    @objc
    public class func setUserLanguage(_ language: String,
                       resolver resolve: RCTPromiseResolveBlock,
                       rejecter reject: RCTPromiseRejectBlock) {
        
        guard Utils.isValidField(language) else {
            let error = NSError(domain: Error.invalidLanguage, code: 0, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        EMMA.setUserLanguage(language)
        resolve(nil)
    }
    
    // MARK: - Events
    @objc
    public class func trackEvent(_ requestMap: [String : Any],
                    resolver resolve: RCTPromiseResolveBlock,
                    rejecter reject: RCTPromiseRejectBlock) {
        
        let token = requestMap["eventToken"] as? String
        let attributes = requestMap["eventAttributes"] as? Dictionary<String, Any>
        guard Utils.isValidField(token) else {
            let error = NSError(domain: Error.invalidToken, code: 0, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        let request = EMMAEventRequest(token: token!)
        if let attributes = attributes {
            request.attributes = attributes
        }
        
        EMMA.trackEvent(request: request)
        resolve(nil)
    }
    
    private class func processLoginRegister(_ loginMap: [String: Any],
                              type: DefaultEvent,
                              resolver resolve: RCTPromiseResolveBlock,
                              rejecter reject: RCTPromiseRejectBlock) {
        
        let userId = loginMap["userId"] as? String
        guard Utils.isValidField(userId) else {
            let error = NSError(domain: Error.invalidUserId, code: 0, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        let email = loginMap["email"] as? String ?? ""
        
        if (type == .login) {
            EMMA.loginUser(userId: userId!, forMail: email, andExtras: nil)
        } else {
            EMMA.registerUser(userId: userId!, forMail: email, andExtras: nil)
        }
        
        resolve(nil)
    }
    
    @objc
    public class func loginUser(_ loginMap: [String: Any],
                   resolver resolve: RCTPromiseResolveBlock,
                   rejecter reject: RCTPromiseRejectBlock) {
        processLoginRegister(loginMap, type: .login, resolver: resolve, rejecter: reject)
    }
    
    @objc
    public class func registerUser(_ registerMap: [String: Any],
                      resolver resolve: RCTPromiseResolveBlock,
                      rejecter reject: RCTPromiseRejectBlock) {
        processLoginRegister(registerMap, type: .register, resolver: resolve, rejecter: reject)
    }
    
    // MARK: - Inapp messaging methods
    @objc
    public class func inAppMessage(_ messageMap: [String: Any],
                                   resolve: @escaping (Any?) -> Void, reject: @escaping (String, String, NSError?) -> Void ) {
        
        let type = messageMap["type"] as? String
        guard let requestType = EmmaSerializer.inAppTypeFromString(inAppType: type) else {
            let error = NSError(domain: Error.unknowInappType, code: 0, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        if (requestType == InAppType.NativeAd) {
            let templateId = messageMap["templateId"] as? String
            guard Utils.isValidField(templateId) else {
                let error = NSError(domain: Error.invalidTemplateId, code: 1, userInfo: nil)
                reject(String(error.code), error.domain, error)
                return
            }
            
            let batch = messageMap["batch"] as? Bool ?? false
            
            let nativeAdDelegate = EmmaNativeAdDelegate(resolve: resolve, reject: reject)
            
            let request = EMMANativeAdRequest()
            request.requestDelegate = nativeAdDelegate
            request.templateId = templateId
            request.isBatch = batch
            
            EMMA.inAppMessage(request: request, withDelegate: nativeAdDelegate)
        } else {
            let request = EMMAInAppRequest(type: requestType)
            EMMA.inAppMessage(request: request)
            resolve(NSNull())
        }
    }
    
    private class func processInAppAction(_ params: [String : Any],
                            inappAction: InAppAction,
                            resolver resolve: RCTPromiseResolveBlock,
                            rejecter reject: RCTPromiseRejectBlock) {
        
        let type = params["type"] as? String
        guard let requestType = EmmaSerializer.numericInAppTypeFromString(inAppType: type) else {
            let error = NSError(domain: Error.unknowInappType, code: 0, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        guard let campaignId = params["campaignId"] as? Int else {
            let error = NSError(domain: Error.invalidCampaignId, code: 1, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        if (inappAction == .impression) {
            EMMA.sendImpression(campaignType: requestType, withId: String(campaignId))
        } else if (inappAction == .click) {
            EMMA.sendClick(campaignType: requestType, withId: String(campaignId))
        } else {
            EMMA.sendDismissedClick(campaignType: requestType, withId: String(campaignId))
        }
        
        resolve(nil)
    }
    
    @objc
    public class func sendInAppImpression(_ params: [String : Any],
                             resolver resolve: RCTPromiseResolveBlock,
                             rejecter reject: RCTPromiseRejectBlock) {
        
        processInAppAction(params, inappAction: .impression, resolver: resolve, rejecter: reject)
    }
    
    @objc
    public class func sendInAppClick(_ params: [String : Any],
                        resolver resolve: RCTPromiseResolveBlock,
                        rejecter reject: RCTPromiseRejectBlock) {
        
        processInAppAction(params, inappAction: .click, resolver: resolve, rejecter: reject)
    }
    
    @objc
    public class func sendInAppDismissedClick(_ params: [String : Any],
                        resolver resolve: RCTPromiseResolveBlock,
                        rejecter reject: RCTPromiseRejectBlock) {
        
        processInAppAction(params, inappAction: .dismissedClick, resolver: resolve, rejecter: reject)
    }
    
    @objc
    public class func openNativeAd(_ params: [String : Any],
                      resolver resolve: RCTPromiseResolveBlock,
                      rejecter reject: RCTPromiseRejectBlock) {
        
        guard let campaignId = params["campaignId"] as? Int else {
            let error = NSError(domain: Error.invalidCampaignId, code: 0, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        DispatchQueue.main.async {
            EMMA.openNativeAd(campaignId: String(campaignId))
        }
        
        resolve(nil)
    }
    
    // MARK: - Push methods
    // Push params only for Android
    @objc
    public class func startPush(_ pushParams: NSDictionary,
                   resolver resolve: RCTPromiseResolveBlock,
                   rejecter reject: RCTPromiseRejectBlock) {
        
        EmmaReactNativePush.shared.startPush()
        resolve(nil)
    }
    
    @objc
    public class func sendPushToken(_ token: String,
                       resolver resolve: RCTPromiseResolveBlock,
                       rejecter reject: RCTPromiseRejectBlock) {
        
        guard Utils.isValidField(token) else {
            let error = NSError(domain: Error.invalidPushToken, code: 0, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        EMMA.trackExtraUserInfo(info: ["token": token])
        resolve(nil)
    }
    
    // MARK: - GDPR
    @objc
    public class func isUserTrackingEnabled(_ resolve: RCTPromiseResolveBlock,
                               rejecter reject: RCTPromiseRejectBlock) {
        resolve(EMMA.isUserTrackingEnabled())
    }
    
    @objc
    public class func enableUserTracking(_ resolve: RCTPromiseResolveBlock,
                            rejecter reject: RCTPromiseRejectBlock) {
        EMMA.enableUserTracking()
        resolve(nil)
    }
    
    @objc
    public class func disableUserTracking(_ deleteUser: Bool,
                            resolver resolve: RCTPromiseResolveBlock,
                            rejecter reject: RCTPromiseRejectBlock) {
        EMMA.disableUserTracking(deleteUser: deleteUser)
        resolve(nil)
    }

    @objc
    public class func startOrder(_ orderMap: [String : Any],
                    resolver resolve: RCTPromiseResolveBlock,
                    rejecter reject: RCTPromiseRejectBlock) {
        
        let orderId = orderMap["orderId"] as? String
        let totalPrice = (orderMap["totalPrice"] as? NSNumber)?.doubleValue
        let customerId = orderMap["customerId"] as? String
        let coupon = orderMap["coupon"] as? String
        let extras = orderMap["extras"] as? Dictionary<String, String>
        
        guard Utils.isValidField(orderId) else {
            let error = NSError(domain: Error.invalidOrderId, code: 0, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        guard let totalPrice = totalPrice, totalPrice >= 0 else {
            let error = NSError(domain: Error.invalidTotalPrice, code: 1, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        guard Utils.isValidField(customerId) else {
            let error = NSError(domain: Error.invalidCustomerId, code: 2, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        EMMA.startOrder(orderId: orderId!, andCustomer: customerId!, withTotalPrice: Float(totalPrice), withExtras: extras, assignCoupon: coupon)
        resolve(nil)
    }
    
    @objc
    public class func addProduct(_ productMap: [String : Any],
                    resolver resolve: RCTPromiseResolveBlock,
                    rejecter reject: RCTPromiseRejectBlock) {
        
        let productId = productMap["productId"] as? String
        let productName = productMap["productName"] as? String
        let quantity = productMap["quantity"] as? Double ?? 1
        let price = productMap["price"] as? Double ?? 0.0
        let extras = productMap["extras"] as? Dictionary<String, String>
        
        guard Utils.isValidField(productId) else {
            let error = NSError(domain: Error.invalidProductId, code: 0, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        EMMA.addProduct(productId: productId!, andName: productName!, withQty: Float(quantity), andPrice: Float(price), withExtras: extras)
        resolve(nil)
    }
    
    @objc
    public class func trackOrder(_ resolve: RCTPromiseResolveBlock,
                    rejecter reject: RCTPromiseRejectBlock) {
        
        EMMA.trackOrder()
        resolve(nil)
    }
    
    @objc
    public class func cancelOrder(_ orderId: String,
                     resolver resolve: RCTPromiseResolveBlock,
                     rejecter reject: RCTPromiseRejectBlock) {
        
        guard Utils.isValidField(orderId) else {
            let error = NSError(domain: Error.invalidOrderId, code: 0, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        EMMA.cancelOrder(orderId: orderId)
        resolve(nil)
        
    }
    
    @objc
    public class func requestTrackingWithIdfa(_ resolve: RCTPromiseResolveBlock,
                                 rejecter reject: RCTPromiseRejectBlock) {
        if #available(iOS 14, *) {
            DispatchQueue.main.async {
                EMMA.requestTrackingWithIdfa()
            }
        }
        
        resolve(nil)
    }

    @objc
    public class func updateConversionValue(_ conversionValue: Int,
                                           resolver resolve: RCTPromiseResolveBlock,
                                           rejecter reject: RCTPromiseRejectBlock) {
        guard conversionValue >= 1, conversionValue <= 63 else {
            let error = NSError(domain: Error.invalidConversionValue, code: 0, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        EMMA.updatePostbackConversionValue(conversionValue)
    }
    
    @objc
    public class func updateConversionValueSkad4(_ conversionModel: [String: Any],
                                           resolver resolve: RCTPromiseResolveBlock,
                                           rejecter reject: RCTPromiseRejectBlock) {
        
        let conversionValue = conversionModel["conversionValue"] as? Int ?? 0
        let coarseValue = conversionModel["coarseValue"] as? String ?? ""
        let lockWindow = conversionModel["lockWindow"] as? Bool ?? false
        
        guard conversionValue >= 1, conversionValue <= 63 else {
            let error = NSError(domain: Error.invalidConversionValue, code: 1, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        let validCoarseValues = ["high", "medium", "low"]
        guard Utils.isValidField(coarseValue), validCoarseValues.contains(coarseValue)  else {
            let error = NSError(domain: Error.invalidConversionValue, code: 0, userInfo: nil)
            reject(String(error.code), error.domain, error)
            return
        }
        
        EMMA.updatePostbackConversionValue(conversionValue, coarseValue: coarseValue, lockWindow: lockWindow)
    }
}
