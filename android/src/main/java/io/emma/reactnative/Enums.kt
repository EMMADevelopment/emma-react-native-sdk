package io.emma.reactnative

enum class DefaultEvent {
    Login,
    Register
}

enum class InAppAction {
    Click,
    Impression
}

enum class PermissionStatus {
    Granted,
    Denied,
    ShouldPermissionRationale,
    Unsupported,
}