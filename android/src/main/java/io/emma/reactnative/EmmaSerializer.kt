package io.emma.reactnative

import android.content.Context
import com.facebook.react.bridge.*
import io.emma.android.EMMA
import io.emma.android.enums.CommunicationTypes
import io.emma.android.model.EMMACampaign
import io.emma.android.model.EMMANativeAd
import io.emma.android.model.EMMANativeAdField
import io.emma.android.model.EMMAProduct
import io.emma.android.model.EMMAPurchaseRequest
import io.emma.android.utils.EMMALog


object EmmaSerializer {

    private fun ReadableMap.getDoubleOrNull(key: String): Double? {
        return if (hasKey(key) && getType(key) == ReadableType.Number) {
            getDouble(key)
        } else null
    }

    private fun ReadableMap.getArrayOrNull(key: String): ReadableArray? {
        return if (hasKey(key) && getType(key) == ReadableType.Array) {
            getArray(key)
        } else null
    }

    private fun ReadableMap.getStringOrNull(key: String): String? {
        return if (hasKey(key) && getType(key) == ReadableType.String) {
            getString(key)
        } else null
    }

    private fun ReadableMap.getMapOrNull(key: String): ReadableMap? {
        return if (hasKey(key) && getType(key) == ReadableType.Map) {
            getMap(key)
        } else null
    }

    private fun ReadableArray.getMapOrNull(index: Int): ReadableMap? {
        return if (getType(index) == ReadableType.Map) {
            getMap(index)
        } else null
    }

    fun mapToConfiguration(context: Context,
                           configurationMap: ReadableMap): EMMA.Configuration? {

        val configuration = EMMA.Configuration.Builder(context)

        val sessionKey = configurationMap.getString("sessionKey")
        val apiUrl = if (configurationMap.hasKey("apiUrl"))
            configurationMap.getString("apiUrl") else null

        val debugActive = if (configurationMap.hasKey("isDebug"))
            configurationMap.getBoolean("isDebug") else false

        val queueTime = if (configurationMap.hasKey("queueTime"))
            configurationMap.getInt("queueTime") else 0

        val powlinkDomains = if (configurationMap.hasKey("customPowlinkDomains"))
            configurationMap.getArray("customPowlinkDomains") else null

        val shortPowlinkDomains = if (configurationMap.hasKey("customShortPowlinkDomains"))
            configurationMap.getArray("customShortPowlinkDomains") else null

        if (!Utils.isValidField(sessionKey)) {
            return null
        }
        configuration.setSessionKey(sessionKey)
        configuration.setDebugActive(debugActive)

        if (Utils.isValidField(apiUrl)) {
            configuration.setWebServiceUrl(apiUrl!!)
        }

        if (queueTime > 0) {
            configuration.setQueueTime(queueTime)
        }

        if (Utils.isValidField(powlinkDomains)) {

            configuration.setPowlinkDomains(*powlinkDomains!!.toStringArray())
        }

        if (Utils.isValidField(shortPowlinkDomains)) {
            configuration.setShortPowlinkDomains(*shortPowlinkDomains!!.toStringArray())
        }

        return configuration.build()
    }



    private fun paramsToWritableMap(params: Map<String, String>?): WritableMap {
        val writableMap = WritableNativeMap()
        params?.let {
            it.forEach { param -> writableMap.putString(param.key, param.value) }
        }
        return writableMap
    }


    fun nativeAdToWritableMap(nativeAd: EMMANativeAd): WritableMap? {
        val nativeAdMap = WritableNativeMap()
        try {
            with(nativeAdMap) {
                putInt("id", nativeAd.campaignID.toInt())
                putString("templateId", nativeAd.templateId)
                putInt("times", nativeAd.times.toInt())
                putString("tag", nativeAd.tag)
                putString("cta", nativeAd.cta)
                putString("showOn", if(nativeAd.showOnWebView()) "inapp" else "browser")
                putMap("params", paramsToWritableMap(nativeAd.params))
                putMap("fields", nativeAdFieldsToWritableMap(nativeAd.nativeAdContent))
            }

        } catch (e: Exception) {
            EMMALog.e("Error parsing native ad", e)
            return null
        }
        return nativeAdMap
    }

    private fun processNativeAdContainer(fieldsContainer: List<Map<String, EMMANativeAdField>>): WritableArray {
        val writableArray = WritableNativeArray()
        fieldsContainer.forEach { container -> writableArray.pushMap(nativeAdFieldsToWritableMap(container))}
        return writableArray
    }

