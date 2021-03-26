package io.emma.reactnative

object Error {
    const val INVALID_SESSION_KEY = "Session key not found or empty"
    const val INVALID_TOKEN = "Event token not found or empty"
    const val INVALID_USER_TAGS = "User info tag or invalid key/value, values must be string"
    const val INVALID_USER_ID = "User ID not found or empty"
    const val UNKNOW_INAPP_TYPE = "Unknown inapp type"
    const val INVALID_TEMPLATE_ID = "Template ID not found or empty"
    const val REQUEST_FAILED = "Request failed. Check if templateId exists or network is reachable"
    const val INVALID_CAMPAIGN = "Invalid campaignId must be numeric"
    const val INVALID_CTA = "Cta not found or empty"
    const val INVALID_PUSH_CLASS_TO_OPEN = "Push class to open not found. Is mandatory to start push"
    const val INVALID_PUSH_ICON = "Push icon not found. Is mandatory to start push"
    const val INVALID_PUSH_TOKEN = "Push token cannot be empty"
    const val INVALID_ORDER_ID = "Order Id cannot be empty"
    const val INVALID_TOTAL_PRICE = "Order totalPrice is mandatory"
    const val INVALID_PRODUCT_ID = "Product id cannot be empty"
    const val INVALID_CUSTOMER_ID = "Customer ID cannot be empty"
}