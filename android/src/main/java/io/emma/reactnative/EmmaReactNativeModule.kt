package io.emma.reactnative

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build;
import com.facebook.react.bridge.*
import io.emma.android.EMMA
import io.emma.android.enums.*
import io.emma.android.model.*
import io.emma.android.interfaces.EMMAPermissionInterface
import io.emma.android.utils.EMMAUtils



class EmmaReactNativeModule(reactContext: ReactApplicationContext) :
        ReactContextBaseJavaModule(reactContext), ActivityEventListener {

    init {
        reactContext.addActivityEventListener(this)
    }

    override fun getName(): String {
        return "EmmaReactNative"
    }

    /**
     * Listeners
     */
    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        // Not used by this module
    }

    override fun onNewIntent(intent: Intent) {
        reactApplicationContext.currentActivity?.let { EMMA.getInstance().setCurrentActivity(it) }
        EMMA.getInstance().onNewNotification(intent, true)
    }


    /**
     * Session and basics
     */

    @ReactMethod
    fun startSession(configurationMap: ReadableMap, promise: Promise) {
        val configuration = EmmaSerializer.mapToConfiguration(reactApplicationContext.applicationContext, configurationMap)
        if (configuration == null) {
            promise.reject("0", Error.INVALID_SESSION_KEY)
            return
        }

        EMMA.getInstance().startSession(configuration) {
            promise.resolve(null)
        }
    }

    @ReactMethod
    fun getSdkVersion(promise: Promise) {
        promise.resolve(EMMA.getInstance().getSDKVersion())
    }
    
    @ReactMethod
    fun trackUserLocation(promise: Promise) {
        Utils.runOnMainThread {
            EMMA.getInstance().startTrackingLocation()
        }
        promise.resolve(null)
    }

    @ReactMethod
    fun trackUserExtraInfo(tagsInfo: ReadableMap, promise: Promise) {
        val userTags = tagsInfo.getMap("userTags")
        if (!Utils.isValidField(userTags)) {
            promise.reject("0", Error.INVALID_USER_TAGS)
            return
        }

        EMMA.getInstance().trackUserTags(userTags!!.toStringMap())
        promise.resolve(null)
    }

    @ReactMethod
    fun trackUserTags(tagsInfo: ReadableMap, promise: Promise) {
        val userTags = tagsInfo.getMap("userTags")
        if (!Utils.isValidField(userTags)) {
            promise.reject("0", Error.INVALID_USER_TAGS)
            return
        }

        EMMA.getInstance().trackUserTags(userTags!!.toStringMap())
        promise.resolve(null)
    }

    @ReactMethod
    fun setEmail(email: String, promise: Promise) {
        if (!Utils.isValidField(email)) {
            promise.reject("0", Error.INVALID_EMAIL)
            return
        }

        EMMA.getInstance().setEmail(email)
        promise.resolve(null)
    }

    @ReactMethod
    fun setUserProfile(profileMap: ReadableMap, promise: Promise) {
        val customerId = if (profileMap.hasKey("customerId")) profileMap.getString("customerId") else null
        val email = if (profileMap.hasKey("email")) profileMap.getString("email") else null
        val tags = if (profileMap.hasKey("tags")) profileMap.getMap("tags")?.toStringMap() else null

        if (!Utils.isValidField(customerId)) {
            promise.reject("0", Error.INVALID_CUSTOMER_ID)
            return
        }

        EMMA.getInstance().setUserProfile(customerId!!, email, tags)
        promise.resolve(null)
    }

    /**
     * Events
     */

    @ReactMethod
    fun trackEvent(eventMap: ReadableMap, promise: Promise) {
        val token = eventMap.getString("eventToken")
        val attributes = if (eventMap.hasKey("eventAttributes")) eventMap.getMap("eventAttributes") else null

        if (!Utils.isValidField(token)) {
            promise.reject("0", Error.INVALID_TOKEN)
            return
        }

        val eventRequest = EMMAEventRequest(token!!)
        attributes?.let {
            eventRequest.attributes = attributes.toMap()
        }

        EMMA.getInstance().trackEvent(eventRequest)
        promise.resolve(null)
    }

    private fun processLoginRegister(loginRegisterMap: ReadableMap,
                                     type: DefaultEvent, promise: Promise) {

        val userId = loginRegisterMap.getString("userId")
        if (!Utils.isValidField(userId)) {
            promise.reject("0", Error.INVALID_USER_ID)
            return
        }

        val email = if (loginRegisterMap.hasKey("email")) loginRegisterMap.getString("email") else ""

        if (type == DefaultEvent.Login) {
            EMMA.getInstance().loginUser(userId!!, email)
        } else {
            EMMA.getInstance().registerUser(userId!!, email)
        }
        promise.resolve(null)
    }

    @ReactMethod
    fun loginUser(loginMap: ReadableMap, promise: Promise) {
        processLoginRegister(loginMap, DefaultEvent.Login, promise)
    }

    @ReactMethod
    fun registerUser(registerMap: ReadableMap, promise: Promise) {
        processLoginRegister(registerMap, DefaultEvent.Register, promise)
    }

    /**
     * Messaging in-app
     */

    private fun processNativeAd(templateId: String, batch: Boolean, promise: Promise) {
        val nativeAdListener = object : EmmaNativeAdListener {
            override fun onStarted(id: String?) {
                // Not implemented
            }

            override fun onSuccess(id: String?, data: Boolean) {
                if (!data) {
                    if (batch) {
                        EMMA.getInstance().removeBatchNativeAdListenenr(this)
                    } else {
                        EMMA.getInstance().removeNativeAdListener(this)
                    }
                    promise.resolve(WritableNativeArray())
                }
            }

            override fun onFailed(id: String?) {
                if (batch) {
                    EMMA.getInstance().removeBatchNativeAdListenenr(this)
                } else {
                    EMMA.getInstance().removeNativeAdListener(this)
                }
                promise.reject("0", Error.REQUEST_FAILED)
            }

            override fun onShown(campaign: EMMACampaign?) {
                // Not implemented
            }

            override fun onHide(campaign: EMMACampaign?) {
                // Not implemented
            }

            override fun onClose(campaign: EMMACampaign?) {
                // Not implemented
            }

            override fun onReceived(nativeAd: EMMANativeAd) {
                val processedNativeAds = WritableNativeArray()
                processedNativeAds.pushMap(EmmaSerializer.nativeAdToWritableMap(nativeAd))
                promise.resolve(processedNativeAds)
            }

            override fun onBatchReceived(nativeAds: MutableList<EMMANativeAd>) {
                val processedNativeAds = WritableNativeArray()
                nativeAds.forEach {
                    val processedNativeAd = EmmaSerializer.nativeAdToWritableMap(it)
                    processedNativeAds.pushMap(processedNativeAd)
                }
                promise.resolve(processedNativeAds)
            }
        }

        val request = EMMANativeAdRequest()
        request.templateId = templateId
        request.requestListener = nativeAdListener
        request.isBatch = batch

        EMMA.getInstance().getInAppMessage(request, nativeAdListener)
    }

    @ReactMethod
    fun inAppMessage(messageMap: ReadableMap, promise: Promise) {
        val rawType = messageMap.getString("type")
        val type = EmmaSerializer.inAppRequestTypeFromString(rawType)
        if (!Utils.isValidField(type)) {
            promise.reject("0", Error.UNKNOW_INAPP_TYPE)
            return
        }

        reactApplicationContext.currentActivity?.let { EMMA.getInstance().setCurrentActivity(it) }

        if (type == EMMACampaign.Type.NATIVEAD) {
            val templateId = if (messageMap.hasKey("templateId")) messageMap.getString("templateId") else null
            if (!Utils.isValidField(templateId)) {
                promise.reject("1", Error.INVALID_TEMPLATE_ID)
                return
            }

            val batch = if (messageMap.hasKey("batch")) messageMap.getBoolean("batch") else false

            processNativeAd(templateId!!, batch, promise)
        } else {
            val request = EMMAInAppRequest(type!!)
            EMMA.getInstance().getInAppMessage(request)
            promise.resolve(null)
        }
    }

    private fun processInAppAction(params: ReadableMap, actionType: InAppAction, promise: Promise) {
        val rawType = params.getString("type")
        val campaignId = params.getInt("campaignId")
        val numericType = EmmaSerializer.inAppTypeToCommType(rawType)
        if (!Utils.isValidField(numericType)) {
            promise.reject("0", Error.UNKNOW_INAPP_TYPE)
            return
        }

        if (!Utils.isValidField(campaignId)) {
            promise.reject("0", Error.INVALID_CAMPAIGN)
            return
        }

        val campaign = EMMANativeAd()
        campaign.campaignID = campaignId
        if (actionType == InAppAction.Impression) {
            EMMA.getInstance().sendInAppImpression(numericType, campaign)
        } else if (actionType == InAppAction.Click) {   
            EMMA.getInstance().sendInAppClick(numericType, campaign)
        } else {
            EMMA.getInstance().sendInAppDismissedClick(numericType, campaign)
        }
    }

    @ReactMethod
    fun sendInAppImpression(params: ReadableMap, promise: Promise) {
        processInAppAction(params, InAppAction.Impression, promise)
    }

    @ReactMethod
    fun sendInAppClick(params: ReadableMap, promise: Promise) {
        processInAppAction(params, InAppAction.Click, promise)
    }

    @ReactMethod
    fun sendInAppDismissedClick(params: ReadableMap, promise: Promise) {
        processInAppAction(params, InAppAction.DismissedClick, promise)
    }

    @ReactMethod
    fun openNativeAd(params: ReadableMap, promise: Promise) {
        val campaignId = params.getInt("campaignId")
        val showOn = params.getString("showOn")
        val cta = params.getString("cta")
        if (!Utils.isValidField(campaignId)) {
            promise.reject("0", Error.INVALID_CAMPAIGN)
            return
        }

        if (!Utils.isValidField(cta)) {
            promise.reject("1", Error.INVALID_CTA)
            return
        }

        val nativeAd = EMMANativeAd()
        nativeAd.campaignID = campaignId
        nativeAd.campaignUrl = cta
        nativeAd.setShowOn(if (showOn != null && showOn == "browser") 1 else 0)

        Utils.runOnMainThread {
            EMMA.getInstance().openNativeAd(nativeAd)
        }

        promise.resolve(null)
    }

    /**
     * Push
     */

    @ReactMethod
    fun startPush(pushParams: ReadableMap, promise: Promise) {
        val classToOpen = pushParams.getString("classToOpen")
        val iconResource = pushParams.getString("iconResource")
        val color = if (pushParams.hasKey("color")) pushParams.getString("color") else null
        val defaultChannel  = Utils.getAppName(reactApplicationContext) ?: "EMMA"
        val channelName = if (pushParams.hasKey("channelName"))pushParams.getString("channelName") else defaultChannel
        val channelId = if (pushParams.hasKey("channelId")) pushParams.getString("channelId") else null

        if (!Utils.isValidField(classToOpen)) {
            promise.reject("0", Error.INVALID_PUSH_CLASS_TO_OPEN)
            return
        }

        if (!Utils.isValidField(iconResource)) {
            promise.reject("1", Error.INVALID_PUSH_ICON)
            return
        }

        val foundedPushIcon = Utils.getNotificationIcon(reactApplicationContext, iconResource!!)
        if (foundedPushIcon == 0) {
            promise.reject("2", Error.INVALID_PUSH_ICON)
            return
        }

        val clazz = Utils.getClassFromStr(classToOpen!!)
        if (clazz == null) {
            promise.reject("3", Error.INVALID_PUSH_CLASS_TO_OPEN)
            return
        }

        val pushOpt = EMMAPushOptions.Builder(clazz, foundedPushIcon)
                .setNotificationChannelName(channelName)

        if (Utils.isValidField(color)) {
            try {
                val colorInt: Int = Color.parseColor(color);
                pushOpt.setNotificationColor(colorInt)
            } catch (ex: IllegalArgumentException) {
                promise.reject("4", Error.INVALID_HEXA_COLOR)
                return
            }
        }

        if (channelId != null) {
            pushOpt.setNotificationChannelId(channelId)
        }

        EMMA.getInstance().startPushSystem(pushOpt.build())

        reactApplicationContext.currentActivity?.let { EMMA.getInstance().setCurrentActivity(it) }
        // Check pending pushes
        EMMA.getInstance().checkForRichPushUrl()

        promise.resolve(null)
    }

    @ReactMethod
    fun sendPushToken(token: String, promise: Promise) {
        if (!Utils.isValidField(token)) {
            promise.reject("0", Error.INVALID_PUSH_TOKEN)
            return
        }
        // At the moment only can be sending the fcm tokens
        EMMA.getInstance().addPushToken(token, EMMAPushType.FCM)
        promise.resolve(null)
    }

    @ReactMethod
    fun unregisterPushSystem(promise: Promise) {
        EMMA.getInstance().unregisterPushSystem()
        promise.resolve(null)
    }

    /**
     * GDPR
     */

    @ReactMethod
    fun isUserTrackingEnabled(promise: Promise) {
        promise.resolve(EMMA.getInstance().isUserTrackingEnabled)
    }

    @ReactMethod
    fun enableUserTracking(promise: Promise) {
        EMMA.getInstance().enableUserTracking()
        promise.resolve(null)
    }

    @ReactMethod
    fun disableUserTracking(deleteUser: Boolean, promise: Promise) {
        EMMA.getInstance().disableUserTracking(deleteUser)
        promise.resolve(null)
    }

    /**
     * Purchase
     */

    @ReactMethod
    fun startOrder(startOrderMap: ReadableMap, promise: Promise) {
        val orderId = if (startOrderMap.hasKey("orderId")) startOrderMap.getString("orderId") else ""
        val totalPrice = if(startOrderMap.hasKey("totalPrice")) startOrderMap.getDouble("totalPrice") else 0.0
        val customerId = if(startOrderMap.hasKey("customerId")) startOrderMap.getString("customerId") else null
        val coupon = if(startOrderMap.hasKey("coupon")) startOrderMap.getString("coupon") else null

        if (!Utils.isValidField(orderId)) {
            promise.reject("0", Error.INVALID_ORDER_ID)
            return
        }

        if (!Utils.isValidField(totalPrice)) {
            promise.reject("1", Error.INVALID_TOTAL_PRICE)
            return
        }

        if (!Utils.isValidField(customerId)) {
            promise.reject("2", Error.INVALID_CUSTOMER_ID)
            return
        }

        val extras = EmmaSerializer.orderExtrasMap(startOrderMap)

        EMMA.getInstance().startOrder(orderId, customerId, totalPrice.toFloat(), coupon, extras)
    }

    @ReactMethod
    fun addProduct(addProductMap: ReadableMap, promise: Promise) {
        val productId = if (addProductMap.hasKey("productId")) addProductMap.getString("productId") else ""
        val productName = if(addProductMap.hasKey("productName")) addProductMap.getString("productName") else ""
        val quantity = if(addProductMap.hasKey("quantity")) addProductMap.getInt("quantity") else 1
        val price = if(addProductMap.hasKey("price")) addProductMap.getDouble("price") else 0.0

        if (!Utils.isValidField(productId)) {
            promise.reject("0", Error.INVALID_ORDER_ID)
            return
        }

        EMMA.getInstance().addProduct(productId, productName, quantity.toFloat(), price.toFloat())
        promise.resolve(null)
    }

    @ReactMethod
    fun trackOrder(promise: Promise) {
        EMMA.getInstance().trackOrder()
        promise.resolve(null)
    }

    @ReactMethod
    fun trackPurchase(purchaseMap: ReadableMap, promise: Promise) {
        val purchaseRequest = EmmaSerializer.mapToPurchaseRequest(purchaseMap)
        if (purchaseRequest == null) {
            promise.reject("0", Error.INVALID_PURCHASE_REQUEST)
            return
        }

        EMMA.getInstance().trackPurchase(purchaseRequest)
        promise.resolve(null)
    }

    @ReactMethod
    fun setCustomerId(customerId: String, promise: Promise) {
        EMMA.getInstance().setCustomerId(customerId);
        promise.resolve(null)
    }

    @ReactMethod
    fun setUserLanguage(language: String, promise: Promise) {
        if (!Utils.isValidField(language)) {
            promise.reject("0", Error.INVALID_LANGUAGE)
            return
        }

        EMMA.getInstance().setUserLanguage(language);
        promise.resolve(null)
    }

    @ReactMethod
    fun areNotificationsEnabled(promise: Promise) {
        promise.resolve(EMMA.getInstance().areNotificationsEnabled())
    }

    @ReactMethod
    fun requestNotificationPermission(promise: Promise) {
       if (Build.VERSION.SDK_INT < 33 || EMMAUtils.getTargetSdkVersion(reactApplicationContext.applicationContext) < 33) {
            promise.resolve(PermissionStatus.Unsupported.ordinal)
            return;
        }

        val permissionListener = object: EMMAPermissionInterface {
            override fun onPermissionGranted(permission: String, isFirstTime: Boolean) {
                promise.resolve(PermissionStatus.Granted.ordinal)
            }

            override fun onPermissionDenied(permission: String) {
                promise.resolve(PermissionStatus.Denied.ordinal)
            }

            override fun onPermissionWaitingForAction(permission: String) { }

            override fun onPermissionShouldShowRequestPermissionRationale(permission: String) {
                promise.resolve(PermissionStatus.ShouldPermissionRationale.ordinal)
            }
        }

         Utils.runOnMainThread {
            EMMA.getInstance().requestNotificationPermission(permissionListener)
        }
    }
}