    private fun nativeAdFieldsToWritableMap(fields: Map<String, EMMANativeAdField>): WritableMap {
        val fieldsMap = WritableNativeMap()
        for ((_, value) in fields.entries) {
            if (!value.fieldContainer.isNullOrEmpty()){
                fieldsMap.putArray(value.fieldName, processNativeAdContainer(value.fieldContainer!!))
            } else {
                fieldsMap.putString(value.fieldName, value.fieldValue)
            }
        }
        return fieldsMap
    }

    fun inAppRequestTypeFromString(type: String?): EMMACampaign.Type? {
        return when(type) {
            "startview" -> {
                EMMACampaign.Type.STARTVIEW
            }
            "banner" -> {
                EMMACampaign.Type.BANNER
            }
            "adball" -> {
                EMMACampaign.Type.ADBALL
            }
            "strip" -> {
                EMMACampaign.Type.STRIP
            }
            "nativeAd" -> {
                EMMACampaign.Type.NATIVEAD
            }
            else -> {
                null
            }
        }
    }

    fun inAppTypeToCommType(type: String?): CommunicationTypes? {
        return when(type) {
            "nativeAd" -> {
                CommunicationTypes.NATIVE_AD
            }
            else -> {
                null
            }
        }
    }

    fun orderExtrasMap(orderMap: ReadableMap): Map<String, String>? {
        if(orderMap.hasKey("extras")) {
            val extrasType = orderMap.getType("extras")
            if (extrasType == ReadableType.Map) {
                val extrasMap = orderMap.getMap("extras")
                return extrasMap?.toStringMap()
            }
        }
        return null
    }

    fun mapToPurchaseRequest(purchaseMap: ReadableMap): EMMAPurchaseRequest? {
        val id = purchaseMap.getStringOrNull("id")
        val totalPrice = purchaseMap.getDoubleOrNull("totalPrice")
        val productsArray = purchaseMap.getArrayOrNull("products")
        val customerId = purchaseMap.getStringOrNull("customerId")
        val coupon = purchaseMap.getStringOrNull("coupon")
        val extras = purchaseMap.getMapOrNull("extras")?.toStringMap()

        if (!Utils.isValidField(id) || !Utils.isValidField(productsArray) || totalPrice == null) {
            return null
        }

        if (!totalPrice.isFinite() || totalPrice < 0) {
            return null
        }

        val products = mutableListOf<EMMAProduct>()
        for (i in 0 until productsArray!!.size()) {
            val productMap = productsArray.getMapOrNull(i) ?: return null
            val product = mapToProduct(productMap) ?: return null
            products.add(product)
        }

        if (products.isEmpty()) {
            return null
        }

        return EMMAPurchaseRequest(
            id = id!!,
            totalPrice = totalPrice.toFloat(),
            products = products,
            customerId = customerId,
            coupon = coupon,
            extras = extras
        )
    }

    private fun mapToProduct(productMap: ReadableMap): EMMAProduct? {
        val id = productMap.getStringOrNull("id")
        val name = productMap.getStringOrNull("name") ?: ""
        val price = productMap.getDoubleOrNull("price")?.toFloat()
        val qty = productMap.getDoubleOrNull("qty")?.toFloat()
        val extras = productMap.getMapOrNull("extras")?.toStringMap()

        if (!Utils.isValidField(id) || price == null || qty == null) {
            return null
        }

        if (!price.isFinite() || price < 0 || !qty.isFinite() || qty <= 0) {
            return null
        }

        return EMMAProduct(
            id = id!!,
            name = name,
            price = price,
            qty = qty,
            extras = extras
        )
    }
}

fun ReadableMap.toMap(): Map<String, Any> {
    val map: MutableMap<String, Any> = HashMap()
    val iterator: ReadableMapKeySetIterator = keySetIterator()

    while (iterator.hasNextKey()) {
        val key = iterator.nextKey()
        when (getType(key)) {
            ReadableType.Number -> map[key] = getDouble(key)
            ReadableType.String -> map[key] = getString(key) ?: ""
            else -> continue
        }
    }
    return map
}

fun ReadableMap.toStringMap(): Map<String, String> {
    val map: MutableMap<String, String> = HashMap()
    val iterator: ReadableMapKeySetIterator = keySetIterator()

    while (iterator.hasNextKey()) {
        val key = iterator.nextKey()
        when (getType(key)) {
            ReadableType.Number -> map[key] = getDouble(key).toString()
            ReadableType.String -> map[key] = getString(key) ?: ""
            else -> continue
        }
    }
    return map
}

fun ReadableArray.toStringArray(): Array<String> {
    val array = arrayListOf<String>()
    for (i in 0 until size()) {
        if (getType(i) == ReadableType.String) {
            getString(i)?.let { array.add(it) }
        }
    }
    return array.toTypedArray()
}